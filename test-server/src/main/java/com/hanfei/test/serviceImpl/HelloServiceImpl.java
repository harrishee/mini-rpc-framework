package com.hanfei.test.serviceImpl;

import com.hanfei.rpc.annotation.Service;
import com.hanfei.rpc.api.HelloObject;
import com.hanfei.rpc.api.HelloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
@Service
public class HelloServiceImpl implements HelloService {

    private static final Logger logger = LoggerFactory.getLogger(HelloServiceImpl.class);

    @Override
    public String hello(HelloObject object) {
        logger.info("Hello 实现类被调用，参数：{}", object.getMessage());
        return "Return value from server，id=" + object.getId();
    }
}
