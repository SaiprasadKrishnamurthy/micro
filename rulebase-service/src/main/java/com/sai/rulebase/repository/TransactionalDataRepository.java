package com.sai.rulebase.repository;

import com.sai.rulebase.entity.Rule;
import com.sai.rulebase.entity.RuleFlow;
import com.sai.rulebase.entity.RuleFlowEdge;
import com.sai.rules.rulebase.RuleExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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
    }

    public RuleExecutionContext<?> contextFor(final String transactionId) {
        return contexts.get(transactionId);
    }

    public List<Rule> nextRulesFor(final String transactionId, final String ruleName) {
        List<RuleFlowEdge> edges = ruleFlowEdges.get(transactionId);
        return edges.stream()
                .filter(edge -> edge.getRuleNameFrom().equals(ruleName))
                .map(RuleFlowEdge::getRuleNameTo)
                .map(ruleRepository::findByName)
                .collect(Collectors.toList());
    }

    public Rule ruleFor(final String transactionId, final String ruleName) {
        return ruleRepository.findByName(ruleName);
    }
}
