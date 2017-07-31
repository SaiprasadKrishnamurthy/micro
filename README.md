# A Simple Recipe using Microservices with Spring Cloud

# Usecase

## Airlines Route Search Service

* Given a ROUTE represented as an origin-destination pair (eg: LHR-JFK), the service will list all the Airlines (Operators) operational on this route.
* Given a Traveller checking in their flight at the checkin desk at the airport (Preclearance), their Nationalities are aggregated and shown in a dashboard real-time.

The flights data are sourced from this site:  https://openflights.org/data.html

## List of microservices
* admin-service - A simple UI showing the monitoring stats of all the running microservices.
* config-service - A centralized configuration management server.
* discovery-service - A Eureka powered service registry.
* refdata-service - A Reference data service that has the embedded database of all the airlines/route details.
* rulebase-service - The edge service that is consumed by the end consumers.
* preclearancestats-service - The edge service shows a *realtime* aggregates of the nationalities of people being precleared (during flight checkin).
* zipkin-service - The service that has the Zipkin-UI to trace the distributed logs.


# Prerequisites
* Java 8
* Maven 3

# Setup (Sequence is important)
* mvn clean install (At the top level).
* Start the Discovery microservice: ```java -jar discovery-service/target/discovery-service-0.0.1-SNAPSHOT.jar```
* Start the Configuration microservice: ```java -jar config-service/target/config-0.0.1-SNAPSHOT.jar```
* Start the Admin microservice: ```java -jar admin-service/target/admin-0.0.1-SNAPSHOT.jar```
* Start the Zipkin microservice: ```java -jar zipkin-service/target/zipkin-0.0.1-SNAPSHOT.jar```
* Start the Refdata microservice: ```java -jar refdata-service/target/refdata-service-0.0.1-SNAPSHOT.jar```
* Start the Rulebase microservice: ```java -jar rulebase-service/target/rulebase-0.0.1-SNAPSHOT.jar```
* Start the Preclearancestats microservice: ```java -jar preclearancestats-service/target/preclearancestats-service-0.0.1-SNAPSHOT.jar```

You should see all the services started up and they will have their own logs at the project root.

# Fun begins:
* As the server ports are completely dynamic, we can get the service endpoint only through the discovery service.
* Open the discovery service in a browser:  http://localhost:8761/
* This will show all the registered services.
* Click on the link against:  RULEBASE-SERVICE
* Send a few requests -
    * http://<DOMAIN:PORT>/flights/LHR/JFK
    * http://<DOMAIN:PORT>/flights/LHR/CDG
    * http://<DOMAIN:PORT>/flights/LHR/MAA
    * http://<DOMAIN:PORT>/popular-routes
* Back to your Discovery Service UI, Click on the link against:  ADMIN-SERVICE - This will take you the admin UI which shows the monitoring stats of the running microservices.
* Back to your Discovery Service UI, Click on the link against:  ZIPKIN - This will take you the zipkin UI which shows the log traces.

## License

This project is licensed under Apache License 2.0.
