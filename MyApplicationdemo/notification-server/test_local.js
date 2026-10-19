/**
 * 通知伺服器邏輯測試 — 不需要 Firebase 憑證
 * 執行：node test_local.js
 */

function buildNotification(group, tokens, expense) {
    const { name: groupName = "", members = [], nicknames = {} } = group;
    const { adderNickname = "", payer = "", description = "", totalAmount = 0 } = expense;

    const otherIds = members.filter((id) => (nicknames[id] || "") !== adderNickname);
    const fcmTokens = otherIds
        .filter((id) => tokens[id] && tokens[id].token)
        .map((id) => tokens[id].token);

    return {
        title: `${groupName} 新增支出`,
        body: `${payer} 付了 ${description} $${Math.round(totalAmount)}`,
        tokens: fcmTokens,
    };
}

let passed = 0, failed = 0;
function assert(cond, label) {
    if (cond) { console.log(`  ✅ ${label}`); passed++; }
    else       { console.error(`  ❌ ${label}`); failed++; }
}

const G2 = {
    name: "TestSettle",
    members: ["d-ming", "d-alice"],
    nicknames: { "d-ming": "Ming", "d-alice": "Alice" },
};
const T2 = { "d-ming": { token: "tok-ming" }, "d-alice": { token: "tok-alice" } };

console.log("\n=== notification-server 邏輯測試 ===\n");

console.log("Test 1: Ming 新增 → 只通知 Alice");
const r1 = buildNotification(G2, T2, { adderNickname: "Ming", payer: "Ming", description: "Dinner", totalAmount: 600 });
assert(r1.tokens.length === 1, "只通知 1 人");
assert(r1.tokens[0] === "tok-alice", "token 為 Alice");
assert(r1.title === "TestSettle 新增支出", "標題正確");
assert(r1.body  === "Ming 付了 Dinner $600", "內容正確");

console.log("\nTest 2: Alice 新增 → 只通知 Ming");
const r2 = buildNotification(G2, T2, { adderNickname: "Alice", payer: "Alice", description: "Hotel", totalAmount: 900 });
assert(r2.tokens.length === 1, "只通知 1 人");
assert(r2.tokens[0] === "tok-ming", "token 為 Ming");

console.log("\nTest 3: 無 token → 不發送");
const r3 = buildNotification(G2, {}, { adderNickname: "Ming", payer: "Ming", description: "Taxi", totalAmount: 100 });
assert(r3.tokens.length === 0, "空 token 陣列");

console.log("\nTest 4: 3 人群組");
const G3 = { name: "旅遊", members: ["d1","d2","d3"], nicknames: { d1:"A", d2:"B", d3:"C" } };
const T3 = { d1:{ token:"t1" }, d2:{ token:"t2" }, d3:{ token:"t3" } };
const r4 = buildNotification(G3, T3, { adderNickname: "C", payer: "C", description: "機票", totalAmount: 15000 });
assert(r4.tokens.length === 2, "通知 2 人");
assert(r4.tokens.includes("t1") && r4.tokens.includes("t2"), "包含 A B");
assert(!r4.tokens.includes("t3"), "不含 C 自己");

console.log(`\n結果：${passed} 通過，${failed} 失敗\n`);
process.exit(failed > 0 ? 1 : 0);
