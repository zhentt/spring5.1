package com.liuzhen.iocbeanlifecicle;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

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
	}
}
