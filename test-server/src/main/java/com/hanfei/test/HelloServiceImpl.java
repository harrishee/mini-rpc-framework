package com.hanfei.test;

import com.hanfei.rpc.api.HelloObject;
import com.hanfei.rpc.api.HelloService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class HelloServiceImpl implements HelloService {

    private static final Logger logger = LoggerFactory.getLogger(HelloServiceImpl.class);

    @Override
    public String hello(HelloObject object) {
        logger.info("***HelloServiceImpl*** Get msg：[{}]", object.getMessage());
        return "Return value from server，id=" + object.getId();
    }
}
