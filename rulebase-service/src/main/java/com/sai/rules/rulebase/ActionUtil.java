package com.sai.rules.rulebase;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by saipkri on 02/08/17.
 */
public class ActionUtil {

    @RuleLibrary(documentation = "Simple Print to the console")
    public static void print(final Object obj) {
        System.out.println("\t Performing action now.." + obj);
    }

    @RuleLibrary(documentation = "Function that initiates the Risk Assessment by calling various sources")
    public static void initiateRiskAssessment(final RuleExecutionContext ruleExecutionContext, final List<String> watchlists, final List<String> profiles) {
        System.out.println("\t Performing Risk assessment using watchlists: " + watchlists + " and Profiles: " + profiles);
        ruleExecutionContext.getStateVariables().put("WatchlistResponse", "<WatchlistResponse />");
        ruleExecutionContext.getStateVariables().put("ProfilerResponse", "<ProfilerResponse />");
    }

    @RuleLibrary(documentation = "Records the rule that's matched")
    public static void recordMatch(final RuleExecutionContext ruleExecutionContext) {
        ruleExecutionContext.getStateVariables()
                .compute("RECORDED_MATCHES",
                        (k, v) -> (v == null) ? new ArrayList<>() : Lists.newArrayList(ruleExecutionContext.getRulesExecutedChain().get(ruleExecutionContext.getRulesExecutedChain().size() - 1)));
    }
}
