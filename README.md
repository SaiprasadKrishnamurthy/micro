# A Simple Recipe using Microservices with Spring Cloud

Documentation is in progress..

# Usecase

## Airlines Route Search Service

* Given a ROUTE represented as an origin-destination pair (eg: LHR-JFK), the service will list all the Airlines (Operators) operational on this route.

The flights data are sourced from this site:  https://openflights.org/data.html

## List of microservices
* admin-service - A simple UI showing the monitoring stats of all the running microservices.
* config-service - A centralized configuration management server.
* discovery-service - A Eureka powered service registry.
* refdata-service - A Reference data service that has the embedded database of all the airlines/route details.
* rulebase-service - The edge service that is consumed by the end consumers.
* zipkin-service - The service that has the Zipkin-UI to trace the distributed logs.


## License

This project is licensed under Apache License 2.0.
