package com.swafel.shop;


import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.swafel.shop.model.InventoryItem;

import java.net.InetAddress;
import java.net.UnknownHostException;

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
public class ServicesConfiguration {

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

        String hostname = "localhost";

		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			// ignore
		}

		brave.Tracer braveTracer = brave.Tracer.newBuilder().localServiceName(System.getProperty("user.name") + "@" + hostname).reporter(reporter).build();
        return BraveTracer.wrap(braveTracer);
    }

	@Bean
	@Scope("prototype")
	public Client httpClient() {
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setDefaultMaxPerRoute(10);
		cm.setMaxTotal(10);
		return new ApacheHttpClient(HttpClientBuilder.create().setConnectionManager(cm).build());
	}

    @Bean
	public InventoryService inventoryService(Tracer tracer, Client httpClient) {
		// bind current span to Hystrix thread
		TracingConcurrencyStrategy.register();

		FallbackFactory<InventoryService> fallbackFactory = new FallbackFactory<InventoryService>() {
			@Override
			public InventoryService create(Throwable throwable) {
				return new InventoryService() {
					@Override
					public InventoryItem getItem(long id) {
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

		return HystrixFeign.builder()
				.client(new TracingClient(
						httpClient,
						tracer))
				.logger(new Logger.ErrorLogger()).logLevel(Logger.Level.BASIC)
				.decoder(new JacksonDecoder())
				.target(CatalogService.class, catalogServiceUrl);
	}
}
