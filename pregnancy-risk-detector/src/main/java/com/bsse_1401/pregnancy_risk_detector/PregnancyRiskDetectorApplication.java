package com.bsse_1401.pregnancy_risk_detector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.bsse_1401.pregnancy_risk_detector")
public class PregnancyRiskDetectorApplication {

	public static void main(String[] args) {
		SpringApplication.run(PregnancyRiskDetectorApplication.class, args);
		System.out.println("=================================================");
		System.out.println("🤖 AI Pregnancy Risk Detection System Started!");
		System.out.println("🌐 Access the application at: http://localhost:8080");
		System.out.println("=================================================");
	}
}