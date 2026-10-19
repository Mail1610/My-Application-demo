# 顯 — 多人共用記帳 APP

## 系統簡介

**顯** 是一款以 Android（Java）開發的群組記帳應用程式，解決現有記帳 APP 無法多人共用的痛點。
以「群組」為核心，支援情侶、室友、朋友出遊、家庭等各種共同記帳場景。

### 核心概念

```
使用者
 │
 ├─ 群組 A（室友帳）─── 成員：小明、小花、阿強
 │    ├─ 支出：水電費 $1,800（小明付）
 │    ├─ 支出：租金 $30,000（小花付）
 │    └─ 結算：阿強欠小明 $600、小花欠阿強 $200…
 │
 └─ 群組 B（旅遊帳）─── 成員：小明、大雄、胖虎
      ├─ 支出：機票 $15,000（大雄付）
      └─ 結算：小明欠大雄 $5,000…
```

---

## 技術棧

### Android 前端
- Java 11
- MVVM + Repository Pattern
- ViewBinding
- Firebase Firestore（即時資料庫）
- Firebase Cloud Messaging（推播通知，進行中）
- Material Components 3

---

## 專案結構

```
app/src/main/
├── AndroidManifest.xml
│
├── java/com/example/myapplicationdemo/
│   ├── SplashActivity.java              # 啟動封面
│   ├── MainActivity.java                # 群組列表主畫面
│   ├── GroupDetailActivity.java         # 群組詳情、支出管理
│   ├── SettlementBottomSheetFragment.java  # 結算清單
│   ├── AppFirebaseMessagingService.java # FCM 推播服務
│   │
│   ├── adapter/
│   │   ├── GroupAdapter.java            # 群組列表 Adapter
│   │   └── ExpenseAdapter.java          # 支出列表 Adapter（含滑動刪除）
│   │
│   ├── model/
│   │   ├── Group.java
│   │   └── Expense.java
│   │
│   ├── repository/
│   │   ├── GroupRepository.java         # Firestore 群組 CRUD
│   │   └── ExpenseRepository.java       # Firestore 支出 CRUD
│   │
│   ├── viewmodel/
│   │   ├── MainViewModel.java
│   │   └── GroupDetailViewModel.java
│   │
│   └── utils/
│       ├── LogUtils.java                # 統一日誌工具
│       └── SessionManager.java          # 本地暱稱 / 裝置 ID 快取
│
└── res/
    ├── layout/                          # XML 版面
    ├── menu/                            # Toolbar 選單
    ├── drawable/                        # 圖示、漸層背景
    └── values/                          # colors、strings、themes
```

---

## 功能進度

| 模組 | 狀態 | 備註 |
|------|------|------|
| APP 啟動封面 | ✅ 完成 | 漸層背景 + 淡入動畫 |
| 設定暱稱 | ✅ 完成 | 首次啟動引導設定 |
| 建立群組 | ✅ 完成 | 含邀請碼生成 |
| 加入群組 | ✅ 完成 | 邀請碼加入 |
| 邀請碼管理 | ✅ 完成 | 複製、修改邀請碼 |
| 群組重新命名 | ✅ 完成 | 管理員專屬 |
| 新增支出 | ✅ 完成 | 含分類（食/住/行/娛/其他） |
| 編輯支出 | ✅ 完成 | 長按支出卡片 |
| 刪除支出 | ✅ 完成 | 滑動刪除 + Snackbar 復原 |
| 平均分攤計算 | ✅ 完成 | 一鍵平均分攤按鈕 |
| 結算清單 | ✅ 完成 | BottomSheet 顯示餘額明細 |
| 記錄付款 | ✅ 完成 | 欠款方才能操作 |
| 更改暱稱 | ✅ 完成 | 即時同步 Firestore |
| 管理員設定 | ✅ 完成 | 創建者可授予/撤銷管理員 |
| 踢出成員 | ✅ 完成 | 管理員專屬 |
| 離開群組 | ✅ 完成 | 非創建者可主動離開 |
| 刪除群組 | ✅ 完成 | 管理員專屬 |
| 支出類別篩選 | ✅ 完成 | 群組詳情頁 ChipGroup 即時篩選 |
| 最少交易筆數結算 | ✅ 完成 | 貪婪算法，結算清單顯示最優方案 |
| 推播通知（FCM） | ✅ 完成 | Android 端完成；通知伺服器邏輯本地測試 10/10 通過；部署到 Railway 免費方案（見下方部署步驟） |

---

## 開發環境

- Android Studio Meerkat 以上
- minSdk 24（Android 7.0）
- targetSdk 37
- JDK 11
- Firebase Firestore（本地 Emulator 開發）

**啟用 ViewBinding**（已設定於 `app/build.gradle.kts`）：
```kotlin
buildFeatures {
    viewBinding = true
}
```

---

## 撰寫規則

### 日誌規範
- 禁用 `System.out.println()` 及直接使用 `android.util.Log`
- 統一使用 `LogUtils.info()` / `LogUtils.error()` / `LogUtils.debug()` / `LogUtils.warn()`

### 分層原則
- **Activity / Fragment**：只處理 UI 事件，業務邏輯移到 ViewModel
- **ViewModel**：呼叫 Repository，持有 LiveData，背景執行緒 + Handler 回主執行緒
- **Repository**：Firestore 操作，使用 `Tasks.await()` 同步呼叫

### 命名慣例
- Layout ID：`snake_case`（例：`til_email`、`btn_login`）
- Java 變數：`camelCase`
- 常數：`UPPER_SNAKE_CASE`

---

## 結算演算法

使用「最少交易次數」貪婪演算法（已實作並驗證）：

1. 計算每位成員淨餘額（已付 - 應付）
2. 正數 → 應收錢；負數 → 應付錢
3. 最大堆 / 最小堆配對，每次消除最大債務，最小化交易筆數

範例：Ming 付了 Dinner $600、Alice 付了 Hotel $900，2 人均分
→ Ming 欠 Alice $150（1 筆，最優）✅

---

## FCM 推播通知 — Railway 部署步驟

通知伺服器位於 `notification-server/`，使用 Firestore collectionGroup 監聽觸發。

### 步驟 1 — 取得 Firebase 服務帳號金鑰
1. 開啟 [Firebase Console](https://console.firebase.google.com/) → 你的專案
2. **專案設定** → **服務帳號** → **產生新的私密金鑰**
3. 下載 JSON 檔案，複製全部內容（包含大括號）

### 步驟 2 — 部署到 Railway
1. 前往 [railway.app](https://railway.app) 建立免費帳號
2. 建立新 Project → **Deploy from GitHub repo** → 選擇此倉庫
3. 在 **Variables** 頁面新增環境變數：
   - Key：`FIREBASE_SERVICE_ACCOUNT`
   - Value：步驟 1 複製的 JSON 字串（整段貼上）
4. Railway 自動讀取 `railway.toml`，執行 build 並啟動伺服器

### 驗證
伺服器啟動後，在 Railway **Logs** 頁面應看到：
```
Ledgr notification server started: 2024-xx-xxTxx:xx:xx.xxxZ
Initial snapshot: N existing expenses (skipped)
```
之後每次群組新增支出，Log 會顯示：
```
[groupId] 描述 → notified N/N devices
```
