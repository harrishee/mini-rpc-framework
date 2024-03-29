# Mini RPC 框架

简体中文 | [English](README.md)

## 1. 项目介绍

Mini RPC 框架是一个基于 Java 的轻量级 RPC 框架，旨在屏蔽底层网络通信细节来简化远程过程调用，让开发者能够更加专注于业务逻辑的实现。
该框架支持 Socket 和 Netty 两种网络通信方式，提供 JSON 和 Kryo 两种序列化，使用 Nacos 作为服务注册中心，实现了简单的轮询负载均衡机制，采用自定义的简单传输协议。

## 2. 关于 RPC

RPC（Remote Procedure Call）远程过程调用，是一种通过网络从远程计算机程序上请求服务，而不需要了解底层网络技术的过程。RPC
抽象了网络通信的细节，使得开发者可以像调用本地方法一样调用远程方法。

工作过程可以分为以下几个步骤：

1. **客户端调用过程**：客户端调用一个本地的 stub 函数，这个过程与调用普通函数无异。
2. **请求封装**：stub 接收到调用后，会将方法名、参数等信息封装成一个请求消息。
3. **网络传输**：通过网络将这个请求消息发送到服务端。
4. **服务端解析请求**：服务端监听器收到请求后，解析请求消息，找到请求指定的方法和参数等。
5. **执行远程过程**：服务端找到请求指定的方法后，通过反射调用该方法，并将结果返回。
6. **返回结果给客户端**：服务端通过网络将响应消息发送回客户端的 stub，stub 解析出结果，返回给客户端调用者。

## 3. 使用流程

推荐使用 IDE 打开本项目，以便测试自定义的服务接口和实现。

### 1: 安装 Nacos

确保设备已经安装 Nacos。如还未安装，可通过以下 Docker 命令快速启动 Nacos：

```bash
docker run --name nacos-rpc-dev -e MODE=standalone -p 8848:8848 nacos/nacos-server:v2.2.0
```

### 2: 验证 Nacos

访问 [http://localhost:8848/nacos](http://localhost:8848/nacos) 确认 Nacos 已经启动并且可以访问。

### 3: 添加服务接口

在 `rpc-api` 模块中添加你需要对外暴露的服务接口。

### 4: 实现服务接口

在 `start-server` 模块下的 `impl` 包中完成这些接口的实现。

### 5: 启动服务端

启动 `RpcServerLauncher`，可修改默认的 `serverType`、`host` 和 `port`。

### 6: 客户端调用服务

在 `start-client` 模块中，通过 `RpcClientLauncher` 进行服务调用。

## 4. 模块概览

- **rpc-api**：定义所有服务接口，服务提供者和消费者共享的部分
- **rpc-common**：框架的通用组件，如异常、枚举、传输格式 `RpcRequest` `RpcResponse`
- **rpc-core**：核心实现，包括服务注册、发现、序列化和网络传输等功能
- **start-server**：服务端启动入口，负责启动 RPC 服务端，注册服务
- **start-client**：客户端启动入口，负责发起 RPC 调用，消费服务

## 5. 自定义传输格式

### RpcRequest

`RpcRequest` 类是 RPC 调用过程中客户端向服务端发送请求的数据格式：

- **requestId**：唯一标识一个请求，用于异步处理和响应匹配。
- **interfaceName**、**methodName**、**parameters**、**paramTypes**：这四个字段共同确定了要调用的方法。`interfaceName` 指明了请求的接口名，`methodName` 指定了接口内的方法名，`parameters` 和 `paramTypes` 分别表示方法的参数值和参数类型。这套组合确保了方法的唯一定位和正确调用，防止方法重载时的冲突。
- **heartBeat**：指示该请求是否为心跳检测消息，用于维持连接的活性，防止长时间空闲导致的连接断开。

### RpcResponse

`RpcResponse<T>` 类是服务端处理完请求后，返回给客户端的数据格式：

- **requestId**：与 `RpcRequest` 中的 `requestId` 对应，用于客户端匹配请求与响应。
- **statusCode**：响应状态码，用于表示处理的结果（成功、失败或其他状态）。
- **message**：响应消息，通常在调用失败时包含错误信息。
- **data**：泛型参数，表示响应的具体数据，即方法调用的返回值。

### 自定义协议格式

在 Mini RPC 框架中，自定义了一套协议格式来规范数据的传输：

- **魔数**：4字节，固定的值 `0xCAFEBABE`，用于在数据传输开始时标识一个有效的 RPC 协议包。
- **包类型**：4字节，传输数据的类型，区分是请求数据（`RpcRequest`）还是响应数据（`RpcResponse`）。
- **序列化器类型**：4字节，使用的序列化器代码，确定如何对请求或响应数据进行序列化和反序列化。
- **数据长度**：4字节，后续数据部分的长度，用于读取和解析具体的数据内容。
- **数据**：根据数据长度读取的具体数据内容，是序列化后的 `RpcRequest` 或 `RpcResponse` 对象。

## 6. TODO

- [ ] 文档完善
- [ ] 注释和日志英文化
- [ ] 一致性哈希负载均衡
- [ ] 修复 Docker 部署问题
