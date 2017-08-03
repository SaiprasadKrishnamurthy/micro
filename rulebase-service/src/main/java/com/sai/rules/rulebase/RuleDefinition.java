package com.sai.rules.rulebase;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.easyrules.core.BasicRule;
import org.springframework.util.StopWatch;

@Data
@NoArgsConstructor
@Setter
public class RuleDefinition extends BasicRule {

    private RuleFamilyType family;

    @JsonIgnore
    private RuleExecutionContext ruleExecutionContext;
    private String when;
    private String then;
    private int priority;
    private boolean shortCircuit;
    private boolean active;
    @JsonIgnore
    private final StopWatch stopWatch = new StopWatch();

    RuleDefinition(final String name, final String description, final RuleFamilyType family, final String when, final String then, final RuleExecutionContext ruleExecutionContext, int priority, boolean shortCircuit) {
        super(name, description);
        this.family = family;
        this.priority = priority;
        this.shortCircuit = shortCircuit;
        this.description = description;
        this.ruleExecutionContext = ruleExecutionContext;
        this.when = when;
        this.then = then;
    }

    @Override
    public boolean evaluate() {
        stopWatch.start();
        return !ruleExecutionContext.isShortCircuited() && SpelUtils.eval(ruleExecutionContext, when);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void execute() {
        ruleExecutionContext.getRulesExecutedChain().add(this);
        System.out.println("B4 spel --- "+then);
        SpelUtils.execute(ruleExecutionContext, then);
        System.out.println("After spel --- "+then);
        ruleExecutionContext.setShortCircuited(shortCircuit);
        stopWatch.stop();
        ruleExecutionContext.getRuleExecutionTimingsInMillis().put(this.name, stopWatch.getTotalTimeMillis());
    }
}