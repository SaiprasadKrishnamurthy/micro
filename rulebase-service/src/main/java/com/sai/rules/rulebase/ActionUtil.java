package com.sai.rules.rulebase;

import java.util.List;

/**
 * Created by saipkri on 02/08/17.
 */
public class ActionUtil {

    public static void print(final Object obj) {
        System.out.println("\t Performing action now.."+obj);
    }

    public static void initiateRiskAssessment(final RuleExecutionContext ruleExecutionContext, final List<String> watchlists, final List<String> profiles) {
        System.out.println("\t Performing Risk assessment using watchlists: "+watchlists+" and Profiles: "+profiles);
        ruleExecutionContext.getStateVariables().put("WatchlistResponse", "<WatchlistResponse />");
        ruleExecutionContext.getStateVariables().put("ProfilerResponse", "<ProfilerResponse />");
    }
}
