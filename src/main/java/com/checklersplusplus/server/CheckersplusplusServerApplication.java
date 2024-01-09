package com.checklersplusplus.server;

import org.apache.catalina.webresources.TomcatURLStreamHandlerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CheckersplusplusServerApplication {

	public static void main(String[] args) {
		TomcatURLStreamHandlerFactory.disable();
		SpringApplication.run(CheckersplusplusServerApplication.class, args);
	}

}
