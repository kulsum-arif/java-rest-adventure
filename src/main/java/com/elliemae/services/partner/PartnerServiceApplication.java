package com.synkrato.services.partner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.map.repository.config.EnableMapRepositories;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@ServletComponentScan
@SpringBootApplication(scanBasePackages = {"com.synkrato"})
@EnableCaching
@EnableRetry
@EnableMapRepositories("com.synkrato.services.partner.data")
@PropertySource("classpath:project.properties")
public class PartnerServiceApplication {

  @Autowired MessageSource epcMessageSource;

  public static void main(String[] args) {
    SpringApplication.run(PartnerServiceApplication.class, args);
  }

  @Bean
  public LocalValidatorFactoryBean validator() {
    LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
    bean.setValidationMessageSource(epcMessageSource);
    return bean;
  }
}
