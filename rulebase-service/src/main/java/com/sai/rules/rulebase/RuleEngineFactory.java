package com.sai.rules.rulebase;

import org.easyrules.api.RulesEngine;

import static org.easyrules.core.RulesEngineBuilder.aNewRulesEngine;

/**
 * Created by saipkri on 02/08/17.
 */
public class RuleEngineFactory {


    public RulesEngine ruleEngineFor(final RuleExecutionContext<? extends Object> ruleExecutionContext) {
        RulesEngine rulesEngine = aNewRulesEngine()
                .named("Rule Engine: " + ruleExecutionContext.getRuleFamilyType())
                .withSilentMode(true)
                .build();

        // TODO LOOK UP FROM DB.
        RuleDefinition evenNoRule = new RuleDefinition("Rule1", "My fancy rule 1", RuleFamilyType.RISK_RULE, "payload % 2 == 0", "T(com.sai.rules.rulebase.ActionUtil).print(#ctx)", ruleExecutionContext, 1, false);
        RuleDefinition greaterThan10Rule = new RuleDefinition("Rule2", "My fancy rule 2", RuleFamilyType.RISK_RULE, "payload > 0", "T(com.sai.rules.rulebase.ActionUtil).print(rulesExecutedChain)", ruleExecutionContext, 2, false);

        rulesEngine.registerRule(evenNoRule);
        rulesEngine.registerRule(greaterThan10Rule);
        return rulesEngine;
    }
}
