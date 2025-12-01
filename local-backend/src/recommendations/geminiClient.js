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
    timeoutMs = 1000,
    retryDelayMs = 200,
    logger = console,
  } = {}) {
    this.apiKey = typeof apiKey === 'string' ? apiKey.trim() : '';
    this.fetchImpl = fetchImpl;
    this.timeoutMs = timeoutMs;
    this.retryDelayMs = retryDelayMs;
    this.logger = logger || console;
  }

  async scoreCard({ storeName, storeCategory, cardName, normalizedCategories = [], discover = false } = {}) {
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

    return this.executeWithRetry(() => this.performRequest(url, body));
  }

  async performRequest(url, body) {
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), this.timeoutMs);

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
        throw new GeminiClientError('Gemini request timed out', 'TIMEOUT');
      }
      if (error instanceof GeminiClientError) {
        throw error;
      }
      throw new GeminiClientError(error.message || 'Gemini request failed', 'NETWORK');
    } finally {
      clearTimeout(timeout);
    }
  }

  async executeWithRetry(operation) {
    let attempt = 0;
    let lastError;
    while (attempt < 2) {
      try {
        return await operation();
      } catch (error) {
        lastError = error;
        const shouldRetry = error instanceof GeminiClientError && (error.code === 'NETWORK' || error.code === 'TIMEOUT');
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
