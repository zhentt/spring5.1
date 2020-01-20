package com.liuzhen.iocbeanlifecicle.springdemo;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.stereotype.Component;

/**
 * @ClassName MyBeanDefinitionRegistryPostProcessor
 * @Description 构建BeanDefinition的实例对象
 * BeanDefinitionBuilder是Builder模式的应用。通过这个类我们可以方便的构建BeanDefinition的实例对象。
 * @Author LZ
 * @Date 2020/1/20 9:38
 * @Version 1.0
 */
@Component
public class MyBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {
	/**
	 * org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor#postProcessBeanDefinitionRegistry(org.springframework.beans.factory.support.BeanDefinitionRegistry)
	 * 该方法允许程序员通过代码编码的方式手动往程序中注册 BeanDefinition
	 * @param registry the bean definition registry used by the application context
	 * @throws BeansException
	 */
	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(OrderService.class)
				// 这里的属性名是根据setter方法
				.addPropertyReference("dao", "orderDao")
				.setInitMethodName("init")
				.setScope(BeanDefinition.SCOPE_SINGLETON)
				.getBeanDefinition();

		registry.registerBeanDefinition("orderService", beanDefinition);
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

	}
}
