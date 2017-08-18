package com.sai.rulebase.model;

import com.sai.rules.rulebase.RuleExecutionContext;

/**
 * Created by saipkri on 18/08/17.
 */
public interface RuleExecutor {
    boolean evaluate(RuleExecutionContext<?> ruleExecutionContext);

    void execute(RuleExecutionContext<?> ruleExecutionContext);
}
