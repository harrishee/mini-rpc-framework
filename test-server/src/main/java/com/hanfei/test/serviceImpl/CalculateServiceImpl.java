package com.hanfei.test.serviceImpl;

import com.hanfei.rpc.annotation.Service;
import com.hanfei.rpc.api.CalculateService;

/**
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
@Service
public class CalculateServiceImpl implements CalculateService {

    @Override
    public String addition(int a, int b, String message) {
        return "The sum of " + a + " and " + b + " is " + (a + b);
    }

    @Override
    public String multiplication(int a, int b, String message) {
        return "The product of " + a + " and " + b + " is " + (a * b);
    }

    @Override
    public String division(int a, int b, String message) {
        return "The division of " + a + " and " + b + " is " + (a / b);
    }
}
