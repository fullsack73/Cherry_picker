# Specification: Create UI/UX for the App

## Goal
To design and implement a user-friendly and intuitive UI/UX for the Android application, focusing on location-based credit card recommendations and card management, adhering to Material Design 3 principles and leveraging Jetpack Compose.

## User Stories
- As a user, I want to see my current location on a map and discover nearby stores so that I can easily find places to use my credit cards.
- As a user, I want to view personalized credit card recommendations for a selected store so that I can maximize my rewards and savings.
- As a user, I want to easily add and manage my credit cards within the app so that I can receive accurate recommendations.
- As a user, I want to access settings like dark mode and profile management so that I can personalize my app experience.

## Core Requirements
### Functional Requirements
- **Main Screen:** Display an interactive map component showing the user's current location and nearby stores with distinct icons.
- **Search Functionality:** Implement a search bar for users to find specific locations or stores.
- **Store Selection & Recommendation Display:** Allow users to select a store (from map or search) and view a dynamic list of recommended credit cards, including benefits, match rates, and action buttons (e.g., "Apply for Card", "View Details").
- **Credit Card Management Screen:** Provide a dedicated screen where users can search, filter by category, and add new credit cards, as well as view their existing cards.
- **Settings Menu:** Implement a dropdown menu accessible from the main screen, offering options such as Dark Mode toggle, Add Credit Card navigation, Profile Settings, and Notification Settings.
- **Navigation:** Ensure clear and intuitive navigation between the main screen, credit card management, and settings.

### Non-Functional Requirements
- **Performance:** The UI should be smooth and responsive, with quick loading times for map data and card recommendations.
- **Accessibility:** The UI should be designed with accessibility in mind, catering to various Android device screens and user needs.
- **Responsiveness:** The UI must adapt seamlessly to different Android device screen sizes and orientations.
- **Consistency:** Adhere strictly to Material Design 3 guidelines for a consistent look and feel.

## Visual Design
- Mockup reference: `planning/visuals/main_screen.png`, `planning/visuals/card_list_when_store_selected.png`, `planning/visuals/card_list_extended.png`, `planning/visuals/add_credit_cards_menu.png`, `planning/visuals/dropdown_settings.png`
- Key UI elements to implement: Interactive map, search bar, filter/settings icons, card-style list items, category filter chips, dropdown menus, action buttons.
- Responsive breakpoints required: The UI should be flexible enough to accommodate various Android phone and tablet screen sizes.

## Reusable Components
### Existing Code to Leverage
- Components: `Cherry_pickerTheme` for Material Design 3 styling. `LocationScreen` (from `MainActivity.kt`) can be used as a base for location handling, but its UI will be replaced by the new map and recommendation views.
- Services: `ApiClient` and `ApiService` for existing backend communication patterns.
- Patterns: Existing permission handling logic in `MainActivity.kt` for location services.

### New Components Required
- **Map Component:** An interactive map view (likely using Google Maps Compose) to display user location and nearby stores.
- **Card Recommendation List Component:** A scrollable list component to display credit card recommendations with detailed information and action buttons.
- **Add Card UI Component:** A dedicated UI for searching, filtering, and adding credit cards.
- **Search Bar Component:** A reusable search input field for locations/stores and card management.
- **Settings Dropdown Component:** A Material Design compliant dropdown menu for settings options.
- **Store Marker/Icon Component:** Custom UI elements for displaying stores on the map.
- **Category Filter Chips Component:** Reusable UI for filtering cards by category.

## Technical Approach
- **Frontend:** Develop the UI using Jetpack Compose, adhering to Material Design 3.
- **Map Integration:** Integrate Google Maps Platform for the interactive map component.
- **Data Flow:** Utilize existing `ApiClient` and `ApiService` for fetching store data, card benefits, and sending user location.
- **State Management:** Implement appropriate state management patterns for Compose UI (e.g., ViewModel, mutableStateOf).
- **Navigation:** Implement Jetpack Navigation Compose for managing app screens.
- **Testing:** Implement UI tests for key components and integration tests for screen flows.

## Out of Scope
- UI/UX for iOS or web platforms.
- Login/logout features.
- Complex animations beyond standard Material Design transitions.
- Detailed backend implementation (focus is on UI/UX).

## Success Criteria
- The application successfully displays an interactive map with nearby stores and provides accurate credit card recommendations.
- Users can easily add and manage their credit cards.
- The UI is visually appealing, intuitive, and consistent with Material Design 3.
- The application is responsive across various Android device screens.
- All core functional requirements are met as per the visual mockups.
