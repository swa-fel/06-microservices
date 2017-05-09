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


