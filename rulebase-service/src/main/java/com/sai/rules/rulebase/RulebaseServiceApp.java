package com.sai.rules.rulebase;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.easyrules.api.RulesEngine;
import org.jooq.lambda.Unchecked;
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
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
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
@Data
class RulebaseConfiguration {

    public static List<Method> LIB_METHODS = new ArrayList<>();

    @Value("${ruleLibraryBasePkgs}")
    private String ruleLibraryBasePkgs;

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

@AllArgsConstructor
@Data
@NoArgsConstructor
class RuleLibraryHolder {
    private Class<?> clazz;
    private String name;
    private Object instance;
    private List<Method> methods;
}

@AllArgsConstructor
@Data
@NoArgsConstructor
class RuleFunction {
    private String name;
    private String documentation;
    private List<String> argTypes;
}

@AllArgsConstructor
@Data
@NoArgsConstructor
class RuleLibraryInfo {
    private String clazz;
    private String clazzDocumentation;
    private List<RuleFunction> functions;
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

    @GetMapping("/rulelibrary")
    List<?> rulelibraries() {
        Span span = tracer.createSpan("rulelibrary");
        try {
            return rulesRepository.getRuleLibraryHolders().stream()
                    .map(ruleLibraryHolder -> {
                        String clazz = ruleLibraryHolder.getName();
                        List<RuleFunction> ruleFunctions = ruleLibraryHolder.getMethods().stream()
                                .filter(m -> m.getDeclaredAnnotations().length > 0)
                                .map(m -> new RuleFunction(m.getName(), m.getAnnotation(RuleLibrary.class).documentation(), Stream.of(m.getParameterTypes()).map(Class::getName).collect(Collectors.toList())))
                                .collect(Collectors.toList());
                        return new RuleLibraryInfo(clazz, "", ruleFunctions);
                    })
                    .collect(Collectors.toList());
        } finally {
            tracer.close(span);
        }
    }

    @GetMapping("/ruleaudits")
    List<?> ruleAudits() {
        Span span = tracer.createSpan("/rules/{family}");
        try {
            return rulesRepository.getTraces();
        } finally {
            tracer.close(span);
        }
    }

    @PostMapping("/ruleresult/{family}")
    RuleExecutionContext ruleresult(@PathVariable("family") final RuleFamilyType ruleFamilyType, @RequestBody Map payload, @RequestParam(value = "trace", defaultValue = "true") boolean trace) {
        Span span = tracer.createSpan("/ruleresult/{family}");
        try {
            // Set up the context per transaction.
            RuleExecutionContext<Map> context = new RuleExecutionContext<>();
            context.setPayload(payload);
            context.setRuleFamilyType(ruleFamilyType);
            RulesEngine ruleEngine = ruleEngineFactory.ruleEngineFor(context);
            ruleEngine.fireRules();
            if (trace) {
                rulesRepository.saveTrace(context);
            }
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
            // Should be moved to AOP and can be supplemented with annotations (as an optional item).
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

        private final Tracer tracer;

        private final RulebaseConfiguration rulebaseConfiguration;
        private List<RuleLibraryHolder> ruleLibraryHolders;


        @Autowired
        RulesRepository(final JdbcTemplate jdbcTemplate, final Tracer tracer, final RulebaseConfiguration rulebaseConfiguration) {
            this.jdbcTemplate = jdbcTemplate;
            this.tracer = tracer;
            this.rulebaseConfiguration = rulebaseConfiguration;
            loadRuleLibraries();
        }

        private void loadRuleLibraries() {
            String[] rulebaseBasePkg = rulebaseConfiguration.getRuleLibraryBasePkgs().split(",");
            ClassPathScanningCandidateComponentProvider scanner =
                    new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new AnnotationTypeFilter(RuleLibrary.class));
            ruleLibraryHolders = Stream.of(rulebaseBasePkg)
                    .flatMap(pkg -> scanner.findCandidateComponents(pkg).stream())
                    .map(bd -> Unchecked.function(dontCare -> {
                        Class<?> clazz = Class.forName(bd.getBeanClassName());
                        return new RuleLibraryHolder(clazz, StringUtils.uncapitalize(clazz.getSimpleName()), clazz.newInstance(), Arrays.asList(clazz.getDeclaredMethods()));
                    }))
                    .map(f -> f.apply(null))
                    .map(ruleLibraryHolder -> {
                        RulebaseConfiguration.LIB_METHODS.addAll(ruleLibraryHolder.getMethods());
                        return ruleLibraryHolder;
                    })
                    .collect(Collectors.toList());
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

        public List<RuleLibraryHolder> getRuleLibraryHolders() {
            return this.ruleLibraryHolders;
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
                String sql = "select * from RuleDefs where family=? order by priority";
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

        @Async
        public void saveTrace(final RuleExecutionContext<Map> context) {
            StringBuilder out = new StringBuilder();
            for (int i = 0; i < context.getRulesExecutedChain().size() - 1; i++) {
                RuleDefinition curr = context.getRulesExecutedChain().get(i);
                RuleDefinition next = context.getRulesExecutedChain().get(i + 1);
                out.append(curr.getName())
                        .append(" (")
                        .append(context.getRuleExecutionTimingsInMillis().get(curr.getName())).append(" ms) ")
                        .append("->")
                        .append(next.getName())
                        .append(" (")
                        .append(context.getRuleExecutionTimingsInMillis().get(next.getName())).append(" ms) ")
                        .append(":NEXT RULE")
                        .append("\n");
            }
            String sql = "insert into RuleAudits(id, family, traceText, created) values (?,?,?,?)";
            jdbcTemplate.update(sql, context.getId(), context.getRuleFamilyType().toString(), out.toString(), System.currentTimeMillis());
        }

        public List<Map<String, Object>> getTraces() {
            return jdbcTemplate.queryForList("select * from RuleAudits order by created desc");
        }

    }
}
