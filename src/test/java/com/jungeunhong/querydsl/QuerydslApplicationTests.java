package com.jungeunhong.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Entity;
import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class QuerydslApplicationTests {

//	@Autowired
//	EntityManager em;

	@Test
	void contextLoads() {
//
//		Hello hello = new Hello();
//
//		em.persist(hello);
//
//		JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);
//
//		QHello qHello = new QHello("h");
//		Hello fetchOne = jpaQueryFactory.selectFrom(qHello).fetchOne();
//		assertThat(fetchOne).isEqualTo(hello);
//		assertThat(fetchOne.getId()).isEqualTo(hello.getId());
	}

}
