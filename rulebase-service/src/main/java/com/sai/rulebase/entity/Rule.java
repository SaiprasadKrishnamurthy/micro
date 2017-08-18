package com.sai.rulebase.entity;

import com.sai.rules.rulebase.RuleFamilyType;
import lombok.Data;

import javax.persistence.*;

/**
 * Created by saipkri on 16/08/17.
 */
@Entity
@Data
public class Rule {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column
    private String name;
    @Column
    private String description;
    @Column
    private String evaluationCondition;
    @Column
    private String executionAction;
    @Column
    private int priority;
    @Column
    private boolean shortCircuit;
    @Column
    private boolean active;
    @Column
    private RuleFamilyType family;
    @Column
    private boolean abortOnError = true;
}
