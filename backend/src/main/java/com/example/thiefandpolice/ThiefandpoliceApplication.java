package com.example.thiefandpolice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ThiefandpoliceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ThiefandpoliceApplication.class, args);
	}

}
