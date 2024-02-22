package com.hanfei.rpc.transport;

public interface RpcServer {
    void start();

    <T> void publishService(String serviceName, T service);
}
