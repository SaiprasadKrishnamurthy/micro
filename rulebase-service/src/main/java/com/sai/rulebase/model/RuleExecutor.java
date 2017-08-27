package com.sai.rulebase.model;

import com.sai.rulebase.entity.Rule;
import com.sai.rules.rulebase.RuleExecutionContext;

/**
 * Created by saipkri on 18/08/17.
 */
public interface RuleExecutor {
    boolean evaluate(Rule rule, RuleExecutionContext<?> ruleExecutionContext);

    void execute(Rule rule, RuleExecutionContext<?> ruleExecutionContext) throws Exception;
}
