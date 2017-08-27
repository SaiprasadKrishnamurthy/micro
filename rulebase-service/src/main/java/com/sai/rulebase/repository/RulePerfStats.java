package com.sai.rulebase.repository;

public interface RulePerfStats {

    String getTransactionId();

    long getTotalTimeTakenInMillis();
}