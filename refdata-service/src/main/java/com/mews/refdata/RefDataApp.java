package com.mews.refdata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Created by saipkri on 30/07/17.
 */

@EnableAutoConfiguration
@SpringBootApplication
@EnableDiscoveryClient
@EnableJms
@EnableScheduling
public class RefDataApp {
    public static void main(String[] args) {
        SpringApplication.run(RefDataApp.class, args);
    }

    @RestController
    class FlightRouteFinderRestApi {

        @Autowired
        private Tracer tracer;

        @Autowired
        private FlightRouteFinderRepository flightRouteFinderRepository;

        @GetMapping("/flights/{origin}/{destination}")
        List<Map<String, Object>> flightsForRoute(@PathVariable("origin") final String origin,
                                                  @PathVariable("destination") final String destination) {
            Span span = tracer.createSpan("/flights/{origin}/{destination}");
            try {
                return flightRouteFinderRepository.flightsForRoute(origin.toUpperCase(), destination.toUpperCase());
            } finally {
                tracer.close(span);
            }
        }
    }

    @Service
    class PreclearanceService {

        @Autowired
        private JmsTemplate jmsTemplate;
        private List<String> lines;
        private Random random = new Random();
        private ObjectMapper objectMapper = new ObjectMapper();
        ;

        public PreclearanceService() throws Exception {
            lines = new BufferedReader(new InputStreamReader(PreclearanceService.class.getClassLoader().getResourceAsStream("events.txt")))
                    .lines()
                    .collect(Collectors.toList());
        }

        @Scheduled(fixedRate = 1000L)
        void fireMessages() throws IOException {
            String line = lines.get(random.nextInt(lines.size()));
            Map payload = objectMapper.readValue(line, Map.class);
            payload.put("value", random.nextInt(10));
            payload.put("color", "orange");

            jmsTemplate.send(new ActiveMQTopic("PreClearance"), session -> {
                javax.jms.Message message = null;
                try {
                    message = session.createTextMessage(objectMapper.writeValueAsString(payload));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                System.out.println("local-sent");
                return message;
            });
        }
    }

    @Repository
    class FlightRouteFinderRepository {

        private final JdbcTemplate jdbcTemplate;

        @Autowired
        private Tracer tracer;

        @Autowired
        FlightRouteFinderRepository(final JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
        }

        List<Map<String, Object>> flightsForRoute(final String origin, final String destination) {
            Span span = tracer.createSpan("FlightRouteFinderRepository::flightsForRoute");
            try {
                String sql = "select a.name as AirlineName, a.Country as Headquarters, r.SourceAirport as Origin, r.DestinationAirport Destination, r.Equipment Aircraft from Airlines a INNER JOIN Routes r ON a.IATA=r.Airline WHERE r.SourceAirport=? AND r.DestinationAirport=? ORDER BY r.Airline ";
                return jdbcTemplate.queryForList(sql, origin, destination);
            } finally {
                tracer.close(span);
            }
        }
    }
}

