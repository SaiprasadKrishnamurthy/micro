package com.sai.rules.rulebase;

import org.apache.log4j.Logger;
import org.easyrules.api.RulesEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.easyrules.core.RulesEngineBuilder.aNewRulesEngine;

@EnableDiscoveryClient
@EnableAutoConfiguration
@SpringBootApplication
@EnableFeignClients
public class RulebaseServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(RulebaseServiceApp.class, args);
    }
}

@Configuration
class RulebaseConfiguration {

    @LoadBalanced
    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

@RestController
@RefreshScope
class FlightRoutesRestAPI {

    private static final Logger LOG = Logger.getLogger(FlightRoutesRestAPI.class.getName());

    @Autowired
    private RestTemplate restTemplate;

    @Value("${popular.routes}")
    private String popularRoutes;

    @Autowired
    private Tracer tracer;

    @GetMapping("/popular-routes")
    public List<?> popularRoutes() throws Exception {
        Span span = tracer.createSpan("/popular-routes");
        try {
            String[] popularRoutesPair = popularRoutes.split(",");
            return Stream.of(popularRoutesPair)
                    .map(pair -> getRoutes(pair))
                    .map(HttpEntity::getBody)
                    .collect(Collectors.toList());
        } finally {
            tracer.close(span);
        }
    }


    private ResponseEntity<List> getRoutes(final String pair) {
        return this.restTemplate.exchange(
                "http://refdata-service/flights/" + pair.split("-")[0] + "/" + pair.split("-")[1],
                HttpMethod.GET,
                null,
                List.class);
    }


    @GetMapping("/flights/{origin}/{destination}")
    List<?> flightsForRoute(@PathVariable("origin") final String origin,
                            @PathVariable("destination") final String destination) {
        Span span = tracer.createSpan("/flights/{origin}/{destination}");
        try {
            return getRoutes(origin + "-" + destination).getBody();
        } finally {
            tracer.close(span);
        }
    }
}


@RestController
@RefreshScope
class RulesRestAPI {

    private static final Logger LOG = Logger.getLogger(com.sai.rules.rulebase.FlightRoutesRestAPI.class.getName());

    @Autowired
    private Tracer tracer;

    @Autowired
    private RulesRepository rulesRepository;

    @Autowired
    private RuleEngineFactory ruleEngineFactory;

    @GetMapping("/rules")
    public List<RuleDefinition> rules() throws Exception {
        Span span = tracer.createSpan("/rules");
        try {
            return rulesRepository.allRules();
        } finally {
            tracer.close(span);
        }
    }

    @PutMapping(value = "/rule", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> saveOrUpdateRule(@RequestBody final RuleDefinition ruleDefinition) throws Exception {
        Span span = tracer.createSpan("/rules");
        try {
            rulesRepository.saveOrUpdateRule(ruleDefinition);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } finally {
            tracer.close(span);
        }
    }

    @GetMapping("/rules/{family}")
    List<?> rulesForFamily(@PathVariable("family") final RuleFamilyType ruleFamilyType) {
        Span span = tracer.createSpan("/rules/{family}");
        try {
            return rulesRepository.rulesFor(ruleFamilyType);
        } finally {
            tracer.close(span);
        }
    }

    @PostMapping("/ruleresult/{family}")
    RuleExecutionContext ruleresult(@PathVariable("family") final RuleFamilyType ruleFamilyType, @RequestBody Map payload) {
        Span span = tracer.createSpan("/ruleresult/{family}");
        try {
            // Set up the context per transaction.
            RuleExecutionContext<Map> context = new RuleExecutionContext<>();
            context.setPayload(payload);
            context.setRuleFamilyType(ruleFamilyType);
            RulesEngine ruleEngine = ruleEngineFactory.ruleEngineFor(context);
            ruleEngine.fireRules();
            return context;
        } finally {
            tracer.close(span);
        }
    }

    @Service
    class RuleEngineFactory {

        @Autowired
        private Tracer tracer;

        @Autowired
        private RulesRepository rulesRepository;

        public RulesEngine ruleEngineFor(final RuleExecutionContext<? extends Object> ruleExecutionContext) {
            Span span = tracer.createSpan("RuleEngineFactory::ruleEngineFor");

            try {
                RulesEngine rulesEngine = aNewRulesEngine()
                        .named("Rule Engine: " + ruleExecutionContext.getRuleFamilyType())
                        .withSilentMode(true)
                        .build();

                rulesRepository
                        .rulesFor(ruleExecutionContext.getRuleFamilyType())
                        .stream()
                        .map(rd -> {
                            rd.setRuleExecutionContext(ruleExecutionContext);
                            return rd;
                        })
                        .forEach(rulesEngine::registerRule);
                return rulesEngine;
            } finally {
                tracer.close(span);
            }
        }
    }

    @Repository
    class RulesRepository {

        private final JdbcTemplate jdbcTemplate;

        @Autowired
        private Tracer tracer;

        @Autowired
        RulesRepository(final JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
        }

        List<RuleDefinition> allRules() {
            Span span = tracer.createSpan("RulesRepository::allRules");
            try {
                String sql = "select * from RuleDefs";
                return jdbcTemplate.query(sql, (rs, rowNum) -> {
                    RuleDefinition ruleDefinition = new RuleDefinition(rs.getString("name"),
                            rs.getString("description"),
                            RuleFamilyType.valueOf(rs.getString("family")),
                            rs.getString("evaluationCondition"),
                            rs.getString("executionAction"),
                            null,
                            rs.getInt("priority"),
                            rs.getString("shortCircuit").equalsIgnoreCase("y"));
                    ruleDefinition.setActive(true);
                    return ruleDefinition;
                });
            } finally {
                tracer.close(span);
            }
        }

        int saveOrUpdateRule(final RuleDefinition ruleDefinition) {
            Span span = tracer.createSpan("RulesRepository::allRules");
            try {
                // short cut. bad practice. delete and insert
                List existing = jdbcTemplate.queryForList("select * from RuleDefs where name=?", ruleDefinition.getName());
                System.out.println(existing);
                if (existing == null || existing.isEmpty()) {
                    String sql = "insert into RuleDefs (name, description, family, evaluationCondition, executionAction, active, priority, shortcircuit) values(?,?,?,?,?,?,?,?)";
                    int update = jdbcTemplate.update(sql, ruleDefinition.getName(), ruleDefinition.getDescription(), ruleDefinition.getFamily().toString(), ruleDefinition.getWhen(), ruleDefinition.getThen(), ruleDefinition.isActive() ? "Y" : "N", ruleDefinition.getPriority(), ruleDefinition.isShortCircuit() ? "Y" : "N");
                    System.out.println("Rows inserted: " + update);
                    return update;
                } else {
                    String sql = "update RuleDefs set description=?, family=?, evaluationCondition=?, executionAction=?, active=?, priority=?, shortcircuit=? where name=?";
                    int update = jdbcTemplate.update(sql, ruleDefinition.getDescription(), ruleDefinition.getFamily().toString(), ruleDefinition.getWhen(), ruleDefinition.getThen(), ruleDefinition.isActive() ? "Y" : "N", ruleDefinition.getPriority(), ruleDefinition.isShortCircuit() ? "Y" : "N", ruleDefinition.getName());
                    System.out.println("Rows updated: " + update);
                    return update;
                }
            } finally {
                tracer.close(span);
            }
        }


        List<RuleDefinition> rulesFor(final RuleFamilyType ruleFamilyType) {
            Span span = tracer.createSpan("RulesRepository::rulesFor");
            try {
                String sql = "select * from RuleDefs where family=?";
                return jdbcTemplate.query(sql, new Object[]{ruleFamilyType.toString()}, (rs, rowNum) -> {
                    RuleDefinition ruleDefinition = new RuleDefinition(rs.getString("name"),
                            rs.getString("description"),
                            RuleFamilyType.valueOf(rs.getString("family")),
                            rs.getString("evaluationCondition"),
                            rs.getString("executionAction"),
                            null,
                            rs.getInt("priority"),
                            rs.getString("shortCircuit").equalsIgnoreCase("y"));
                    return ruleDefinition;
                });
            } finally {
                tracer.close(span);
            }
        }
    }
}
