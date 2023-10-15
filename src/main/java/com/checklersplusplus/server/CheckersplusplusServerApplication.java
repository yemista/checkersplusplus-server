package com.checklersplusplus.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CheckersplusplusServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CheckersplusplusServerApplication.class, args);
	}

}
