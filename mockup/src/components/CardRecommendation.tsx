import { useState } from 'react';
import { ChevronUp, ChevronDown, CreditCard, TrendingUp, Star } from 'lucide-react';
import { Button } from './ui/button';
import { Badge } from './ui/badge';
import type { CreditCard as CreditCardType, Store } from '../App';

interface CardRecommendationProps {
  cards: CreditCardType[];
  selectedStore: Store | null;
}

export function CardRecommendation({ cards, selectedStore }: CardRecommendationProps) {
  const [isExpanded, setIsExpanded] = useState(false);

  return (
    <div
      className={`bg-white dark:bg-gray-800 rounded-t-3xl shadow-2xl transition-all duration-300 ease-out ${
        isExpanded ? 'h-[70vh]' : 'h-[280px]'
      }`}
    >
      {/* Handle */}
      <button
        onClick={() => setIsExpanded(!isExpanded)}
        className="w-full py-3 flex justify-center"
      >
        <div className="w-12 h-1.5 bg-gray-300 dark:bg-gray-600 rounded-full" />
      </button>

      {/* Header */}
      <div className="px-6 pb-4 border-b dark:border-gray-700">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="flex items-center gap-2 dark:text-white">
              <TrendingUp className="w-5 h-5 text-blue-500" />
              추천 카드
            </h2>
            <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">
              {selectedStore ? `${selectedStore.name} 주변 최적 카드` : '현재 위치 기반 추천'}
            </p>
          </div>
          <button
            onClick={() => setIsExpanded(!isExpanded)}
            className="p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg transition-colors"
          >
            {isExpanded ? (
              <ChevronDown className="w-5 h-5 dark:text-gray-300" />
            ) : (
              <ChevronUp className="w-5 h-5 dark:text-gray-300" />
            )}
          </button>
        </div>
      </div>

      {/* Cards List */}
      <div className={`overflow-y-auto ${isExpanded ? 'h-[calc(70vh-120px)]' : 'h-[180px]'} px-6 py-4`}>
        <div className="space-y-3">
          {cards.map((card, index) => (
            <div
              key={card.id}
              className="bg-white dark:bg-gray-700 border border-gray-200 dark:border-gray-600 rounded-2xl p-4 hover:shadow-lg transition-all cursor-pointer group"
            >
              <div className="flex gap-4">
                {/* Card Visual */}
                <div
                  className={`w-24 h-16 rounded-xl bg-gradient-to-br ${card.color} shadow-md flex items-center justify-center flex-shrink-0`}
                >
                  <CreditCard className="w-8 h-8 text-white" />
                </div>

                {/* Card Info */}
                <div className="flex-1 min-w-0">
                  <div className="flex items-start justify-between gap-2">
                    <div className="flex-1">
                      <div className="flex items-center gap-2">
                        <h3 className="truncate dark:text-white">{card.name}</h3>
                        {index === 0 && (
                          <Badge variant="default" className="bg-blue-500 text-xs">
                            BEST
                          </Badge>
                        )}
                      </div>
                      <p className="text-sm text-gray-500 dark:text-gray-400">{card.bank}</p>
                    </div>
                    <div className="flex items-center gap-1 text-amber-500">
                      <Star className="w-4 h-4 fill-current" />
                      <span className="text-sm">{card.matchScore}</span>
                    </div>
                  </div>

                  <div className="mt-3 space-y-1">
                    {card.benefits.slice(0, isExpanded ? undefined : 2).map((benefit, idx) => (
                      <div key={idx} className="flex items-start gap-2 text-sm">
                        <span className="text-blue-500 mt-0.5">•</span>
                        <span className="text-gray-700 dark:text-gray-300">{benefit}</span>
                      </div>
                    ))}
                  </div>

                  {isExpanded && (
                    <div className="mt-4 flex gap-2">
                      <Button
                        size="sm"
                        className="flex-1 bg-blue-500 hover:bg-blue-600"
                      >
                        카드 신청
                      </Button>
                      <Button
                        size="sm"
                        variant="outline"
                        className="flex-1"
                      >
                        상세보기
                      </Button>
                    </div>
                  )}
                </div>
              </div>

              {/* Match Score Bar */}
              <div className="mt-3 pt-3 border-t border-gray-100 dark:border-gray-600">
                <div className="flex items-center justify-between text-xs text-gray-500 dark:text-gray-400 mb-1">
                  <span>매칭도</span>
                  <span>{card.matchScore}%</span>
                </div>
                <div className="w-full bg-gray-100 dark:bg-gray-600 rounded-full h-2">
                  <div
                    className="bg-gradient-to-r from-blue-500 to-blue-600 h-2 rounded-full transition-all duration-500"
                    style={{ width: `${card.matchScore}%` }}
                  />
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
