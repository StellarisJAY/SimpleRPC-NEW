# 简易RPC框架

## 项目结构

- **/rpc-core**：RPC框架核心部分
- **/rpc-samples/***：RPC案例
- **/rpc-samples/server**：案例服务端
- **/rpc-samples/client**：案例客户端

**旧项目地址**：https://github.com/StellarisJAY/SimpleRPC

## 目标
1、完成Spring整合   √

2、完善协议格式

3、完善序列化过程

4.1、整合Zookeeper作为服务注册中心 √

4.2、Zookeeper做服务发现 √

4.3、服务注册中心抽象，通过配置切换注册中心 

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

# 配置注册中心类型，现在仅支持ZooKeeper
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



## 原理简介

