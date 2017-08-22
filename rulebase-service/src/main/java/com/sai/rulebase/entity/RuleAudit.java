package com.sai.rulebase.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

/**
 * Created by saipkri on 16/08/17.
 */
@Entity
@Data
public class RuleAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String transactionId;

    @Column
    private long timestamp;

    @Column
    private String flowName;

    @Column
    private String flowDescription;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "RULEEXECTIME")
    @MapKeyColumn(name = "KEY")
    @Column(name = "VALUE")
    private Map<String, Long> ruleExecTime = new HashMap<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ERRORS")
    @MapKeyColumn(name = "KEY")
    @Column(name = "VALUE")
    private Map<String, String> ruleErrors = new HashMap<>();

    @Column
    private long totalTimeTakenInMillis;

    @JoinColumn(name = "EDGES")
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<RuleFlowEdgeSnapshot> edges;

    @Transient
    private String pipeline;

    @Transient
    private String displayId;

    @Transient
    private String labelType;

    @JoinColumn(name = "RULES")
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<RuleSnapshot> rules;

    @Data
    private static class RuleInfo {
        private String ruleName;
        private Long execTime;
        private String status;
    }

    @Transient
    private List<RuleInfo> ruleInfos = new ArrayList<>();

    public String getPipeline() {
        Set<String> allRules = new LinkedHashSet<>();
        StringBuilder out = new StringBuilder();
        out.append("graph LR").append("\n");
        edges.forEach(ruleFlowEdgeSnapshot -> {
            out.append(ruleFlowEdgeSnapshot.getRuleNameFrom()).append("[").append(ruleFlowEdgeSnapshot.getRuleNameFrom()).append("]");
            out.append("  ==>  ");
            out.append(ruleFlowEdgeSnapshot.getRuleNameTo()).append("[").append(ruleFlowEdgeSnapshot.getRuleNameTo()).append("]");
            out.append("\n");
            allRules.add(ruleFlowEdgeSnapshot.getRuleNameFrom());
            allRules.add(ruleFlowEdgeSnapshot.getRuleNameTo());

        });
        Set<String> notExecuted = allRules.stream()
                .filter(name -> rules.stream().noneMatch(r -> name.equals(r.getName())))
                .collect(Collectors.toSet());

        String notExecutedRules = notExecuted.stream()
                .collect(Collectors.joining(","));

        out.append("\n");
        out.append("classDef green fill:#9f6,stroke:#333,stroke-width:2px;").append("\n");
        out.append("classDef red fill:red,stroke:#333,stroke-width:4px;").append("\n");
        out.append("classDef orange fill:orange,stroke:#333,stroke-width:4px;").append("\n");
        String successRules = allRules.stream().filter(name -> !notExecuted.contains(name)).collect(joining(","));
        String errorRules = ruleErrors.keySet().stream().collect(joining(","));
        out.append("class ").append(successRules).append(" green ").append("\n");
        if (errorRules.trim().length() > 0) {
            out.append("class ").append(errorRules).append(" red ").append("\n");
        }
        return out.toString();
    }

    public String getDisplayId() {
        return "a" + transactionId;
    }

    public String getLabelType() {
        return !ruleErrors.isEmpty() ? "danger" : "info";
    }

    public List<RuleInfo> getRuleInfos() {
        ruleExecTime.entrySet().forEach(entry -> {
            RuleInfo ruleInfo = new RuleInfo();
            ruleInfo.setRuleName(entry.getKey());
            ruleInfo.setExecTime(entry.getValue());
            ruleInfo.setStatus(ruleErrors.containsKey(entry.getKey()) ? "ERROR" : "SUCCESS");
            ruleInfos.add(ruleInfo);
        });
        return ruleInfos;
    }
}
