# Full-Stack Bakery POS & Kitchen Management System

A comprehensive, full-stack Android application built in Android Studio designed for modern bakery operations. This solution bridges front-of-house operations, back-of-house kitchen workflows, and back-office administrative management into a unified system featuring a Sales Floor POS, Kitchen Display System (KDS), and Admin Dashboard.

---

## Key Features & Modules

### 🛒 1. Sales Floor (Point of Sale)
- **Interactive POS Interface:** Fast category and product selection optimized for touchscreen interaction.
- **Order Customization:** Support for item modifiers (e.g., custom cake messages, sugar levels, milk alternatives, dietary tags).
- **Cart & Checkout Management:** Quick item adding/removing, split billing, percentage/flat discounts, and tax calculations.
- **Payment Processing:** Accepts multiple payment methods (Cash, Card, Mobile Wallet/QR).
- **Receipt Generation:** Integrated digital receipt sending (Email/SMS) and Bluetooth/Network thermal printer printing.

### 🍳 2. Kitchen Display System (KDS)
- **Real-Time Order Sync:** Instant routing of new orders from the POS to kitchen displays via WebSocket/Firebase listener.
- **Status Workflow Management:** Interactive ticket cards transitioning across states: `New` ➔ `In Preparation` ➔ `Ready for Pickup` ➔ `Completed`.
- **Item-Level Progress:** Bakeshop staff can cross off individual items on multi-item custom cake or pastry orders.
- **Time Tracking & Alerts:** Visual color-coded timers alerting bakers to urgent or overdue custom orders.

### 3. Admin Panel & Management
- **Inventory & Recipe Management:** Real-time ingredient tracking, stock deduction per sale, low-stock threshold alerts, and batch management.
- **Product & Menu Management:** Add, edit, or archive products, manage categories, set dynamic pricing, and control item availability.
- **Reporting & Analytics:**
  - Daily, weekly, and monthly sales breakdowns.
  - Best-selling baked goods & category performance.
  - Peak sales hours & KDS order preparation time metrics.
- **User Role & Staff Management:** Role-Based Access Control (RBAC) separating `Cashier`, `Baker/Kitchen Staff`, and `Admin` permissions.

---

## Architecture & Tech Stack

- **Platform:** Android (Java / Kotlin)
- **IDE:** Android Studio (Jellyfish / Koala or newer)
- **UI Framework:** Android XML Layouts / Jetpack Compose with Material Design 3
- **Architecture Pattern:** MVVM (Model-View-ViewModel) with Clean Architecture principles
- **Asynchronous Processing:** Kotlin Coroutines & Flow / RxJava
- **Database & Persistence:**
  - **Local Storage:** Room Database (Offline-first caching and local POS transactions)
  - **Cloud / Backend:** Firebase Cloud Firestore or RESTful API (Node.js/Spring Boot/Laravel)
- **Real-Time Communication:** Firebase Realtime Database / WebSockets (STOMP) for KDS updates
- **Dependency Injection:** Hilt / Dagger
- **Networking:** Retrofit2 & OkHttp3 / Firebase SDK

---

## Project Structure

```text
BakeryPOS/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/bakery/pos/
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/          # Room DB entities, DAOs, Database
│   │   │   │   │   ├── remote/         # API services, Firebase, DTOs
│   │   │   │   │   └── repository/     # Data repository implementations
│   │   │   │   ├── di/                 # Dependency injection modules (Hilt)
│   │   │   │   ├── domain/             # Use cases, business logic, domain models
│   │   │   │   ├── ui/
│   │   │   │   │   ├── sales/          # POS Register UI & ViewModels
│   │   │   │   │   ├── kds/            # Kitchen Display System UI & ViewModels
│   │   │   │   │   ├── admin/          # Inventory, Products & Analytics UI
│   │   │   │   │   ├── auth/           # Login & Staff Role selection
│   │   │   │   │   └── common/         # Shared adapters, dialogs, and components
│   │   │   │   └── utils/              # Thermal printer SDKs, formatters, helpers
│   │   │   ├── res/                    # Layouts, drawables, values, navigation
│   │   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
└── settings.gradle.kts
```

---

## Getting Started

### Prerequisites

- **Android Studio:** Electric Eel (2025.1.1) or newer recommended.
- **JDK:** Java Development Kit 17 or higher.
- **Android Device / Emulator:** API Level 24 (Android 7.0) or higher.
- **Backend Setup:** A active Firebase Project or configured REST API backend.

---

### Installation & Setup

1. **Clone the Repository:**
   ```bash
   git clone https://github.com/your-username/BakeryPOS-Android.git
   cd BakeryPOS-Android
   ```

2. **Open in Android Studio:**
   - Launch Android Studio.
   - Select **Open** and navigate to the cloned project folder.

3. **Configure Backend Services:**
   - **For Firebase Backend:**
     1. Create a project in the [Firebase Console](https://console.firebase.google.com/).
     2. Add an Android app with package name `com.bakery.pos`.
     3. Download `google-services.json` and place it in the `app/` directory.
   - **For REST API Backend:**
     1. Open `app/src/main/java/com/bakery/pos/utils/Constants.kt` (or `gradle.properties`).
     2. Update `BASE_URL` with your local IP or hosted backend URL (e.g., `http://192.168.1.100:8080/api/`).

4. **Sync Project & Build:**
   - Click **File > Sync Project with Gradle Files**.
   - Build the APK or run the app directly on an emulator or physical device.

---

## Available User Roles

| Role | Access Level | Primary Functions |
| :--- | :--- | :--- |
| **Cashier / Sales** | POS Floor | Take orders, process payments, print receipts, view daily drawer totals. |
| **Baker / Kitchen** | KDS Display | View incoming tickets, update preparation status, view order modifiers. |
| **Administrator** | Full Access | Manage inventory stock, modify products & prices, view sales analytics, manage staff accounts. |


## Contributing

1. Fork the Repository.
2. Create a Feature Branch (`git checkout -b feature/KitchenTimerAlerts`).
3. Commit your Changes (`git commit -m 'Add custom timer for KDS tickets'`).
4. Push to the Branch (`git push origin feature/KitchenTimerAlerts`).
5. Open a Pull Request.

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
