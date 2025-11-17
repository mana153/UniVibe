# 🎯 UniVibe App Navigation Diagram

## Application Flow Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    UNIVIBE APP NAVIGATION                       │
└─────────────────────────────────────────────────────────────────┘

                          APP STARTUP
                              │
                              ▼
                    ╔═══════════════════╗
                    ║ SplashActivity    ║ ◄── LAUNCHER
                    ║  (Welcome Screen) ║
                    ╚═══════════════════╝
                            │
                    "Join Now" Button
                            │
                            ▼
                    ╔═══════════════════╗
                    ║ LoginActivity     ║
                    ║ (Login Form)      ║
                    ╚═══════════════════╝
                            │
                     "Log In" Button
                            │
                            ▼
        ┌───────────────────────────────────────────┐
        │                                           │
    ╔═══════════════════════════════════════════════════╗
    ║           MainActivity                          ║
    ║      (Main App with Bottom Nav)                 ║
    ║                                                 ║
    ║  ┌─────────────────────────────────────────┐  ║
    ║  │  RecyclerView (Events from Firestore)  │  ║
    ║  └─────────────────────────────────────────┘  ║
    ║                    │                            ║
    ║            FAB Add Event Button                ║
    ║                    │                            ║
    ║                    ▼                            ║
    ║  ┌─────────────────────────────────────────┐  ║
    ║  │  Bottom Navigation View                 │  ║
    ║  │  ┌────┬────┬────┬────┬──────────────┐  │  ║
    ║  │  │Home│Srch│Add │Msg │Notification│  │  ║
    ║  │  └────┴────┴────┴────┴──────────────┘  │  ║
    ║  └─────────────────────────────────────────┘  ║
    ║                                                 ║
    ╚═══════════════════════════════════════════════════╝
        │                    │                    │
        │                    │                    │
        ├──FAB/Add───────►   │                    │
        │                    │                    │
        │          ┌─────────┴────────────────┐   │
        │          ▼                          ▼   │
    ╔════════════════════╗          ╔═════════════════════════╗
    ║  AddEventActivity  ║          ║ NotificationsActivity   ║
    ║ (Create Event)     ║          ║ (Notifications List)    ║
    ║                    ║          ║                         ║
    ║ Add Event Form     ║          ║  Back Button ┐          ║
    ╚════════════════════╝          ║             │          ║
             │                      ║      System Back Button  ║
             │                      ║             │          ║
             └──Back──────────┬─────┘             │          ║
                              │                   │          ║
                              └──Returns to───────┴─────────┘
                                MainActivity
```

---

## Detailed Activity Relationships

```
SPLASH ACTIVITY
├─ Layout: activity_splash.xml
├─ Components:
│  ├─ ImageView (App Logo)
│  ├─ TextView (Title: "CampusConnect")
│  ├─ TextView (Subtitle: "Stay updated...")
│  └─ MaterialButton (btn_join_now)
├─ Click Listeners:
│  └─ btn_join_now → Intent to LoginActivity
└─ Behavior: finish() on navigation (no back to splash)

LOGIN ACTIVITY
├─ Layout: activity_login.xml
├─ Components:
│  ├─ ImageView (Welcome illustration)
│  ├─ TextInputLayout + TextInputEditText (Email)
│  ├─ TextInputLayout + TextInputEditText (Password)
│  └─ MaterialButton (btn_login)
├─ Click Listeners:
│  └─ btn_login → Intent to MainActivity
└─ Behavior: finish() on navigation (no back to login)

MAIN ACTIVITY
├─ Layout: activity_main.xml
├─ Components:
│  ├─ AppBarLayout (Header)
│  │  ├─ TextView ("Discover" Title)
│  │  ├─ ImageView (Search Icon)
│  │  └─ ChipGroup (Filter: All, Events, Transport, Lost & Found)
│  ├─ RecyclerView (events_recycler_view) - Events Grid
│  ├─ FloatingActionButton (fab_add_event) - Add Event
│  └─ BottomNavigationView (bottom_navigation) - 5 Items
├─ Click Listeners:
│  ├─ fabAddEvent → Intent to AddEventActivity
│  └─ Bottom Navigation Items:
│     ├─ nav_home → Stay on MainActivity
│     ├─ nav_search → Future: Search Screen
│     ├─ nav_add → Intent to AddEventActivity
│     ├─ nav_messages → Future: Messages Screen
│     └─ nav_notifications → Intent to NotificationsActivity
└─ Behavior: No finish() (main screen of app)

NOTIFICATIONS ACTIVITY
├─ Layout: activity_notifications.xml
├─ Components:
│  ├─ AppBarLayout
│  │  ├─ ImageView (btn_back)
│  │  ├─ TextView (Title: "Notifications")
│  │  └─ ImageView (Filter Icon)
│  ├─ NestedScrollView
│  │  ├─ LinearLayout (Notification Items)
│  │  ├─ Text Groups (Today, This Week, Earlier)
│  │  └─ Notification Cards (5 example items)
│  └─ BottomNavigationView (Same as MainActivity)
├─ Click Listeners:
│  ├─ btn_back → Intent to MainActivity + finish()
│  └─ OnBackPressedDispatcher → Intent to MainActivity + finish()
└─ Behavior: Returns to MainActivity on back

ADD EVENT ACTIVITY
├─ Layout: activity_add_event.xml
├─ Components:
│  ├─ TextInputLayout (Event Title)
│  ├─ TextInputLayout (Description)
│  ├─ EditText (Date/Time Picker)
│  ├─ Button (Save Event)
│  └─ Firebase Firestore Integration
├─ Functionality:
│  ├─ Date/Time Picker Dialog
│  └─ Save to Firestore
└─ Behavior: Returns to MainActivity on back (default)
```

---

## Navigation Intent Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                      INTENT MAPPING                             │
└─────────────────────────────────────────────────────────────────┘

SplashActivity.btnJoinNow
    └─► Intent(SplashActivity, LoginActivity)
        └─► finish() // Close Splash, prevent back

LoginActivity.btnLogin
    └─► Intent(LoginActivity, MainActivity)
        └─► finish() // Close Login, prevent back

MainActivity.fabAddEvent
    └─► Intent(MainActivity, AddEventActivity)

MainActivity.bottomNav.nav_add
    └─► Intent(MainActivity, AddEventActivity)

MainActivity.bottomNav.nav_notifications
    └─► Intent(MainActivity, NotificationsActivity)

NotificationsActivity.btnBack
    └─► Intent(NotificationsActivity, MainActivity)
        └─► finish() // Close Notifications

NotificationsActivity (System Back Button)
    └─► OnBackPressedDispatcher Callback
        └─► Intent(NotificationsActivity, MainActivity)
            └─► finish() // Close Notifications
```

---

## Bottom Navigation Menu Structure

```
MENU: bottom_navigation_menu.xml
│
├─ Item: nav_home
│  ├─ Icon: ic_home
│  ├─ Title: "Home"
│  └─ Action: Stay on MainActivity
│
├─ Item: nav_search
│  ├─ Icon: ic_search
│  ├─ Title: "Search"
│  └─ Action: [FUTURE] Navigate to SearchActivity
│
├─ Item: nav_add
│  ├─ Icon: ic_add
│  ├─ Title: "Add"
│  └─ Action: Intent to AddEventActivity
│
├─ Item: nav_messages
│  ├─ Icon: ic_messages
│  ├─ Title: "Messages"
│  └─ Action: [FUTURE] Navigate to MessagesActivity
│
└─ Item: nav_notifications
   ├─ Icon: ic_messages (repurposed)
   ├─ Title: "Notifications"
   └─ Action: Intent to NotificationsActivity
```

---

## Back Stack Visualization

```
USER JOURNEY - Back Stack Evolution:

1. App Starts
   Stack: [SplashActivity]

2. User taps "Join Now"
   Stack: [LoginActivity]  ← Splash removed by finish()

3. User taps "Log In"
   Stack: [MainActivity]   ← Login removed by finish()

4. User taps Notifications
   Stack: [MainActivity, NotificationsActivity]

5. User taps Back on Notifications
   Stack: [MainActivity]   ← Notifications removed by finish()

6. User taps Add (FAB)
   Stack: [MainActivity, AddEventActivity]

7. User taps Back on Add Event
   Stack: [MainActivity]   ← AddEvent removed by natural back

8. User taps system back button
   App closes (normal back behavior)
```

---

## Component Relationship Matrix

```
┌────────────────┬──────────────────┬─────────────────┬────────────┐
│ From Activity  │ Trigger Element  │ To Activity     │ Method     │
├────────────────┼──────────────────┼─────────────────┼────────────┤
│ Splash         │ Join Now Button  │ Login           │ startActivity |
│ Login          │ Log In Button    │ MainActivity    │ startActivity |
│ MainActivity   │ FAB              │ AddEvent        │ startActivity |
│ MainActivity   │ Nav: Add         │ AddEvent        │ startActivity |
│ MainActivity   │ Nav: Notif       │ Notifications   │ startActivity |
│ Notifications  │ Back Button      │ MainActivity    │ startActivity |
│ Notifications  │ System Back      │ MainActivity    │ OnBackPressed |
│ AddEvent       │ System Back      │ MainActivity    │ Default       |
└────────────────┴──────────────────┴─────────────────┴────────────┘
```

---

## State Management Diagram

```
ACTIVITY LIFECYCLE DURING NAVIGATION

SplashActivity onCreate → ...→ onDestroy (finish() called)
                                    ▼
LoginActivity onCreate → ...→ onDestroy (finish() called)
                                    ▼
MainActivity onCreate → ...→ onPause (background)
                    ▲                    ▼
                    │           NotificationsActivity onCreate
                    │                    ▼
                    │           NotificationsActivity onDestroy (finish() called)
                    │                    ▼
                    └──────────── MainActivity onResume (foreground)
```

---

## Resource ID Reference

```
SPLASH ACTIVITY
├─ R.id.btn_join_now          → "Join Now" button
└─ R.layout.activity_splash   → Layout file

LOGIN ACTIVITY
├─ R.id.et_email              → Email input field
├─ R.id.et_password           → Password input field
├─ R.id.btn_login             → "Log In" button
└─ R.layout.activity_login    → Layout file

MAIN ACTIVITY
├─ R.id.events_recycler_view  → Events grid
├─ R.id.fab_add_event         → Add event FAB
├─ R.id.bottom_navigation     → Bottom nav bar
├─ R.id.chip_all              → Filter: All
├─ R.id.chip_events           → Filter: Events
├─ R.id.chip_transport        → Filter: Transport
├─ R.id.chip_lost_found       → Filter: Lost & Found
└─ R.layout.activity_main     → Layout file

NOTIFICATIONS ACTIVITY
├─ R.id.btn_back              → Back button
├─ R.id.bottom_navigation     → Bottom nav bar
└─ R.layout.activity_notifications → Layout file

BOTTOM NAVIGATION
├─ R.id.nav_home              → Home item
├─ R.id.nav_search            → Search item
├─ R.id.nav_add               → Add item
├─ R.id.nav_messages          → Messages item
├─ R.id.nav_notifications     → Notifications item
└─ R.menu.bottom_navigation_menu → Menu file
```

---

## Package Structure

```
com.example.univibe/
├─ MainActivity.kt              ✅ Main app screen
├─ SplashActivity.kt            ✅ Welcome screen
├─ LoginActivity.kt             ✅ Login screen
├─ NotificationsActivity.kt      ✅ Notifications
├─ AddEventActivity.kt           ✅ Add event form
├─ UniVibeApp.kt                ✅ Application class
├─ models/
│  ├─ Event.kt
│  ├─ LostFoundItem.kt
│  └─ Transport.kt
├─ adapters/
│  └─ [Event adapters for RecyclerView]
└─ ui/
   └─ theme/
      ├─ Color.kt
      ├─ Theme.kt
      └─ Type.kt
```

---

**Diagram Generated:** 2025-11-18  
**Status:** ✅ COMPLETE & ACCURATE

