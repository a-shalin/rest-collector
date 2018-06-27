package ru.cloudinfosys.rc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class RcApplication {
	public static void main(String[] args) {
		SpringApplication.run(RcApplication.class, args);
	}
}
