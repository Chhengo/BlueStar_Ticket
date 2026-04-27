# BlueStar_Ticket 测试套件使用指南

## 📦 文件清单

```
测试相关文件:
├── BlueStarTicketCompleteTest.java    # 完整测试代码 (单元+集成+并发+安全+性能)
├── application-test.yml                # 测试环境配置
├── test-dependencies.xml               # Maven 依赖配置
├── PRODUCTION_TESTING_GUIDE.md         # 生产测试理论指南
└── README_TEST.md                      # 本文件 - 使用说明
```

---

## 🚀 快速开始

### 1️⃣ 安装依赖

将 `test-dependencies.xml` 中的依赖复制到你的 `pom.xml` 文件中：

```bash
# 1. 打开项目的 pom.xml
# 2. 找到 <dependencies> 标签
# 3. 将 test-dependencies.xml 的内容粘贴进去
# 4. 更新 Maven 依赖

mvn clean install
```

### 2️⃣ 配置测试环境

#### 选项A: 使用内存数据库 (推荐，快速启动)

```yaml
# application-test.yml 已配置好 H2 内存数据库
# 无需额外操作，直接运行即可
```

#### 选项B: 使用真实 MySQL + Redis

```bash
# 1. 启动 MySQL
docker run -d -p 3306:3306 -e MYSQL_ROOT_PASSWORD=123456 -e MYSQL_DATABASE=ticket_test mysql:8.0

# 2. 启动 Redis
docker run -d -p 6379:6379 redis:7 redis-server --requirepass 123456

# 3. 修改 application-test.yml
# 取消注释 MySQL 配置，注释掉 H2 配置
```

### 3️⃣ 放置文件

```bash
# 1. 将测试类放到测试目录
cp BlueStarTicketCompleteTest.java src/test/java/com/ticket/test/

# 2. 将测试配置放到资源目录
cp application-test.yml src/test/resources/

# 3. 确保目录结构正确
src/
├── main/
│   └── java/com/ticket/...
└── test/
    ├── java/com/ticket/test/
    │   └── BlueStarTicketCompleteTest.java
    └── resources/
        └── application-test.yml
```

### 4️⃣ 运行测试

```bash
# 运行所有测试
mvn test

# 运行指定测试类
mvn test -Dtest=BlueStarTicketCompleteTest

# 运行指定测试方法
mvn test -Dtest=BlueStarTicketCompleteTest#shouldGrabTicketSuccessfully

# 生成测试报告
mvn test surefire-report:report

# 查看覆盖率报告
mvn test jacoco:report
# 打开: target/site/jacoco/index.html
```

---

## 📊 测试套件包含内容

### ✅ 单元测试 (3个测试)

| 测试名称 | 测试内容 | 预期结果 |
|---------|---------|---------|
| shouldThrowException_WhenOrderNotFound | 支付不存在的订单 | 抛出 IllegalArgumentException |
| shouldDeductStockAtomically | Redis 原子扣库存 | 库存正确扣减 |
| shouldCalculateExpireTimeCorrectly | 订单过期时间计算 | 15分钟后过期 |

### ✅ 集成测试 (5个测试)

| 测试名称 | 测试内容 | 预期结果 |
|---------|---------|---------|
| shouldGrabTicketSuccessfully | 成功抢票 | 返回 200 + orderNo |
| shouldRejectUnauthorizedGrabRequest | 未授权抢票 | 返回 401/403 |
| shouldPayOrderSuccessfully | 成功支付 | 返回 200 + 支付成功 |
| shouldGetEventsList | 查询活动列表 | 返回分页数据 |
| shouldGetTicketTypeDetails | 查询票档详情 | 返回票档列表 |

### ✅ 并发测试 (2个测试)

| 测试名称 | 测试内容 | 预期结果 |
|---------|---------|---------|
| shouldNotOversellTickets_WhenConcurrentGrab | 100并发抢10张票 | 成功10单，无超卖 |
| shouldPreventDuplicateGrab | 重复抢票幂等性 | 5次请求只成功1次 |

### ✅ 安全测试 (4个测试)

| 测试名称 | 测试内容 | 预期结果 |
|---------|---------|---------|
| shouldPreventSQLInjection | SQL注入防护 | 注入被拦截 |
| shouldRejectExpiredToken | JWT过期验证 | 拒绝过期Token |
| shouldRejectTamperedToken | JWT篡改验证 | 拒绝篡改Token |
| shouldSanitizeUserInput | XSS防护 | 特殊字符转义 |

### ✅ 性能测试 (2个测试)

| 测试名称 | 测试内容 | 预期结果 |
|---------|---------|---------|
| shouldMeetPerformanceBenchmark | 接口性能基准 | 平均 <200ms, P95 <500ms |
| shouldTestRedisPerformance | Redis操作性能 | 1000次操作 <5秒 |

---

## 🔧 常见问题排查

### ❌ 问题1: Redis 连接失败

```
错误信息: Could not get a resource from the pool

解决方案:
1. 检查 Redis 是否运行: redis-cli ping
2. 检查端口是否正确: 6379
3. 检查密码是否正确: application-test.yml 中的 password
4. 或者在 application-test.yml 中设置:
   test.enable-real-redis: false  # 使用嵌入式Redis
```

### ❌ 问题2: MySQL 连接失败

```
错误信息: Communications link failure

解决方案:
1. 使用 H2 内存数据库 (推荐):
   # application-test.yml 中已配置
   spring.datasource.driver-class-name: org.h2.Driver
   
2. 或者启动真实 MySQL:
   docker run -d -p 3306:3306 -e MYSQL_ROOT_PASSWORD=123456 mysql:8.0
```

### ❌ 问题3: JWT 测试失败

```
错误信息: 401 Unauthorized

解决方案:
1. 检查 JWT Secret 是否一致:
   application-test.yml 中的 jwt.secret
   
2. 确保测试类能生成有效 Token:
   generateTestToken() 方法使用的密钥要与配置文件一致
```

### ❌ 问题4: 并发测试不稳定

```
问题: 并发测试时而通过时而失败

解决方案:
1. 确保 Redis 正常运行
2. 增加超时时间:
   doneLatch.await(30, TimeUnit.SECONDS);  // 从10秒改为30秒
   
3. 检查系统资源:
   top -o cpu  # Mac/Linux
   taskmgr     # Windows
```

### ❌ 问题5: 依赖冲突

```
错误信息: NoSuchMethodError 或 ClassNotFoundException

解决方案:
1. 清理并重新安装:
   mvn clean install -U
   
2. 检查依赖版本冲突:
   mvn dependency:tree
   
3. 确保 Spring Boot 版本一致:
   <parent>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-parent</artifactId>
       <version>3.2.5</version>
   </parent>
```

---

## 📈 查看测试报告

### 1. Surefire 测试报告

```bash
# 生成报告
mvn surefire-report:report

# 查看报告
open target/site/surefire-report.html  # Mac
xdg-open target/site/surefire-report.html  # Linux
start target/site/surefire-report.html  # Windows
```

### 2. JaCoCo 代码覆盖率报告

```bash
# 生成覆盖率报告
mvn test jacoco:report

# 查看报告
open target/site/jacoco/index.html
```

报告内容:
- **绿色**: 覆盖率高 (>80%)
- **黄色**: 覆盖率中等 (50-80%)
- **红色**: 覆盖率低 (<50%)

---

## 🎯 测试最佳实践

### 1. 测试命名规范

```java
// ✅ 好的命名
@Test
void shouldReturnOrderNo_WhenGrabSuccessfully() { }

@Test
void shouldThrowException_WhenStockInsufficient() { }

// ❌ 不好的命名
@Test
void test1() { }

@Test
void grabTest() { }
```

### 2. Given-When-Then 模式

```java
@Test
void shouldCancelOrder_WhenPaymentTimeout() {
    // Given: 准备测试数据
    Order order = createTestOrder();
    
    // When: 执行被测方法
    orderService.cancel(order.getOrderNo());
    
    // Then: 验证结果
    assertThat(order.getStatus()).isEqualTo(2);
}
```

### 3. 测试隔离

```java
// ✅ 每个测试独立，不依赖其他测试
@Test
void test1() {
    // 自己准备数据
    String data = prepareTestData();
    // 自己清理数据
    cleanup(data);
}

// ❌ 测试间相互依赖
@Test
void test1() {
    testData = "some data";  // 设置全局变量
}

@Test
void test2() {
    useData(testData);  // 依赖 test1
}
```

---

## 🔄 CI/CD 集成

### GitHub Actions 配置示例

创建 `.github/workflows/test.yml`:

```yaml
name: Test Suite

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_ROOT_PASSWORD: 123456
          MYSQL_DATABASE: ticket_test
        ports:
          - 3306:3306
      
      redis:
        image: redis:7
        ports:
          - 6379:6379
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Cache Maven packages
        uses: actions/cache@v3
        with:
          path: ~/.m2
          key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
      
      - name: Run Tests
        run: mvn test
      
      - name: Generate Coverage Report
        run: mvn jacoco:report
      
      - name: Upload Coverage to Codecov
        uses: codecov/codecov-action@v3
        with:
          file: ./target/site/jacoco/jacoco.xml
```

---

## 📚 扩展阅读

### 测试理论
- [PRODUCTION_TESTING_GUIDE.md](./PRODUCTION_TESTING_GUIDE.md) - 生产测试完全指南
- [测试金字塔](https://martinfowler.com/articles/practical-test-pyramid.html)
- [TDD 测试驱动开发](https://martinfowler.com/bliki/TestDrivenDevelopment.html)

### 工具文档
- [JUnit 5 官方文档](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito 官方文档](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [AssertJ 官方文档](https://assertj.github.io/doc/)
- [Spring Boot Test](https://spring.io/guides/gs/testing-web)

---

## ✅ 测试检查清单

运行测试前确认:

- [ ] 所有依赖已安装 (`mvn clean install`)
- [ ] 测试配置文件已放置 (`src/test/resources/application-test.yml`)
- [ ] MySQL/Redis 已启动 (如使用真实服务)
- [ ] 测试类已放置 (`src/test/java/com/ticket/test/`)
- [ ] JWT Secret 已配置
- [ ] 端口无冲突 (3306, 6379, 8080)

---

## 🎉 总结

这套测试代码提供了:

1. **完整覆盖**: 单元→集成→并发→安全→性能
2. **即插即用**: 复制粘贴即可运行
3. **详细日志**: 每个步骤都有输出说明
4. **生产级别**: 遵循最佳实践

**运行一次完整测试:**

```bash
mvn clean test
```

**预期输出:**

```
========================================
🚀 BlueStar_Ticket 测试套件启动
========================================
✅ JWT Token 已生成
✅ Redis 测试数据已初始化

▶ 开始测试: 单元测试 - 支付服务应抛出异常当订单不存在
✅ 正确抛出 IllegalArgumentException
✓ 完成测试

▶ 开始测试: 并发测试 - 100并发抢10张票不超卖
🏁 开始并发抢票...
========================================
📊 并发测试结果:
  总请求数: 100
  成功数: 10
  失败数: 90
  最终库存: 0
  耗时: 1234ms
========================================
✅ 并发测试通过：无超卖

========================================
🏁 测试套件执行完毕，清理测试数据
========================================

Tests run: 16, Failures: 0, Errors: 0, Skipped: 0
```

---

**祝测试顺利！如有问题，请检查上面的常见问题排查部分。** 🚀
