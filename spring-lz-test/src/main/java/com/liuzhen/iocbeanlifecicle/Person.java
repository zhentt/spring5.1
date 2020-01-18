package com.liuzhen.iocbeanlifecicle;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.InitializingBean;

/**
 * @ClassName Person
 * @Description Person实体
 * @Author LiuZhen
 * @Date 2020/1/13 21:21
 * @Version 1.0
 */
public class Person implements InitializingBean {
	private String name;
	private String sex;

	public Person() {
	}

	public Person(String name, String sex) {
		this.name = name;
		this.sex = sex;
	}

	public void initPerson() {
		System.out.println("init方法");
	}

	@PostConstruct
	public void init() {
		System.out.println("PostConstruct init");
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		System.out.println("afterPropertiesSet ...方法");
	}

	@Override
	public String toString() {
		return "Person{" +
				"name='" + name + '\'' +
				", sex='" + sex + '\'' +
				'}';
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}
}
