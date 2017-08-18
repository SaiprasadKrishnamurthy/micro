package com.sai.rulebase;

import com.google.common.collect.Iterables;
import com.sai.rulebase.entity.Rule;
import com.sai.rulebase.entity.RuleFlow;
import com.sai.rulebase.entity.RuleFlowEdge;
import com.sai.rulebase.repository.RuleFlowRepository;
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

import java.util.ArrayList;
import java.util.List;

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
    public CommandLineRunner loadData(final RuleRepository ruleRepository, final RuleFlowRepository ruleFlowRepository) {
        return (args) -> {
            Iterable<Rule> all = ruleRepository.findAll();
            Rule[] rules = Iterables.toArray(all, Rule.class);

            List<RuleFlowEdge> edges = new ArrayList<>();
            for (int i = 0; i < rules.length - 1; i++) {
                RuleFlowEdge ruleFlowEdge = new RuleFlowEdge();
                ruleFlowEdge.setRuleNameFrom(rules[i].getName());
                ruleFlowEdge.setRuleNameTo(rules[i + 1].getName());
                edges.add(ruleFlowEdge);
            }
            RuleFlow ruleFlow = new RuleFlow();
            ruleFlow.setName("RiskRuleFlow");
            ruleFlow.setDescription("Rule flow definition for risk rules");
            ruleFlow.setEdges(edges);
            ruleFlowRepository.save(ruleFlow);
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(RulebaseApp.class);
    }
}
