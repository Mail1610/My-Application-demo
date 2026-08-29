# 顯 — 多人共用記帳 APP

## 系統簡介

**顯** 是一款以 Android（Java）開發的群組記帳應用程式，解決現有記帳 APP 無法多人共用的痛點。
以「群組」為核心，支援情侶、室友、朋友出遊、家庭等各種共同記帳場景，並提供最優結算計算，以最少交易筆數完成清帳。

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
- Retrofit2 + OkHttp（後端 API 串接）
- Room Database（本地快取）
- Material Components 3

### 後端（獨立專案）
- Java 17 + Spring Boot 3
- Spring Security + JWT
- Spring Data JPA / Hibernate
- MySQL 8

---

## 專案結構

```
app/src/main/
├── AndroidManifest.xml
│
├── java/com/example/myapplicationdemo/
│   │
│   ├── auth/                              # 登入 / 註冊
│   │   ├── LoginActivity.java
│   │   └── RegisterActivity.java
│   │
│   ├── group/                             # 群組管理（待開發）
│   │   ├── GroupListActivity.java
│   │   ├── GroupDetailActivity.java
│   │   └── CreateGroupActivity.java
│   │
│   ├── expense/                           # 支出記錄（待開發）
│   │   ├── ExpenseListFragment.java
│   │   └── AddExpenseActivity.java
│   │
│   ├── settlement/                        # 結算（待開發）
│   │   └── SettlementFragment.java
│   │
│   ├── model/                             # 資料模型（待開發）
│   │   ├── User.java
│   │   ├── Group.java
│   │   ├── Expense.java
│   │   └── Settlement.java
│   │
│   ├── network/                           # API 網路層（待開發）
│   │   ├── ApiService.java
│   │   └── ApiClient.java
│   │
│   └── utils/
│       └── LogUtils.java                  # 統一日誌工具
│
└── res/
    ├── drawable/
    │   └── ic_arrow_back.xml
    ├── layout/
    │   ├── activity_login.xml
    │   └── activity_register.xml
    └── values/
        ├── colors.xml
        ├── strings.xml
        └── themes.xml
```

---

## 功能進度

| 模組 | 狀態 |
|------|------|
| 登入頁面 | ✅ 完成 |
| 註冊頁面 | ✅ 完成 |
| 群組管理 | 🔲 待開發 |
| 新增支出 | 🔲 待開發 |
| 分帳計算 | 🔲 待開發 |
| 結算清單 | 🔲 待開發 |
| 後端 API 串接 | 🔲 待開發 |
| 推播通知（FCM） | 🔲 待開發 |

---

## 開發環境

- Android Studio Meerkat 以上
- minSdk 24（Android 7.0）
- targetSdk 37
- JDK 11

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
- **ViewModel**：呼叫 Repository，持有 LiveData
- **Repository**：決定資料來源（網路 or Room 快取）
- **Network**：只做 API 呼叫，不含業務判斷

### 命名慣例
- Layout ID：`snake_case`（例：`til_email`、`btn_login`）
- Java 變數：`camelCase`
- 常數：`UPPER_SNAKE_CASE`

---

## 結算演算法

使用「最少交易次數」貪婪演算法：

1. 計算每位成員淨餘額（已付 - 應付）
2. 正數 → 應收錢；負數 → 應付錢
3. 最大堆 / 最小堆配對，每次消除最大債務，最小化交易筆數

範例：A 付了 $300，B 付了 $0，C 付了 $0，三人均分 $100
→ B 欠 A $100，C 欠 A $100（2 筆，最優）
#   M y - A p p l i c a t i o n - d e m o 
 
 
