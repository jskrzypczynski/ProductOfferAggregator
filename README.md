# ProductOfferAggregator

This is small product offer aggregator, every offer has two fields "price" and "productCode". Whats app gives you is a aggregation of offers
with fields: min, max, average price, count of offers.

Please note that this application is not production ready and too complicated (over-engineered) in some places for demonstration purposes

## Database layer

Application use simple H2 database (it allows set up the application environment very easily), the only parameter that we have to bring is
connection pool size.

With application startup 'aggregations' table is created (it is persistance layer for offers aggregations), table schema:

```
productCode VARCHAR(200)
minPrice NUMERIC(20, 2)
maxPrice NUMERIC(20, 2)
avg NUMERIC(20, 2)
numOffers INTEGER
status VARCHAR(20)
offers CHARACTER LARGE OBJECT)
```

## Rest Api layer

Service API allows:

- Querying for an aggregation for a given product code;
  Type: GET;
  Endpoint: `api/aggregation/find/<productCode>`
- closing the aggregation Type: GET Endpoint: `api/aggregation/close/<productCode>`

  Application will close only aggregation with more than N offers (close-limit parameter in configuration file)

- supplying offers for the aggregation 
  Type: POST 
  Endpoint: `api/offer/supply`

Payload format:

```
       [
        {
            "price": Float,
            "productCode": String
        }
       ]
```

example of payload:

```json
       [
  {
    "price": 7641.0,
    "productCode": "95069110"
  },
  {
    "price": 3490.0,
    "productCode": "95069110"
  }
]
```

- find aggregation with the highest number of offers (for debugging purposes)
  Type: GET 
  Endpoint: `api/aggregation/highestOffer`

## Offers Queue

Application uses fs2 Queue in order to transmit offers for aggregation, you can manipulate Queue capacity within configuration file

## Initial offers reading

The application allows you to get offers from a file at startup, you need to provide path, batch-size and max-concurrent-reads parameters,
also startup-aggregation parameter is need to be set to true.

File have to be in specific format (CSV), with columns:

```text
Category,Price,Product URL,Product Name,ProductCode,Brand,EAN,Description,Stock_status,Delivery Time,Shipping_cost,Product_id,Condition,Bundle,Colour,Gender,Size
```

Check provided file in project resources

## Configuration

Application is configured during startup from text file, you need provide path to config as first argument of application parameters.

example of config:

```text
close-limit = 3 // Aggregation closing lower limit
http-server {
  port = 8080 // http port
}
file-reader {
  path = "<PATH>"
  batch-size = 65000
  max-concurrent-reads = 12
  startup-aggregation = true
}
queue {
  queue-capacity = 50000
}
database {
  connection-pool-size = 5
}
```

## Application build and run

To build Fat-Jar you need to run `sbt assembly` command

example of application run from terminal:
`java -jar target/scala-2.13/ProductOfferAggregator-assembly-0.1.jar /ProductOfferAggregator/src/main/resources/application.conf `


## Further development

Application could be improve/enhance in many areas:
- replace the SQL database with a in-memory data structure (cats.effect.Ref[F])
- add authorization and authentication to Rest AP
- Offers persistence as a separate table
- Add more unit tests and integration tests