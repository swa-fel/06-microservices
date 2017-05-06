package com.swafel.api;


import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

import java.util.Collections;
import java.util.List;

import brave.opentracing.BraveTracer;

import feign.Logger;
import feign.httpclient.ApacheHttpClient;
import feign.hystrix.FallbackFactory;
import feign.hystrix.HystrixFeign;
import feign.jackson.JacksonDecoder;
import feign.opentracing.TracingClient;
import feign.opentracing.hystrix.TracingConcurrencyStrategy;
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
        brave.Tracer braveTracer = brave.Tracer.newBuilder().localServiceName("api_service").reporter(reporter).build();
        return BraveTracer.wrap(braveTracer);
    }


    @Bean
	public InventoryService inventoryService(Tracer tracer) {
		// bind current span to Hystrix thread
		TracingConcurrencyStrategy.register();

		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(200);
		cm.setDefaultMaxPerRoute(20);

		FallbackFactory<InventoryService> fallbackFactory = cause -> new InventoryService() {
			@Override
			public HystrixCommand<InventoryItem> getItem(long id) {
				return new HystrixCommand<InventoryItem>(HystrixCommandGroupKey.Factory.asKey("inventoryFallback")) {
					@Override
					protected InventoryItem run() throws Exception {
						return null;
					}
				};
			}
		};

		return HystrixFeign.builder()
				.client(new TracingClient(
						new ApacheHttpClient(HttpClientBuilder.create().setConnectionManager(cm).build()),
						tracer))
				.logger(new Logger.ErrorLogger()).logLevel(Logger.Level.BASIC)
				.decoder(new JacksonDecoder())
				.target(InventoryService.class, "http://inventory:8080/", fallbackFactory);
	}

	@Bean
	public CatalogService catalogService(Tracer tracer) {
		// bind current span to Hystrix thread
		TracingConcurrencyStrategy.register();

		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		// Increase max total connection to 200
		cm.setMaxTotal(200);
		cm.setDefaultMaxPerRoute(20);

		FallbackFactory<CatalogService> fallbackFactory = cause -> new CatalogService() {
			@Override
			public HystrixCommand<CatalogItem> getItem(long id) {
				return new HystrixCommand<CatalogItem>(HystrixCommandGroupKey.Factory.asKey("catalogFallback")) {
					@Override
					protected CatalogItem run() throws Exception {
						return null;
					}
				};
			}

			@Override
			public List<CatalogItem> listItems() {
				return Collections.emptyList();
			}
		};

		return HystrixFeign.builder()
				.client(new TracingClient(
						new ApacheHttpClient(HttpClientBuilder.create().setConnectionManager(cm).build()),
						tracer))
				.logger(new Logger.ErrorLogger()).logLevel(Logger.Level.BASIC)
				.decoder(new JacksonDecoder())
				.target(CatalogService.class, "http://catalog:8080/",
						fallbackFactory);
	}
}
