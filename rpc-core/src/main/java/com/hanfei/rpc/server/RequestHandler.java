package com.hanfei.rpc.server;

import com.hanfei.rpc.entity.RpcRequest;
import com.hanfei.rpc.entity.RpcResponse;
import com.hanfei.rpc.enumeration.RpcResponseCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 请求处理器，用于处理客户端请求并调用相应的服务方法
 * 负责根据 RpcRequest 调用服务对象的方法，并返回 RpcResponse
 * 处理过程中会处理反射调用异常，记录调用日志
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class RequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    /**
     * 处理客户端请求并调用服务方法
     *
     * @param rpcRequest 客户端请求对象
     * @param service    服务对象
     * @return RpcResponse，表示服务调用结果
     */
    public Object handle(RpcRequest rpcRequest, Object service) {
        Object result = null;
        try {
            // 调用 invokeTargetMethod 方法，执行具体的服务调用
            result = invokeTargetMethod(rpcRequest, service);
            logger.info("***RequestHandler*** 服务: {} 成功调用方法: {}", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error("调用或发送时有错误发生：", e);
        }
        // 返回服务调用结果对象
        return result;
    }

    /**
     * 根据 RpcRequest 调用服务对象的方法
     *
     * @param rpcRequest 客户端请求对象
     * @param service    服务对象
     * @return 调用结果对象
     * @throws IllegalAccessException    调用方法权限异常
     * @throws InvocationTargetException 调用目标方法异常
     */
    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) throws IllegalAccessException, InvocationTargetException {
        Method method;
        try {
            // 通过反射获取服务对象的方法
            method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
        } catch (NoSuchMethodException e) {
            return RpcResponse.error(RpcResponseCodeEnum.METHOD_NOT_FOUND);
        }
        // 通过反射调用目标方法，并返回方法的执行结果
        return method.invoke(service, rpcRequest.getParameters());
    }
}
