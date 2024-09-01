package com.example.accounting_demo;

import com.example.accounting_demo.service.RmiEntityService;
import net.datafaker.Faker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.remoting.rmi.RmiServiceExporter;

@SpringBootApplication
@PropertySource("file:./application-env.properties")
public class AccountingDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(AccountingDemoApplication.class, args);
    }

    @Bean
    public Faker getFaker() {
        return new Faker();
    }

    @Bean
    public RmiServiceExporter rmiServiceExporter(RmiEntityService rmiEntityService) {
        RmiServiceExporter exporter = new RmiServiceExporter();
        exporter.setServiceName("RmiEntityService");
        exporter.setService(rmiEntityService);
        exporter.setServiceInterface(RmiEntityService.class);
        exporter.setRegistryPort(1099);
        return exporter;
    }
}
