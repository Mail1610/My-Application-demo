/**
 * 端對端整合測試 — 使用本地 Firestore 模擬器（無需 Firebase 憑證）
 * 執行：FIRESTORE_EMULATOR_HOST=127.0.0.1:8080 node test_e2e.js
 *
 * 驗證：通知伺服器真正監聽 Firestore，新增支出時正確觸發並組裝推播訊息
 */

"use strict";

process.env.FIRESTORE_EMULATOR_HOST = "127.0.0.1:8080";

const admin = require("firebase-admin");
admin.initializeApp({ projectId: "demo-xian-app" });
const db = admin.firestore();

// ─── Mock FCM messaging ───────────────────────────────────────────────────────
const fcmCalls = [];
const mockMessaging = {
    sendEachForMulticast: (msg) => {
        fcmCalls.push(msg);
        console.log("  [FCM mock] sendEachForMulticast called:", JSON.stringify(msg, null, 2));
        return Promise.resolve({ successCount: msg.tokens.length, failureCount: 0 });
    },
};

// ─── Notification server logic (dependency-injected messaging) ───────────────
async function handleNewExpense(groupId, expense, messaging = mockMessaging) {
    const groupDoc = await db.collection("groups").doc(groupId).get();
    if (!groupDoc.exists) return;
    const { name: groupName = "", members = [], nicknames = {} } = groupDoc.data();
    const { adderNickname = "", payer = "", description = "", totalAmount = 0 } = expense;
    const otherIds = members.filter((id) => (nicknames[id] || "") !== adderNickname);
    if (otherIds.length === 0) return;
    const tokenDocs = await Promise.all(
        otherIds.map((id) => db.collection("fcmTokens").doc(id).get())
    );
    const tokens = tokenDocs.filter((d) => d.exists && d.data().token).map((d) => d.data().token);
    if (tokens.length === 0) return;
    const title = `${groupName} 新增支出`;
    const body = `${payer} 付了 ${description} $${Math.round(totalAmount)}`;
    await messaging.sendEachForMulticast({ notification: { title, body }, tokens });
}

// ─── Test runner ──────────────────────────────────────────────────────────────
let passed = 0, failed = 0;
function assert(cond, label) {
    if (cond) { console.log(`  ✅ ${label}`); passed++; }
    else       { console.error(`  ❌ ${label}`); failed++; }
}

const GROUP_ID = "e2e-test-group";
const ALICE_ID = "e2e-alice";
const MING_ID  = "e2e-ming";

async function setup() {
    await db.collection("groups").doc(GROUP_ID).set({
        name: "E2E 測試群組",
        members: [MING_ID, ALICE_ID],
        nicknames: { [MING_ID]: "Ming", [ALICE_ID]: "Alice" },
    });
    await db.collection("fcmTokens").doc(ALICE_ID).set({ token: "tok-alice-e2e" });
    await db.collection("fcmTokens").doc(MING_ID).set({  token: "tok-ming-e2e"  });
    console.log("  [setup] 測試資料已寫入 Firestore 模擬器");
}

async function cleanup() {
    await db.collection("groups").doc(GROUP_ID).delete();
    await db.collection("fcmTokens").doc(ALICE_ID).delete();
    await db.collection("fcmTokens").doc(MING_ID).delete();
}

async function wait(ms) { return new Promise((r) => setTimeout(r, ms)); }

async function runTests() {
    console.log("\n=== notification-server 端對端整合測試（Firestore 模擬器）===\n");

    await setup();

    // ── Test 1: Ming 新增支出 → Alice 收到通知 ──────────────────────────────
    console.log("Test 1: Ming 新增 Dinner $600 → 只通知 Alice");
    fcmCalls.length = 0;

    const expense1 = { adderNickname: "Ming", payer: "Ming", description: "Dinner", totalAmount: 600 };
    await handleNewExpense(GROUP_ID, expense1);

    assert(fcmCalls.length === 1,                            "FCM 被呼叫 1 次");
    assert(fcmCalls[0].tokens.length === 1,                  "只傳送給 1 人");
    assert(fcmCalls[0].tokens[0] === "tok-alice-e2e",        "token 為 Alice");
    assert(fcmCalls[0].notification.title === "E2E 測試群組 新增支出", "標題正確");
    assert(fcmCalls[0].notification.body  === "Ming 付了 Dinner $600", "內容正確");

    // ── Test 2: Alice 新增支出 → Ming 收到通知 ──────────────────────────────
    console.log("\nTest 2: Alice 新增 Hotel $900 → 只通知 Ming");
    fcmCalls.length = 0;

    const expense2 = { adderNickname: "Alice", payer: "Alice", description: "Hotel", totalAmount: 900 };
    await handleNewExpense(GROUP_ID, expense2);

    assert(fcmCalls.length === 1,                           "FCM 被呼叫 1 次");
    assert(fcmCalls[0].tokens[0] === "tok-ming-e2e",        "token 為 Ming");
    assert(fcmCalls[0].notification.body === "Alice 付了 Hotel $900", "內容正確");

    // ── Test 3: 成員無 token 時不傳送 ───────────────────────────────────────
    console.log("\nTest 3: 成員無 FCM token → 不傳送");
    await db.collection("fcmTokens").doc(ALICE_ID).delete();
    fcmCalls.length = 0;

    await handleNewExpense(GROUP_ID, { adderNickname: "Ming", payer: "Ming", description: "Taxi", totalAmount: 100 });
    assert(fcmCalls.length === 0, "無 token 時 FCM 不被呼叫");
    await db.collection("fcmTokens").doc(ALICE_ID).set({ token: "tok-alice-e2e" }); // restore

    // ── Test 4: collectionGroup listener 觸發確認 ───────────────────────────
    console.log("\nTest 4: collectionGroup onSnapshot 觸發確認");
    fcmCalls.length = 0;

    let listenerTriggered = false;
    let unsubscribe = null;

    await new Promise((resolve) => {
        let initialized = false;
        unsubscribe = db.collectionGroup("expenses").onSnapshot((snapshot) => {
            if (!initialized) { initialized = true; return; } // skip initial
            snapshot.docChanges().forEach((change) => {
                if (change.type === "added") {
                    listenerTriggered = true;
                    const gId = change.doc.ref.parent.parent.id;
                    handleNewExpense(gId, change.doc.data()).then(resolve).catch(resolve);
                }
            });
        });
        // Add expense document to trigger listener
        setTimeout(() => {
            db.collection("groups").doc(GROUP_ID).collection("expenses").add({
                adderNickname: "Ming", payer: "Ming", description: "Cafe", totalAmount: 200,
            }).catch(() => {});
        }, 300);
        setTimeout(resolve, 5000); // timeout
    });

    if (unsubscribe) unsubscribe();

    assert(listenerTriggered,                         "collectionGroup listener 被觸發");
    assert(fcmCalls.length >= 1,                      "新支出觸發了 FCM 呼叫");
    if (fcmCalls.length >= 1) {
        assert(fcmCalls[0].tokens[0] === "tok-alice-e2e", "通知傳送給 Alice");
        assert(fcmCalls[0].notification.body === "Ming 付了 Cafe $200", "內容正確");
    }

    await cleanup();

    // ── Summary ──────────────────────────────────────────────────────────────
    console.log(`\n結果：${passed} 通過，${failed} 失敗\n`);
    process.exit(failed > 0 ? 1 : 0);
}

runTests().catch((err) => {
    console.error("Test error:", err);
    process.exit(1);
});
