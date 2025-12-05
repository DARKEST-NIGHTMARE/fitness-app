package com.fitness.smart_tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SmartTrackerApplication {

	public static void main(String[] args)
	{ System.setProperty("java.net.preferIPv4Stack", "true");
		SpringApplication.run(SmartTrackerApplication.class, args);
	}

}
