package com.sai.rulebase.rest;

import com.sai.rulebase.repository.RuleAuditRepository;
import com.sai.rulebase.repository.RulePerfStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.util.*;

@Controller
public class RuleStatsEventsController {

    private final SimpMessagingTemplate template;

    private final RuleAuditRepository ruleAuditRepository;

    private String recentTransaction;


    @Autowired
    public RuleStatsEventsController(final SimpMessagingTemplate template, final RuleAuditRepository ruleAuditRepository) {
        this.template = template;
        this.ruleAuditRepository = ruleAuditRepository;
    }


    @MessageMapping("/hello")
    @SendTo("/topic/rulestats")
    public Map preclearanceEvents() throws Exception {
        System.out.println("Started...");
        return new HashMap();
    }

    @Scheduled(fixedRate = 100)
    public void push() throws Exception {
        Map<String, Long> timings = new LinkedHashMap<>();
        List<RulePerfStats> allByOrderByTimestampDesc = ruleAuditRepository.findTop10ByOrderByTimestampDesc();
        if (!allByOrderByTimestampDesc.isEmpty()) {
            if (!allByOrderByTimestampDesc.get(0).getTransactionId().equals(recentTransaction)) {
                Collections.reverse(allByOrderByTimestampDesc);
                allByOrderByTimestampDesc
                        .forEach(ra -> timings.put(ra.getTransactionId(), ra.getTotalTimeTakenInMillis()));
                template.convertAndSend("/topic/rulestats", allByOrderByTimestampDesc);
                recentTransaction = allByOrderByTimestampDesc.get(0).getTransactionId();
            }
        }
    }
}