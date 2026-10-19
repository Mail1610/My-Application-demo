# 顯 — 開發日誌

記錄每次迭代做了什麼、為什麼這樣做。

---

## 歷史功能摘要

| 日期 | Commit | 說明 |
|------|--------|------|
| 2026-08-29 | `60e3765` `ba9432e` | 初始上傳、初始原型 demo |
| 2026-08-31 | `0cfa780` `f2f0c77` | 登入介面與基礎架構、首頁群組列表與餘額總覽 |
| 2026-09-07 | `954f0bf` `c4a0645` | 查看結算與記錄付款、刪除支出／群組（基礎版）|
| 2026-09-08 | `85cb05c` `c7e0256` | 修復群組餘額不同步、滑動刪除 + Snackbar 復原 + 登出按鈕 |
| 2026-09-18 | `cedc2c6` | 多人加入群組（邀請碼、暱稱、Firebase 即時同步）|
| 2026-10-17 | `d2c19a6` `6ed1986` | 查看群組成員名單 |
| 2026-10-18 | `6ae137d` `fb3e701` `e5e5b2e` `060013b` `90d8243` （新）| 管理員權限、修復多個 Bug、餘額計算錯誤、結算 UX 改善、OOP 重構、踢出成員 + 更改暱稱 |
| 2026-10-19 | `1bff97f` `5219bdf` `57cca12` `888d8e8` `8721925` `6353571` `73c4b97` `37b81f7` `8504521` `b5da32b` | APP 改名 Ledgr、支出類別篩選、最少交易結算算法、FCM 推播通知方案（Railway 伺服器）、支出列表一天一節 |

---

## 2026-08-29

### 初始上傳 `60e3765`

專案初始化，上傳基礎程式碼骨架。

---

### 初始原型 `ba9432e`

早期 demo 版本，建立基本 Activity 結構與 Firestore 連線測試。

---

## 2026-08-31

### 新增登入介面與基礎架構 `0cfa780`

**新功能：**
建立 App 基礎架構：登入流程、群組管理頁、支出列表。

**影響範圍：** `AndroidManifest.xml`、`GroupDetailActivity.java`、`MainActivity.java`、`ExpenseAdapter.java`、`GroupAdapter.java`、`Expense.java`、`Group.java`

---

### 新增首頁群組列表與餘額總覽 `f2f0c77`

**新功能：**
登入後主頁顯示所有群組列表，每個群組顯示名稱與我的當前餘額，頂部 header 顯示所有群組的淨餘額加總。

**影響範圍：** `LoginActivity.java`、`RegisterActivity.java`、`activity_login.xml`、`activity_register.xml`

---

## 2026-09-07

### 新增結算與記錄付款功能 `954f0bf`

**新功能：**
群組詳情頁新增「查看結算」按鈕，點擊後展開 BottomSheet，顯示：
- 我目前的淨餘額（正值為待收，負值為待付）
- 我欠哪些人多少錢
- 其他人欠我多少
- 「記錄付款」按鈕：新增一筆結算支出，金額歸零

**架構新增：**
- `SettlementBottomSheetFragment`（新增）
- `fragment_settlement_bottom_sheet.xml` layout
- `dialog_record_payment.xml` layout
- `bg_gradient_header.xml`、`bg_circle.xml` 漸層背景
- `activity_group_detail.xml` 加入結算按鈕區域
- `item_expense.xml`、`item_group.xml` 設計定稿

**影響範圍：** 幾乎所有 Activity/ViewModel/Adapter，及多個 layout 與 drawable

---

### 新增刪除功能（基礎版）`c4a0645`

**新功能：**
支出可以刪除（無復原），群組可以刪除。為後續滑動刪除+復原（`c7e0256`）奠定資料層基礎。

**實作：**
- `ExpenseAdapter` 新增刪除 callback interface
- `GroupAdapter` 新增刪除 callback
- `ExpenseDao` / `GroupDao` 新增 delete 方法
- `ExpenseRepository` / `GroupRepository` 串接 DAO delete
- `GroupDetailViewModel` / `MainViewModel` 暴露 delete 方法
- `ic_delete.xml` 刪除圖示

**影響範圍：** `GroupDetailActivity.java`、`MainActivity.java`、`ExpenseAdapter.java`、`GroupAdapter.java`、`ExpenseDao.java`、`GroupDao.java`、`ExpenseRepository.java`、`GroupRepository.java`、`GroupDetailViewModel.java`、`MainViewModel.java`、`ic_delete.xml`、`strings.xml`

---

## 2026-09-08

### 修復群組餘額與詳情頁不同步 `85cb05c`

**問題：**
`MainActivity` 顯示的群組餘額總覽，與進入 `GroupDetailActivity` 看到的餘額不一致。根本原因是 `MainViewModel` 計算餘額後快取值沒有在群組變動時重新觸發。

**解法：**
`GroupRepository` 新增 group listener 並在 `MainViewModel` 訂閱，群組資料（含成員數）變更時重新計算主頁餘額。

**影響範圍：** `MainActivity.java`、`GroupRepository.java`、`MainViewModel.java`

---

### 新增刪除功能（滑動刪除 + Snackbar 復原）`c7e0256`

**新功能：**
支出列表支援左滑刪除，刪除後出現 Snackbar 提供「復原」選項，5 秒內可撤銷操作。

**新增登出按鈕：**
`activity_main.xml` Toolbar 新增登出圖示（`ic_logout`），點擊後清除 Session 並返回登入頁。

**影響範圍：** `GroupDetailActivity.java`、`LoginActivity.java`、`MyApplication.java`、`SettlementBottomSheetFragment.java`、`GroupAdapter.java`、`ExpenseRepository.java`、`GroupDetailViewModel.java`、`activity_group_detail.xml`、`activity_main.xml`、`menu_main.xml`、`strings.xml`

---

## 2026-09-18

### 新增多人功能（邀請碼 + Firebase 即時同步）`cedc2c6`

**背景：**
原有架構使用 Mock API + Room 本機資料庫，只支援單裝置使用。此版本全面改為 Firebase Firestore，並加入多人加入群組的邀請碼機制。

**新功能：**
1. 邀請碼加入群組：創建群組時自動生成 6 位英數邀請碼，其他人輸入邀請碼後填寫暱稱即可加入
2. 暱稱系統：每位成員加入時設定暱稱，Expense/Group 資料以暱稱標識操作者
3. 即時同步：Firestore snapshot listener，支出與群組資料多人同步更新
4. 離開群組：成員可離開群組，最後一人離開時刪除群組
5. 登入/註冊介面整合：`LoginActivity` / `RegisterActivity` 使用 Firebase Auth 方案

**架構改動：**
- 移除 Room DAO (`AppDatabase`、`ExpenseDao`、`GroupDao`) 的核心角色，改由 Firestore 驅動
- 新增 `database/` 包保留 Room schema（備用）
- `network/` 包改用 `ApiService` + `MockInterceptor` 過渡期處理
- `SessionManager` 儲存裝置 ID + 暱稱到 SharedPreferences
- `MyApplication` 初始化 Firebase

**影響範圍：** `AndroidManifest.xml`、幾乎所有 Java 檔案及 layout

---

## 2026-10-17

### 新增查看人員名單 `6ed1986` `d2c19a6`

**新功能：**
在群組詳情頁新增「查看成員」功能，可以看到群組內所有成員的暱稱列表。

**實作：**
- `GroupRepository` 新增 `getMembers(groupId)` 方法，從 Firestore 讀取 members 子集合
- `GroupDetailViewModel` 新增 `loadMembers(groupId, callback)` 方法
- `GroupDetailActivity` 新增成員清單對話框，點擊 toolbar 成員數顯示彈窗
- `activity_group_detail.xml` 成員數 TextView 加入點擊區域
- `menu_group_detail.xml` 新增「查看成員」選單項目
- `strings.xml` 新增成員相關文字

**影響範圍：** `GroupRepository.java`、`GroupDetailViewModel.java`、`GroupDetailActivity.java`、`GroupAdapter.java`、`ExpenseRepository.java`、`activity_group_detail.xml`、`menu_group_detail.xml`、`strings.xml`

---

## 2026-10-18

### 新增管理員權限 `6ae137d`

**新功能：**
群組創建者可以將其他成員設為管理員，或取消管理員身份。管理員擁有與創建者相同的群組管理權限（改名、修改邀請碼、踢人）。

**實作：**
- `Group` model 新增 `adminDeviceIds` 欄位（List<String>），儲存管理員裝置 ID 列表
- `GroupRepository` 新增 `grantAdmin(groupId, targetDeviceId)` / `revokeAdmin(groupId, targetDeviceId, creatorDeviceId)` 方法，直接操作 Firestore arrayUnion/arrayRemove
- `GroupDetailViewModel` 暴露 `grantAdmin` / `revokeAdmin` 方法供 Activity 呼叫
- `GroupDetailActivity` 成員清單對話框加入長按選單，顯示「設為管理員」/「取消管理員」選項，依權限動態顯示
- `dialog_change_invite_code.xml` 新增修改邀請碼對話框 layout
- `menu_group_detail.xml` 新增菜單項目（改名、修改邀請碼）
- `colors.xml` 補充缺少的顏色定義

**影響範圍：** `Group.java`、`GroupRepository.java`、`GroupDetailViewModel.java`、`GroupDetailActivity.java`、`MainActivity.java`、`ExpenseAdapter.java`、`GroupAdapter.java`、`Expense.java`、`ExpenseRepository.java`、`dialog_add_expense.xml`、`dialog_change_invite_code.xml`、`menu_group_detail.xml`、`colors.xml`、`strings.xml`

---

### 修復多個 Bug `fb3e701`

**Bug 1 — 結算付款方向錯誤：**
`SettlementBottomSheetFragment` 記錄付款時，付款者角色與受款者角色對調，導致結算方向錯誤。修正 payer/payee 邏輯，確認「我付給對方」方向正確。

**Bug 2 — 非創建者管理員無法離開群組：**
`GroupDetailActivity` 離開群組的判斷只檢查是否為創建者，忽略管理員情況，導致管理員點離開後無反應。修正條件判斷，管理員也可以正常離開。

**Bug 4 — 記錄付款後結算 BottomSheet 不關閉：**
付款成功後 UI 更新，但 BottomSheet 停留在原位沒有收起。在成功回調後加入 `dismiss()`。

**Bug 5 — 硬編碼字串：**
`strings.xml` 補充缺少的 String resource，替換程式碼中的硬編碼文字。

**影響範圍：** `GroupDetailActivity.java`、`SettlementBottomSheetFragment.java`、`strings.xml`

---

### 修復多人群組餘額計算錯誤 `e5e5b2e`

**問題：**
`myShare` 欄位只記錄「支出新增者」自己的分攤金額，但系統誤用它來計算群組內所有人的餘額。只要有人輸入非平均分攤，其他人的餘額就全錯。

**解法：**
在 Expense 新增 `adderNickname` 欄位，計算時判斷「這筆支出是不是我新增的」：
- 是 → 用 `myShare`
- 否 → 用 `(totalAmount - myShare) / (memberCount - 1)`

**影響範圍：** `Expense.java`、`ExpenseRepository.java`、`GroupDetailViewModel.java`、`SettlementBottomSheetFragment.java`、`GroupDetailActivity.java`

**向後相容：** 舊支出 `adderNickname` 為空時，退回使用 `myShare`（原有行為不變）。

---

### 改善結算清單 UX `060013b`

**問題：**
記錄付款後，結算清單同時顯示「你欠 T $100」+「待收款 $100」，雖然淨額為 $0，但兩行並列讓使用者以為帳還沒結清，造成困惑。

**解法：**
1. 淨額為 $0 時直接顯示「此群組已結清 ✓」，跳過個別明細列表
2. 開啟「記錄付款」對話框時，自動帶入欠款最多的對象名稱到「付給誰」欄位

**影響範圍：** `SettlementBottomSheetFragment.java`

---

### 重構分攤計算邏輯並修正狀態列遮擋 `90d8243`

**問題 1 — OOP 重構：**
`shareFor` 私有方法重複出現在三個類別（`ExpenseRepository`、`GroupDetailViewModel`、`SettlementBottomSheetFragment`），違反封裝原則，日後維護需同步修改三處。

**解法：**
將邏輯整合為 `Expense.getShareFor(nickname, memberCount)` public method，三個呼叫方統一使用 `e.getShareFor()`。

**問題 2 — 狀態列遮擋：**
`AppBarLayout` 未設定 `fitsSystemWindows`，導致 header 內容從螢幕最頂端開始，被手機時間、電量列擋住。

**解法：**
`activity_main.xml` 與 `activity_group_detail.xml` 的 `AppBarLayout` 加入 `android:fitsSystemWindows="true"`。

**影響範圍：** `Expense.java`、`activity_main.xml`、`activity_group_detail.xml`

---

### 新增踢出成員 + 更改暱稱 `（未命名 commit）`

**新功能 1 — 踢出成員：**
管理員可在「管理成員」對話框長按成員，選擇踢出（창建者受保護，不可被踢）。踢出後即時從 Firestore 移除該成員的所有資料（members、nicknames、balances、admins，memberCount -1）。

**新功能 2 — 更改暱稱：**
所有成員均可透過選單「更改暱稱」修改自己在群組內的暱稱，修改後即時同步至 Firestore 並更新本地 SessionManager 快取。

**實作：**
- `GroupRepository` 新增 `kickMember(groupId, targetDeviceId)` 方法
- `GroupRepository` 新增 `updateNickname(groupId, deviceId, newNickname)` 方法
- `GroupDetailViewModel` 新增 `KickMemberCallback` interface 及 `kickMember()` 方法
- `GroupDetailViewModel` 新增 `UpdateNicknameCallback` interface 及 `updateNickname()` 方法
- `GroupDetailActivity.showManageMembersDialog()` 重寫：點擊非自身成員呼叫 `showMemberActionDialog()`
- `GroupDetailActivity.showMemberActionDialog()` 新增：兩層對話框（動作選單 → 確認踢出）
- `GroupDetailActivity.showChangeNicknameDialog()` 新增：使用 `DialogSetNicknameBinding`，預填當前暱稱
- `menu_group_detail.xml` 新增 `action_change_nickname` 選單項目
- `strings.xml` 新增 kick/nickname 相關字串

**影響範圍：** `GroupRepository.java`、`GroupDetailViewModel.java`、`GroupDetailActivity.java`、`menu_group_detail.xml`、`strings.xml`

---

## 2026-10-19

### APP 改名 Ledgr + 換圖示與啟動畫面 `1bff97f` `5219bdf`

**背景：**
原本 APP 名稱為「顯」，圖示為預設 Android launcher icon。此版本完成品牌化，改名為 **Ledgr**。

**變更：**
- `strings.xml` app_name 改為 Ledgr
- 所有解析度 mipmap（hdpi / mdpi / xhdpi / xxhdpi / xxxhdpi）換為 Ledgr logo png
- `activity_splash.xml` 啟動畫面改用橫版 Ledgr banner 圖（去浮水印版本）

**影響範圍：** `mipmap-*/ic_launcher*.png`、`activity_splash.xml`、`strings.xml`

---

### 新增支出類別篩選 `57cca12`

**新功能：**
群組詳情頁支出列表頂部新增 ChipGroup，可依類別（全部 / 食 / 住 / 行 / 娛 / 其他）過濾支出。

**實作：**
- `activity_group_detail.xml` 加入 `HorizontalScrollView` + `ChipGroup`，每個類別一個 `Chip`
- `GroupDetailActivity` 監聽 `setOnCheckedStateChangeListener`，切換時重新呼叫 `updateExpenseList()` 並過濾
- 預設選取「全部」；切換類別不重新請求 Firestore，直接過濾本地快取列表

**影響範圍：** `GroupDetailActivity.java`、`activity_group_detail.xml`、`strings.xml`

---

### 新增最少交易筆數結算算法 `888d8e8`

**背景：**
原結算只顯示每人對每人的個別欠款，n 人群組最多有 n(n-1) 筆交易。新功能使用貪婪算法計算最少交易方案，自動合併多筆欠款，減少結清所需付款次數。

**算法：**
1. 計算每位成員的淨餘額（正 = 待收，負 = 待付）
2. 維護兩個 heap（最大債主、最大債務人），每次配對金額最大的兩方
3. 重複到所有餘額歸零為止，產生最少交易清單

**實作：**
- `SettlementBottomSheetFragment` 新增 `computeMinTransactions()` 方法，替換原本逐對計算的邏輯
- 新增 `setTestData()` 方法，可在離線測試模式下繞過 Firestore 直接驗證算法
- `GroupDetailActivity` 加入 `add_test_alice` intent extra 觸發測試模式

**影響範圍：** `SettlementBottomSheetFragment.java`、`GroupDetailActivity.java`、`strings.xml`

---

### 修復餘額同步 + 加入 FCM 推播服務基礎 `8721925`

**Bug 修復：**

**問題 1 — MainActivity 顯示 $0：**
`GroupDetailViewModel` 計算完餘額後只更新本地 LiveData，未寫回 Firestore，導致 `MainActivity` 從 Firestore 讀到的餘額始終為 $0。
**解法：** `GroupDetailViewModel` 計算餘額後呼叫 `GroupRepository.updateBalance()` 寫回 Firestore。

**問題 2 — 結算成員不匹配：**
`GroupRepository.getMembers()` 在管理員暱稱後附加「★」標記，導致結算時成員名稱對不上 Expense 的 adderNickname。
**解法：** `getMembers()` 改為回傳純暱稱列表，星號標記移至 UI 層顯示。

**FCM 推播基礎：**
- 新增 `AppFirebaseMessagingService`：繼承 `FirebaseMessagingService`，接收推播並建立通知
- `MainActivity` 加入執行期通知權限申請（Android 13+）
- `AndroidManifest` 註冊 service，`GroupDetailActivity` 改回 `exported=false`

**影響範圍：** `GroupDetailViewModel.java`、`GroupRepository.java`、`SettlementBottomSheetFragment.java`、`GroupDetailActivity.java`、`MainActivity.java`、`AppFirebaseMessagingService.java`（新增）

---

### FCM 推播通知方案：Cloud Functions + Railway 伺服器 `6353571` `73c4b97` `37b81f7` `8504521`

**背景：**
Firebase Cloud Messaging 需要伺服器端觸發推播。Firebase Cloud Functions 需要 Blaze（付費）方案，因此同時實作以 Railway 免費服務為替代方案。

**方案一 — Cloud Functions（`6353571`）：**
- `functions/index.js`：Firestore `expenses/{expenseId}` onCreate 觸發，讀取同群組所有成員的 FCM token，呼叫 FCM API 推播
- `firebase.json` 加入 functions 設定
- 本地測試 13/13 通過（`73c4b97`）
- 狀態：程式碼完成，需升級 Firebase Blaze 後 deploy

**方案二 — Railway 長駐伺服器（`37b81f7`）：**
- `notification-server/index.js`：Node.js 長駐服務，使用 Firestore `collectionGroup` 監聽所有群組的 expenses 集合，新支出寫入時觸發 FCM 推播給同群組其他成員
- `notification-server/test_local.js`：邏輯測試 10/10 通過（2人/3人群組、無 token 邊界情況）
- `railway.toml`：Railway 部署設定（免費方案）
- 端對端測試（`8504521`）：`test_e2e.js` 使用本地 Firestore 模擬器執行整合測試，13/13 通過

**影響範圍：** `functions/index.js`（新增）、`functions/package.json`（新增）、`notification-server/index.js`（新增）、`notification-server/test_local.js`（新增）、`notification-server/test_e2e.js`（新增）、`railway.toml`（新增）、`firebase.json`

---

### 支出列表一天一節 + 移除模擬器連線 `b5da32b`

**新功能 — 支出列表日期分組：**
原本每張支出卡片都顯示日期，同一天有多筆支出時日期重複出現。改為用日期標題行分隔，同日支出只在最上方顯示一次日期。

**實作：**
- `ExpenseAdapter` 改用混合列表 `List<Object>`（`String` 日期標題 + `Expense` 支出項目），`getItemViewType()` 區分兩種 ViewHolder
- 新增 `DateHeaderViewHolder` 對應 `item_date_header.xml`（置中文字 + 兩側橫線）
- `item_expense.xml` 隱藏卡片上原有的日期欄位
- `GroupDetailActivity.setupSwipeToDelete()` 覆寫 `getSwipeDirs()`，標題行回傳 0 不可滑動刪除；`onSwiped()` 加 null 檢查

**修復 — 移除 Firestore 模擬器連線：**
`MyApplication` 移除 `useEmulator()` 呼叫，APP 改連線至正式 Firebase Firestore。

**影響範圍：** `ExpenseAdapter.java`、`GroupDetailActivity.java`、`MyApplication.java`、`item_expense.xml`、`item_date_header.xml`（新增）
