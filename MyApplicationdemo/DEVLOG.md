# 顯 — 開發日誌

記錄每次迭代做了什麼、為什麼這樣做。

---

## 2026-10-18

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
記錄付款後，結算清單同時顯示「你欠 T $100」+ 「待收款 $100」，雖然淨額為 $0，但兩行並列讓使用者以為帳還沒結清，造成困惑。

**解法：**
1. 淨額為 $0 時直接顯示「此群組已結清 ✓」，跳過個別明細列表
2. 開啟「記錄付款」對話框時，自動帶入欠款最多的對象名稱到「付給誰」欄位

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

---

## 歷史功能摘要

| Commit | 說明 |
|--------|------|
| `fb3e701` | 修復結算付款方向錯誤（Bug 1）、非創建者管理員無法離開群組（Bug 2）、結算清單記錄付款後不關閉（Bug 4）、硬編碼字串（Bug 5） |
| `6ae137d` | 新增管理員權限（設為/取消管理員） |
| `d2c19a6` `6ed1986` | 查看群組成員名單 |
| `cedc2c6` | 多人加入群組（邀請碼、暱稱、即時同步） |
| `c7e0256` `c4a0645` | 滑動刪除支出 + Snackbar 復原 |
| `85cb05c` | 修復群組餘額與詳情頁不同步 |
| `954f0bf` | 新增查看結算、記錄付款功能 |
| `f2f0c77` | 首頁群組列表、餘額總覽 |
| `0cfa780` | 登入介面 |
