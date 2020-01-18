package com.liuzhen.iocbeanlifecicle;

import java.util.Date;
import java.util.Objects;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ConversionServiceFactoryBean;

/**
 * @ClassName MainStarter
 * @Description 主类启动测试
 * @Author LiuZhen
 * @Date 2020/1/13 21:25
 * @Version 1.0
 */
public class MainStarter {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(MainConfig.class);

		//ClassPathXmlApplicationContext context1 = new ClassPathXmlApplicationContext("beans.xml");

		Person person = (Person)context.getBean("person");
		System.out.println("person: " + person);

		//ConversionServiceFactoryBean conversionServiceFactoryBean = context.getBean(ConversionServiceFactoryBean.class);
		//
		//Date date = Objects.requireNonNull(conversionServiceFactoryBean.getObject()).convert("2019-01-18 10:00:00", Date.class);
		//System.out.println(date);
	}
}
