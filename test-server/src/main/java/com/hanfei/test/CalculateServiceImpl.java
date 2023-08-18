package com.hanfei.test;

import com.hanfei.rpc.api.CalculateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class CalculateServiceImpl implements CalculateService {

    private static final Logger logger = LoggerFactory.getLogger(CalculateServiceImpl.class);

    @Override
    public String addNums(int a, int b, String message) {
        logger.info("***CalculateServiceImpl*** Get msg：[{}]", message);
        return "Return value from server，the sum of " + a + " and " + b + " is " + (a + b);
    }
}
