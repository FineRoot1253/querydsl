package com.jungeunhong.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;

import javax.persistence.EntityManager;
import java.util.Optional;
import java.util.UUID;

@SpringBootApplication
public class QuerydslApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuerydslApplication.class, args);
	}

	@Bean
	AuditorAware<String> auditorAwareProvider(){
		return ()-> Optional.of(UUID.randomUUID().toString());
	}

	@Bean
	JPAQueryFactory queryFactory(EntityManager em){
		return new JPAQueryFactory(em);
	}

}
