package com.liuzhen.iocbeanlifecicle;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName MainConfig
 * @Description 配置类
 * @Author LiuZhen
 * @Date 2020/1/13 21:12
 * @Version 1.0
 */
@Configuration
@ComponentScan(basePackages = {"com.liuzhen.iocbeanlifecicle"})
public class MainConfig {
	@Bean
	public Person person() {
		Person person = new Person();
		person.setName("liuzhen");
		person.setSex("男");
		return person;
	}
}
