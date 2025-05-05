package com.alesh100.BankingApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@OpenAPIDefinition(
//		info = @Info(
//				title = "Banking Application",
//				description = "For carry out major banking activities",
//				version = "v2.1",
//				contact = @Contact(
//						name = "Toyyib Alesh",
//						email = "aleshinloyetoheeb@gmail.com",
//						url = "git/alesh100.com"
//				),
//				license = @License(
//						name = "Toyyib Alesh",
//						url = "git/alesh100.com"
//				)
//		)
//)
public class BankingApplication {

	public static void main(String[] args) {
		SpringApplication.run(BankingApplication.class, args);
	}

}
