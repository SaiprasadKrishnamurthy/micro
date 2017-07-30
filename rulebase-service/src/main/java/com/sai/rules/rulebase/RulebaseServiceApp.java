package com.sai.rules.rulebase;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@EnableDiscoveryClient
@EnableAutoConfiguration
@SpringBootApplication
@EnableFeignClients
@EnableJms
public class RulebaseServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(RulebaseServiceApp.class, args);
    }
}

@Configuration
class RulebaseConfiguration {

    @LoadBalanced
    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

@RestController
@RefreshScope
class FlightRoutesRestAPI {

    private static final Logger LOG = Logger.getLogger(FlightRoutesRestAPI.class.getName());

    @Autowired
    private RestTemplate restTemplate;

    @Value("${popular.routes}")
    private String popularRoutes;

    @Autowired
    private Tracer tracer;

    @GetMapping("/popular-routes")
    public List<?> popularRoutes() throws Exception {
        Span span = tracer.createSpan("/popular-routes");
        try {
            String[] popularRoutesPair = popularRoutes.split(",");
            return Stream.of(popularRoutesPair)
                    .map(pair -> getRoutes(pair))
                    .map(HttpEntity::getBody)
                    .collect(Collectors.toList());
        } finally {
            tracer.close(span);
        }
    }

    private ResponseEntity<List> getRoutes(final String pair) {
        return this.restTemplate.exchange(
                "http://refdata-service/flights/" + pair.split("-")[0] + "/" + pair.split("-")[1],
                HttpMethod.GET,
                null,
                List.class);
    }


    @GetMapping("/flights/{origin}/{destination}")
    List<?> flightsForRoute(@PathVariable("origin") final String origin,
                            @PathVariable("destination") final String destination) {
        Span span = tracer.createSpan("/flights/{origin}/{destination}");
        try {
            return getRoutes(origin + "-" + destination).getBody();
        } finally {
            tracer.close(span);
        }
    }
}

@Service
class PreclearanceMessageListener {

    @JmsListener(destination = "PreClearance")
    void onMessage(final Message msg) throws Exception{
        msg.acknowledge();
        String m = ((TextMessage)msg).getText();
        System.out.println(" ---- "+m);
    }



}