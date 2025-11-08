import { useState } from 'react';
import { MapView } from './components/MapView';
import { CardRecommendation } from './components/CardRecommendation';
import { SearchBar } from './components/SearchBar';
import { StoreMarker } from './components/StoreMarker';
import { AddCardScreen } from './components/AddCardScreen';
import { ThemeProvider } from './components/ThemeProvider';

export interface Store {
  id: string;
  name: string;
  category: 'cafe' | 'restaurant' | 'shopping' | 'gas' | 'convenience';
  position: { x: number; y: number };
  distance: number;
}

export interface CreditCard {
  id: string;
  name: string;
  bank: string;
  benefits: string[];
  discount: string;
  image: string;
  color: string;
  matchScore: number;
}

export interface OwnedCard {
  id: string;
  name: string;
  bank: string;
  color: string;
}

export default function App() {
  const [selectedStore, setSelectedStore] = useState<Store | null>(null);
  const [userLocation] = useState({ x: 50, y: 50 });
  const [currentScreen, setCurrentScreen] = useState<'map' | 'addCard'>('map');
  const [ownedCards, setOwnedCards] = useState<OwnedCard[]>([]);

  const stores: Store[] = [
    { id: '1', name: '스타벅스 강남점', category: 'cafe', position: { x: 45, y: 35 }, distance: 120 },
    { id: '2', name: 'CGV 강남', category: 'shopping', position: { x: 60, y: 40 }, distance: 250 },
    { id: '3', name: '올리브영 강남역점', category: 'shopping', position: { x: 55, y: 55 }, distance: 180 },
    { id: '4', name: 'GS25 편의점', category: 'convenience', position: { x: 40, y: 60 }, distance: 90 },
    { id: '5', name: '교촌치킨', category: 'restaurant', position: { x: 65, y: 50 }, distance: 200 },
    { id: '6', name: 'SK주유소', category: 'gas', position: { x: 30, y: 45 }, distance: 300 },
  ];

  const recommendedCards: CreditCard[] = [
    {
      id: '1',
      name: '신한 Deep Dream',
      bank: '신한카드',
      benefits: ['스타벅스 30% 할인', '편의점 10% 할인', 'CGV 영화 5,000원 할인'],
      discount: '최대 30% 할인',
      image: '',
      color: 'from-blue-500 to-blue-700',
      matchScore: 95,
    },
    {
      id: '2',
      name: '현대카드 M Edition2',
      bank: '현대카드',
      benefits: ['모든 가맹점 1.2% 적립', '해외 결제 3% 적립'],
      discount: '최대 1.2% 적립',
      image: '',
      color: 'from-purple-500 to-purple-700',
      matchScore: 88,
    },
    {
      id: '3',
      name: 'KB국민 Liiv Mate',
      bank: 'KB국민카드',
      benefits: ['편의점 20% 할인', '카페 15% 할인', '주유 리터당 100원 할인'],
      discount: '최대 20% 할인',
      image: '',
      color: 'from-amber-500 to-amber-700',
      matchScore: 92,
    },
  ];

  const handleAddCard = (card: OwnedCard) => {
    setOwnedCards([...ownedCards, card]);
    setCurrentScreen('map');
  };

  if (currentScreen === 'addCard') {
    return (
      <ThemeProvider>
        <AddCardScreen
          onBack={() => setCurrentScreen('map')}
          onAddCard={handleAddCard}
          ownedCards={ownedCards}
        />
      </ThemeProvider>
    );
  }

  return (
    <ThemeProvider>
      <div className="h-screen w-full bg-gray-50 dark:bg-gray-900 flex flex-col max-w-md mx-auto">
        {/* Header */}
        <div className="bg-white dark:bg-gray-800 shadow-sm z-10">
          <SearchBar 
            onNavigateToAddCard={() => setCurrentScreen('addCard')}
            ownedCardsCount={ownedCards.length}
          />
        </div>

        {/* Map View */}
        <div className="flex-1 relative">
          <MapView
            stores={stores}
            userLocation={userLocation}
            selectedStore={selectedStore}
            onStoreSelect={setSelectedStore}
          />
        </div>

        {/* Card Recommendations - Only show when store is selected */}
        {selectedStore && (
          <CardRecommendation
            cards={recommendedCards}
            selectedStore={selectedStore}
          />
        )}
      </div>
    </ThemeProvider>
  );
}
