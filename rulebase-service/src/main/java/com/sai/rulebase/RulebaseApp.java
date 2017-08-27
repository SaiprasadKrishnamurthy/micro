package com.sai.rulebase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sai.rulebase.entity.RuleFlow;
import com.sai.rulebase.entity.RuleFlowEdge;
import com.sai.rulebase.repository.RuleFlowRepository;
import com.sai.rulebase.repository.RuleRepository;
import com.sai.rulebase.rest.RuleExecApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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

    @LoadBalanced
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Autowired
    private RuleExecApi ruleExecApi;

    @Bean
    public CommandLineRunner loadData(final RuleRepository ruleRepository, final RuleFlowRepository ruleFlowRepository) {
        return (args) -> {
            {
                String dsl = "CountryOfBirthCheckRule -> NationalityCheckRule -> EventTypeAndSubTypeCheckRule -> GenderCheckRule -> PlaceOfBirthRule -> TypeOfVisaRule; CountryOfBirthCheckRule -> SSNCheckRule -> PlaceOfBirthRule";
                RuleFlow ruleFlow = new RuleFlow();
                ruleFlow.setName("RuleFlowDef1");
                ruleFlow.setDescription("Rule flow definition for risk rules");
                ruleFlow.setEdges(edges(dsl));
                ruleFlow.setPostExecutionCallback("#buildResponse(#ctx)");
                ruleFlowRepository.save(ruleFlow);
            }

            {
                String dsl = "CountryOfBirthCheckRule -> NationalityCheckRule -> EventTypeAndSubTypeCheckRule -> GenderCheckRule -> PlaceOfBirthRule -> SSNCheckRule -> TypeOfVisaRule";
                RuleFlow ruleFlow = new RuleFlow();
                ruleFlow.setName("RuleFlowDef2");
                ruleFlow.setDescription("Rule flow definition for risk rules 2");
                ruleFlow.setEdges(edges(dsl));
                ruleFlow.setPostExecutionCallback("#buildResponse(#ctx)");
                ruleFlowRepository.save(ruleFlow);
            }

            System.out.println(ruleExecApi.ruleresult("RuleFlowDef1", new ObjectMapper().readValue(RulebaseApp.class.getClassLoader().getResourceAsStream("payload1.json"), Map.class), true));
            System.out.println(ruleExecApi.ruleresult("RuleFlowDef2", new ObjectMapper().readValue(RulebaseApp.class.getClassLoader().getResourceAsStream("payload1.json"), Map.class), true));
            System.out.println(ruleExecApi.ruleresult("RuleFlowDef1", new ObjectMapper().readValue(RulebaseApp.class.getClassLoader().getResourceAsStream("payload.json"), Map.class), true));
            System.out.println(ruleExecApi.ruleresult("RuleFlowDef2", new ObjectMapper().readValue(RulebaseApp.class.getClassLoader().getResourceAsStream("payload.json"), Map.class), true));
        };
    }

    private List<RuleFlowEdge> edges(final String dsl) {
        List<RuleFlowEdge> edges = new ArrayList<>();
        Stream.of(dsl.split(";"))
                .forEach(line -> Stream.of(line.split("->"))
                        .reduce((a, b) -> {
                            RuleFlowEdge edge = new RuleFlowEdge();
                            edge.setRuleNameFrom(a.trim());
                            edge.setRuleNameTo(b.trim());
                            edges.add(edge);
                            return b;
                        }));
        return edges;
    }

    public static void main(String[] args) {
        SpringApplication.run(RulebaseApp.class);
    }
}
