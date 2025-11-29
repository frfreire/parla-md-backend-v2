package br.gov.md.parla_md_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ParlaMdApplication {

	public static void main(String[] args) {
		SpringApplication.run(ParlaMdApplication.class, args);
	}


}
