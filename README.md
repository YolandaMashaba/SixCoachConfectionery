# SixCoachConfectionery Android POS & KDS Mobile App

SixCoachConfectionery is a bakery Point-of-Sale (POS), Kitchen Display System (KDS), and admin-management Android app for staff ordering, kitchen workflows, and inventory control.

It uses Kotlin, Android Jetpack, Firebase Authentication, Firestore, and Google Sign-In for secure staff access and order processing.

YouTube link: https://youtube.com/shorts/GkLbJgSIv_4?si=66T2o3O6PXWhMI0p

---

## Features

- Staff login and registration
- Admin dashboard for user management and role assignment
- Sales workflow with add-to-cart, quantity adjustments, and checkout
- Cash and card payment flow
- Kitchen order queue and completion tracking
- Inventory stock reduction on successful order placement
- Firestore-backed user and order persistence
- Google Sign-In support using the configured Web Client ID

---

## Tech Stack

- Android: Kotlin, ViewBinding, Material Components
- Firebase: Firebase Auth, Firestore
- Google Identity: Google Sign-In / Google One Tap
- Build: Gradle + Android Gradle Plugin

---

## Firebase & Google Setup

Before running the app, configure the Firebase project and Google credentials:

1. Add your Android app to Firebase.
2. Download `google-services.json` and place it in `app/`.
3. In Google Cloud Console, create or configure the OAuth Web Client ID.
4. Use the Web Client ID in the app's Google Sign-In request:

  337377997144-bjd8tsh7vksnuk7sd04hinojrekrn0eu.apps.googleusercontent.com

5. Ensure the Firebase Auth user and Firestore security rules allow the required flows for sign-in, order creation, and admin operations.

---

## Default Admin Account

The app seeds a default admin account during startup:

- Email: `admin@email.com`
- Password: `admin@123`

This account is used for admin access and user-management operations.

---

## Firestore Security Notes

The app relies on authenticated Firestore access for:

- user profile reads and admin deletion
- order creation
- stock deduction during checkout
- inventory transaction logging
- kitchen order updates

Important rule requirements include:

- authenticated users only
- `customerId == request.auth.uid` for order creation
- numeric `totalAmount` and `orderDate`
- admin-only user deletion
- authenticated stock updates for valid sale flows

---

## Build & Run

From the project root:

```bash
./gradlew app:assembleDebug
./gradlew testDebugUnitTest
```

---

## Usage Flow

1. Launch the app.
2. Log in with an existing account or register a staff user.
3. Use the Sales screen to add items to the cart.
4. Tap Pay to complete checkout.
5. The transaction writes the order, inventory records, and kitchen order data.
6. Admin users can manage staff users from the admin panel.

---

## Troubleshooting

Common issues:

- Firestore `PERMISSION_DENIED`: check auth state and Firestore rule conditions.
- Google Sign-In issues: verify the Web Client ID matches the Google Cloud OAuth client.
- Admin delete fails: ensure the signed-in user is an actual admin profile in Firestore.
- Missing credentials: confirm `requestIdToken` / Google ID token generation is configured correctly.

---

## Notes

This project is intended for bakery POS workflows and is structured around a Firebase + Android mobile stack. It supports both staff operations and admin management in a single Android app.
