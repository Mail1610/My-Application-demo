const admin = require("firebase-admin");

// Firebase credentials via environment variable (set in Railway dashboard)
const serviceAccount = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT);
admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
});

const db = admin.firestore();
const messaging = admin.messaging();

let initialized = false;
console.log("Ledgr notification server started:", new Date().toISOString());

// Listen to all expenses across all groups via collectionGroup query
db.collectionGroup("expenses").onSnapshot(
    (snapshot) => {
        // Skip initial snapshot (existing documents before server started)
        if (!initialized) {
            initialized = true;
            console.log(`Initial snapshot: ${snapshot.size} existing expenses (skipped)`);
            return;
        }

        snapshot.docChanges().forEach((change) => {
            if (change.type !== "added") return;
            const groupId = change.doc.ref.parent.parent.id;
            handleNewExpense(groupId, change.doc.data()).catch((err) =>
                console.error("handleNewExpense error:", err)
            );
        });
    },
    (err) => console.error("Firestore listener error:", err)
);

async function handleNewExpense(groupId, expense) {
    const groupDoc = await db.collection("groups").doc(groupId).get();
    if (!groupDoc.exists) return;

    const { name: groupName = "", members = [], nicknames = {} } = groupDoc.data();
    const { adderNickname = "", payer = "", description = "", totalAmount = 0 } = expense;

    const otherIds = members.filter((id) => (nicknames[id] || "") !== adderNickname);
    if (otherIds.length === 0) return;

    const tokenDocs = await Promise.all(
        otherIds.map((id) => db.collection("fcmTokens").doc(id).get())
    );
    const tokens = tokenDocs
        .filter((doc) => doc.exists && doc.data().token)
        .map((doc) => doc.data().token);

    if (tokens.length === 0) return;

    const title = `${groupName} 新增支出`;
    const body = `${payer} 付了 ${description} $${Math.round(totalAmount)}`;

    const result = await messaging.sendEachForMulticast({ notification: { title, body }, tokens });
    console.log(
        `[${groupId}] ${description} → notified ${result.successCount}/${tokens.length} devices`
    );
}

process.on("SIGTERM", () => process.exit(0));
