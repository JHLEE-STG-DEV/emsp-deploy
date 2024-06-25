package com.chargev.emsp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@MapperScan("com.chargev.emsp.mapper") // MyBatis 매퍼 패키지 스캔
public class ChargevOcpiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChargevOcpiApplication.class, args);
		
	}

}
