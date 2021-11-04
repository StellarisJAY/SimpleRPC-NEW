# 简易RPC框架

## 项目结构

- **/rpc-core**：RPC框架核心部分
- **/rpc-samples/***：RPC案例
- **/rpc-samples/server**：案例服务端
- **/rpc-samples/client**：案例客户端

**旧项目地址**：https://github.com/StellarisJAY/SimpleRPC



## 目标
- [x] 完成Spring整合   
- [ ] 完善协议格式
- [ ] 完善序列化过程
- [x] 整合Zookeeper作为服务注册中心 
- [x] Zookeeper做服务发现 
- [x] 服务注册中心抽象，通过配置切换注册中心  
- [x] 整合 Redis 作为服务注册中心 
- [x] 服务心跳，用于续约服务和状态检测 
- [ ] 服务管理中心页面
- [ ] 服务心跳包
- [ ] 服务熔断

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

#### 启动Zookeeper并在配置文件中添加

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

同服务端，添加Maven、添加配置、添加@EnableRpc注解。

### 获取代理对象

使用RpcProxy类的create方法创建代理对象，第一个参数为类型，第二个参数为服务提供方的服务名称。

```java
@RestController
public class HelloController {
    // 注入 Rpc代理工具
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

调用代理对象的方法将会从Zookeeper找到服务提供方的地址，然后发送RPC请求获取执行结果。

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



## 原理简介

