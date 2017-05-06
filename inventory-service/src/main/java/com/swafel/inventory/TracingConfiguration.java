package com.swafel.inventory;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import brave.opentracing.BraveTracer;

import io.opentracing.NoopTracerFactory;
import io.opentracing.Tracer;
import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.Reporter;
import zipkin.reporter.urlconnection.URLConnectionSender;

@Configuration
public class TracingConfiguration {

    @Bean
    public Tracer tracer() {
        String zipkinServerUrl = System.getenv("ZIPKIN_SERVER_URL");
        if (zipkinServerUrl == null) {
            return NoopTracerFactory.create();
        }

        System.out.println("Using Zipkin tracer");
        Reporter<Span> reporter = AsyncReporter.builder(URLConnectionSender.create(zipkinServerUrl + "/api/v1/spans"))
                .build();
        brave.Tracer braveTracer = brave.Tracer.newBuilder().localServiceName("inventory_service").reporter(reporter).build();
        return BraveTracer.wrap(braveTracer);
    }

    /**
     *
     * This is were the "magic" happens: it creates a Feign, which is a proxy interface for remote calling a
     * REST endpoint with Hystrix fallback support.
     */
   /* @Bean
    public HolaService holaService(Tracer tracer) {
        // bind current span to Hystrix thread
        TracingConcurrencyStrategy.register();

        return HystrixFeign.builder()
                .client(new TracingClient(new ApacheHttpClient(HttpClientBuilder.create().build()), tracer))
                .logger(new Logger.ErrorLogger()).logLevel(Logger.Level.BASIC)
                .decoder(new JacksonDecoder())
                .target(HolaService.class, "http://hola:8080/",
                        () -> Collections.singletonList("Hola response (fallback)"));
    }*/
}
