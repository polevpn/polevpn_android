# PoleVPN Android

## Project Overview

PoleVPN Android is a VPN client application for Android devices. It uses a hybrid architecture combining a native Android VPN service with a WebView-based UI frontend.

**Key Characteristics:**
- **Application Type**: Android VPN client application
- **Package Name**: `com.polevpn.application`
- **Architecture**: Hybrid (Native Android Service + WebView UI)
- **Primary Language**: Java
- **UI Framework**: Vue.js 3 + Element Plus (running inside WebView)
- **VPN Core**: Native Go library (`polevpnmobile.aar`)

## Technology Stack

### Build System
- **Gradle**: 8.6
- **Android Gradle Plugin**: 8.2.2
- **Build Tools Version**: 34.0.0

### Android Configuration
- **Compile SDK**: 34
- **Target SDK**: 34
- **Minimum SDK**: 21 (Android 5.0)
- **Java Version**: 1.8
- **Supported ABIs**: armeabi-v7a, arm64-v8a, x86

### Key Dependencies
- `androidx.appcompat:appcompat:1.2.0`
- `com.google.android.material:material:1.2.1`
- `androidx.constraintlayout:constraintlayout:1.1.3`
- `androidx.navigation:navigation-fragment:2.2.2`
- `androidx.navigation:navigation-ui:2.2.2`
- `polevpnmobile.aar` (Custom Go-based VPN library)

### UI Stack (WebView Assets)
- Vue.js 3 (bundled in assets)
- Element Plus UI library
- Single-page application loaded from `file:///android_asset/index.html`

## Project Structure

```
polevpn_android/
├── app/
│   ├── build.gradle              # App-level build configuration
│   ├── proguard-rules.pro        # ProGuard configuration (mostly empty)
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── assets/           # Web UI files (Vue.js app)
│       │   │   ├── index.html    # Main UI entry point
│       │   │   ├── vue.js        # Vue.js library
│       │   │   └── element-ui/   # Element Plus UI components
│       │   ├── java/com/polevpn/application/
│       │   │   ├── App.java                  # Application class
│       │   │   ├── MainActivity.java         # Main activity with WebView
│       │   │   ├── data/
│       │   │   │   └── Node.java             # Data model (mostly empty)
│       │   │   ├── services/
│       │   │   │   ├── AccessServer.java     # Server configuration model
│       │   │   │   ├── NetworkMonitor.java   # Network state monitoring
│       │   │   │   ├── PoleVPNManager.java   # Singleton VPN manager
│       │   │   │   └── PoleVPNService.java   # Android VPN service
│       │   │   └── tools/
│       │   │       ├── SharePref.java        # SharedPreferences wrapper
│       │   │       └── Utils.java            # Utility functions
│       │   └── res/              # Android resources
│       ├── androidTest/          # Instrumented tests
│       └── test/                 # Unit tests
├── build.gradle                  # Root build configuration
├── gradle.properties             # Gradle properties
├── settings.gradle               # Project settings
└── local.properties              # Local SDK path (not in git)
```

## Code Organization

### Main Components

1. **App.java** - Application singleton
   - Initializes the native database and logging
   - Provides global application context

2. **MainActivity.java** - Main UI controller
   - Hosts a WebView that loads the Vue.js frontend
   - Implements JavaScript interface (`@JavascriptInterface`) for WebView-to-native communication
   - Handles VPN permission requests and lifecycle
   - Manages VPN connection state machine

3. **PoleVPNService.java** - Android VPN Service
   - Extends `android.net.VpnService`
   - Creates and manages the VPN tunnel interface
   - Shows foreground notification when VPN is active
   - Handles VPN interface configuration (IP, DNS, routes)

4. **PoleVPNManager.java** - VPN Manager Singleton
   - Singleton pattern for global VPN state management
   - Manages `PoleVPN` instance from native library
   - Handles network monitoring callbacks
   - Provides message handling mechanism between components

5. **AccessServer.java** - Data Model
   - Configuration model for VPN server connections
   - Contains endpoint, credentials, SNI, routing rules

6. **NetworkMonitor.java** - Network State Monitoring
   - Monitors network connectivity changes
   - Uses `ConnectivityManager.NetworkCallback`

### JavaScript Interface Methods (MainActivity)

The WebView exposes these native methods to JavaScript:

| Method | Description |
|--------|-------------|
| `GetVersion()` | Returns app version JSON |
| `GetUpDownBytes()` | Returns upload/download traffic statistics |
| `GetAllLogs()` | Returns all VPN logs |
| `GetAllAccessServer(req)` | Returns configured servers |
| `DeleteAccessServer(req)` | Deletes a server configuration |
| `UpdateAccessServer(req)` | Updates a server configuration |
| `AddAccessServer(req)` | Adds a new server configuration |
| `StopAccessServer(req)` | Stops VPN connection |
| `ConnectAccessServer(req)` | Initiates VPN connection |

## Build Commands

### Build APK
```bash
./gradlew assembleDebug      # Build debug APK
./gradlew assembleRelease    # Build release APK
```

### Install
```bash
./gradlew installDebug       # Install debug build
./gradlew installRelease     # Install release build
```

### Clean
```bash
./gradlew clean              # Clean build artifacts
```

### Test
```bash
./gradlew test               # Run unit tests
./gradlew connectedAndroidTest  # Run instrumented tests (requires device)
```

## Development Conventions

### Code Style
- Standard Java conventions
- Package naming: `com.polevpn.application.*`
- Class naming: PascalCase
- Method/variable naming: camelCase
- Chinese comments used in some files (NetworkMonitor.java)

### Error Handling
- Try-catch blocks with `e.printStackTrace()` for debugging
- Logging via `Polevpnmobile.log(level, message)` for native library integration
- Android Log class used for platform logging

### Threading
- UI updates use `Handler(Looper.getMainLooper()).post()`
- VPN operations run on background threads via native library

## Testing Strategy

**Current State**: Minimal test coverage

- **Unit Tests**: Basic example test only (`ExampleUnitTest.java`)
- **Instrumented Tests**: Basic example test only (`ExampleInstrumentedTest.java`)
- **No integration tests** for VPN functionality

### Running Tests
```bash
# Unit tests
./gradlew test

# Instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest
```

## Deployment Process

### Release Build Configuration
- **Signing**: Configured in `local.properties` (not tracked by git)
  - Properties: `RELEASE_STORE_FILE`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_PASSWORD`, `RELEASE_KEY_ALIAS`
- **ProGuard**: Disabled (`minifyEnabled false`)
- **Version**: Defined in `app/build.gradle`
  - `versionCode`: 1
  - `versionName`: "1.0"

## Security Considerations

1. **Signing Configuration**: Ensure signing credentials are properly secured (use environment variables or local.properties, avoid committing to version control)
2. **WebView Security**: 
   - `setAllowUniversalAccessFromFileURLs(true)` is enabled
   - JavaScript is enabled
   - File access is enabled
3. **VPN Permissions**: App requires `CONTROL_VPN` permission (system-level permission)
4. **SSL Verification**: App supports skipping SSL verification (`skipSSLVerify` flag in server config)
5. **Native Library**: Core VPN logic is in `polevpnmobile.aar` (closed source)

## Native Library Integration

The app depends on `polevpnmobile.aar` which provides:
- `PoleVPN` - Main VPN client class
- `Polevpnmobile` - Utility class for database and logging
- `PoleVPNEventHandler` - Event callback interface

Key native methods used:
- `Polevpnmobile.initDB(path)` - Initialize local database
- `Polevpnmobile.setLogPath(path)` - Set log directory
- `Polevpnmobile.setLogLevel(level)` - Set logging level
- `Polevpnmobile.getAllLogs()` - Retrieve logs
- `Polevpnmobile.getRouteIpsFromDomain(domains)` - DNS resolution
- `polevpn.start(endpoint, user, password, sni, skipSSLVerify)` - Start VPN
- `polevpn.stop()` - Stop VPN
- `polevpn.attach(fd)` - Attach VPN interface file descriptor

## Notes for AI Agents

1. **WebView UI Changes**: The UI is implemented in `app/src/main/assets/index.html` using Vue.js, not in native Android layouts.

2. **VPN Permission**: The app requires user approval for VPN. The flow is:
   - Call `VpnService.prepare()` → Get intent → Start for result
   - User grants permission → Start `PoleVPNService`
   - Create VPN interface → Get FD → Attach to native library

3. **Network Routes**: Routes can come from:
   - Remote server (if `useRemoteRouteRules` is true)
   - Local manual configuration (`localRouteRules`)
   - Domain-based proxy rules (resolved to IPs)

4. **Foreground Service**: VPN service runs as a foreground service with a persistent notification.

5. **Chinese Comments**: Some files contain Chinese comments (especially NetworkMonitor.java).

## Build System Upgrade History

### 2024-03-13: Gradle & AGP Upgrade

**Problem**: Original build system (Gradle 6.5 + AGP 4.1.1) incompatible with Java 21

**Changes Made**:
1. **Gradle Wrapper**: 6.5 → 8.6
2. **Android Gradle Plugin**: 4.1.1 → 8.2.2
3. **Compile SDK**: 30 → 34
4. **Target SDK**: 30 → 34
5. **Build Tools**: 30.0.2 → 34.0.0
6. **Repositories**: Replaced deprecated `jcenter()` with `mavenCentral()`

**Code Changes Required**:
1. **app/build.gradle**:
   - Added `namespace 'com.polevpn.application'`
   - Added `buildFeatures { buildConfig true }`
   - Updated repositories to include `google()` and `mavenCentral()`

2. **AndroidManifest.xml**:
   - Removed `package="com.polevpn.application"` attribute (now in build.gradle)
   - Added `android:exported="true"` to MainActivity (required for API 31+)

3. **MainActivity.java**:
   - Removed deprecated `webSettings.setAppCacheEnabled(true)` (removed in API 33)
   - Updated `polevpn.start()` call to match new native library signature (added 2 String parameters)

**Build Status**: ✅ Debug build successful

### 2024-03-13: Signing Configuration Security Fix

**Problem**: Signing credentials hardcoded in `app/build.gradle`

**Solution**: Moved signing configuration to `local.properties` (which is in `.gitignore`)

**Changes Made**:
1. **app/build.gradle**: Modified to read signing config from `local.properties` dynamically
2. **local.properties**: Added signing properties (not tracked by git)
   - `RELEASE_STORE_FILE`
   - `RELEASE_STORE_PASSWORD`
   - `RELEASE_KEY_PASSWORD`
   - `RELEASE_KEY_ALIAS`

**Security Benefit**: Sensitive credentials no longer exposed in version control
