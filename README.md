# 简易RPC框架

## 项目结构

- **/rpc-core**：RPC框架核心部分
- **/rpc-admin**：管理平台
- **/rpc-samples/***：RPC案例
- **/rpc-samples/server**：案例服务端
- **/rpc-samples/client**：案例客户端

**旧项目地址**：https://github.com/StellarisJAY/SimpleRPC



## 框架特点
- [x] Spring整合
- [x] 多种服务注册中心支持（ZooKeeper、Redis）
- [x] 用户自定义过滤器
- [x] 服务端限流器
- [x] 报文压缩
- [ ] TCP连接池
- [ ] 服务集群注册与负载均衡
- [ ] 管理控制页面

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
# 服务名称（必要）
spring.application.name=rpcService
```

#### 添加@EnableRpc注解和@EnbaleXXXRegistry注解

- @EnableRpc注解中通过basePackage指定服务提供类的路径。
- 使用@EnableXXXRegistry来指定注册中心类型，比如ZooKeeper、Redis

```java
@EnableRpc(basePackage="com.jay.sample")
@EnableZooKeeperRegistry
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

同服务端，添加Maven、添加配置、添加**@EnableRpc**和**@EnableXXXRegistry**注解。

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



### 使用ZooKeeper作为注册中心

1. 启动类使用**@EnableZooKeeperRegistry**注解
2. 在配置文件添加以下内容：

```properties
# zookeeper地址
rpc.service.registry.zk.hosts=192.168.154.128:2181
# session超时时长
rpc.service.registry.zk.session-timeout=4000
```



### 使用 Redis 作为注册中心

1. 启动类使用**@EnableRedisRegistry**注解
2. 在配置文件添加以下内容：

```properties
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

### RPC流程

![RPC-network](https://images-1257369645.cos.ap-chengdu.myqcloud.com/notes/RPC-network.png)

1. 客户端通过动态代理，创建接口的RPC代理对象。
2. 代理对象从服务注册中心获取服务地址
3. 通过Netty发送RPC请求，RpcEncoder将请求序列化并封装出RPC报文
4. 服务端接收到后，通过RpcDecoder反序列化，然后交给上层处理器
5. 经过限流器、权限过滤器、用户自定义过滤器过滤。
6. 到达RpcRequestHandler，通过反射找到目标方法并调用。
7. 生成RpcResponse，通过RpcEncoder序列化后发送给客户端。
8. 客户端收到，代理对象根据RpcResponse输出结果。

### 协议格式

![RPC-message](https://images-1257369645.cos.ap-chengdu.myqcloud.com/notes/RPC-message.png)

- 头部大小共16字节。
- **魔数**（magic number）：4字节，用于识别报文。
- **版本**（version）：1字节，用于检查版本。
- **总长度**（length）：4字节，表示整个报文的长度，即头部16字节 + 数据部分长度
- **类型**（type）：1字节，区分报文类型，请求/返回/心跳。
- **序列化方式**（serializer）：1字节，数据部分序列化方式，比如Protostuff、Kyro。
- **压缩方式**（compress）：1字节，数据部分的压缩方式，比如GZIP。如果为0，表示数据部分未压缩。
- **请求ID**（request ID）：4字节，用于区分报文，有IDProvider提供的自增数值。
- **数据部分**（Data）：实际的请求或返回，序列化并压缩后的数据。

### TCP粘包和拆包的解决

TCP粘包和拆包都是由TCP协议的Nagles算法导致的，该算法要求TCP每次在缓存装满后发送整个缓存的数据，这就会导致多个应用层的小数据包粘连，或者大数据包被拆分。

Netty提供了叫LengthFieldBasedFrameDecoder来解决粘包拆包。通过在报文中添加长度字段，以及该Decoder的几个重要参数，就能够区分出粘连或拆分的报文。

```java
public class RpcDecoder extends LengthFieldBasedFrameDecoder {

    public RpcDecoder() {
        // 注意这里的参数值，都是根据各参数的意义和协议头部格式计算出来的
        this(RpcConstants.MAX_MESSAGE_LENGTH, 5, 4, -9, 0);
    }
	
    public RpcDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);

    }
```

需要配置的参数有以下几个：

- **maxFrameLength**：数据帧最大长度，即一个应用层数据包最大的长度。
- **lengthFieldOffset**：长度字段偏移，即长度字段在ByteBuf中的起始位置，比如当前协议的起始位置是 5。暂时称该位置为A。
- **lengthFieldLength**：长度字段长度，即长度字段有多少个字节，比如当前协议是 4字节。暂时称它表示的数值为 d，位置B = A + 字段长度。
- **lengthAdjustment**：用来调整传递到下一层处理器的末尾指针位置。该位置为，D = （B + d - lengthAdjustment）。在当前协议下，B+d明显超出了可读范围，超出的值是魔数+版本+长度。
- **initialBytesToStrip**：用来调整传到下一层处理器的起始指针。比如下一层想跳过魔数，那么该参数就是 4。

### 过滤器原理

过滤器是对Netty的**ChannelInboundHandler**的封装。服务器会发现用户实现的Filter类，并将它们放在系统过滤器和请求处理器之间。

过滤器的封装使用了**模板模式**(Template Pattern)，在Netty的channelRead方法中实现了过滤规则的骨架，用户通过实现骨架中的doFilter方法来完成一个过滤器。

```java
public abstract class Filter extends SimpleChannelInboundHandler<RpcRequest>{
    @Override
    public void channelRead0(ChannelHandlerContext context, RpcRequest request){
        // 写死的过滤规则，会调用doFilter
    }
    
    // 由子类提供
    public boolean doFilter(ChannelHandlerContext context, RpcRequest request) throws FilterException;
}
```

### 多种注册中心的支持

Simple-RPC目前支持ZooKeeper和Redis两种注册中心。它们都是继承了**Registry**这个抽象类，Registry中有服务注册的各种方法。

只需要根据不同注册中心的逻辑实现各个方法即可添加一种注册中心的支持。

```java
public abstract class Registry {

    protected ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1, r -> new Thread(r, "heart-beat"));
    protected int heartBeatTime = 30;
    
    public abstract String getServiceAddress(String serviceName);

    
    public abstract List<ApplicationInfo> discoverService();

    
    public abstract void registerService(String applicationName, String address) throws Exception;

    
    public abstract void heartBeat(String applicationName, String address);

    /**
     * 开启心跳
     * 模板方法，实现heartBeat()来开启心跳检测
     * 通过定时任务线程池来完成心跳
     * @param applicationName 服务名
     * @param address 地址
     */
    public void startHearBeat(String applicationName, String address){
        // 延迟一个周期开启
        executor.scheduleAtFixedRate(()->{heartBeat(applicationName, address);}, heartBeatTime, heartBeatTime, TimeUnit.SECONDS);
    }
}

```

### ZooKeeper作为注册中心

ZooKeeper的Znode有一种session级别的生命周期，当创建它的连接断开，该node就会销毁。

这样的性质就可以用来实现服务注册的功能，即服务一旦挂掉，服务注册中心就不会再有该服务的信息存在。

### Redis作为注册中心

Redis没有ZooKeeper那样的session生命周期的key，但是Redis的key能够设置过期时间。

我们可以在注册时为key设置一个过期时间，然后每次心跳的时候重置这个时间（心跳续约）。这样一旦服务挂掉，那么注册中心也可以在一个心跳周期内发现。

关于心跳周期，周期时长的选择很关键：

- 如果周期太长，注册中心将长时间得不到服务器的状态。
- 如果周期太短，频繁的心跳又会影响服务器性能。

因为不同服务器和业务场景的对心跳周期要求不同，所以框架应该将选择权交给用户。

### 限流算法

常见的限流算法有**漏桶算法**、**令牌桶算法**、**滑动窗口**等。漏桶和令牌桶思想有所不同。

- 漏桶是将请求缓存下来，然后以一个稳定的速率放行。
- 令牌桶不会缓存请求，桶中缓存通行令牌，并以一个稳定的速率补充令牌。如果一个请求得不到令牌就不会被放行。

目前该项目中使用的是令牌桶算法，该算法的一种常见的实现思路是，用一个单独的线程来向令牌桶稳定的提供令牌。不过对于框架而言，应该尽量减少自身的线程开销。为了减少线程开销，该项目使用了一种类似**懒汉**的方式。

1. 在每次获取令牌前，先补充令牌。
2. 补充令牌时计算与上次补充的时间间隔，该时间乘以速率就是要补充的令牌数量。
3. 当桶装满后不再补充。

