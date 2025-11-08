import { MapPin } from 'lucide-react';

interface StoreMarkerProps {
  name: string;
  category: string;
  isSelected?: boolean;
  onClick?: () => void;
}

export function StoreMarker({ name, category, isSelected, onClick }: StoreMarkerProps) {
  return (
    <button
      onClick={onClick}
      className={`relative transition-all duration-200 ${
        isSelected ? 'scale-125' : 'hover:scale-110'
      }`}
    >
      <MapPin
        className={`w-8 h-8 ${
          isSelected ? 'text-blue-500' : 'text-gray-600'
        }`}
        fill="currentColor"
      />
      {isSelected && (
        <div className="absolute top-10 left-1/2 transform -translate-x-1/2 bg-white px-3 py-1 rounded-lg shadow-lg whitespace-nowrap text-sm">
          {name}
        </div>
      )}
    </button>
  );
}
