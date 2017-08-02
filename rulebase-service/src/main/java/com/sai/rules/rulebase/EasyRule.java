package com.sai.rules.rulebase;

import org.easyrules.api.RulesEngine;

/**
 * Created by saipkri on 02/08/17.
 */
public class EasyRule {

    public static void main(String[] args) {
        RuleEngineFactory ruleEngineFactory = new RuleEngineFactory();

        // Set up the context per transaction.
        RuleExecutionContext<Integer> context = new RuleExecutionContext<>();
        context.setPayload(2);

        RulesEngine rulesEngine = ruleEngineFactory.ruleEngineFor(context);
        rulesEngine.fireRules();

        System.out.println(context);
    }
}
