package com.liuzhen.iocbeanlifecicle.springdemo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.core.convert.converter.Converter;

/**
 * @ClassName String2DateConversionService
 * @Description 类型转换器 例子
 * @Author LiuZhen
 * @Date 2020/1/18 10:21
 * @Version 1.0
 */
public class String2DateConversionService implements Converter<String, Date> {
	@Override
	public Date convert(String source) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		try {
			return dateFormat.parse(source);
		} catch (ParseException e) {
			return null;
		}
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
}
