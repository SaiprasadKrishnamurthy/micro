package com.sai.rulebase.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

/**
 * Created by saipkri on 16/08/17.
 */
@Entity
@Data
public class RuleFlow {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String name;

    @Column
    private String description;

    @OneToMany
    private List<RuleFlowEdge> edges;
}
