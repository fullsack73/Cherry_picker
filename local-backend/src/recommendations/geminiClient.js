class GeminiClientError extends Error {
  constructor(message, code = 'UNKNOWN') {
    super(message);
    this.name = 'GeminiClientError';
    this.code = code;
  }
}

class GeminiClient {
  constructor({
    apiKey = process.env.GEMINI_API_KEY,
    fetchImpl = typeof fetch === 'function' ? fetch.bind(globalThis) : null,
    timeoutMs = 10000,
    retryDelayMs = 500,
    logger = console,
  } = {}) {
    this.apiKey = typeof apiKey === 'string' ? apiKey.trim() : '';
    this.fetchImpl = fetchImpl;
    this.timeoutMs = timeoutMs;
    this.retryDelayMs = retryDelayMs;
    this.logger = logger || console;
  }

  async scoreCard({ storeName, storeCategory, cardName, normalizedCategories = [], discover = false, signal } = {}) {
    if (!this.apiKey) {
      throw new GeminiClientError('GEMINI_API_KEY missing', 'CONFIGURATION');
    }

    if (!this.fetchImpl) {
      throw new GeminiClientError('Fetch implementation unavailable', 'CONFIGURATION');
    }

    const url = `https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=${this.apiKey}`;
    const prompt = this.buildPrompt({ storeName, storeCategory, cardName, normalizedCategories, discover });
    const body = {
      contents: [
        {
          role: 'user',
          parts: [{ text: prompt }],
        },
      ],
    };

    return this.executeWithRetry(() => this.performRequest(url, body, signal), signal);
  }

  async performRequest(url, body, signal) {
    if (signal?.aborted) {
      throw new GeminiClientError('Gemini request aborted', 'CANCELLED');
    }
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), this.timeoutMs);
    const abortHandler = () => controller.abort();

    if (signal) {
      if (signal.aborted) {
        clearTimeout(timeout);
        throw new GeminiClientError('Gemini request aborted', 'CANCELLED');
      }
      signal.addEventListener('abort', abortHandler, { once: true });
    }

    try {
      const response = await this.fetchImpl(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Accept: 'application/json',
        },
        body: JSON.stringify(body),
        signal: controller.signal,
      });

      if (!response.ok) {
        throw new GeminiClientError(`Gemini returned ${response.status}`, 'NETWORK');
      }

      const payload = await response.json();
      return this.parseResponse(payload);
    } catch (error) {
      if (error.name === 'AbortError') {
        if (signal?.aborted) {
          throw new GeminiClientError('Gemini request aborted', 'CANCELLED');
        }
        throw new GeminiClientError('Gemini request timed out', 'TIMEOUT');
      }
      if (error instanceof GeminiClientError) {
        throw error;
      }
      throw new GeminiClientError(error.message || 'Gemini request failed', 'NETWORK');
    } finally {
      clearTimeout(timeout);
      if (signal) {
        signal.removeEventListener('abort', abortHandler);
      }
    }
  }

  async executeWithRetry(operation, signal) {
    let attempt = 0;
    let lastError;
    while (attempt < 2) {
      if (signal?.aborted) {
        throw new GeminiClientError('Gemini scoring aborted', 'CANCELLED');
      }
      try {
        return await operation();
      } catch (error) {
        lastError = error;
        const isGeminiError = error instanceof GeminiClientError;
        const isCancelled = isGeminiError && error.code === 'CANCELLED';
        if (isCancelled) {
          throw error;
        }
        const shouldRetry = isGeminiError && (error.code === 'NETWORK' || error.code === 'TIMEOUT');
        if (!shouldRetry || attempt === 1) {
          this.logger?.warn?.('Gemini scoring failed', { code: error.code });
          throw error;
        }
        attempt += 1;
        await this.delay(this.retryDelayMs);
      }
    }

    throw lastError;
  }

  delay(ms) {
    return new Promise((resolve) => setTimeout(resolve, ms));
  }

  buildPrompt({ storeName, storeCategory, cardName, normalizedCategories, discover }) {
    const categoryList = Array.isArray(normalizedCategories) && normalizedCategories.length > 0
      ? normalizedCategories.join(', ')
      : 'UNCATEGORIZED';

    const discoverCopy = discover ? 'The user is exploring new cards they do not own.' : 'The user owns this card.';
    return [
      'Rate how strong this credit card is for the described store on a scale from 0-100.',
      `Store Name: ${storeName || 'Unknown Store'}`,
      `Store Category: ${storeCategory || 'UNKNOWN'}`,
      `Card Name: ${cardName}`,
      `Card Benefit Categories: ${categoryList}`,
      discoverCopy,
      'Respond ONLY with JSON: { "score": <0-100>, "rationale": "short reason" }',
    ].join('\n');
  }

  parseResponse(payload) {
    if (!payload) {
      throw new GeminiClientError('Empty Gemini payload', 'PARSING');
    }

    let text = '';
    if (Array.isArray(payload.candidates) && payload.candidates.length > 0) {
      const candidate = payload.candidates[0];
      if (candidate?.content?.parts?.length) {
        text = candidate.content.parts.map((part) => part.text || '').join('\n').trim();
      }
    }

    if (!text && typeof payload.output_text === 'string') {
      text = payload.output_text.trim();
    }

    let parsedScore;
    let rationale;

    if (text) {
      try {
        const asJson = JSON.parse(text);
        parsedScore = asJson.score;
        rationale = asJson.rationale || asJson.explanation;
      } catch (error) {
        const match = text.match(/(-?\d{1,3})/);
        if (match) {
          parsedScore = Number(match[1]);
        }
        rationale = text;
      }
    }

    if (typeof payload.score === 'number' && Number.isFinite(payload.score)) {
      parsedScore = payload.score;
    }
    if (!rationale && typeof payload.rationale === 'string') {
      rationale = payload.rationale;
    }

    const resolvedScore = Math.max(0, Math.min(100, Math.round(Number(parsedScore) || 60)));
    const resolvedRationale = rationale || 'Gemini did not provide rationale.';

    return {
      score: resolvedScore,
      rationale: resolvedRationale,
    };
  }
}

module.exports = {
  GeminiClient,
  GeminiClientError,
};
