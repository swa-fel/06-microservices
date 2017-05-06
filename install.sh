#!/bin/sh

oc new-app openzipkin/zipkin
oc new-app mlabouardy/hystrix-dashboard:latest

oc expose service zipkin --hostname=zipkin.swafel.com --port=9411
oc expose service hystrix-dashboard --hostname=hystrix.swafel.com --port=9002

for i in api catalog inventory shop
do
  pushd $i-service
  oc new-app --docker-image=registry.access.redhat.com/redhat-openjdk-18/openjdk18-openshift:latest . --name=$i
  oc start-build $i --from-dir=.
  oc env dc $i ZIPKIN_SERVER_URL=http://zipkin:9411
  popd
done
