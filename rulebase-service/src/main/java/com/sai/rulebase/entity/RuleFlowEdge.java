package com.sai.rulebase.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * Created by saipkri on 17/08/17.
 */
//@Entity
@Data
public class RuleFlowEdge {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String ruleNameFrom;

    @Column
    private String ruleNameTo;

}
