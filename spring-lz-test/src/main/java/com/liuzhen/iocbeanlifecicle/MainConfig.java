package com.liuzhen.iocbeanlifecicle;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.liuzhen.iocbeanlifecicle.springdemo.String2DateConversionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.converter.Converter;

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

	@Bean
	public ConversionServiceFactoryBean conversionService() {
		ConversionServiceFactoryBean factoryBean = new ConversionServiceFactoryBean();
		Set<Converter<String, Date>> converterSet = new HashSet<>();
		converterSet.add(new String2DateConversionService());
		factoryBean.setConverters(converterSet);
		return factoryBean;
	}
}
