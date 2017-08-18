package com.sai.rulebase.repository;

import com.sai.rulebase.entity.RuleFlow;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by saipkri on 17/08/17.
 */
public interface RuleFlowRepository extends CrudRepository<RuleFlow, Long> {
    RuleFlow findByName(String name);
}
