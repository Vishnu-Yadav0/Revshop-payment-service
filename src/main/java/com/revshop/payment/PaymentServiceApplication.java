package com.revshop.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class PaymentServiceApplication {

	public static void main(String[] args) {
		loadEnv();
		SpringApplication.run(PaymentServiceApplication.class, args);
	}

	private static void loadEnv() {
		java.io.File envFile = new java.io.File(".env");
		if (envFile.exists()) {
			try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(envFile))) {
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.trim().isEmpty() || line.startsWith("#")) continue;
					String[] parts = line.split("=", 2);
					if (parts.length == 2) {
						System.setProperty(parts[0].trim(), parts[1].trim());
					}
				}
			} catch (java.io.IOException e) {
				System.err.println("Could not load .env file: " + e.getMessage());
			}
		}
	}

}
