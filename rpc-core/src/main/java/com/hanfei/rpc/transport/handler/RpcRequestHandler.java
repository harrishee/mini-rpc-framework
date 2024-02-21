package com.hanfei.rpc.transport.handler;

import com.hanfei.rpc.entity.RpcRequest;
import com.hanfei.rpc.entity.RpcResponse;
import com.hanfei.rpc.enums.ResponseEnum;
import com.hanfei.rpc.provider.ServiceProvider;
import com.hanfei.rpc.provider.ServiceProviderImpl;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


@Slf4j
public class RpcRequestHandler {

    private static final ServiceProvider serviceProvider;

    // initialize service provider
    static {
        serviceProvider = new ServiceProviderImpl();
    }

    /**
     * handle the incoming RPC request
     */
    public Object handleRequest(RpcRequest rpcRequest) {
        // get the service instance from the service provider, then invoke the target method
        Object serviceInstance = serviceProvider.getServiceInstanceByName(rpcRequest.getInterfaceName());
        return invokeTargetMethod(rpcRequest, serviceInstance);
    }

    /**
     * invoke the target method
     */
    private Object invokeTargetMethod(RpcRequest rpcRequest, Object serviceInstance) {
        Object result;
        try {
            Method method = serviceInstance.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            log.info("Starts calling method: [{}]，parameters: [{}]", rpcRequest.getMethodName(), rpcRequest.getParameters());

            // invoke the target method
            result = method.invoke(serviceInstance, rpcRequest.getParameters());
            log.info("Successfully called method: [{}], result: [{}]", method, result.toString());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return RpcResponse.error(rpcRequest.getRequestId(), ResponseEnum.METHOD_NOT_FOUND);
        }
        return result;
    }
}
