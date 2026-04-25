# [玉米表型采析系统后端](./CornPhenoUltra)

> 玉米表型数据采集与分析系统的后端服务，支持微信小程序登录、采集记录管理、算法服务调用、分析报告生成等功能

## ✨ 模块与主要功能

| 模块 | 功能 |
|------|------|
| 用户认证 | 微信小程序登录 / JWT 无状态认证 |
| 采集记录 | 上传、查询、修改、删除 |
| 文件管理 | 图片/视频上传、存储、下载 |
| 算法调用 | RabbitMQ 异步调用算法分析服务 |
| 报告生成 | 异步生成 PDF 分析报告 |
| 数据统计 | 多维度聚合统计（地块/品种/时间） |

## 🛠️ 技术栈选型

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 21 | 后端主语言 |
| Spring Boot | 3.x | 应用框架 |
| Spring Security | 6.x | 认证授权 |
| MySQL | 8.0 | 数据存储 |
| MyBatis-Plus | 3.5.7 | ORM 框架 |
| RabbitMQ | 3.x | 消息队列 |
| Docker | 24.x | 容器化部署 |
| Nginx | 1.24 | 反向代理 / HTTPS |

## 📁 项目结构
```bash
src/main/java/com/qian/
├── config/ # 配置类
│ ├── MybatisPlusConfig # MybatisPlus配置
│ ├── RabbitMQConfig # 消RabbitMQ配置
│ ├── RestTemplateConfig # RestTemplate配置
│ ├── SecurityConfig # Spring Security 配置
│ ├── VirtualThreadConfig # 虚拟线程池配置
│ ├── WebConfig # 拦截器配置
│ └── WebMvcConfiguration # API文档、跨域、限定访问域名等配置
├── controller/ # 控制器层
│ ├── AdminController # 管理端
│ ├── AnalysisController # 分析报告生成
│ ├── AuthController # 微信登录，登出
│ ├── CaptureRecordController # 采集记录 CRUD
│ ├── MediaFileController # 文件上传下载
│ └── UserController # 用户管理
├── dto/ # 数据传输对象
│ ├── request
│ ├── response
│ ├── AnalysisRequestMessage.java
│ └── AnalysisTaskRequest.java
├── exception/ # 自定义异常类
│ ├── AuthenticationFailureException.java # 认证异常
│ ├── BusinessException.java # 业务异常
│ ├── GlobalExceptionHandler.java # 全局异常处理类
│ ├── InvalidOperationException.java # 非法操作异常
│ └── ResourceNotFoundException.java # 资源不存在异常
├── service/ # 业务逻辑层
│ ├── impl/ # 实现类
├── mapper/ # MyBatis-Plus Mapper
├── pojo/ # 实体类
│ ├── AnalysisReport.java # 分析报告实体类
│ ├── CaptureRecord.java # 采集记录实体类
│ ├── MediaFile.java # 媒体文件实体类
│ └── User.java # 用户实体类
├── utils/ # 工具类
│ ├── CurrentHolder # ThreadLocal 工具类
│ ├── FFmpegUtil # FFmpeg 调用工具类
│ ├── JwtUtils # JWT 工具类
│ ├── OSSUtil # 阿里云 OSS 工具类
│ └── Util # 其他
```

## 🚀 快速开始

### 环境要求

- JDK 21+
- MySQL 8.0+
- Maven 3.8+
- RabbitMQ 3.x

### 配置

#### 1. 克隆项目

```bash
git clone https://github.com/atom-gradle/CornPheno.git
cd CornPheno
```

#### 2.修改配置
```bash
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/db_cornpheno?useSSL=false&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
  
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest

# JWT 配置
jwt:
  secret: your-jwt-secret-key
  expiration: 86400000  # 24小时

# 微信小程序配置
wx:
  app-id: your-app-id
  app-secret: your-app-secret
```

#### 3.运行

```bash
# 配置数据库
musqldump -u your_username -p db_cornpheno_backup.sql

# 开发环境
mvn spring-boot:run

# 生产环境
mvn clean package
java -jar target/CornPheno.jar

# 构建镜像
docker build -t CornPheno .

# 运行容器
docker run -d \
  --name CornPheno \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  CornPheno
```

## 核心实现
### 1.Interceptor + JWT 实现无状态认证

```java
@Override
public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    String requestUri = request.getRequestURI();
    log.info("请求路径为:{}", requestUri);

    for (String excludePath : EXCLUDE_PATHS) {
        if (matchPath(excludePath, requestUri)) {
            log.info("路径 {} 匹配排除模式 {}, 放行", requestUri, excludePath);
            return true;
        }
    }

    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
        log.info("收到 OPTIONS 预检请求，直接放行");
        return true;
    }

    // 获取Authorization头
    String authHeader = request.getHeader("Authorization");
    log.info("Authorization header: {}", authHeader);

    // 判断令牌是否存在
    if (!StringUtils.hasLength(authHeader) || !authHeader.startsWith("Bearer ")) {
        log.info("获取到Jwt令牌为空或格式不正确, 返回错误结果");

        response.setStatus(HttpStatus.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        // 返回JSON格式的错误信息
        String errorJson = "{\"code\":401,\"message\":\"请先登录\"}";
        response.getWriter().write(errorJson);
        return false;
    }

    // 提取token（去掉"Bearer "前缀）
    String jwt = authHeader.substring(7).trim();
    log.info("提取的token: {}", jwt);

    // 解析token
    try {
        Claims claims = JwtUtils.parseJWT(jwt);
        Integer userIdInt = (Integer)claims.get("userId");
        Long userId = userIdInt != null ? userIdInt.longValue() : null;

        // 1.检查userId
        if (userId == null) {
            log.error("token中未找到userId");
            response.setStatus(HttpStatus.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"无效的token\"}");
            return false;
        }

        // 2.放行需要令牌，但不需要autid_status == 2的路径
        log.info("放行需要令牌，但不需要autid_status == 2的路径");
        if(EXCLUDE_PATHS2.contains(requestUri)) {
            User user_ = userMapper.selectById(userId);
            if(user_ == null) {
                log.info("用户不存在，不放行");
                return false;
            }
            CurrentHolder.setCurrentId(userId);
            log.info("认证相关接口无需audit_status == 2，放行, userId: {}, 放行", userId);
            return true;
        }

        // 3.进一步检查audit_status，未审核通过则拒绝
        log.info("进一步检查audit_status == 2，才放行，userId为：{}", userId);
        User user = userMapper.selectById(userId);
        if(user == null || user.getAuditStatus() != 2) {
            log.info("用户不存在或 用户状态 != 审核通过，不予放行！");
            return false;
        }

        // 4.保存到ThreadLocal
        CurrentHolder.setCurrentId(userId);
        log.info("token解析成功, userId: {}, 放行", userId);
        return true;

    } catch (Exception e) {
        log.error("解析令牌失败: {}", e.getMessage());
        response.setStatus(HttpStatus.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"token无效或已过期\"}");
        return false;
    }
}

@Override
public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    CurrentHolder.remove();
    log.info("请求结束，已清空当前线程绑定的用户ID");
}
```

### 2.采用RabbitMQ，异步调用算法服务，释放请求线程

```java
// AnalysisController.java
@Operation(
        summary = "提交分析请求",
        description = "为指定的captureId提交采集记录进行AI分析"
)
@GetMapping("/{captureId}/submit")
public Result<?> submitAnalysis(@PathVariable String captureId) {
    log.info("收到分析请求: captureId={}", captureId);

    // 35s超时
    DeferredResult<Object> deferredResult = new DeferredResult<>(35000L);

    var future = analysisReportService.submitAnalysis(captureId);

    future.whenComplete((result, throwable) -> {
        if(throwable != null) {
            log.error("分析任务失败");
            deferredResult.setErrorResult(throwable.getMessage());
        } else {
            log.info("分析任务成功");
            deferredResult.setResult(result);
        }
    });

    return Result.success(deferredResult);
}
```


```java
// AnalysisResultConsumerService.java
/**
 * 监听结果队列，接收Python算法服务返回的结果
 */
@RabbitListener(queues = RabbitMQConfig.RESULT_QUEUE, containerFactory = "rabbitListenerContainerFactory")
public void consumeResult(AnalysisResultMessage result, Message message, Channel channel) {
    try {
        log.info("收到计算结果: taskId={}, 结果={}, 耗时={}ms",
                result.getTaskId(),
                result.getResultUrl(),
                result.getElapsedTime());

        analysisServiceImpl.completeTask(result.getTaskId(), result);

        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

    } catch (Exception e) {
        log.error("处理结果失败: {}", e.getMessage(), e);
        try {
            // 拒收消息，不重新入队
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
        } catch (Exception ex) {
            log.error("确认消息失败", ex);
        }
    }
}

/**
 * 处理死信队列的消息
 */
@RabbitListener(queues = RabbitMQConfig.DEAD_LETTER_QUEUE)
public void handleDeadLetter(Message message, Channel channel,
                             @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
    try {
        String body = new String(message.getBody());
        log.error("收到死信消息: {}", body);

        try {
            AnalysisTaskRequest task = objectMapper.readValue(body, AnalysisTaskRequest.class);
            String captureId = task.getCaptureId();

            // 更新数据库状态为失败
            AnalysisReport report = analysisServiceImpl.getOne(
                    new LambdaQueryWrapper<AnalysisReport>()
                            .eq(AnalysisReport::getCaptureId, captureId)
            );

            if (report != null) {
                report.setStatus(3);// 失败状态
                report.setErrorMessage("任务处理失败，已进入死信队列");
                report.setUpdateTime(LocalDateTime.now());
                analysisServiceImpl.updateById(report);
            }

            analysisServiceImpl.completeTaskExceptionally(captureId, "任务处理失败");

        } catch (Exception e) {
            log.error("解析死信消息失败", e);
        }

        channel.basicAck(deliveryTag, false);

    } catch (Exception e) {
        log.error("处理死信消息失败", e);
        try {
            // 如果处理死信也失败，记录日志后确认，避免无限循环
            channel.basicAck(deliveryTag, false);
        } catch (IOException ex) {
            log.error("确认死信消息失败", ex);
        }
    }
}
```


## 性能优化案例
### 业务需求：查询某个地块在时间段内的所有采集记录，按品种统计
```SQL
 SELECT
 cr.block_id,
 cr.variety_name,
 cr.sample_type,
 COUNT(*) as sample_count,
 MIN(cr.create_time) as first_collect_time,
 MAX(cr.update_time) as last_update_time
 FROM capture_record cr
 LEFT JOIN media_file mf ON mf.capture_id = cr.id
 WHERE cr.status between 1 and 3  -- 已完成分析的
 AND cr.block_id IN ('BLOCK_D01', 'BLOCK_B01', 'BLOCK_E01')
 AND cr.create_time BETWEEN '2026-01-01 00:00:00' AND '2026-12-31 00:00:00'
 GROUP BY cr.block_id, cr.variety_name, cr.sample_type
 ORDER BY cr.block_id, sample_count DESC;
```

### 优化过程
```SQL
explain ...;
```
`EXPLAIN` 查看语句执行情况

```bash
mysql> explain  SELECT
    ->  cr.block_id,
    ->  cr.variety_name,
    ->  cr.sample_type,
    ->  COUNT(*) as sample_count,
    ->  MIN(cr.create_time) as first_collect_time,
    ->  MAX(cr.update_time) as last_update_time
    ->  FROM capture_record cr
    ->  LEFT JOIN media_file mf ON mf.capture_id = cr.id
    ->  WHERE cr.status between 1 and 3  -- 已完成分析的
    ->  AND cr.block_id IN ('BLOCK_D01', 'BLOCK_B01', 'BLOCK_E01')
    ->  AND cr.create_time BETWEEN '2026-01-01 00:00:00' AND '2026-12-31 00:00:00'
    ->  GROUP BY cr.block_id, cr.variety_name, cr.sample_type
    ->  ORDER BY cr.block_id, sample_count DESC;
+----+-------------+-------+------------+------+----------------+----------------+---------+--------------------+--------+----------+----------------------------------------------+
| id | select_type | table | partitions | type | possible_keys  | key            | key_len | ref                | rows   | filtered | Extra                                        |
+----+-------------+-------+------------+------+----------------+----------------+---------+--------------------+--------+----------+----------------------------------------------+
|  1 | SIMPLE      | cr    | NULL       | ALL  | NULL           | NULL           | NULL    | NULL               | 489450 |     0.37 | Using where; Using temporary; Using filesort |
|  1 | SIMPLE      | mf    | NULL       | ref  | idx_capture_fk | idx_capture_fk | 8       | db_cornpheno.cr.id |      1 |   100.00 | Using index                                  |
+----+-------------+-------+------------+------+----------------+----------------+---------+--------------------+--------+----------+----------------------------------------------+
2 rows in set, 1 warning (0.01 sec)
```

```SQL
show index in capture_record;

select count(distinct block_id),count(distinct variety_name),count(distinct status),count(distinct sample_type) from capture_record;
```

```bash
mysql> show index in capture_record;
+----------------+------------+-----------------+--------------+--------------+-----------+-------------+----------+--------+------+------------+---------+------------------------------------------------------------+---------+------------+
| Table          | Non_unique | Key_name        | Seq_in_index | Column_name  | Collation | Cardinality | Sub_part | Packed | Null | Index_type | Comment | Index_comment                                              | Visible | Expression |
+----------------+------------+-----------------+--------------+--------------+-----------+-------------+----------+--------+------+------------+---------+------------------------------------------------------------+---------+------------+
| capture_record |          0 | PRIMARY         |            1 | id           | A         |      501995 |     NULL |   NULL |      | BTREE      |         |                                                            | YES     | NULL       |
| capture_record |          0 | capture_id      |            1 | capture_id   | A         |      418770 |     NULL |   NULL |      | BTREE      |         |                                                            | YES     | NULL       |
| capture_record |          1 | idx_user_id     |            1 | user_id      | A         |        2835 |     NULL |   NULL |      | BTREE      |         |                                                            | YES     | NULL       |
| capture_record |          1 | idx_cover_group |            1 | block_id     | A         |           9 |     NULL |   NULL |      | BTREE      |         | 查询某个地块在时间段 内的所有采集记录，按品种统计的复合索引 | YES     | NULL       |
| capture_record |          1 | idx_cover_group |            2 | create_time  | A         |      334730 |     NULL |   NULL | YES  | BTREE      |         | 查询某个地块在时间段 内的所有采集记录，按品种统计的复合索引 | YES     | NULL       |
| capture_record |          1 | idx_cover_group |            3 | status       | A         |      465504 |     NULL |   NULL | YES  | BTREE      |         | 查询某个地块在时间段 内的所有采集记录，按品种统计的复合索引 | YES     | NULL       |
| capture_record |          1 | idx_cover_group |            4 | variety_name | A         |      501995 |     NULL |   NULL | YES  | BTREE      |         | 查询某个地块在时间段 内的所有采集记录，按品种统计的复合索引 | YES     | NULL       |
| capture_record |          1 | idx_cover_group |            5 | sample_type  | A         |      501995 |     NULL |   NULL |      | BTREE      |         | 查询某个地块在时间段 内的所有采集记录，按品种统计的复合索引 | YES     | NULL       |
| capture_record |          1 | idx_cover_group |            6 | update_time  | A         |      501995 |     NULL |   NULL | YES  | BTREE      |         | 查询某个地块在时间段 内的所有采集记录，按品种统计的复合索引 | YES     | NULL       |
+----------------+------------+-----------------+--------------+--------------+-----------+-------------+----------+--------+------+------------+---------+------------------------------------------------------------+---------+------------+
9 rows in set (0.02 sec)
```

`SHOW INDEX` `cadinality`分析字段选择性，其中`block_id`的`cardinality`为9，`variety_name`字段的`cardinality`为501,995，不准确，原因在于`variety_name`列允许NULL值，而MySQL默认抽样20页统计，所以统计不准确
结合`count(distince field)`查看和评估选择性
```bash
mysql> select count(distinct block_id),count(distinct variety_name),count(distinct status),count(distinct sample_type) from capture_record;
+--------------------------+------------------------------+------------------------+-----------------------------+
| count(distinct block_id) | count(distinct variety_name) | count(distinct status) | count(distinct sample_type) |
+--------------------------+------------------------------+------------------------+-----------------------------+
|                       10 |                           20 |                      5 |                           5 |
+--------------------------+------------------------------+------------------------+-----------------------------+
1 row in set (0.33 sec)
```
设计复合索引，顺序为(`block_id`,`create_time`,`status`,`variety_name`,`sample_type`,`update_time`)

```bash
+----+-------------+-------+------------+-------+-----------------+-----------------+---------+--------------------+--------+----------+-----------------------------------------------------------+
| id | select_type | table | partitions | type  | possible_keys   | key             | key_len | ref                | rows   | filtered | Extra                                                     |
+----+-------------+-------+------------+-------+-----------------+-----------------+---------+--------------------+--------+----------+-----------------------------------------------------------+
|  1 | SIMPLE      | cr    | NULL       | range | idx_cover_group | idx_cover_group | 138     | NULL               | 304688 |    11.11 | Using where; Using index; Using temporary; Using filesort |
|  1 | SIMPLE      | mf    | NULL       | ref   | idx_capture_fk  | idx_capture_fk  | 8       | db_cornpheno.cr.id |      1 |   100.00 | Using index                                               |
+----+-------------+-------+------------+-------+-----------------+-----------------+---------+--------------------+--------+----------+-----------------------------------------------------------+
2 rows in set, 1 warning (0.02 sec)
```

### 优化结果
| 指标 | 优化前 | 优化后 | 提升 |
|------|------|------|------|
| 查询耗时 | 约1.15s | 稳定0.37s | 60% |
| 扫描行数 | 51万（全表） | <1000（索引范围） | 99%+ |
| 是否回表 | 是 | 否（覆盖索引）| ✅ |

测试说明：数据量 51 万条，MySQL 8.0，Buffer Pool 已预热，取稳定后耗时

## 部署

```bash
# 1. 打包
mvn clean package -DskipTests

# 2. 上传到服务器
scp target/ccnupromax-backend.jar root@your-server:/app/

# 3. 启动服务（使用 systemd 或 nohup）
nohup java -jar /app/ccnupromax-backend.jar --spring.profiles.active=prod > /app/logs/app.log 2>&1 &

# 4. Nginx 配置
cat > /etc/nginx/sites-available/api.ccnupromax.com << 'EOF'
server {
    listen 443 ssl http2;
    server_name api.ccnupromax.com;
    
    ssl_certificate /etc/nginx/ssl/cert.pem;
    ssl_certificate_key /etc/nginx/ssl/key.pem;
    
    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
EOF

ln -s /etc/nginx/sites-available/api.ccnupromax.com /etc/nginx/sites-enabled/
nginx -t && systemctl reload nginx
```

## API文档
启动后访问：http://localhost:5000/swagger-ui.html

## 许可证
MIT LISCENCE

## 联系我
pro.gradle@outlook.com

