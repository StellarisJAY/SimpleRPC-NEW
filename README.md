# 简易RPC框架

## 项目结构

- **/rpc-core**：RPC框架核心部分
- **/rpc-admin**：管理平台
- **/rpc-samples/***：RPC案例
- **/rpc-samples/server**：案例服务端
- **/rpc-samples/client**：案例客户端

**旧项目地址**：https://github.com/StellarisJAY/SimpleRPC



## 项目目标
- [x] 完成Spring整合   
- [ ] 完善协议格式
- [ ] 完善序列化过程
- [x] 整合Zookeeper作为服务注册中心 
- [x] Zookeeper做服务发现 
- [x] 服务注册中心抽象，通过配置切换注册中心  
- [x] 整合 Redis 作为服务注册中心 
- [x] 服务心跳，用于续约服务和状态检测 
- [x] 用户自定义过滤器
- [ ] 权限验证
- [x] 服务端流量控制（guava RateLimiter）
- [ ] 管理中心（服务管理、流量监控、执行日志、限流）

## 使用说明   

### 服务提供方（服务端）

#### 导入Maven依赖

```xml
<dependency>
    <groupId>com.jay</groupId>
    <artifactId>rpc</artifactId>
    <version>1.0</version>
</dependency>		
```

#### 配置文件

```properties
# RPC服务器地址
rpc.service.port=8000

# 配置注册中心类型，现支持ZooKeeper和Redis
rpc.service.registry.type=zookeeper

# Zookeeper地址
rpc.service.registry.zk.hosts=192.168.154.128:2181

# Zookeeper Session 断开超时时间
rpc.service.registtry.zk.session-timeout=5000
# 服务名称（必要）
spring.application.name=rpcService
```

#### 添加@EnableRpc注解

```java
@EnableRpc
@SpringBootApplication
public class TestApplication{
    public static void main(String[] args){
        SpringApplication.run(TestApplication.class, args);
    }
}
```

#### 声明服务实现类

在服务实现类上用@RpcService替代@Service或@Component

```java
@RpcService
public class HelloServiceImpl implements HelloService{
	...
}
```

### 服务调用方（客户端）

同服务端，添加Maven、添加配置、添加**@EnableRpc**注解。

#### 远程调用

使用**RpcProxy**类的create方法创建代理对象，第一个参数为类型，第二个参数为服务提供方的服务名称。

```java
@RestController
public class HelloController {
    // Rpc代理工具
    @Resource
    private RpcProxy rpcProxy;

    @GetMapping("/test")
    public String hello(@RequestParam("name") String name){
        // 创建代理对象
        HelloService service = rpcProxy.create(HelloService.class, "rpcService");
        return service.hello(name);
    }
}

```

调用代理对象的方法将会从**服务注册中心**找到服务提供方的地址，然后发送RPC请求获取执行结果。

### 使用 Redis 作为服务注册中心

在配置文件添加以下内容：

```properties
# 配置注册中心类型
rpc.service.registry.type=redis
# redis 地址
rpc.service.registry.redis.host=localhost
rpc.service.registry.redis.port=6379
# 密码
rpc.service.registry.redis.password=
rpc.service.registry.redis.max-wait-millis=4000
```

### 用户自定义过滤器

1. 创建过滤器类，继承自**Filter**。
2. 实现**doFilter**方法，通过返回值boolean或抛出异常来控制过滤。
3. 使用**@Component**将过滤器配置为Spring容器管理。

```java
@Component
public class MyFilter extends Filter {

    private Logger logger = LoggerFactory.getLogger(filterName);

    public MyFilter() {
        super("my-filter");
    }

    @Override
    public boolean doFilter(ChannelHandlerContext context, RpcRequest request) throws FilteredException {
        logger.info("执行过滤器：{}", filterName);
        return true;
    }
}
```

### 限流器配置

修改配置文件开启限流器

```properties
# 开启限流
rpc.traffic.enable-control=true
# 每秒允许的请求数量
rpc.traffic.permits-per-second=100
```



## 原理简介

![RPC-network](C:\Users\76040\Desktop\RPC-network.png)

