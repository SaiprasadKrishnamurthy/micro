package com.sai.rulebase;

import com.google.common.collect.Iterables;
import com.sai.rulebase.repository.RuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Created by saipkri on 18/08/17.
 */
@EnableDiscoveryClient
@EnableAutoConfiguration
@SpringBootApplication
@EnableFeignClients
@EnableSwagger2
@EnableJpaRepositories(basePackages = {"com.sai.rulebase.repository"})
@EnableTransactionManagement
public class RulebaseApp {

    private static final Logger log = LoggerFactory.getLogger(RulebaseApp.class);

    @Bean
    public CommandLineRunner demo(final RuleRepository ruleRepository) {
        return (args) -> {
            System.out.println(Iterables.toString(ruleRepository.findAll()));

        };
    }

    public static void main(String[] args) {
        SpringApplication.run(RulebaseApp.class);
    }
}
