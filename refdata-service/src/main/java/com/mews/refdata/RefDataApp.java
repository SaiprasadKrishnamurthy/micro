package com.mews.refdata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
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

    @Service
    class PreclearanceService {

        private List<String> lines;
        private Random random = new Random();
        private ObjectMapper objectMapper = new ObjectMapper();

        @Autowired
        private HazelcastInstance instance;

        public PreclearanceService() throws Exception {
            lines = new BufferedReader(new InputStreamReader(PreclearanceService.class.getClassLoader().getResourceAsStream("events.txt")))
                    .lines()
                    .collect(Collectors.toList());
        }

        @Scheduled(fixedRate = 1000L)
        void fireMessages() throws IOException {
            ITopic<String> topic = instance.getTopic("PreclearanceEvents");
            String line = lines.get(random.nextInt(lines.size()));
            Map payload = objectMapper.readValue(line, Map.class);
            payload.put("value", random.nextInt(10));
            payload.put("color", "orange");
            topic.publish(objectMapper.writeValueAsString(payload));
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

