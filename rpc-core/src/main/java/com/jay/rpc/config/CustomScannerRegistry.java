package com.jay.rpc.config;

import com.jay.rpc.annotation.EnableRpc;
import com.jay.rpc.annotation.RpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * <p>
 *  import 该类来自定义组件扫描
 * </p>
 *
 * @author Jay
 * @date 2021/11/17
 **/
public class CustomScannerRegistry implements ImportBeanDefinitionRegistrar {

    private Logger logger = LoggerFactory.getLogger(CustomScannerRegistry.class);
    private static final String RPC_COMPONENT_PACKAGE = "com.jay.rpc";

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        // @EnableRpc注解的属性
        Map<String, Object> enableRpcAnnotation = importingClassMetadata.getAnnotationAttributes(EnableRpc.class.getName());
        AnnotationAttributes annotationAttributes = AnnotationAttributes.fromMap(enableRpcAnnotation);
        assert annotationAttributes != null;
        // basePackage属性
        String[] basePackages = annotationAttributes.getStringArray("basePackage");
        if(basePackages.length > 0){
            // 扫描RpcService
            CustomScanner rpcServiceScanner = new CustomScanner(registry, RpcService.class);
            int serviceCount = rpcServiceScanner.scan(basePackages);
            logger.info("共扫描到 {} 个服务提供类", serviceCount);
        }

        CustomScanner rpcComponentScanner = new CustomScanner(registry, Component.class);
        rpcComponentScanner.scan(RPC_COMPONENT_PACKAGE);
    }
}
