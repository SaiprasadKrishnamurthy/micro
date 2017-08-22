package com.sai.rulebase.repository;

import com.sai.rulebase.entity.Rule;
import com.sai.rulebase.entity.RuleFlow;
import com.sai.rulebase.entity.RuleFlowEdge;
import com.sai.rules.rulebase.RuleExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class TransactionalDataRepository {
    private ConcurrentHashMap<String, List<RuleFlowEdge>> ruleFlowEdges = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, RuleExecutionContext<?>> contexts = new ConcurrentHashMap<>();

    private final RuleRepository ruleRepository;
    private final RuleFlowRepository ruleFlowRepository;

    @Autowired
    public TransactionalDataRepository(final RuleFlowRepository ruleFlowRepository, final RuleRepository ruleRepository) {
        this.ruleFlowRepository = ruleFlowRepository;
        this.ruleRepository = ruleRepository;
    }

    public void setup(final RuleExecutionContext<?> ruleExecutionContext, final String ruleFlowName) {
        RuleFlow ruleFlow = ruleFlowRepository.findByName(ruleFlowName);
        ruleFlowEdges.putIfAbsent(ruleExecutionContext.getId(), ruleFlow.getEdges());
        contexts.putIfAbsent(ruleExecutionContext.getId(), ruleExecutionContext);
        ruleExecutionContext.setRuleFlow(ruleFlow);
    }

    public Rule firstRule(final RuleExecutionContext<?> ruleExecutionContext) {
        List<RuleFlowEdge> edges = ruleFlowEdges.get(ruleExecutionContext.getId());
        return ruleRepository.findByName(edges.get(0).getRuleNameFrom());
    }

    public RuleExecutionContext<?> contextFor(final String transactionId) {
        return contexts.get(transactionId);
    }

    public List<Rule> nextRulesFor(final String transactionId, final String ruleName) {
        List<RuleFlowEdge> edges = ruleFlowEdges.get(transactionId);
        if (edges == null) {
            return Collections.emptyList();
        }
        return edges.stream()
                .filter(edge -> edge.getRuleNameFrom().equals(ruleName))
                .filter(edge -> edge.getRuleNameTo() != null)
                .map(RuleFlowEdge::getRuleNameTo)
                .map(ruleRepository::findByName)
                .collect(Collectors.toList());
    }

    public Rule ruleFor(final String ruleName) {
        return ruleRepository.findByName(ruleName);
    }

    public void clear(final String transactionId) {
        ruleFlowEdges.remove(transactionId);
        contexts.remove(transactionId);
    }
}
