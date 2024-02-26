# Mini RPC Framework

English | [简体中文](README_zh.md)

## 1. Project Introduction

The Mini RPC Framework is a Java-based lightweight RPC framework designed to simplify remote procedure calls by hiding the complexities of underlying network communication. It allows developers to focus more on implementing business logic. The framework supports both Socket and Netty for network communication, offers JSON and Kryo for serialization, utilizes Nacos as its service registry center, and implements a simple round-robin load balancing mechanism along with a custom, straightforward transmission protocol.

## 2. About RPC

RPC (Remote Procedure Call) allows for requesting services from a remote computer program across a network without needing to understand the underlying network technologies. It abstracts the details of network communication, enabling developers to invoke remote methods as if they were local.

The process involves the following steps:

1. **Client-side Call**: The client invokes a local stub function, similar to calling any standard function.
2. **Request Packaging**: The stub packages the method name, parameters, and other information into a request message upon invocation.
3. **Network Transmission**: This request message is sent over the network to the server.
4. **Server-side Request Parsing**: The server listener decodes the request, identifying the specified method and parameters.
5. **Remote Procedure Execution**: The server executes the identified method and sends the result back.
6. **Result Return**: The server sends the response message back to the client's stub over the network, which then parses and returns the result to the client caller.

## 3. Usage Process

Recommend to open the project in an IDE for testing custom service interfaces and implementations.

### 1: Install Nacos

Ensure Nacos is installed on your device. If not, quickly start Nacos with the following Docker command:

```bash
docker run --name nacos-rpc-dev -e MODE=standalone -p 8848:8848 nacos/nacos-server:v2.2.0
```

### 2: Verify Nacos

Visit [http://localhost:8848/nacos](http://localhost:8848/nacos) to confirm Nacos is operational.

### 3: Add Service Interfaces

Add the required service interfaces to the `rpc-api` module.

### 4: Implement Service Interfaces

Implement these interfaces in the `impl` package under the `start-server` module.

### 5: Start the Server

Launch `RpcServerLauncher`, with options to adjust the default `serverType`, `host`, and `port`.

### 6: Client Service Calls

In the `start-client` module, call services using `RpcClientLauncher`.

## 4. Module Overview

- **rpc-api**: Defines all service interfaces, shared between service providers and consumers.
- **rpc-common**: Common framework components, like exceptions, enums, and transport formats `RpcRequest` and `RpcResponse`.
- **rpc-core**: Core implementation, including service registration, discovery, serialization, and network transmission.
- **start-server**: Server startup entry, responsible for launching the RPC server and service registration.
- **start-client**: Client startup entry, responsible for initiating RPC calls and consuming services.

## 5. Custom Transport Format

### RpcRequest

`RpcRequest` class format for client requests to the server during an RPC call:

- **requestId**: Uniquely identifies a request for asynchronous processing and response matching.
- **interfaceName**, **methodName**, **parameters**, **paramTypes**: Together determine the method to be called. They ensure unique method positioning and correct invocation, preventing conflicts in method overloading.
- **heartBeat**: Indicates if the request is a heartbeat message to maintain connection liveliness and prevent disconnections from prolonged idleness.

### RpcResponse

`RpcResponse<T>` class format for server responses back to the client:

- **requestId**: Corresponds to `RpcRequest`'s `requestId`, used for client-side request-response matching.
- **statusCode**: Indicates the processing outcome (success, failure, or other statuses).
- **message**: Response message, typically contains error information if the call fails.
- **data**: The generic parameter representing the specific response data or method call return value.

### Custom Protocol Format

A custom protocol format standardizes data transmission within the Mini RPC framework:

- **Magic Number**: 4 bytes, a fixed value `0xCAFEBABE`, identifies a valid RPC protocol package.
- **Package Type**: 4 bytes, distinguishes between request (`RpcRequest`) and response (`RpcResponse`) data types.
- **Serializer Type**: 4 bytes, indicates the serializer used for serializing and deserializing request or response data.
- **Data Length**: 4 bytes, the length of the subsequent data part, for parsing specific data content.
- **Data**: The actual data content, serialized `RpcRequest` or `RpcResponse` objects.

## 6. TODO

- [ ] Documentation improvements
- [ ] English code comments and logs
- [ ] Consistent hashing for load balancing
- [ ] Fix Docker deployment issues
