# TV Remote App Implementation Flow

This document is the execution tracker for the TV infrared remote app. Update the checkbox and the implementation summary after each major step to preserve context.

## 1. Product And Architecture Baseline

Status: ✅ Completed

### Subflow
- ✅ Confirm user flow: home empty/data state, brand list, model list, add flow, remote screen.
- ✅ Confirm UI direction: Material Design 3 light theme, commercial Android utility, no distorted concept UI.
- ✅ Confirm code constraints: decoupled modules, maintainable structure, comments where helpful.
- ✅ Confirm project baseline: Android XML/ViewBinding app, existing `MainActivity`, Material3 theme.

### Implementation Summary
Created `.impeccable.md` with persistent design context. The app will use native Android views with programmatic screen builders to keep UI modular without introducing a large navigation framework. Business logic will be separated into data, storage, infrared, and UI layers.

## 2. IRDB Asset Integration

Status: ✅ Completed

### Subflow
- ✅ Download or clone the open-source Flipper IRDB TV directory.
- ✅ Copy the TV brand/model `.ir` files into `app/src/main/assets/irdb/tv`.
- ✅ Include source attribution and license/readme metadata in assets.
- ✅ Keep file structure close to upstream so brand folders map directly to app brands.

### Implementation Summary
Integrated the upstream `TVs` directory from `Lucaslhm/Flipper-IRDB` into `app/src/main/assets/irdb/tv`. The package now includes 397 TV `.ir` files across 118 brand folders, plus `Flipper-IRDB-LICENSE.txt` and `Flipper-IRDB-README.md` for attribution. Runtime brand/model listing can map directly to asset folders and files.

## 3. Data Model, Parsing, And Repository

Status: ✅ Completed

### Subflow
- ✅ Define remote domain models: brand, model/profile, command, signal, saved TV.
- ✅ Implement Flipper `.ir` parser for parsed and raw command entries.
- ✅ Implement asset repository that lists brands and model files from packaged assets.
- ✅ Normalize common command names into app buttons such as Power, Volume, Channel, Home, Back, Menu, Source, and Mute.
- ✅ Add lightweight persistence for added TVs and selected scenes.

### Implementation Summary
Added `IrModels.kt`, `FlipperIrParser.kt`, `AssetTvRemoteRepository`, and `SharedPreferencesSavedTvRepository`. The app can parse Flipper parsed/raw command entries, list TV brands from asset folders, load model/profile files, map common IR command names to app remote buttons, and persist added TVs with scene labels.

## 4. Infrared Encoding And Transmit Layer

Status: ✅ Completed

### Subflow
- ✅ Wrap Android `ConsumerIrManager` behind an `IrTransmitter` interface.
- ✅ Implement raw signal transmission from Flipper raw data.
- ✅ Implement protocol encoders for the most common TV protocols in IRDB: NEC, NECext, Samsung32, Sony variants, RC5, and RC6 where practical.
- ✅ Return clear unsupported-protocol errors instead of crashing.
- ✅ Add device capability checks for phones without an IR emitter.

### Implementation Summary
Added `AndroidIrTransmitter`, `IrSendResult`, and `FlipperIrEncoder`. Raw Flipper timings transmit directly through `ConsumerIrManager`. Parsed commands are encoded into Android `frequency + pattern` arrays for NEC, NECext, Samsung32, SIRC/SIRC15/SIRC20, Kaseikyo, RCA, Pioneer, RC5/RC5X, and RC6. The manifest declares `android.hardware.consumerir` as optional so the app remains installable on phones without an IR emitter.

## 5. Material UI Screens

Status: ✅ Completed

### Subflow
- ✅ Replace the template home layout with a screen container.
- ✅ Implement home empty state with add entry.
- ✅ Implement home data state with saved TV cards and scene labels.
- ✅ Implement brand list page from IRDB brand folders.
- ✅ Implement model list page from selected brand files.
- ✅ Implement add flow for scene/name customization.
- ✅ Implement remote page with power, D-pad, volume/channel, and utility buttons. No numeric keypad.

### Implementation Summary
Added `RemoteUiKit.kt` and `TvRemoteScreens.kt`. The app now builds Material-style screens programmatically with reusable colors, cards, buttons, and spacing helpers. Implemented home empty/data states, searchable brand list, searchable model list, add dialog, and remote screen. The remote page intentionally excludes numeric keypad buttons and only shows power, D-pad, volume/channel, and utility controls.

### Revision Summary
Reworked this step after design review. Removed the dynamic view-composition screen builder from the app flow and replaced it with independent XML layouts:
- `activity_main_home.xml` for home empty/data states.
- `activity_brand_list.xml` for the brand list page.
- `activity_model_list.xml` for the model list page.
- `activity_remote_control.xml` for the remote page.

All list surfaces now use `RecyclerView` with independent item XML files:
- `item_saved_tv_card.xml`
- `item_common_brand.xml`
- `item_brand_row.xml`
- `item_model_row.xml`

### Home Component Revision Summary
✅ Reworked the home page to match `reference-home-empty.png` and `reference-home-data.png` with real Material components instead of hand-built top/bottom bars. `activity_main_home.xml` now uses `MaterialToolbar` for the top add action, `BottomNavigationView` for the bottom tabs, `RecyclerView` for saved TVs, and independent empty/data state containers. `MainActivity` keeps state rendering and navigation wiring separate from XML styling.

### Home Spec Alignment Summary
✅ Rechecked current Android/Material guidance and Material Components dimensions after the toolbar/bottom-nav height review. The home page now uses a 64dp small top app bar and an 80dp Material 3 bottom navigation bar, with bottom-nav icon/text sizes returned to standard component proportions.

## 6. Flow Wiring And State Handling

Status: ✅ Completed

### Subflow
- ✅ Wire home add button to brand list.
- ✅ Wire brand item to model list, and auto-enter add flow when a brand has one model.
- ✅ Wire model selection to add flow.
- ✅ Save TV and automatically enter remote screen on success.
- ✅ Wire saved TV card to remote screen.
- ✅ Handle back navigation predictably.

### Implementation Summary
Reworked `MainActivity` into a lightweight screen state machine. It owns repositories, transmitter, back stack, add dialog, and send-result feedback while delegating view creation to `TvRemoteScreens`. Brand selection opens the model list unless a brand only has one model, in which case it directly opens the add dialog. Successful add saves the TV and jumps to the remote screen.

### Revision Summary
Replaced the single-Activity state machine with separate Activity pages:
- `MainActivity`: home page only, shows empty state or saved TVs and opens `BrandListActivity`.
- `BrandListActivity`: brand search/list page, opens `ModelListActivity` or direct add dialog when the brand has one model.
- `ModelListActivity`: model search/list page, selects a model and opens the add dialog.
- `RemoteControlActivity`: remote-control page and infrared send actions.

The add dialog was extracted to `AddRemoteDialog` so single-model brands and model-list selection share the same add behavior.

## 7. Verification

Status: ✅ Completed

### Subflow
- ✅ Run Gradle build or compile task.
- ✅ Fix compile errors.
- ✅ Verify no unrelated dirty files were reverted.
- ✅ Update this document with final implementation summaries.

### Implementation Summary
Ran `./gradlew :app:assembleLocalDebug`. The first run exposed a corrupted Gradle transform cache, which was removed and regenerated. The second run exposed Kotlin compile issues (`singleLine` property usage and a missing `MaterialButton` import), which were fixed. Final build completed successfully.

### Revision Summary
Ran `./gradlew :app:assembleLocalDebug` again after the independent Activity/XML/RecyclerView refactor. The build completed successfully.

### Home Component Verification
- ✅ Ran `./gradlew :app:assembleLocalDebug` after the MaterialToolbar/BottomNavigationView home-page revision. The build completed successfully.
- ✅ Ran `./gradlew :app:assembleLocalDebug` after correcting toolbar and bottom navigation dimensions to Material-standard sizes. The build completed successfully.

## 8. Brand Logo Assets

Status: ✅ Completed

### Subflow
- ✅ Add a reproducible logo download script for TV brands with verified domain mappings.
- ✅ Download verified logo assets into `res/drawable-nodpi` so the app works offline after packaging.
- ✅ Generate `BrandLogoResolver` to map IRDB brand folder names to local drawable resources.
- ✅ Bind logos in the current refactored UI: saved TV cards, all-brand rows, common-brand cards, and model-list brand header.
- ✅ Keep fallback rendering for uncovered brands: initials on brand tiles and generic TV icon on saved-device cards.
- ✅ Retry uncovered brands with corrected domains/source order and keep poor or mismatched candidates excluded.

### Implementation Summary
Added `tools/download_tv_brand_logos.py`, `BrandLogoResolver.kt`, and `BrandLogoViews.kt`. The current package includes 82 verified local logo assets with source metadata in `brand_logo_sources.json`. After the UI refactor, logo binding was re-applied to the active adapters/layouts instead of the older replaced views.

### Brandfetch Evaluation Summary
✅ Added `tools/generate_brandfetch_logo_preview.py` to evaluate Brandfetch Logo API quality without downloading or caching Brandfetch images. The script reads the current TV brand domain mapping and generates `brandfetch-logo-preview.html`, which directly embeds Brandfetch CDN URLs with `BRANDFETCH_CLIENT_ID`.

### Brandfetch CDN Download Summary
✅ Updated `tools/download_tv_brand_logos.py` to use Brandfetch Logo API CDN URLs directly with `BRANDFETCH_CLIENT_ID`, without calling the paid REST API. The script downloads CDN logo images into local `drawable-nodpi` resources for offline APK usage and keeps Uplead/Google favicon only as fallback sources. The current generated set contains 82 logos, with most sourced from Brandfetch Logo API CDN.

### Missing Brand Retry Summary
✅ Retried uncovered brands with corrected or more specific domains and per-brand source ordering. Added local logos for AWA, Akai, Apex, BBK, Bauhn, Brandt, ContinentalEdison, Devant, Ffalcon, Nevir, Onn, PDi, Technika, Vitec, and Zenith. The script now normalizes downloaded images by trimming transparent/near-white padding and writing PNG output, improving visual balance in RecyclerView rows and saved-device cards. Brands with wrong, generic, or unreadable candidates, such as DYON, United, Viano, and Wbox, remain on fallback initials instead of packaging misleading assets.

### Verification
- ✅ Ran `./gradlew :app:assembleLocalDebug` after re-binding logos to the current UI. The build completed successfully.
- ✅ Ran `./gradlew :app:assembleLocalDebug` after switching logo generation to Brandfetch Logo API CDN downloads. The build completed successfully.

## 9. IRDB Command Alias Compatibility

Status: ✅ Completed

### Subflow
- ✅ Inspect Xiaomi IRDB raw command naming and confirm `Open` is a raw IR command, not an unsupported protocol.
- ✅ Centralize additional command aliases in `RemoteAction.fromIrName()` instead of adding page-level special cases.
- ✅ Map `Open`, `Standby`, `On/Off`, and `Power_on` style names to the app `POWER` action.
- ✅ Handle slash-separated names such as `Return/back` so they map to `BACK`.
- ✅ Add unit tests for Xiaomi `Open`, Xiaomi `More`, slash-separated back, and action-menu aliases.

### Implementation Summary
Fixed command-action normalization in `IrModels.kt`. Xiaomi `Xiaomi_TV.ir` uses `name: Open` for a raw power signal and `name: More` for the menu signal, and the transmitter already supports raw signals; the missing piece was action alias mapping. The remote page now enables the power and menu buttons for that profile because `Open` maps to `RemoteAction.POWER` and `More` maps to `RemoteAction.MENU`. Added `RemoteActionTest` and `FlipperIrParserTest` to lock this behavior.

### Verification
- ✅ Ran `./gradlew :app:testLocalDebugUnitTest`. Unit tests passed.
- ✅ Ran `./gradlew :app:assembleLocalDebug`. Build completed successfully.

### Alias Coverage Expansion Summary
✅ Scanned all 397 packaged TV `.ir` files and 10,976 `name:` entries. Expanded command matching for aliases that can safely map to the current remote UI actions: discrete power variants, compound volume/channel names, `Exit`/`Cancel` return variants, HDMI/USB/source variants, quick-menu variants, guide/help/info variants, and common OK/select variants. `TvRemoteProfile.supportedActions` now uses match priority so canonical names such as `Power` win over lower-priority aliases such as `Power_off` when both exist. After the expansion, mapped command entries increased from 4,781 to 5,505, and unique unmapped command names dropped from 1,373 to 1,098. Remaining unmapped names are mainly numeric keypad, media transport, color keys, subtitle/audio, sleep, and app shortcut keys that do not have dedicated controls in the current UI.

## 10. Remote Button Haptic Feedback

Status: ✅ Completed

### Subflow
- ✅ Add the Android vibration permission to the main manifest.
- ✅ Encapsulate platform vibrator APIs behind a small reusable remote haptic helper.
- ✅ Use `VibratorManager` on Android 12+ and `Vibrator` on older supported versions.
- ✅ Trigger a short tick only when a supported remote action is clicked.

### Implementation Summary
Added `android.permission.VIBRATE` and `RemoteHapticFeedback`. `RemoteControlActivity` initializes the helper once and calls `performKeyPress()` before transmitting a supported IR command. Unsupported/disabled buttons do not trigger haptics.

### Verification
- ✅ Ran `./gradlew :app:assembleLocalDebug`. Build completed successfully.

## 11. Infrared Capability Tip

Status: ✅ Completed

### Subflow
- ✅ Reuse `AndroidIrTransmitter.hasEmitter()` as the device infrared capability check.
- ✅ Add a compact warning tip below the remote toolbar.
- ✅ Show the tip only when the phone has no consumer IR emitter.
- ✅ Keep send-time `NoEmitter` handling as a safety fallback.

### Implementation Summary
Added a `noIrTip` row to `activity_remote_control.xml` below the toolbar divider and styled it with `bg_remote_ir_tip`. `RemoteControlActivity` now calls `isInfraredSupported()` after creating the transmitter and toggles the tip visibility. Supported devices keep the previous clean remote layout.

### Verification
- ✅ Ran `./gradlew :app:assembleLocalDebug`. Build completed successfully.

## 12. Chinese String Resource Internationalization

Status: ✅ Completed

### Subflow
- ✅ Add `values-zh/strings.xml` for Simplified Chinese UI copy.
- ✅ Keep `values/strings.xml` and `values-en/strings.xml` aligned as default/English fallbacks.
- ✅ Move home, settings, brand list, model list, add-remote, remote error/tip, delete dialog, and ad fallback copy out of XML/Kotlin hardcoded text.
- ✅ Keep remote-control professional button labels and symbols hardcoded where appropriate: `VOL`, `CH`, `OK`, direction arrows, `+`, and `−`.
- ✅ Remove remaining Chinese comments and unused imports found during scan.

### Implementation Summary
Centralized user-facing copy into Android string resources and added a Chinese resource folder. XML pages now reference `@string/...`, Kotlin Toast/Dialog/default-field text uses `getString(...)`, adapters use formatted resources for counts/protocol labels, and ad renderer fallback labels are resource-backed. Runtime hardcoded XML text now only contains the allowed remote-control symbols/professional labels.

### Verification
- ✅ Ran Chinese residual scan outside `values-zh`; no Chinese hardcoded text remains in app source outside localized resources.
- ✅ Ran runtime XML hardcoded text scan; remaining entries are only remote-control symbols/professional labels.
- ✅ Ran `./gradlew :app:testLocalDebugUnitTest`. Unit tests passed.
- ✅ Ran `./gradlew --stop` after a parallel Gradle cache conflict, then reran `./gradlew :app:assembleLocalDebug` sequentially. Build completed successfully.

## 13. App Language Switching

Status: ✅ Completed

### Subflow
- ✅ Replace the settings-page language `AlertDialog` with an independent `BottomSheetDialog`.
- ✅ Add a rounded top sheet background, centered handle, and RecyclerView country/language list.
- ✅ Centralize supported app languages in `AppLanguage`, with country code, country name, and language name resolved from the active locale.
- ✅ Apply language changes through `AppCompatDelegate.setApplicationLocales(...)`, keeping the empty locale list as the follow-system option.
- ✅ Expand `locales_config.xml` for system per-app language support.
- ✅ Add complete string resources for selected mainstream/populous countries: United States, China, India, Spain, Saudi Arabia, Brazil, Bangladesh, Russia, Indonesia, Japan, Korea, Germany, France, Vietnam, and Turkey.
- ✅ Verify every localized `strings.xml` contains the same resource keys as the default file.

### Implementation Summary
Added `LanguageBottomSheet`, `LanguageOptionAdapter`, and `AppLanguage` to decouple language selection from `SettingsFragment`. The settings page now opens a Material bottom sheet with a handle and selectable country rows; selecting a row immediately calls the AndroidX AppCompat locale API. Resource coverage now includes 15 country locale tags plus follow-system, and each locale has a full 71-key translation file.

### Verification
- ✅ Compared all 18 `strings.xml` files against the default resource file; no missing or extra keys.
- ✅ Compared `AppLanguage.supportedLocaleTags` with `locales_config.xml`; no mismatches.
- ✅ Ran runtime XML hardcoded text scan; remaining entries are only remote-control symbols/professional labels.
- ✅ Ran `./gradlew :app:assembleLocalDebug`. Build completed successfully.
- ✅ Ran `./gradlew :app:testLocalDebugUnitTest`. Unit tests passed.
