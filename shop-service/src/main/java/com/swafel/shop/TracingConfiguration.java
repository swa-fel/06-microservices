package com.swafel.shop;


import org.apache.http.HttpHost;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

import java.lang.reflect.Method;
import java.util.Collections;

import brave.opentracing.BraveTracer;

import feign.Feign;
import feign.Logger;
import feign.Target;
import feign.httpclient.ApacheHttpClient;
import feign.hystrix.FallbackFactory;
import feign.hystrix.HystrixFeign;
import feign.hystrix.SetterFactory;
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
        brave.Tracer braveTracer = brave.Tracer.newBuilder().localServiceName("shop_service").reporter(reporter).build();
        return BraveTracer.wrap(braveTracer);
    }


    @Bean
	public InventoryService inventoryService(Tracer tracer) {
		// bind current span to Hystrix thread
		TracingConcurrencyStrategy.register();

		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		// Increase max total connection to 200
		cm.setMaxTotal(200);
		// Increase default max connection per route to 20
		cm.setDefaultMaxPerRoute(20);
		// Increase max connections for localhost:80 to 50
		//HttpHost localhost = new HttpHost("127.0.0.1", 8081);
		//cm.setMaxPerRoute(new HttpRoute(localhost), 200);

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
		// Increase default max connection per route to 20
		cm.setDefaultMaxPerRoute(20);
		// Increase max connections for localhost:80 to 50
		//HttpHost localhost = new HttpHost("127.0.0.1", 8081);
		//cm.setMaxPerRoute(new HttpRoute(localhost), 200);

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

/*
	@Bean
	public InventoryService inventoryService(Tracer tracer) {
		TracingConcurrencyStrategy.register();

		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		// Increase max total connection to 200
		cm.setMaxTotal(200);
		// Increase default max connection per route to 20
		cm.setDefaultMaxPerRoute(20);
		// Increase max connections for localhost:80 to 50
		//HttpHost localhost = new HttpHost("127.0.0.1", 8081);
		//cm.setMaxPerRoute(new HttpRoute(localhost), 200);

		return Feign.builder()
				.client(new TracingClient(new ApacheHttpClient(HttpClientBuilder.create().setConnectionManager(cm).build()), tracer))
				.logger(new Logger.ErrorLogger()).logLevel(Logger.Level.BASIC)
				.decoder(new JacksonDecoder())
				.target(InventoryService.class, "http://127.0.0.1:8081/");
	}
*/
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
