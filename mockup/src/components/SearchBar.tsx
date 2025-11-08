import { Search, SlidersHorizontal, MapPin, Settings, CreditCard, User, Bell, LogOut, Moon, Sun } from 'lucide-react';
import { Button } from './ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from './ui/dropdown-menu';
import { Badge } from './ui/badge';
import { useTheme } from './ThemeProvider';

interface SearchBarProps {
  onNavigateToAddCard: () => void;
  ownedCardsCount: number;
}

export function SearchBar({ onNavigateToAddCard, ownedCardsCount }: SearchBarProps) {
  const { theme, toggleTheme } = useTheme();

  return (
    <div className="p-4 space-y-3">
      <div className="flex items-center gap-2">
        <div className="flex-1 relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 dark:text-gray-500 w-5 h-5" />
          <input
            type="text"
            placeholder="위치 또는 가게 검색"
            className="w-full pl-10 pr-4 py-3 border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 dark:text-white rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <Button
          variant="outline"
          size="icon"
          className="h-12 w-12 rounded-xl border-gray-200 dark:border-gray-700"
        >
          <SlidersHorizontal className="w-5 h-5" />
        </Button>
        
        {/* Settings Dropdown */}
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button
              variant="outline"
              size="icon"
              className="h-12 w-12 rounded-xl border-gray-200 dark:border-gray-700 relative"
            >
              <Settings className="w-5 h-5" />
              {ownedCardsCount > 0 && (
                <Badge className="absolute -top-1 -right-1 h-5 w-5 flex items-center justify-center p-0 text-xs bg-blue-500">
                  {ownedCardsCount}
                </Badge>
              )}
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end" className="w-56">
            <DropdownMenuLabel>내 계정</DropdownMenuLabel>
            <DropdownMenuSeparator />
            <DropdownMenuItem onClick={toggleTheme}>
              {theme === 'light' ? (
                <>
                  <Moon className="mr-2 h-4 w-4" />
                  <span>다크모드</span>
                </>
              ) : (
                <>
                  <Sun className="mr-2 h-4 w-4" />
                  <span>라이트모드</span>
                </>
              )}
            </DropdownMenuItem>
            <DropdownMenuSeparator />
            <DropdownMenuItem onClick={onNavigateToAddCard}>
              <CreditCard className="mr-2 h-4 w-4" />
              <span>신용카드 추가</span>
            </DropdownMenuItem>
            <DropdownMenuItem>
              <User className="mr-2 h-4 w-4" />
              <span>프로필 설정</span>
            </DropdownMenuItem>
            <DropdownMenuItem>
              <Bell className="mr-2 h-4 w-4" />
              <span>알림 설정</span>
            </DropdownMenuItem>
            <DropdownMenuSeparator />
            <DropdownMenuItem className="text-red-600 dark:text-red-400">
              <LogOut className="mr-2 h-4 w-4" />
              <span>로그아웃</span>
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
      
      <div className="flex items-center gap-2 text-gray-600 dark:text-gray-400">
        <MapPin className="w-4 h-4 text-blue-500" />
        <span className="text-sm">현재 위치: 서울시 강남구</span>
      </div>
    </div>
  );
}
