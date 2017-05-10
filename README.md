# Microservices Lab SWA FEL CVUT summer 2017

# Initial setup

Clone the lab git repo

```
git clone https://github.com/swa-fel/06-microservices.git
```

Download Gatling, the Open Source load & performance testing tool

```
wget https://repo1.maven.org/maven2/io/gatling/highcharts/gatling-charts-highcharts-bundle/2.2.5/gatling-charts-highcharts-bundle-2.2.5-bundle.zip
unzip gatling-charts-highcharts-bundle-2.2.5-bundle.zip
```

Replace the gatling user scripts with our lab scripts

```
rm -rf gatling-charts-highcharts-bundle-2.2.5/user-files/
cp -r 06-microservices/gatling/user-files/ gatling-charts-highcharts-bundle-2.2.5/user-files/
```

Build and attempt to run the lab project

```
cd 06-microservices/shop-service
mvn clean package spring-boot:run
```


Run hystrix dashboard locally either as a docker container, or natively

```
docker run -d --network=host --name hystrix-dashboard mlabouardy/hystrix-dashboard:latest
xdg-open http://127.0.0.1:9002/hystrix/
```

or

```
git clone https://github.com/Netflix/Hystrix.git
cd Hystrix/hystrix-dashboard
../gradlew appRun
xdg-open http://localhost:7979/hystrix-dashboard
```

# Task 00

Configure the following URLs for the backend services in shop-service/src/main/resources/application.properties

```
catalog.service.url = http://api.swafel.com
inventory.service.url = http://api.swafel.com
```

Build and deploy the shop-service

```
mvn clean package spring-boot:run
```

Open http://127.0.0.1:8080 and explore the shop :)

Let the swafel.com admin scale the inventory service down

```
oc scale dc inventory --replicas=0
```

Play with the store, notice that item availability is broken, and so is the store catalog after three requests... 


Try to find out what went wrong and fix the problem.


# Task 01

Enable hystrix timeouts in shop-service/src/main/resources/config.properties

Notice the behavior of timeouts

# Task 02

Enable hystrix circuit breaker in shop-service/src/main/resources/config.properties

Notice the behavior of the circuit breaker

# Task 03

Let the admin scale the inventory service up again, and let it simulate slowness (200ms delay in responses)...

```
oc env dc inventory SLOWNESS=200
oc scale dc inventory --replicas=3
```

Estimate max. rate of users' requests, considering 400ms calls and 10 hystrix command threads (200ms delay of the inventory service + about 200ms for network latency and a bit of a buffer)

Run a load test with 25 user requests /s to http://127.0.0.1:8080/item/1

Watch the hystrix console.

Does the system behave according to our expectations?

# Task 04

Implement fallback for InventoryService

```
FallbackFactory<InventoryService> fallbackFactory = new FallbackFactory<InventoryService>() {
  ...
}
```

# Task 05

Configure tracing to push spans to the zipkin server running on http://zipkin.swafel.com

```
export ZIPKIN_SERVER_URL=http://zipkin.swafel.com
```

Redeploy, run a few requests, and try to find the spans on http://zipkin.swafel.com

# Task 06

Notice the two spans for catalog and inventory services are synchronous. Change InventoryService getItem return type to HystrixCommand and update ShopController, ServicesConfiguration to run the catalog and inventory queries in parallel.


