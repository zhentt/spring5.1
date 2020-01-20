package com.liuzhen.iocbeanlifecicle.springdemo;

import org.springframework.stereotype.Service;

/**
 * @ClassName OrderService
 * @Description OrderService测试类
 * @Author LZ
 * @Date 2020/1/20 12:25
 * @Version 1.0
 */
@Service
public class OrderService {
	public void init() {
		System.out.println("init ...");
	}
}
