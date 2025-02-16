package com.example.ticketing_system;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TicketingSystemApplication {

	public static void main(String[] args) {

		Dotenv dotenv = Dotenv.load();
		System.setProperty("MONGO_URI", dotenv.get("MONGO_URI"));
		System.setProperty("MONGO_DB", dotenv.get("MONGO_DB"));
		System.setProperty("COGNITO_USER_POOL_ID", dotenv.get("COGNITO_USER_POOL_ID"));
		System.setProperty("COGNITO_CLIENT_SECRET", dotenv.get("COGNITO_CLIENT_SECRET"));
		System.setProperty("COGNITO_CLIENT_ID", dotenv.get("COGNITO_CLIENT_ID"));

		SpringApplication.run(TicketingSystemApplication.class, args);
	}

}
