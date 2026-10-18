# SixCoachConfectionery Android POS & KDS Mobile App

SixCoachConfectionery is a mobile-first bakery **Point of Sale (POS)**, **Kitchen Display System (KDS)**, and **Admin Management** Android application. Built using modern Android architecture components with Kotlin, Material Design 3, ViewBinding, and persistent local JSON storage (`six_coach_confectionery_store.json`).

---

## App Screens & Navigation

Navigation is seamlessly accessible via the **Bottom Navigation Bar** and **Top App Header**:

| Navigation Tab | Who | Purpose & Mobile UI Features |
|----------------|-----|------------------------------|
| **Sales** | Cashiers / Staff | **POS Terminal** — Horizontal category filter pills (`All`, `Bread`, `Coffee`, etc.), Product grid with stock badges and prices in Rand (`R 5.00`), Cart summary with item quantity steppers (`-` / `+`), Total calculation, and **Pay** button triggering custom checkout dialog. |
| **Kitchen** | Kitchen Staff | **KDS Display** — Live pending orders view, timer counter showing elapsed time per order (e.g., `0m 7s`), order items list, "Refresh now" / Swipe-to-refresh, and green **Complete** button to clear fulfilled orders. |
| **Admin** | Admin | **Bakery Management Panel** with sub-tabs: <br>• **Overview**: Feature navigation cards (`Manage Items`, `View Analytics`, `Configure System`), Quick Stats (Total Orders, Total Revenue `R29.00`, Items Sold, Avg Order `R14.50`), and Recent Orders. <br>• **Inventory Control**: Add/edit/deactivate bakery items with custom input controls and stock level tracking. <br>• **Sales Analytics**: Filterable sales reports (`This Week`, `Today`, `This Month`, `All Time`), Revenue cards, `#1 BEST` top-selling products, recent orders table, and Export Report. <br>• **User Management**: List registered users and grant/revoke admin privileges. |
| **Account** | Public / Staff | **Authentication & Profile** — Staff login and account registration screens with custom input controls and validation. |

---

## Technical Stack & Architecture

- **Language:** Kotlin
- **UI Architecture:** Material Components (Material 3), ConstraintLayout, ViewBinding, Dynamic Custom Input Controls (`CustomInputField`), Custom Dialogs (`CheckoutDialogFragment`, `ItemEditorDialogFragment`).
- **Data & Persistence:** `SixCoachConfectioneryRepository` with local persistent JSON file storage (`six_coach_confectionery_store.json` in `context.filesDir`). All inventory changes, user registrations, and order checkout records survive app restarts.
- **Theme & Design:** Warm bakery theme palette (`bakery_brown`, `bakery_tan`, `bakery_bg`, `bakery_green`, `bakery_gold`).
- **Build System:** Gradle (AGP 9.2.0, Kotlin JVM 11).

---

## Building and Running the App

### Prerequisites
- Android Studio Ladybug / Jellyfish or newer
- Android SDK 35 (or compileSdk 37 / Android 14+)
- JDK 11 or JDK 17

### Build Commands
From the project root:

```bash
# Build Debug APK
./gradlew app:assembleDebug

# Run Local Unit Tests
./gradlew testDebugUnitTest
```

---

## First-Time Usage & Default Seed Data

On first launch, SixCoachConfectionery automatically seeds initial data into persistent local storage:

1. **Default Admin Account:**
   - **Username:** `ST10385107@rcconnect.edu.za`
   - **Password:** `password123`
2. **Default Bakery Menu Items:**
   - `gwinya` (Category: `Bread`, Price: `R 5.00`, Initial Stock: `14`)
   - `latte` (Category: `Coffee`, Price: `R 24.00`, Initial Stock: `11`)
3. **Checkout Flow:**
   - Tap products on the **Sales** tab to add to cart.
   - Tap **Pay** to open the checkout dialog. Choose Cash or Card.
   - For cash payments, enter amount tendered or use quick cash buttons (`Exact`, `+R10`, `+R20`, `+R50`, `+R100`) to view real-time change due.
   - Completing payment creates a pending order sent to the **Kitchen Display System (KDS)** and deducts stock from inventory automatically.
