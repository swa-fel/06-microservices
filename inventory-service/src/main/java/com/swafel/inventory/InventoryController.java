package com.swafel.inventory;

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
@RequestMapping("/inventory")
public class InventoryController {

	@Autowired
	InventoryService inventoryService;

	@Autowired
	Tracer tracer;

	@RequestMapping(method = RequestMethod.GET, value="/{id}", produces = "application/json")
	public InventoryItem getInventoryItem(@PathVariable long id, HttpServletRequest request) {

		SpanContext serverSpanContext = TracingHandlerInterceptor.serverSpanContext(request);

		Span span = tracer.buildSpan("findById")
            .asChildOf(serverSpanContext)
				.start();

		span.setTag("item id", id);

		InventoryItem ret = inventoryService.findById(id);

		span.finish();

		return ret;
	}
}
