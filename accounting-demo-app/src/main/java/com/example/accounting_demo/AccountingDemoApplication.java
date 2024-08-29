package com.example.accounting_demo;

import com.example.accounting_demo.service.EntityService;
import net.datafaker.Faker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.remoting.rmi.RmiServiceExporter;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("file:./application-env.properties")
public class AccountingDemoApplication {
    private static ConfigurableApplicationContext context;

    public static void main(String[] args) {
        SpringApplication.run(AccountingDemoApplication.class, args);
    }

    @Bean
    public Faker getFaker() {
        return new Faker();
    }

    @Bean
    public RmiServiceExporter rmiServiceExporter(EntityService entityService) {
        RmiServiceExporter exporter = new RmiServiceExporter();
        exporter.setServiceName("EntityService");
        exporter.setService(entityService);
        exporter.setServiceInterface(EntityService.class);
        exporter.setRegistryPort(1099);
        return exporter;
    }
}
