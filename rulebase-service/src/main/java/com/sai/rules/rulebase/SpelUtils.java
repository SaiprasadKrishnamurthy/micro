package com.sai.rules.rulebase;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * Created by saipkri on 02/08/17.
 */
public class SpelUtils {

    private SpelUtils() {
    }

    public static boolean eval(final RuleExecutionContext ruleExecutionContext, final String spelExpression) {
        StandardEvaluationContext simpleContext = new StandardEvaluationContext(ruleExecutionContext);
        simpleContext.setVariable("ctx", ruleExecutionContext);
        ExpressionParser parser = new SpelExpressionParser();
        return (Boolean) parser.parseExpression(spelExpression).getValue(simpleContext);
    }

    public static void execute(final RuleExecutionContext ruleExecutionContext, final String spelExpression) {
        StandardEvaluationContext simpleContext = new StandardEvaluationContext(ruleExecutionContext);
        simpleContext.setVariable("ctx", ruleExecutionContext);
        ExpressionParser parser = new SpelExpressionParser();
        parser.parseExpression(spelExpression).getValue(simpleContext);
    }
}
