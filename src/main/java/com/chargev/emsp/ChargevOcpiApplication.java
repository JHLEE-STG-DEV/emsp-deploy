package com.chargev.emsp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@MapperScan("com.chargev.emsp.mapper") // MyBatis 매퍼 패키지 스캔
@ComponentScan(basePackages = {
    "com.chargev.emsp",  // 기본 패키지와 하위 패키지를 포함
    "com.chargev.utils",  // 외부 패키지 추가
    "com.chargev.logger" 
})
public class ChargevOcpiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChargevOcpiApplication.class, args);
		
	}

}
