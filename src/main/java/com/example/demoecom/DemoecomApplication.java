package com.example.demoecom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class DemoecomApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoecomApplication.class, args);
	}
}
