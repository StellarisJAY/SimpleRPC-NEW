package com.jay.rpc.config;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.annotation.Annotation;

/**
 * <p>
 *
 * </p>
 *
 * @author Jay
 * @date 2021/11/17
 **/
public class CustomScanner extends ClassPathBeanDefinitionScanner {
    public CustomScanner(BeanDefinitionRegistry registry, Class<?extends Annotation> annotation) {
        super(registry);
        // 扫描该注解类型
        super.addIncludeFilter(new AnnotationTypeFilter(annotation));
    }

    @Override
    public int scan(String... basePackages) {
        return super.scan(basePackages);
    }
}
