package com.sai.rulebase.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by saipkri on 17/08/17.
 */
@Entity
@Data
public class RuleFlowEdge {

    @Id
    private Long id;

    @Column
    private String ruleNameFrom;

    @Column
    private String ruleNameTo;

}
