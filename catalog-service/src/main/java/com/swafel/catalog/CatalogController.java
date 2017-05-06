package com.swafel.catalog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.contrib.spring.web.interceptor.TracingHandlerInterceptor;

@RestController
@RequestMapping("/catalog")
public class CatalogController {

	@Autowired
	CatalogService catalogService;

	@Autowired
	Tracer tracer;

	@RequestMapping(method = RequestMethod.GET, value="/{id}", produces = "application/json")
	public CatalogItem getCatalogItem(@PathVariable long id, HttpServletRequest request) {

		SpanContext serverSpanContext = TracingHandlerInterceptor.serverSpanContext(request);

		Span span = tracer.buildSpan("findById")
            .asChildOf(serverSpanContext)
				.start();

		span.setTag("item id", id);

		CatalogItem ret = catalogService.findById(id);

		span.finish();

		return ret;
	}
}
