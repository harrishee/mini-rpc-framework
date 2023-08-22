package com.hanfei.rpc;

/**
 * server 通用接口
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public interface RpcServer {

    /**
     * 启动服务
     */
    void start(int port);
}
