#!/bin/sh

oc new-app openzipkin/zipkin
oc new-app mlabouardy/hystrix-dashboard:latest

oc expose service zipkin --hostname=zipkin.swafel.com --port=9411
oc expose service hystrix-dashboard --hostname=hystrix.swafel.com --port=9002

for i in api catalog inventory shop
do
  pushd $i-service
  mvn clean
  oc new-app --docker-image=registry.access.redhat.com/redhat-openjdk-18/openjdk18-openshift:latest . --name=$i
  oc start-build $i --from-dir=.
  oc env dc $i ZIPKIN_SERVER_URL=http://zipkin:9411
  oc patch service $i -p '{"spec":{"ports":[{"name":"80-tcp","port":80,"targetPort":8080}]}}'
  oc expose service $i --hostname=$i.swafel.com --port=8080
  popd
done

oc env dc shop CATALOG_SERVICE_URL=http://catalog INVENTORY_SERVICE_URL=http://inventory


