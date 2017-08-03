package com.sai.rules.rulebase;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.easyrules.core.BasicRule;

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
        return !ruleExecutionContext.isShortCircuited() && SpelUtils.eval(ruleExecutionContext, when);
    }

    @Override
    public void execute() {
        ruleExecutionContext.getRulesExecutedChain().add(this);
        SpelUtils.execute(ruleExecutionContext, then);
        ruleExecutionContext.setShortCircuited(shortCircuit);
    }
}