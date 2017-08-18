package com.sai.rules.rulebase;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by saipkri on 02/08/17.
 */
@RuleLibrary(documentation = "General set of utilities")
public class ActionUtil {

    @RuleLibrary(documentation = "Simple Print to the console")
    public static void print(final Object obj) {
        System.out.println("\t Performing action now.." + obj);
    }

    @RuleLibrary(documentation = "Function that initiates the Risk Assessment by calling various sources", ruleFamily = RuleFamilyType.RISK_RULE)
    public static void initiateRiskAssessment(final RuleExecutionContext ruleExecutionContext, final List<String> watchlists, final List<String> profiles) {
        System.out.println("\t Performing Risk assessment using watchlists: " + watchlists + " and Profiles: " + profiles);
        ruleExecutionContext.getStateVariables().put("WatchlistResponse", "<WatchlistResponse />");
        ruleExecutionContext.getStateVariables().put("ProfilerResponse", "<ProfilerResponse />");
    }
}
