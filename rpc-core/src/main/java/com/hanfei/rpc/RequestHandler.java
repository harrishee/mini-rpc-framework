package com.hanfei.rpc;

import com.hanfei.rpc.entity.RpcRequest;
import com.hanfei.rpc.entity.RpcResponse;
import com.hanfei.rpc.enums.ResponseEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 请求处理器，用于处理 Client 请求并调用相应的服务方法
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class RequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    /**
     * 处理客户端请求并调用服务方法
     */
    public Object handle(RpcRequest rpcRequest, Object service) {
        Object result = null;
        try {
            // 调用 invokeTargetMethod 方法，执行具体的服务调用
            result = invokeTargetMethod(rpcRequest, service);
            logger.info("服务: {} 成功调用方法: {}，获得结果: {}", rpcRequest.getInterfaceName(), rpcRequest.getMethodName(), result);
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error("调用或发送时有错误发生：", e);
        }
        return result;
    }

    /**
     * 根据 RpcRequest 调用服务对象的方法
     */
    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service)
            throws IllegalAccessException, InvocationTargetException {
        Method method;
        try {
            // 通过反射获取服务对象的方法
            method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
        } catch (NoSuchMethodException e) {
            return RpcResponse.error(ResponseEnum.METHOD_NOT_FOUND);
        }
        // 通过反射调用目标方法，并返回方法的执行结果
        logger.info("开始调用方法: {}，参数: {}", rpcRequest.getMethodName(), rpcRequest.getParameters());
        return method.invoke(service, rpcRequest.getParameters());
    }
}
