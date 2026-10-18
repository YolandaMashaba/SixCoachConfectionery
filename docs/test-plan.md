# Test Plan - Ndu (Software Tester)

| ID | Feature | Input | Expected result | Status |
|----|---------|-------|-----------------|--------|
| T01 | Register | Valid email + 8+ char password | Account created | Pending |
| T02 | Register | Password under 8 chars | Error shown, no crash | Pending |
| T03 | Register | Empty fields | Error shown, no crash | Pending |
| T04 | Login | Correct credentials | User logged in | Pending |
| T05 | Login | Wrong password | Error shown, no crash | Pending |
| T06 | Google SSO | Google account | User signed in | Pending |
| T07 | Cart | Add item | Total updates | Pending |
| T08 | Cart | Negative quantity | Rejected | Pending |
| T09 | Checkout | Pay with items in cart | Order created as pending | Pending |
| T10 | Kitchen | Press Complete | Order leaves queue | Pending |
| T11 | Inventory | Add item, empty name | Error shown | Pending |
| T12 | Settings | Change a setting | Setting saved | Pending |