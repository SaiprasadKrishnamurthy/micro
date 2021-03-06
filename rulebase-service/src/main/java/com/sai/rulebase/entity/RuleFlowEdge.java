package com.sai.rulebase.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * Created by saipkri on 17/08/17.
 */
@Entity
@Table(name = "RULEFLOWEDGE")
@Data
public class RuleFlowEdge {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "RULENAMEFROM")
    private String ruleNameFrom;

    @Column(name = "RULENAMETO")
    private String ruleNameTo;


}
