package com.mews.refdata;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Created by saipkri on 30/07/17.
 */

@EnableAutoConfiguration
@SpringBootApplication
@EnableDiscoveryClient
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

