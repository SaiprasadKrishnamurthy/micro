package com.sai.rules.rulebase;

import lombok.Data;
import lombok.ToString;

import java.util.*;

/**
 * Created by saipkri on 02/08/17.
 */
@Data
@ToString(exclude = {"stateVariables", "rulesExecutedChain"})
public class RuleExecutionContext<T> {
    private RuleFamilyType ruleFamilyType;
    private String id = UUID.randomUUID().toString();
    private T payload;
    private Map<String, Object> stateVariables = new HashMap<>();
    private List<RuleDefinition> rulesExecutedChain = new ArrayList<>();
}
