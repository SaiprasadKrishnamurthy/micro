package com.sai.rulebase.entity;

import com.sai.rules.rulebase.RuleFamilyType;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by saipkri on 16/08/17.
 */
@Entity
@Data
public class Rule {
    @Id
    private Long id;
    @Column
    private String name;
    @Column
    private String description;
    @Column
    private String when;
    @Column
    private String then;
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
