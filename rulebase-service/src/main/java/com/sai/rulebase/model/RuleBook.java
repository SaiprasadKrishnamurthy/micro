package com.sai.rulebase.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by saipkri on 02/08/17.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class RuleBook {
    @JsonProperty("id")
    private String id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("enabled")
    private boolean enabled;
    @JsonProperty("shouldApplyWhen")
    private String shouldApplyWhen;
    @JsonProperty("family")
    private String family;
    @JsonProperty("executions")
    private List<Execution> executions;
}
