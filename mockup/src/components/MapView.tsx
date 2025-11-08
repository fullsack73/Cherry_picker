import { Coffee, ShoppingBag, Utensils, Fuel, Store as StoreIcon, LocateFixed } from 'lucide-react';
import { useState } from 'react';
import { Button } from './ui/button';
import type { Store } from '../App';

interface MapViewProps {
  stores: Store[];
  userLocation: { x: number; y: number };
  selectedStore: Store | null;
  onStoreSelect: (store: Store | null) => void;
}

const categoryIcons = {
  cafe: Coffee,
  restaurant: Utensils,
  shopping: ShoppingBag,
  gas: Fuel,
  convenience: StoreIcon,
};

const categoryColors = {
  cafe: 'bg-amber-500',
  restaurant: 'bg-orange-500',
  shopping: 'bg-pink-500',
  gas: 'bg-green-500',
  convenience: 'bg-blue-500',
};

export function MapView({ stores, userLocation, selectedStore, onStoreSelect }: MapViewProps) {
  const [isRefreshing, setIsRefreshing] = useState(false);

  const handleRefresh = () => {
    setIsRefreshing(true);
    // 위치 새로고침 애니메이션
    setTimeout(() => {
      setIsRefreshing(false);
    }, 1000);
  };

  return (
    <div className="w-full h-full relative bg-gradient-to-br from-blue-50 to-green-50 dark:from-gray-800 dark:to-gray-900 overflow-hidden">
      {/* Map Grid Background */}
      <svg className="absolute inset-0 w-full h-full opacity-10 dark:opacity-5">
        <defs>
          <pattern id="grid" width="40" height="40" patternUnits="userSpaceOnUse">
            <path d="M 40 0 L 0 0 0 40" fill="none" stroke="gray" strokeWidth="1" />
          </pattern>
        </defs>
        <rect width="100%" height="100%" fill="url(#grid)" />
      </svg>

      {/* Location Refresh Button */}
      <div className="absolute top-4 left-4 z-30">
        <Button
          size="icon"
          onClick={handleRefresh}
          className="h-10 w-10 bg-white dark:bg-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700 text-blue-500 shadow-lg rounded-xl border border-gray-200 dark:border-gray-700"
          variant="ghost"
        >
          <LocateFixed className={`w-5 h-5 ${isRefreshing ? 'animate-spin' : ''}`} />
        </Button>
      </div>

      {/* Roads */}
      <svg className="absolute inset-0 w-full h-full">
        <line x1="20%" y1="0" x2="20%" y2="100%" stroke="#cbd5e1" className="dark:stroke-gray-700" strokeWidth="6" />
        <line x1="70%" y1="0" x2="70%" y2="100%" stroke="#cbd5e1" className="dark:stroke-gray-700" strokeWidth="6" />
        <line x1="0" y1="40%" x2="100%" y2="40%" stroke="#cbd5e1" className="dark:stroke-gray-700" strokeWidth="6" />
        <line x1="0" y1="70%" x2="100%" y2="70%" stroke="#cbd5e1" className="dark:stroke-gray-700" strokeWidth="6" />
      </svg>

      {/* Store Markers */}
      {stores.map((store) => {
        const Icon = categoryIcons[store.category];
        const isSelected = selectedStore?.id === store.id;
        
        return (
          <button
            key={store.id}
            onClick={() => onStoreSelect(isSelected ? null : store)}
            className="absolute transform -translate-x-1/2 -translate-y-1/2 transition-all duration-200 hover:scale-110"
            style={{
              left: `${store.position.x}%`,
              top: `${store.position.y}%`,
            }}
          >
            <div className="relative">
              {/* Pulse Animation for Selected */}
              {isSelected && (
                <div className="absolute inset-0 animate-ping">
                  <div className={`w-12 h-12 rounded-full ${categoryColors[store.category]} opacity-75`} />
                </div>
              )}
              
              {/* Marker */}
              <div
                className={`relative w-12 h-12 rounded-full ${categoryColors[store.category]} shadow-lg flex items-center justify-center transition-all ${
                  isSelected ? 'scale-125 ring-4 ring-white' : ''
                }`}
              >
                <Icon className="w-6 h-6 text-white" />
              </div>
              
              {/* Store Name Label */}
              {isSelected && (
                <div className="absolute top-14 left-1/2 transform -translate-x-1/2 bg-white dark:bg-gray-800 px-3 py-1.5 rounded-lg shadow-lg whitespace-nowrap text-sm z-10">
                  <div className="dark:text-white">{store.name}</div>
                  <div className="text-xs text-gray-500 dark:text-gray-400">{store.distance}m</div>
                </div>
              )}
            </div>
          </button>
        );
      })}

      {/* User Location */}
      <div
        className="absolute transform -translate-x-1/2 -translate-y-1/2 z-20"
        style={{
          left: `${userLocation.x}%`,
          top: `${userLocation.y}%`,
        }}
      >
        <div className="relative">
          {/* Pulse Animation */}
          <div className="absolute inset-0 animate-pulse">
            <div className="w-16 h-16 bg-blue-400 rounded-full opacity-30" />
          </div>
          
          {/* User Marker */}
          <div className="relative w-16 h-16 bg-blue-500 rounded-full shadow-xl flex items-center justify-center ring-4 ring-white dark:ring-gray-800">
            <div className="w-3 h-3 bg-white rounded-full" />
          </div>
        </div>
      </div>

      {/* Legend */}
      <div className="absolute top-4 right-4 bg-white/90 dark:bg-gray-800/90 backdrop-blur rounded-xl p-3 shadow-lg space-y-2 text-xs dark:text-gray-200">
        <div className="flex items-center gap-2">
          <div className="w-3 h-3 rounded-full bg-amber-500" />
          <span>카페</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-3 h-3 rounded-full bg-orange-500" />
          <span>음식점</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-3 h-3 rounded-full bg-pink-500" />
          <span>쇼핑</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-3 h-3 rounded-full bg-blue-500" />
          <span>편의점</span>
        </div>
      </div>

      {/* Distance Circles */}
      <svg className="absolute inset-0 w-full h-full pointer-events-none">
        <circle
          cx={`${userLocation.x}%`}
          cy={`${userLocation.y}%`}
          r="15%"
          fill="none"
          stroke="#3b82f6"
          strokeWidth="2"
          strokeDasharray="5,5"
          opacity="0.3"
        />
        <circle
          cx={`${userLocation.x}%`}
          cy={`${userLocation.y}%`}
          r="25%"
          fill="none"
          stroke="#3b82f6"
          strokeWidth="1"
          strokeDasharray="5,5"
          opacity="0.2"
        />
      </svg>
    </div>
  );
}
