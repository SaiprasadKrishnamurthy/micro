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
    @Column(name = "EVALUATIONCONDITION")
    private String evaluationCondition;
    @Column(name = "EXECUTIONACTION")
    private String executionAction;
    @Column
    private int priority;
    @Column(name = "SHORTCIRCUIT")
    private boolean shortCircuit;
    @Column
    private boolean active;
    @Column
    private RuleFamilyType family;
    @Column(name = "ABORTONERROR")
    private boolean abortOnError = true;
}
