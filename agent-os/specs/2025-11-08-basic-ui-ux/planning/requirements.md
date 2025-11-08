# Spec Requirements: Basic UI/UX

## Initial Description
implement basic UI/UX based on a design mockup that i will provide shortly after.

## Requirements Discussion

### First Round Questions

**Q1:** I assume this UI/UX implementation is for the "Store Selection User Interface" feature from the roadmap. Is that correct?
**Answer:** it is more like a design for entire app as a whole

**Q2:** For the frontend technology, I propose we use React with TypeScript and Bootstrap for styling to create a modern and responsive UI. Shall we proceed with this stack?
**Answer:** yes, we will use react + tsx

**Q3:** Regarding UI components, I'm thinking of using a pre-built library like Material-UI to ensure consistency and speed up development. Is this acceptable, or should we build custom components?
**Answer:** you can refer to @mockup/src/** to figure out what library it uses

**Q4:** I assume the UI will fetch data, like a list of nearby stores, from the local backend we've already set up. We will need to define the necessary API endpoints for this. Is this assumption correct?
**Answer:** you are correct

**Q5:** For the user flow, I'm picturing a main screen that lists nearby stores, allowing the user to tap one to confirm their selection. Does this match your vision, or do you have a different flow in mind?
**Answer:** refer to visuals i provided you with

**Q6:** What key information should be displayed for each store in the list? I'm assuming the store's name and its distance from the user are essential. Should anything else be included?
**Answer:** refer to visuals i provided you with

**Q7:** How should the UI handle cases where no nearby stores are found? I'm thinking of a simple message like "No stores found nearby." Would that be sufficient?
**Answer:** yes, that should suffice

**Q8:** Are there any specific accessibility requirements to consider beyond standard best practices (e.g., keyboard navigation, good color contrast)?
**Answer:** no

**Q9:** What should be considered out of scope for this initial implementation? For instance, should we defer features like user accounts or a settings screen for now?
**Answer:** login/logout part should be skipped for now

### Existing Code to Reference
Based on the user's response, the mockup code at `@mockup/**` should be referenced.

**Similar Features Identified:**
- The entire mockup application at `@mockup/**` serves as the reference.
- **Component Library:** The mockup uses `shadcn/ui` which is built on top of Radix UI and Tailwind CSS. This should be the component library for the project.
- **Icons:** `lucide-react` is used for icons.
- **Structure:** The project is a standard Vite-based React TypeScript application.

### Follow-up Questions
None.

## Visual Assets

### Files Provided:
- `add_credit_cards_menu.png`: Shows the UI for adding a new credit card. It includes a search bar, category filters, a list of already owned cards, and a list of available cards to add.
- `dropdown_settings.png`: Displays the main map screen with a settings dropdown menu. The menu includes options for Dark Mode, Add Credit Card, Profile Settings, Notification Settings, and Logout.
- `main_screen.png`: The main screen of the application, featuring a map-based view. It includes a search bar, the user's current location, color-coded store markers, and a legend for the store categories.

### Visual Insights:
- **Design Pattern:** The design is a modern, clean, mobile-first interface. It uses a map-centric approach for the main screen.
- **User Flow:** The primary user flow appears to be:
    1. View nearby stores on the map.
    2. Tap a store to select it.
    3. (Presumably) see recommended credit cards for that store.
    4. Users can add their own credit cards via a separate screen accessible from the settings menu.
- **UI Components:** The UI uses standard components like search bars, buttons, dropdowns, and lists, styled in a consistent manner. The use of `shadcn/ui` is evident from the component structure and styling.
- **Fidelity Level:** High-fidelity mockup.

## Requirements Summary

### Functional Requirements
- Implement the main screen with a map view showing the user's location and nearby stores.
- Implement the "Add Credit Card" screen, allowing users to search, filter, and add credit cards to their profile.
- Implement the settings dropdown menu with navigation to the "Add Credit Card" screen and a dark mode toggle.
- The UI should fetch store data from the backend.
- The UI should be responsive and mobile-first.

### Reusability Opportunities
- The entire `@mockup/**` codebase should be used as a reference for components, styling, and application structure.
- The `shadcn/ui` components from the mockup should be reused.

### Scope Boundaries
**In Scope:**
- Main map screen UI.
- Add Credit Card screen UI.
- Settings dropdown with navigation and dark mode toggle.
- Integration with the local backend to fetch store data.

**Out of Scope:**
- Login/Logout functionality.
- Profile Settings screen.
- Notification Settings screen.
- Actual payment processing or credit card validation.

### Technical Considerations
- **Frontend Stack:** React, TypeScript, Vite.
- **UI Library:** `shadcn/ui` (with Radix UI and Tailwind CSS).
- **Icons:** `lucide-react`.
- **Backend Integration:** The UI will need to communicate with the existing local backend over API endpoints that will need to be defined.
