package com.example.accounting_demo;

import net.datafaker.Faker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;

//@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@SpringBootApplication
public class AccountingDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountingDemoApplication.class, args);
    }

    @Bean
    public Faker getFaker() {
        return new Faker();
    }
}
