package com.sai.rulebase.model;

import com.sai.rulebase.entity.Rule;
import com.sai.rulebase.entity.YesNoType;
import com.sai.rules.rulebase.RuleExecutionContext;
import com.sai.rules.rulebase.SpelUtils;
import org.springframework.util.StopWatch;

/**
 * Created by saipkri on 18/08/17.
 */
public class DefaultRuleExecutor implements RuleExecutor {

    private final Rule rule;
    private final StopWatch stopWatch = new StopWatch();

    public DefaultRuleExecutor(Rule rule) {
        this.rule = rule;
    }

    @Override
    public boolean evaluate(final RuleExecutionContext<?> ruleExecutionContext) {
        stopWatch.start();
        return !ruleExecutionContext.isShortCircuited() && SpelUtils.eval(ruleExecutionContext, rule.getEvaluationCondition());
    }

    @Override
    public void execute(final RuleExecutionContext<?> ruleExecutionContext) {
        ruleExecutionContext.getRulesExecutedChain().add(rule);
        SpelUtils.execute(ruleExecutionContext, rule.getExecutionAction());
        ruleExecutionContext.setShortCircuited(rule.getShortCircuit().equals(YesNoType.Y));
        stopWatch.stop();
        ruleExecutionContext.getRuleExecutionTimingsInMillis().put(rule.getName(), stopWatch.getTotalTimeMillis());
    }
}
