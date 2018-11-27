package com.dong;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication
public class mainApplication/* extends SpringBootServletInitializer*/{

	public static void main(String[] args) throws Exception {
        SpringApplication.run(mainApplication.class, args);
    }

	/*@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(mainApplication.class);
	}*/
	
	
}
