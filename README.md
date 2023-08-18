# Harris RPC 框架

Harris RPC 框架是一个基于 Java 的轻量级 RPC 框架，旨在帮助实现远程过程调用和分布式通信。该框架使用 Netty 进行网络通信，采用 Kryo 进行高效的序列化，同时利用 Nacos 实现服务注册与发现

## 模块概览

本项目由多个模块组成，每个模块负责不同的功能和用途

### 1. rpc-api

定义了通用接口和实体类，用于客户端和服务端之间的数据传输

目前定义了一个实现 Serializable 接口的 `Hello` 对象和一个 `HelloService` 接口

### 2. rpc-common

定义了用于远程调用的通用实体类、枚举以及异常，也就是一种传输格式。这些类提供了在客户端和服务端之间进行数据传输所需的结构和信息

#### 传输协议格式

- `RpcRequest` 类：表示客户端向服务端发送的远程调用请求。该类包含了服务接口名、方法名、方法参数、参数类型等信息，这些信息用于帮助服务端定位并执行相应的方法。

- `RpcResponse` 类：表示服务端对客户端请求的响应。该类包含了响应状态码、状态信息和响应数据，用于向客户端传递请求的执行结果。

- `ResponseCode` 枚举类：定义了远程过程调用的响应状态码以及对应的消息，用于标识请求的执行状态或失败原因。

为了唯一确定服务端需要调用的接口方法，`RpcRequest` 需要了解以下信息：

1. 接口名 `String interfaceName;`
2. 方法名 `String methodName;`
3. 参数类型 `Class<?>[] paramTypes;`：考虑到方法重载，需要包括调用方法的所有参数的类型信息。
4. 参数实际值 `Object[] parameters;`：在客户端调用时，需要传递参数的实际值。

在服务端执行完方法后，会生成一个 `RpcResponse` 对象，其中包含：

1. 响应状态码 `Integer statusCode;`
2. 响应状态信息 `String message;`
3. 响应数据 `T data;`

还有两个快速生成成功和失败的响应对象的方法

### 3. rpc-core

这是整个框架的核心部分，包括了客户端和服务端的实现。客户端利用 `RpcClient` 和 `RpcClientProxy` 实现远程方法调用，而服务端使用 `RpcServer` 和 `WorkerThread` 来处理客户端请求

#### 客户端

客户端并没有具体的接口实现类，无法直接生成实例对象。因此，在客户端的实现中，采用了动态代理的方式来生成实例，并且在方法调用时生成 `RpcRequest` 对象，然后将其发送给服务端

- `RpcClientProxy` 类负责生成客户端的代理对象。通过传入服务端的主机地址和端口来指定服务端的位置，并通过 `getProxy()` 方法生成代理对象

- `InvocationHandler` 接口需要实现 `invoke()` 方法，来指明代理对象的方法被调用时的动作。在这里，我们需要生成一个 `RpcRequest` 对象，将其发送给服务端，然后返回从服务端接收到的结果

- `RpcClient` 负责网络通信和数据传输，而 `RpcClientProxy` 负责生成代理对象，并将方法调用映射到远程调用。通过这两者的协同工作，实现了客户端调用服务端方法的功能

#### 服务端

服务端的实现中，我们使用一个 `ServerSocket` 监听某个端口，循环接收连接请求。每当有客户端发来请求，就创建一个新的线程，在新线程中处理调用。
这里使用了线程池，通过 `RpcServer` 类的 `register()` 方法注册一个服务后，立即开始监听客户端的连接请求

- `RpcServer` 类负责接受客户端连接，并将连接交给相应的 `WorkerThread` 进行处理。`WorkerThread` 类负责具体处理客户端请求，包括调用服务方法并返回响应

### 4. test-client

`test-client` 模块展示了如何使用 Harris RPC 框架的客户端功能。它可以连接到运行中的服务器并发起远程方法调用请求

### 5. test-server

`test-server` 模块展示了如何使用 Harris RPC 框架的服务端功能。它注册了一个示例服务 `HelloService`，并在接收到客户端请求时提供相应的响应

## 项目发展

### 1. feat: basic rpc framework

初步实现了一个基础的RPC框架，采用了JDK序列化和Socket通信方式。 目前实现了基本的远程方法调用功能。
在这个阶段，我们成功地注册了一个名为 helloService 的服务，但是框架限制了每个服务器只能注册一个服务，因为在注册完服务后，服务器就自行启动了

下一步，将服务的注册和服务器启动分离，使得服务端可以提供多个服务

### 2. feat: multi-service support

在这个功能增强中，我们引入了服务注册表机制，以便实现服务的注册和发现。这个机制通过 ServiceRegistry 接口来实现，该接口定义了服务注册和获取的方法。
借助于 ServiceRegistryImpl 类的实现，服务现在可以根据其接口名进行注册，并且可以根据接口名来获取对应的服务实体。这为更灵活的服务处理提供了基础

为了增强服务请求处理逻辑，我们对 RequestHandler 类进行了优化。它现在能够处理多个不同服务的请求。
当收到客户端请求后，RequestHandler 会根据请求的接口名识别相应的服务实体，并调用适当的方法进行处理

我们的框架在服务器端的能力得到了扩展，以支持多个服务。RpcServer 已经得到增强，可以同时注册和处理多个服务。
在服务器启动时，例如在 TestServer 类中，我们可以使用 ServiceRegistry 注册类似 HelloService 的服务。
当客户端请求到达时，RpcServer 会将请求智能路由到正确的服务进行处理

为了确保并发处理多个客户端请求，我们采用了多线程方法。
我们引入了 RequestHandlerThread 类，每个类都在单独的线程中处理一个传入的 RpcRequest。这确保了请求的高效和响应迅速

此外，通过引入自定义的 RpcException 类，我们增强了异常处理。这有助于更好地管理和传递可能在 RPC 框架中出现的异常情况，提高了系统的健壮性