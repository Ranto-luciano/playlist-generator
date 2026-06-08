package com.playlist.generator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PlayListGeneratorApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlayListGeneratorApplication.class, args);
	}

}
