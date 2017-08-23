package com.sai.rulebase.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sai.rulebase.entity.Rule;
import com.sai.rulebase.entity.RuleAudit;
import com.sai.rulebase.entity.RuleFlowEdgeSnapshot;
import com.sai.rulebase.entity.RuleSnapshot;
import com.sai.rulebase.model.RuleFunction;
import com.sai.rulebase.model.RuleLibraryInfo;
import com.sai.rulebase.repository.*;
import com.sai.rulebase.vertx.Bootstrap;
import com.sai.rulebase.vertx.RuleExecutorVerticle;
import com.sai.rules.rulebase.RuleExecutionContext;
import com.sai.rules.rulebase.RuleLibrary;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by saipkri on 18/08/17.
 */
@Api("Rule execution flow")
@RestController
@RefreshScope
public class RuleExecApi {

    @Autowired
    private Bootstrap bootstrap;

    @Autowired
    private TransactionalDataRepository transactionalDataRepository;

    @Autowired
    private RuleFunctionsRepository ruleFunctionsRepository;

    @Autowired
    private RuleAuditRepository ruleAuditRepository;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private RuleFlowRepository ruleFlowRepository;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @PostMapping("/ruleresult/{flowName}")
    public DeferredResult<?> ruleresult(@PathVariable("flowName") final String flowName, final @RequestBody Map payload, @RequestParam(required = false, defaultValue = "false") final boolean debug) throws Exception {
        DeferredResult<Object> response = new DeferredResult<>();
        RuleExecutionContext<?> ruleExecutionContext = RuleExecutionContext.newContext(payload);
        transactionalDataRepository.setup(ruleExecutionContext, flowName);

        // First rule in the flow.
        Rule first = transactionalDataRepository.firstRule(ruleExecutionContext);
        bootstrap.getVertx().eventBus().send(RuleExecutorVerticle.class.getName(), ruleExecutionContext.getId() + "|" + first.getName());

        // React to the response.
        bootstrap.getVertx().eventBus().consumer("DONE|" + ruleExecutionContext.getId(), msg -> {
            response.setResult(transactionalDataRepository.responseFor(ruleExecutionContext));
            // Audit the flow.
            RuleAudit ruleAudit = new RuleAudit();
            ruleAudit.setFlowName(flowName);
            ruleAudit.setRuleErrors(ruleExecutionContext.getErroredRules());
            ruleAudit.setRuleExecTime(ruleExecutionContext.getRuleExecutionTimingsInMillis());
            ruleAudit.setTimestamp(System.currentTimeMillis());
            Set<RuleFlowEdgeSnapshot> snapshots = ruleExecutionContext.getRuleFlow().getEdges().stream()
                    .map(rfe -> {
                        RuleFlowEdgeSnapshot edgeSnapshot = new RuleFlowEdgeSnapshot();
                        edgeSnapshot.setRuleNameFrom(rfe.getRuleNameFrom());
                        edgeSnapshot.setRuleNameTo(rfe.getRuleNameTo());
                        return edgeSnapshot;
                    }).collect(Collectors.toSet());
            ruleAudit.setEdges(snapshots);
            ruleAudit.setTransactionId(ruleExecutionContext.getId());

            ruleAudit.setRules(ruleExecutionContext.getRulesExecutedChain().stream()
                    .map(rule -> {
                        RuleSnapshot ruleSnapshot = new RuleSnapshot();
                        ruleSnapshot.setName(rule.getName());
                        ruleSnapshot.setDescription(rule.getDescription());
                        ruleSnapshot.setFamily(rule.getFamily());
                        ruleSnapshot.setEvaluationCondition(rule.getEvaluationCondition());
                        ruleSnapshot.setExecutionAction(rule.getExecutionAction());
                        ruleSnapshot.setShortCircuit(rule.getShortCircuit());
                        return ruleSnapshot;
                    }).collect(Collectors.toSet()));

            ruleAudit.setTotalTimeTakenInMillis(ruleExecutionContext.getRuleExecutionTimingsInMillis().values().stream().reduce(0L, (a, b) -> a + b));
            try {
                bootstrap.getVertx().eventBus().send("AUDIT", OBJECT_MAPPER.writeValueAsString(ruleAudit));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
        return response;
    }

    @GetMapping("/rulelibrary")
    public List<?> rulelibraries() {
        return ruleFunctionsRepository.getRuleLibraryHolders().stream()
                .map(ruleLibraryHolder -> {
                    String clazz = ruleLibraryHolder.getName();
                    List<com.sai.rulebase.model.RuleFunction> ruleFunctions = ruleLibraryHolder.getMethods().stream()
                            .filter(m -> m.getDeclaredAnnotations().length > 0)
                            .map(m -> new RuleFunction(m.getName(), m.getAnnotation(RuleLibrary.class).documentation(), Stream.of(m.getParameterTypes()).map(Class::getName).collect(Collectors.toList())))
                            .collect(Collectors.toList());
                    return new RuleLibraryInfo(clazz, "", ruleFunctions);
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/ruleaudits")
    public Iterable<?> ruleAudits() {
        return ruleAuditRepository.findAllByOrderByTimestampDesc();
    }

    @GetMapping("/ruleflows")
    public Iterable<?> ruleflows() {
        return ruleFlowRepository.findAll();
    }

    @GetMapping("/rules")
    public Iterable<?> rules() {
        return ruleRepository.findAll();
    }
}
