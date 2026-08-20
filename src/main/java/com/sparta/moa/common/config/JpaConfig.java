package com.sparta.moa.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * BaseEntity 의 @CreatedDate · @LastModifiedDate 는
 * 이 한 줄이 있어야 동작합니다. 없으면 값이 null 로 들어갑니다.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
