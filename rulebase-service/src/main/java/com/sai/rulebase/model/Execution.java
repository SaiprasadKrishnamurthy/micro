package com.sai.rulebase.model;

import lombok.Data;

/**
 * Created by saipkri on 02/08/17.
 */
@Data
public class Execution {
    private String ruleId;
    private boolean terminateWhenMatched;
    private int timeoutInSeconds = -1;
    private String timeOutHandler;
}
