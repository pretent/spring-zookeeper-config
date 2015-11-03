package org.pretent.config.spring.zk.zkspring.util;

import java.io.IOException;

import org.springframework.beans.BeansException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ApplicationContextTest{

	public static void main(String[] args) throws BeansException, IOException {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
		context.start();
		System.in.read();
	}
}