package com.hanfei.rpc.api;

/**
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public interface CalculateService {

    String addition(int a, int b, String message);

    String multiplication(int a, int b, String message);

    String division(int a, int b, String message);
}
