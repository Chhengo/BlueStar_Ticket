import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * BlueStar_Ticket 独立 API 测试脚本
 * 
 * 这是一个纯 Java 脚本，不依赖 Spring Boot Test
 * 可以直接编译运行，用于快速测试接口
 * 
 * 编译运行:
 * javac -cp .:lib/* StandaloneApiTest.java
 * java -cp .:lib/* com.ticket.test.StandaloneApiTest
 * 
 * 或使用 Maven:
 * mvn exec:java -Dexec.mainClass="com.ticket.test.StandaloneApiTest"
 * 
 * @author Test Engineer
 */
public class StandaloneApiTest {

    // ========== 配置参数 ==========
    private static final String BASE_URL = "http://localhost:8080";  // 修改为你的服务地址
    private static final String JWT_SECRET = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFG";
    private static final Long TEST_USER_ID = 1001L;
    private static final String TEST_USER_NAME = "testuser";
    private static final Long TEST_EVENT_ID = 1L;
    private static final Long TEST_TICKET_ID = 1L;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("🚀 BlueStar_Ticket API 测试脚本");
        System.out.println("========================================\n");

        try {
            // 1. 生成 JWT Token
            String token = generateJwtToken(TEST_USER_ID, TEST_USER_NAME);
            System.out.println("✅ 步骤1: JWT Token 已生成");
            System.out.println("   Token: " + token.substring(0, 30) + "...\n");

            // 2. 测试查询活动列表
            System.out.println("▶ 步骤2: 测试查询活动列表");
            testGetEventsList();

            // 3. 测试查询票档详情
            System.out.println("\n▶ 步骤3: 测试查询票档详情");
            testGetTicketTypes(TEST_EVENT_ID);

            // 4. 测试抢票接口
            System.out.println("\n▶ 步骤4: 测试抢票接口");
            String orderNo = testGrabTicket(token);

            // 5. 测试支付接口
            if (orderNo != null) {
                System.out.println("\n▶ 步骤5: 测试支付接口");
                testPayOrder(token, orderNo);
            }

            // 6. 测试查询订单
            if (orderNo != null) {
                System.out.println("\n▶ 步骤6: 测试查询订单");
                testGetOrder(token, orderNo);
            }

            // 7. 并发测试 (可选)
            System.out.println("\n▶ 步骤7: 并发压力测试 (可选)");
            System.out.print("是否运行并发测试? (y/n): ");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("y")) {
                testConcurrentGrab(token);
            } else {
                System.out.println("   跳过并发测试");
            }

            System.out.println("\n========================================");
            System.out.println("✅ 所有测试完成");
            System.out.println("========================================");

        } catch (Exception e) {
            System.err.println("\n❌ 测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========================================================================
    // API 测试方法
    // ========================================================================

    /**
     * 测试: 查询活动列表
     */
    private static void testGetEventsList() throws Exception {
        String url = BASE_URL + "/api/v1/events/list?page=1&size=10";
        
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        
        int responseCode = conn.getResponseCode();
        String response = readResponse(conn);
        
        System.out.println("   状态码: " + responseCode);
        System.out.println("   响应: " + formatJson(response));
        
        if (responseCode == 200) {
            System.out.println("   ✅ 查询活动列表成功");
        } else {
            System.out.println("   ❌ 查询失败");
        }
    }

    /**
     * 测试: 查询票档详情
     */
    private static void testGetTicketTypes(Long eventId) throws Exception {
        String url = BASE_URL + "/api/v1/events/" + eventId + "/ticket-types";
        
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        
        int responseCode = conn.getResponseCode();
        String response = readResponse(conn);
        
        System.out.println("   状态码: " + responseCode);
        System.out.println("   响应: " + formatJson(response));
        
        if (responseCode == 200) {
            System.out.println("   ✅ 查询票档成功");
        } else {
            System.out.println("   ❌ 查询失败");
        }
    }

    /**
     * 测试: 抢票
     */
    private static String testGrabTicket(String token) throws Exception {
        String url = BASE_URL + "/api/v1/orders/grab";
        
        // 构造请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("userId", TEST_USER_ID);
        requestBody.put("userName", TEST_USER_NAME);
        requestBody.put("eventId", TEST_EVENT_ID);
        requestBody.put("ticketId", TEST_TICKET_ID);
        
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setDoOutput(true);
        
        // 发送请求
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        int responseCode = conn.getResponseCode();
        String response = readResponse(conn);
        
        System.out.println("   请求体: " + jsonBody);
        System.out.println("   状态码: " + responseCode);
        System.out.println("   响应: " + formatJson(response));
        
        if (responseCode == 200) {
            System.out.println("   ✅ 抢票成功");
            
            // 尝试从响应中提取订单号
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> result = objectMapper.readValue(response, Map.class);
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) result.get("data");
                if (data != null && data.containsKey("orderNo")) {
                    return (String) data.get("orderNo");
                }
            } catch (Exception e) {
                // 解析失败，返回测试订单号
            }
            return "TEST_ORDER_" + System.currentTimeMillis();
        } else {
            System.out.println("   ❌ 抢票失败");
            return null;
        }
    }

    /**
     * 测试: 支付订单
     */
    private static void testPayOrder(String token, String orderNo) throws Exception {
        String url = BASE_URL + "/v1/api/pay";
        
        // 构造请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("userId", TEST_USER_ID);
        requestBody.put("orderNo", orderNo);
        
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setDoOutput(true);
        
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        int responseCode = conn.getResponseCode();
        String response = readResponse(conn);
        
        System.out.println("   订单号: " + orderNo);
        System.out.println("   状态码: " + responseCode);
        System.out.println("   响应: " + formatJson(response));
        
        if (responseCode == 200) {
            System.out.println("   ✅ 支付成功");
        } else {
            System.out.println("   ❌ 支付失败");
        }
    }

    /**
     * 测试: 查询订单
     */
    private static void testGetOrder(String token, String orderNo) throws Exception {
        String url = BASE_URL + "/api/v1/orders/getPaidOrder";
        
        // 构造请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("orderNo", orderNo);
        
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setDoOutput(true);
        
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        int responseCode = conn.getResponseCode();
        String response = readResponse(conn);
        
        System.out.println("   订单号: " + orderNo);
        System.out.println("   状态码: " + responseCode);
        System.out.println("   响应: " + formatJson(response));
        
        if (responseCode == 200) {
            System.out.println("   ✅ 查询订单成功");
        } else {
            System.out.println("   ❌ 查询失败");
        }
    }

    /**
     * 测试: 并发抢票
     */
    private static void testConcurrentGrab(String token) throws Exception {
        int threadCount = 50;
        System.out.println("   启动 " + threadCount + " 个并发请求...");
        
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            final int userId = 5000 + i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    
                    String url = BASE_URL + "/api/v1/orders/grab";
                    Map<String, Object> requestBody = new HashMap<>();
                    requestBody.put("userId", (long) userId);
                    requestBody.put("userName", "user" + userId);
                    requestBody.put("eventId", TEST_EVENT_ID);
                    requestBody.put("ticketId", TEST_TICKET_ID);
                    
                    String jsonBody = objectMapper.writeValueAsString(requestBody);
                    
                    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                    conn.setDoOutput(true);
                    
                    try (OutputStream os = conn.getOutputStream()) {
                        byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                    }
                    
                    int responseCode = conn.getResponseCode();
                    
                    if (responseCode == 200) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }
                    
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }
        
        long startTime = System.currentTimeMillis();
        startLatch.countDown(); // 开始
        doneLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();
        long endTime = System.currentTimeMillis();
        
        System.out.println("\n   ========================================");
        System.out.println("   📊 并发测试结果:");
        System.out.println("      总请求数: " + threadCount);
        System.out.println("      成功数: " + successCount.get());
        System.out.println("      失败数: " + failCount.get());
        System.out.println("      总耗时: " + (endTime - startTime) + "ms");
        System.out.println("      平均耗时: " + ((endTime - startTime) / threadCount) + "ms/请求");
        System.out.println("   ========================================");
    }

    // ========================================================================
    // 工具方法
    // ========================================================================

    /**
     * 生成 JWT Token
     */
    private static String generateJwtToken(Long userId, String username) {
        return Jwts.builder()
            .setSubject(username)
            .claim("userId", userId)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 24小时
            .signWith(SignatureAlgorithm.HS256, JWT_SECRET)
            .compact();
    }

    /**
     * 读取 HTTP 响应
     */
    private static String readResponse(HttpURLConnection conn) throws Exception {
        Scanner scanner;
        try {
            scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            scanner = new Scanner(conn.getErrorStream(), StandardCharsets.UTF_8.name());
        }
        
        scanner.useDelimiter("\\A");
        String response = scanner.hasNext() ? scanner.next() : "";
        scanner.close();
        
        return response;
    }

    /**
     * 格式化 JSON (简单版)
     */
    private static String formatJson(String json) {
        if (json.length() > 200) {
            return json.substring(0, 200) + "... (已截断)";
        }
        return json;
    }
}
