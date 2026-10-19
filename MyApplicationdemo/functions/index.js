const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

/**
 * 新增支出時通知同群組其他成員。
 * 部署前需升級至 Firebase Blaze 方案：firebase deploy --only functions
 */
exports.notifyNewExpense = functions.firestore
    .document("groups/{groupId}/expenses/{expenseId}")
    .onCreate(async (snap, context) => {
        const expense = snap.data();
        const groupId = context.params.groupId;

        const groupDoc = await admin.firestore()
            .collection("groups").doc(groupId).get();
        if (!groupDoc.exists) return;

        const groupData = groupDoc.data();
        const members = groupData.members || [];
        const nicknames = groupData.nicknames || {};
        const groupName = groupData.name || "";

        const adderNickname = expense.adderNickname || "";
        const payer = expense.payer || "";
        const description = expense.description || "";
        const totalAmount = expense.totalAmount || 0;

        // 排除新增者本人，找出其他成員的 deviceId
        const otherDeviceIds = members.filter(deviceId => {
            const nickname = nicknames[deviceId] || "";
            return nickname !== adderNickname;
        });

        if (otherDeviceIds.length === 0) return;

        // 批次查詢 FCM tokens
        const tokenDocs = await Promise.all(
            otherDeviceIds.map(id =>
                admin.firestore().collection("fcmTokens").doc(id).get()
            )
        );

        const tokens = tokenDocs
            .filter(doc => doc.exists && doc.data().token)
            .map(doc => doc.data().token);

        if (tokens.length === 0) return;

        const title = `${groupName} 新增支出`;
        const body = `${payer} 付了 ${description} $${Math.round(totalAmount)}`;

        await admin.messaging().sendEachForMulticast({ notification: { title, body }, tokens });
    });
