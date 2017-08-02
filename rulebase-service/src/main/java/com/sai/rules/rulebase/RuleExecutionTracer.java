package com.sai.rules.rulebase;

import org.easyrules.api.Rule;
import org.easyrules.api.RuleListener;

/**
 * Created by saipkri on 02/08/17.
 */
public class RuleExecutionTracer implements RuleListener {

    @Override
    public boolean beforeEvaluate(Rule rule) {
        return false;
    }

    @Override
    public void beforeExecute(Rule rule) {

    }

    @Override
    public void onSuccess(Rule rule) {

    }

    @Override
    public void onFailure(Rule rule, Exception e) {

    }
}
