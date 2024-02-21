# Mini RPC Framework

Mini RPC Framework is a lightweight RPC framework based on Java, designed to facilitate remote procedure calls and
distributed communication.
The framework provides two network communication methods, Socket and Netty, as well as two serialization methods, JSON
and Kryo.
It utilizes Nacos as a service registration center and implements a simple load balancing mechanism.

# Getting Started

Chinese version: [Click here](README_CN.md)

Before you begin, ensure that Nacos is installed and properly configured for service registration and discovery.

### 1. Start the Nacos Server

The Nacos server is a crucial component for service registration and discovery in Mini RPC Framework. To start it,
follow these steps:

1. Navigate to the Nacos installation directory in your terminal.
2. Start the Nacos server in standalone mode:
    ```shell
    sh startup.sh -m standalone
    ```

This command initiates the Nacos server and prepares it to manage your services.

### 2. Server-Side Implementation

Now, let's implement a service on the server-side:

1. `Implement a service interface`: Define your service interface that includes the methods you want to make remotely
   accessible.
2. `Register the service`: Use the Mini RPC Framework to register your service on the server. This registration allows
   clients to discover and access your service.
3. `Start the server`: Start your server to make your service available for remote calls.

### 3. Start the Client

Once you've set up the server, you can start the client and begin making remote calls:

1. `Initialize the client`: Configure and initialize the Mini RPC client, specifying the desired serialization method
   and other settings.
2. `Create proxies`: Use the proxy class provided by the framework to create proxy objects for your services. These
   proxy objects enable you to invoke remote methods as if they were local.
3. `Call the service`: Access the methods of your service through the proxy objects. The framework takes care of the
   remote communication details, allowing you to focus on your application logic.

# 1. Module Overview

- `rpc-api`: Common service interfaces
- `rpc-common`: Entities, enumerations, exceptions, utility classes, and more
- `rpc-core`: Client, server, service registration center, serializers, load balancers, and more
- `test-client`: Client functionality for registering services and making requests
- `test-server`: Server functionality for registering services and starting servers to receive and process client
  requests

# 2. Project Development

## 1. `feat: basic rpc framework`

Implemented the basic RPC framework using JDK serialization and Socket communication. Currently, it supports basic
remote method invocation.

Drawback: After registering a service, the server starts independently, allowing only one service to be registered on
each server.

## 2. `feat: multi-service support`

Introduced a service registry mechanism, allowing services to be registered based on their interface names and retrieved
based on interface names.

## 3. `feat: netty communication`

Introduced Netty as the communication framework, replacing the previous Socket-based communication method.
This transitioned from traditional BIO transmission to the more efficient NIO approach.

## 4. `feat: kryo serializer`

Introduced the Kryo serializer, replacing the previous JSON serializer.

## 5. `feat: nacos services`

Introduced Nacos as the service registration center, replacing the previous local registry approach.
This move shifted the service registry from local to remote servers, making service registration and discovery more
flexible.

## 6. `feat: load balance`

Introduced a load balancing mechanism, implementing simple random and round-robin load balancing.

## 7. `feat: auto service registration`

Introduced an automatic service registration mechanism, making service registration more convenient.
It eliminates the need for manual service registration when starting servers.
