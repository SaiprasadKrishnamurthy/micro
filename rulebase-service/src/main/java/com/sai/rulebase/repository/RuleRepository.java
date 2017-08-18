package com.sai.rulebase.repository;

import com.sai.rulebase.entity.Rule;
import com.sai.rules.rulebase.RuleFamilyType;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by saipkri on 17/08/17.
 */
public interface RuleRepository extends CrudRepository<Rule, Long> {
    Rule findByName(String name);
    List<Rule> findByFamily(RuleFamilyType family);
}
