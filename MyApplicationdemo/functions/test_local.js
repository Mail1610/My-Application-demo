/**
 * 本地邏輯測試腳本 — 不需要 Firebase 憑證
 * 模擬 notifyNewExpense 的核心邏輯：成員過濾、token 查詢、訊息組裝
 * 執行：node test_local.js
 */

// ─── Mock data ──────────────────────────────────────────────────────────────

const mockGroup = {
    name: "TestSettle",
    members: ["device-ming", "device-alice"],
    nicknames: {
        "device-ming": "Ming",
        "device-alice": "Alice",
    },
};

const mockTokens = {
    "device-ming":  { token: "fcm-token-ming" },
    "device-alice": { token: "fcm-token-alice" },
};

const mockExpense = {
    adderNickname: "Ming",
    payer: "Ming",
    description: "Dinner",
    totalAmount: 600,
};

// ─── Function logic (extracted from index.js) ───────────────────────────────

function buildNotification(group, tokens, expense) {
    const { name: groupName, members, nicknames } = group;
    const { adderNickname, payer, description, totalAmount } = expense;

    const otherDeviceIds = members.filter(deviceId => {
        const nickname = nicknames[deviceId] || "";
        return nickname !== adderNickname;
    });

    const fcmTokens = otherDeviceIds
        .filter(id => tokens[id] && tokens[id].token)
        .map(id => tokens[id].token);

    const title = `${groupName} 新增支出`;
    const body  = `${payer} 付了 ${description} $${Math.round(totalAmount)}`;

    return { title, body, tokens: fcmTokens };
}

// ─── Tests ───────────────────────────────────────────────────────────────────

let passed = 0;
let failed = 0;

function assert(condition, label) {
    if (condition) {
        console.log(`  ✅ ${label}`);
        passed++;
    } else {
        console.error(`  ❌ ${label}`);
        failed++;
    }
}

console.log("\n=== notifyNewExpense 本地邏輯測試 ===\n");

// Test 1: Ming 新增支出 → 只通知 Alice
console.log("Test 1: 新增者 Ming 付 Dinner $600");
const result1 = buildNotification(mockGroup, mockTokens, {
    adderNickname: "Ming", payer: "Ming", description: "Dinner", totalAmount: 600,
});
assert(result1.tokens.length === 1,          "只通知 1 人（不通知自己）");
assert(result1.tokens[0] === "fcm-token-alice", "通知 Alice 的 token");
assert(result1.title === "TestSettle 新增支出",  "標題正確");
assert(result1.body  === "Ming 付了 Dinner $600", "內容正確");

// Test 2: Alice 新增支出 → 只通知 Ming
console.log("\nTest 2: 新增者 Alice 付 Hotel $900");
const result2 = buildNotification(mockGroup, mockTokens, {
    adderNickname: "Alice", payer: "Alice", description: "Hotel", totalAmount: 900,
});
assert(result2.tokens.length === 1,         "只通知 1 人");
assert(result2.tokens[0] === "fcm-token-ming", "通知 Ming 的 token");
assert(result2.body === "Alice 付了 Hotel $900", "內容正確");

// Test 3: 成員沒有 token → tokens 為空
console.log("\nTest 3: 成員沒有 FCM token");
const result3 = buildNotification(mockGroup, {}, {
    adderNickname: "Ming", payer: "Ming", description: "Taxi", totalAmount: 100,
});
assert(result3.tokens.length === 0, "無 token 時不傳送");

// Test 4: 3 位成員，1 位新增
console.log("\nTest 4: 3 位成員群組（Carol 新增）");
const group3 = {
    name: "旅遊群",
    members: ["d-ming", "d-alice", "d-carol"],
    nicknames: { "d-ming": "Ming", "d-alice": "Alice", "d-carol": "Carol" },
};
const tokens3 = {
    "d-ming":  { token: "tok-ming" },
    "d-alice": { token: "tok-alice" },
    "d-carol": { token: "tok-carol" },
};
const result4 = buildNotification(group3, tokens3, {
    adderNickname: "Carol", payer: "Carol", description: "機票", totalAmount: 15000,
});
assert(result4.tokens.length === 2,                     "通知 2 位（Ming + Alice）");
assert(result4.tokens.includes("tok-ming"),             "包含 Ming");
assert(result4.tokens.includes("tok-alice"),            "包含 Alice");
assert(!result4.tokens.includes("tok-carol"),           "不包含 Carol 自己");
assert(result4.body === "Carol 付了 機票 $15000",       "內容正確");

// ─── Summary ─────────────────────────────────────────────────────────────────
console.log(`\n結果：${passed} 通過，${failed} 失敗\n`);
process.exit(failed > 0 ? 1 : 0);
