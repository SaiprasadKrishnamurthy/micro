package com.sai.rules.rulebase;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.Setter;
import org.easyrules.core.BasicRule;

@Data
@Setter
public class RuleDefinition extends BasicRule {

    private final RuleFamilyType family;

    @JsonIgnore
    private RuleExecutionContext ruleExecutionContext;
    private final String when;
    private final String then;
    private final int priority;
    private final boolean shortCircuit;

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
        return SpelUtils.eval(ruleExecutionContext, when);
    }

    @Override
    public void execute() {
        ruleExecutionContext.getRulesExecutedChain().add(this);
        SpelUtils.execute(ruleExecutionContext, then);
    }
}