import { useState } from 'react';
import { ArrowLeft, Search, CreditCard as CreditCardIcon, Plus, Check } from 'lucide-react';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Badge } from './ui/badge';
import type { OwnedCard } from '../App';

interface AddCardScreenProps {
  onBack: () => void;
  onAddCard: (card: OwnedCard) => void;
  ownedCards: OwnedCard[];
}

interface AvailableCard {
  id: string;
  name: string;
  bank: string;
  color: string;
  category: string;
}

const availableCards: AvailableCard[] = [
  { id: '1', name: '신한 Deep Dream', bank: '신한카드', color: 'from-blue-500 to-blue-700', category: '생활' },
  { id: '2', name: '현대카드 M Edition2', bank: '현대카드', color: 'from-purple-500 to-purple-700', category: '포인트' },
  { id: '3', name: 'KB국민 Liiv Mate', bank: 'KB국민카드', color: 'from-amber-500 to-amber-700', category: '생활' },
  { id: '4', name: '삼성 iD CLEAR', bank: '삼성카드', color: 'from-gray-700 to-gray-900', category: '교통' },
  { id: '5', name: '우리 V2G', bank: '우리카드', color: 'from-green-500 to-green-700', category: '주유' },
  { id: '6', name: '롯데 My SHOT', bank: '롯데카드', color: 'from-red-500 to-red-700', category: '쇼핑' },
  { id: '7', name: 'NH채움 올원카��', bank: 'NH농협카드', color: 'from-teal-500 to-teal-700', category: '생활' },
  { id: '8', name: 'BC바로카드 BOOST', bank: 'BC카드', color: 'from-indigo-500 to-indigo-700', category: '통신' },
];

export function AddCardScreen({ onBack, onAddCard, ownedCards }: AddCardScreenProps) {
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<string>('전체');

  const categories = ['전체', '생활', '쇼핑', '주유', '교통', '포인트', '통신'];

  const filteredCards = availableCards.filter((card) => {
    const matchesSearch = card.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
                         card.bank.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesCategory = selectedCategory === '전체' || card.category === selectedCategory;
    return matchesSearch && matchesCategory;
  });

  const isCardOwned = (cardId: string) => {
    return ownedCards.some((owned) => owned.id === cardId);
  };

  const handleAddCard = (card: AvailableCard) => {
    if (!isCardOwned(card.id)) {
      onAddCard({
        id: card.id,
        name: card.name,
        bank: card.bank,
        color: card.color,
      });
    }
  };

  return (
    <div className="h-screen w-full bg-gray-50 dark:bg-gray-900 flex flex-col max-w-md mx-auto">
      {/* Header */}
      <div className="bg-white dark:bg-gray-800 shadow-sm">
        <div className="p-4">
          <div className="flex items-center gap-3 mb-4">
            <Button
              variant="ghost"
              size="icon"
              onClick={onBack}
              className="h-10 w-10"
            >
              <ArrowLeft className="w-5 h-5" />
            </Button>
            <div className="flex-1">
              <h1 className="dark:text-white">신용카드 추가</h1>
              <p className="text-sm text-gray-500 dark:text-gray-400">
                보유중인 카드를 추가하여 혜택을 확인하세요
              </p>
            </div>
          </div>

          {/* Search Bar */}
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 dark:text-gray-500 w-5 h-5" />
            <Input
              type="text"
              placeholder="카드명 또는 은행명 검색"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="pl-10 h-12 rounded-xl dark:bg-gray-700 dark:border-gray-600 dark:text-white"
            />
          </div>
        </div>

        {/* Category Tabs */}
        <div className="px-4 pb-3 overflow-x-auto">
          <div className="flex gap-2">
            {categories.map((category) => (
              <Button
                key={category}
                variant={selectedCategory === category ? 'default' : 'outline'}
                size="sm"
                onClick={() => setSelectedCategory(category)}
                className={`rounded-full whitespace-nowrap ${
                  selectedCategory === category
                    ? 'bg-blue-500 hover:bg-blue-600'
                    : 'dark:border-gray-600'
                }`}
              >
                {category}
              </Button>
            ))}
          </div>
        </div>
      </div>

      {/* Owned Cards Section */}
      {ownedCards.length > 0 && (
        <div className="bg-white dark:bg-gray-800 mt-2 p-4">
          <div className="flex items-center justify-between mb-3">
            <h2 className="text-sm dark:text-white">내 카드</h2>
            <Badge variant="secondary">{ownedCards.length}개</Badge>
          </div>
          <div className="grid grid-cols-2 gap-3">
            {ownedCards.map((card) => (
              <div
                key={card.id}
                className="relative"
              >
                <div
                  className={`h-24 rounded-xl bg-gradient-to-br ${card.color} shadow-md flex flex-col justify-between p-3`}
                >
                  <div className="flex items-start justify-between">
                    <CreditCardIcon className="w-6 h-6 text-white opacity-80" />
                    <Check className="w-5 h-5 text-white bg-white/20 rounded-full p-0.5" />
                  </div>
                  <div className="text-white">
                    <p className="text-xs opacity-80">{card.bank}</p>
                    <p className="text-sm truncate">{card.name}</p>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Available Cards List */}
      <div className="flex-1 overflow-y-auto p-4 space-y-3">
        <div className="flex items-center justify-between mb-2">
          <h2 className="text-sm dark:text-white">전체 카드</h2>
          <span className="text-sm text-gray-500 dark:text-gray-400">{filteredCards.length}개</span>
        </div>

        {filteredCards.map((card) => {
          const owned = isCardOwned(card.id);
          return (
            <div
              key={card.id}
              className="bg-white dark:bg-gray-800 rounded-2xl p-4 flex items-center gap-4 hover:shadow-md transition-shadow border border-transparent dark:border-gray-700"
            >
              {/* Card Visual */}
              <div
                className={`w-20 h-14 rounded-lg bg-gradient-to-br ${card.color} shadow-md flex items-center justify-center flex-shrink-0`}
              >
                <CreditCardIcon className="w-7 h-7 text-white" />
              </div>

              {/* Card Info */}
              <div className="flex-1 min-w-0">
                <div className="flex items-start gap-2">
                  <div className="flex-1">
                    <h3 className="truncate dark:text-white">{card.name}</h3>
                    <p className="text-sm text-gray-500 dark:text-gray-400">{card.bank}</p>
                  </div>
                  <Badge variant="secondary" className="text-xs">
                    {card.category}
                  </Badge>
                </div>
              </div>

              {/* Add Button */}
              <Button
                size="sm"
                variant={owned ? 'secondary' : 'default'}
                onClick={() => handleAddCard(card)}
                disabled={owned}
                className={owned ? '' : 'bg-blue-500 hover:bg-blue-600'}
              >
                {owned ? (
                  <>
                    <Check className="w-4 h-4 mr-1" />
                    추가됨
                  </>
                ) : (
                  <>
                    <Plus className="w-4 h-4 mr-1" />
                    추가
                  </>
                )}
              </Button>
            </div>
          );
        })}

        {filteredCards.length === 0 && (
          <div className="text-center py-12 text-gray-500 dark:text-gray-400">
            <CreditCardIcon className="w-12 h-12 mx-auto mb-3 opacity-30" />
            <p>검색 결과가 없습니다</p>
          </div>
        )}
      </div>
    </div>
  );
}
