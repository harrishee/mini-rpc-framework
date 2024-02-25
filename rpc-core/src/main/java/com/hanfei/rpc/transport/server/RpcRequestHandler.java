package com.hanfei.rpc.transport.server;

import com.hanfei.rpc.enums.ResponseStatus;
import com.hanfei.rpc.model.RpcRequest;
import com.hanfei.rpc.model.RpcResponse;
import com.hanfei.rpc.provider.LocalServiceProvider;
import com.hanfei.rpc.provider.ServiceProvider;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class RpcRequestHandler {
    private static final ServiceProvider SERVICE_PROVIDER = new LocalServiceProvider();
    private static final Map<String, Method> METHOD_CACHE = new ConcurrentHashMap<>();
    
    public RpcResponse<?> processRequest(RpcRequest rpcRequest) {
        // 获取服务实例
        Object serviceInstance = SERVICE_PROVIDER.getServiceInstance(rpcRequest.getInterfaceName());
        if (serviceInstance == null) {
            log.error("RPC请求处理器，找不到服务: {}", rpcRequest.getInterfaceName());
            return RpcResponse.error(rpcRequest.getRequestId(), ResponseStatus.SERVICE_NOT_FOUND);
        }
        
        // 接口名 + 方法名 + 参数类型 作为唯一标识，防止方法重载导致问题
        String methodKey = generateMethodKey(rpcRequest);
        
        // 获取方法
        Method method = METHOD_CACHE.computeIfAbsent(methodKey, k -> findServiceMethod(rpcRequest, serviceInstance));
        if (method == null) {
            return RpcResponse.error(rpcRequest.getRequestId(), ResponseStatus.METHOD_NOT_FOUND);
        }
        
        try {
            // 使用反射调用目标方法
            Object methodResult = method.invoke(serviceInstance, rpcRequest.getParameters());
            log.info("RPC请求处理器，方法调用成功: [{} : {}]", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
            return RpcResponse.success(rpcRequest.getRequestId(), methodResult);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("RPC请求处理器，方法调用失败: ", e);
            return RpcResponse.error(rpcRequest.getRequestId(), ResponseStatus.ERROR);
        }
    }
    
    // 获取服务方法
    private Method findServiceMethod(RpcRequest rpcRequest, Object serviceInstance) {
        try {
            return serviceInstance.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
        } catch (NoSuchMethodException e) {
            log.error("RPC请求处理器，找不到方法: {}", rpcRequest.getMethodName());
            return null;
        }
    }
    
    // 生成方法的唯一标识
    private String generateMethodKey(RpcRequest rpcRequest) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(rpcRequest.getInterfaceName()).append("#");
        keyBuilder.append(rpcRequest.getMethodName());
        for (Class<?> paramType : rpcRequest.getParamTypes()) {
            keyBuilder.append("#").append(paramType.getName());
        }
        return keyBuilder.toString();
    }
}
