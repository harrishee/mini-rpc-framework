package com.hanfei.rpc.transport.server;

import com.hanfei.rpc.enums.ResponseStatus;
import com.hanfei.rpc.model.RpcRequest;
import com.hanfei.rpc.model.RpcResponse;
import com.hanfei.rpc.provider.LocalServiceProvider;
import com.hanfei.rpc.provider.ServiceProvider;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
public class RpcRequestHandler {
    private static final ServiceProvider SERVICE_PROVIDER = new LocalServiceProvider();
    
    // 不需要对 Method 做缓存
    // 1. 反射获取的速度很快的，缓存也是放到 map 里面，速度没什么提升，反而导致空间占用太大，得不偿失
    // 2. 反射调用的性能会比直接调用慢，但是这个项目反射调用比直接调用好，而且速度瓶颈是网络
    
    public RpcResponse<?> processRequest(RpcRequest rpcRequest) {
        // 获取服务实例
        Object serviceInstance = SERVICE_PROVIDER.getServiceInstance(rpcRequest.getInterfaceName());
        if (serviceInstance == null) {
            log.error("RPC请求处理器，找不到服务: {}", rpcRequest.getInterfaceName());
            return RpcResponse.error(rpcRequest.getRequestId(), ResponseStatus.SERVICE_NOT_FOUND);
        }
        
        Object methodResult;
        try {
            Method method = serviceInstance.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            methodResult = method.invoke(serviceInstance, rpcRequest.getParameters());
            log.info("RPC请求处理器，方法调用成功: [{} : {}]", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
            return RpcResponse.success(rpcRequest.getRequestId(), methodResult);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error("RPC请求处理器，方法调用失败: ", e);
            return RpcResponse.error(rpcRequest.getRequestId(), ResponseStatus.ERROR);
        }
    }
}
