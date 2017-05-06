package com.swafel.shop;


import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

import java.util.Collections;
import java.util.List;

import brave.opentracing.BraveTracer;

import feign.Client;
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

	@Value("${catalog.service.url}")
	private String catalogServiceUrl;

	@Value("${inventory.service.url}")
	private String inventoryServiceUrl;

    @Bean
    public Tracer tracer() {
        String zipkinServerUrl = System.getenv("ZIPKIN_SERVER_URL");
        if (zipkinServerUrl == null) {
            return NoopTracerFactory.create();
        }

        System.out.println("Using Zipkin tracer");
        Reporter<Span> reporter = AsyncReporter.builder(URLConnectionSender.create(zipkinServerUrl + "/api/v1/spans"))
                .build();
        brave.Tracer braveTracer = brave.Tracer.newBuilder().localServiceName("shop_service").reporter(reporter).build();
        return BraveTracer.wrap(braveTracer);
    }

	@Bean
	@Scope("prototype")
	public Client httpClient() {
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		return new ApacheHttpClient(HttpClientBuilder.create().setConnectionManager(cm).build());
	}

    @Bean
	public InventoryService inventoryService(Tracer tracer, Client httpClient) {
		// bind current span to Hystrix thread
		TracingConcurrencyStrategy.register();

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
						httpClient,
						tracer))
				.logger(new Logger.ErrorLogger()).logLevel(Logger.Level.BASIC)
				.decoder(new JacksonDecoder())
				.target(InventoryService.class, inventoryServiceUrl, fallbackFactory);
	}

	@Bean
	public CatalogService catalogService(Tracer tracer, Client httpClient) {
		// bind current span to Hystrix thread
		TracingConcurrencyStrategy.register();

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
						httpClient,
						tracer))
				.logger(new Logger.ErrorLogger()).logLevel(Logger.Level.BASIC)
				.decoder(new JacksonDecoder())
				.target(CatalogService.class, catalogServiceUrl, fallbackFactory);
	}
}
