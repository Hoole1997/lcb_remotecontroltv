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
