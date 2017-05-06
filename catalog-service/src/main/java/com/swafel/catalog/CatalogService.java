package com.swafel.catalog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.opentracing.Tracer;

@Service
public class CatalogService {

	@Autowired
	private Tracer tracer;

	private Map<Long, CatalogItem> items = new HashMap<>();

	public CatalogService() {

		items.put(1l, new CatalogItem(1, "Foo", new BigDecimal("49.99")));
		items.put(2l, new CatalogItem(2, "Bar", new BigDecimal("9.99")));
		items.put(3l, new CatalogItem(3, "Tzar", new BigDecimal("5.55")));
		items.put(4l, new CatalogItem(4, "Gar", new BigDecimal("1.99")));
	}

	public List<CatalogItem> listItems() {
		return new LinkedList<>(items.values());
	}

	public CatalogItem findById(long id) {

		CatalogItem ret = items.get(id);

		/*
		try {
			Thread.sleep(10L);
		} catch (InterruptedException e) {
			// NOP
		}*/

		return ret;
	}
}
