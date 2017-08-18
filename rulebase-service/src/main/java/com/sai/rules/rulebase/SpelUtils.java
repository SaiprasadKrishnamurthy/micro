package com.sai.rules.rulebase;

/**
 * Created by saipkri on 02/08/17.
 */
public class SpelUtils {

    private SpelUtils() {
    }

    public static boolean eval(final RuleExecutionContext ruleExecutionContext, final String spelExpression) {
        /*// TODO do not construct every time.
        StandardEvaluationContext simpleContext = new StandardEvaluationContext(ruleExecutionContext);
        simpleContext.setVariable("ctx", ruleExecutionContext);
        RulebaseConfiguration.LIB_METHODS.forEach(m -> {
            System.out.println(m.getName() +" ----- ***** ");
            simpleContext.registerFunction(m.getName(), m);
        });
        ExpressionParser parser = new SpelExpressionParser();
        return (Boolean) parser.parseExpression(spelExpression).getValue(simpleContext);*/
        return false;
    }

    public static void execute(final RuleExecutionContext ruleExecutionContext, final String spelExpression) {
        /*// TODO do not construct every time.
        StandardEvaluationContext simpleContext = new StandardEvaluationContext(ruleExecutionContext);
        simpleContext.setVariable("ctx", ruleExecutionContext);
        RulebaseConfiguration.LIB_METHODS.forEach(m -> simpleContext.registerFunction(m.getName(), m));
        ExpressionParser parser = new SpelExpressionParser();
        parser.parseExpression(spelExpression).getValue(simpleContext);*/
    }
}
