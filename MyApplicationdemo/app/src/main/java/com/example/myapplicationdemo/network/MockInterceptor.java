package com.example.myapplicationdemo.network;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

/**
 * 模擬後端 API，維持記憶體狀態。
 * 後端上線後，從 ApiClient 移除此 Interceptor 即可。
 */
public class MockInterceptor implements Interceptor {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final Gson gson = new Gson();

    // ── In-memory state ──────────────────────────────────────────
    private final List<MockGroup> groups = new ArrayList<>();
    private final Map<Integer, List<MockExpense>> expensesByGroup = new HashMap<>();
    private int nextGroupId = 4;
    private int nextExpenseId = 6;

    private static class MockGroup {
        int id; String name; int memberCount; double myBalance;
        MockGroup(int id, String name, int memberCount, double myBalance) {
            this.id = id; this.name = name;
            this.memberCount = memberCount; this.myBalance = myBalance;
        }
    }

    private static class MockExpense {
        int id; int groupId; String description; String payer;
        double totalAmount; double myShare; String date;
        MockExpense(int id, int groupId, String description, String payer,
                    double totalAmount, double myShare, String date) {
            this.id = id; this.groupId = groupId; this.description = description;
            this.payer = payer; this.totalAmount = totalAmount;
            this.myShare = myShare; this.date = date;
        }
    }

    private static class MockAuth {
        String token; int userId; String email; String username;
        MockAuth(String token, int userId, String email, String username) {
            this.token = token; this.userId = userId;
            this.email = email; this.username = username;
        }
    }

    public MockInterceptor() {
        // 初始群組資料
        groups.add(new MockGroup(1, "室友帳", 3, -350));
        groups.add(new MockGroup(2, "日本旅遊", 4, 1200));
        groups.add(new MockGroup(3, "情侶帳", 2, 0));

        // 群組 1 的支出資料
        List<MockExpense> g1 = new ArrayList<>();
        g1.add(new MockExpense(1, 1, "超市購物", "小明", 900, 300, "08/29"));
        g1.add(new MockExpense(2, 1, "水電費", "小花", 1800, 600, "08/25"));
        g1.add(new MockExpense(3, 1, "外送晚餐", "阿強", 450, 150, "08/20"));
        g1.add(new MockExpense(4, 1, "網路費", "小明", 600, 200, "08/15"));
        g1.add(new MockExpense(5, 1, "衛生紙／清潔用品", "阿強", 300, 100, "08/10"));
        expensesByGroup.put(1, g1);
    }

    @Override
    public synchronized Response intercept(Chain chain) throws IOException {
        Request req = chain.request();
        String path = req.url().encodedPath();
        String method = req.method();

        String body;
        int code;

        if (path.contains("/auth/")) {
            body = gson.toJson(new MockAuth(
                    "mock-jwt-token-abc123", 1, "user@example.com", "測試使用者"));
            code = 200;
        } else if (path.endsWith("/expenses")) {
            int groupId = extractGroupId(path);
            if ("GET".equals(method)) {
                List<MockExpense> list = expensesByGroup.getOrDefault(groupId, new ArrayList<>());
                body = gson.toJson(list);
                code = 200;
            } else {
                MockExpense e = createExpenseFromBody(req, groupId);
                expensesByGroup.computeIfAbsent(groupId, k -> new ArrayList<>()).add(e);
                body = gson.toJson(e);
                code = 201;
            }
        } else if (path.contains("/api/expenses/")) {
            int expenseId = extractExpenseId(path);
            removeExpense(expenseId);
            body = "";
            code = 204;
        } else if (path.endsWith("/api/groups")) {
            if ("GET".equals(method)) {
                body = gson.toJson(groups);
                code = 200;
            } else {
                MockGroup g = createGroupFromBody(req);
                groups.add(g);
                body = gson.toJson(g);
                code = 201;
            }
        } else {
            // DELETE /api/groups/{id}
            int groupId = extractGroupId(path);
            groups.removeIf(g -> g.id == groupId);
            expensesByGroup.remove(groupId);
            body = "";
            code = 204;
        }

        return new Response.Builder()
                .request(req)
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message(code == 204 ? "No Content" : "OK")
                .body(ResponseBody.create(body, JSON))
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────────

    private int extractGroupId(String path) {
        Matcher m = Pattern.compile("/groups/(\\d+)").matcher(path);
        return m.find() ? Integer.parseInt(m.group(1)) : -1;
    }

    private int extractExpenseId(String path) {
        Matcher m = Pattern.compile("/expenses/(\\d+)").matcher(path);
        return m.find() ? Integer.parseInt(m.group(1)) : -1;
    }

    private String readBody(Request req) throws IOException {
        if (req.body() == null) return "{}";
        Buffer buffer = new Buffer();
        req.body().writeTo(buffer);
        return buffer.readUtf8();
    }

    private MockGroup createGroupFromBody(Request req) throws IOException {
        com.google.gson.JsonObject json = gson.fromJson(readBody(req), com.google.gson.JsonObject.class);
        String name = json.has("name") ? json.get("name").getAsString() : "新群組";
        return new MockGroup(nextGroupId++, name, 1, 0.0);
    }

    private MockExpense createExpenseFromBody(Request req, int groupId) throws IOException {
        com.google.gson.JsonObject json = gson.fromJson(readBody(req), com.google.gson.JsonObject.class);
        return new MockExpense(
                nextExpenseId++,
                groupId,
                json.has("description") ? json.get("description").getAsString() : "",
                json.has("payer") ? json.get("payer").getAsString() : "",
                json.has("totalAmount") ? json.get("totalAmount").getAsDouble() : 0,
                json.has("myShare") ? json.get("myShare").getAsDouble() : 0,
                json.has("date") ? json.get("date").getAsString() : ""
        );
    }

    private void removeExpense(int expenseId) {
        for (List<MockExpense> list : expensesByGroup.values()) {
            list.removeIf(e -> e.id == expenseId);
        }
    }
}
