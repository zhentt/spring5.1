package com.liuzhen.iocbeanlifecicle;

/**
 * @ClassName Person
 * @Description Person实体
 * @Author LiuZhen
 * @Date 2020/1/13 21:21
 * @Version 1.0
 */
public class Person {
	private String name;
	private String sex;

	public Person() {
	}

	public Person(String name, String sex) {
		this.name = name;
		this.sex = sex;
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
