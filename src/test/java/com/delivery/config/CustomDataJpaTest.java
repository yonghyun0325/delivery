package com.delivery.config;

import com.delivery.common.util.CryptoConverter;
import com.delivery.common.util.SsnEncryptor;
import com.delivery.global.config.EncryptConfig;
import com.delivery.global.config.JpaAuditingConfig;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@DataJpaTest
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@EnableJpaAuditing
@Import(TestAuditorConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public @interface CustomDataJpaTest {}
