package com.hanfei.rpc.api;


public interface CalculateService {

    String addition(int a, int b, String message);

    String multiplication(int a, int b, String message);

    String division(int a, int b, String message);
}
