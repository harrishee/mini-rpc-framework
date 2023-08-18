package com.hanfei.rpc.api;

/**
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public interface HelloService {

    String hello(HelloObject object);

    String addNums(int a, int b);
}
