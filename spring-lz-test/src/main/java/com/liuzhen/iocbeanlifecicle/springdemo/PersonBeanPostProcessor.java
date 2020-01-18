package com.liuzhen.iocbeanlifecicle.springdemo;

import com.liuzhen.iocbeanlifecicle.Person;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * @ClassName PersonBeanPostProcessor
 * @Description Person后置处理器
 * @Author LiuZhen
 * @Date 2020/1/18 13:11
 * @Version 1.0
 */
@Component
public class PersonBeanPostProcessor implements BeanPostProcessor {
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof Person) {
			Person person = (Person)bean;
			person.setName("liuzhen shuai");
			return person;
		}
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return null;
	}
}
