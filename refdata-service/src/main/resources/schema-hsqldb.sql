CREATE TABLE IF NOT EXISTS Airlines(
   AirlineID INTEGER  NOT NULL PRIMARY KEY
  ,Name      VARCHAR(81) NOT NULL
  ,Alias     VARCHAR(30)
  ,IATA      VARCHAR(3)
  ,ICAO      VARCHAR(5)
  ,Callsign  VARCHAR(50)
  ,Country   VARCHAR(37)
  ,Active    VARCHAR(1) NOT NULL
);

CREATE TABLE IF NOT EXISTS Routes(
   Airline              VARCHAR(3) NOT NULL
  ,AirlineID            VARCHAR(5)
  ,SourceAirport        VARCHAR(3) NOT NULL
  ,SourceAirportID      VARCHAR(5)
  ,DestinationAirport   VARCHAR(3) NOT NULL
  ,DestinationAirportID VARCHAR(5)
  ,Codeshare            VARCHAR(1)
  ,Stops                BIT  NOT NULL
  ,Equipment            VARCHAR(35)
);

CREATE INDEX Routes_Airline ON Routes (Airline);
CREATE INDEX Routes_SourceAirport ON Routes (SourceAirport);
CREATE INDEX Routes_DestinationAirport ON Routes (DestinationAirport);
CREATE INDEX Airlines_IATA ON Airlines (IATA);

