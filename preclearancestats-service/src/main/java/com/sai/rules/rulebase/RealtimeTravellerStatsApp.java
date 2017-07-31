package com.sai.rules.rulebase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@EnableDiscoveryClient
@EnableAutoConfiguration
@SpringBootApplication
@EnableScheduling
public class RealtimeTravellerStatsApp {
    public static void main(String[] args) {
        SpringApplication.run(RealtimeTravellerStatsApp.class, args);
    }
}

@Configuration
@EnableWebSocketMessageBroker
class RealtimeTravellerStatsConfiguration extends AbstractWebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/gs-guide-websocket").withSockJS();
    }

    @Configuration
    class HazelcastConfiguration {
        @Bean
        HazelcastInstance instance() {
            Config config = new Config();
            config.getNetworkConfig().getJoin().getTcpIpConfig().addMember("localhost").setEnabled(true);
            config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
            return Hazelcast.newHazelcastInstance(config);
        }
    }
}

@Controller
class PreclearanceEventsController {

    private static final ObjectMapper m = new ObjectMapper();

    final ConcurrentHashMap<String, Integer> respo = new ConcurrentHashMap();

    private final SimpMessagingTemplate template;

    private final HazelcastInstance hazelcastInstance;

    @Autowired
    PreclearanceEventsController(HazelcastInstance hazelcastInstance, SimpMessagingTemplate template) {
        this.hazelcastInstance = hazelcastInstance;
        this.template = template;
        this.hazelcastInstance.getTopic("PreclearanceEvents")
                .addMessageListener(msg -> {
                    try {
                        Map r = m.readValue(msg.getMessageObject().toString(), Map.class);
                        respo.compute(r.get("name").toString(), (country, count) -> count == null ? 1 : count + 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }


    @MessageMapping("/hello")
    @SendTo("/topic/preclearance")
    public Map preclearanceEvents() throws Exception {
        System.out.println("Started...");
        return respo;
    }

    @Scheduled(fixedRate = 3000)
    public void push() throws Exception {
        template.convertAndSend("/topic/preclearance", respo);
    }

}
