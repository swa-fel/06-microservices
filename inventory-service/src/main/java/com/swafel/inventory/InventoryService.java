package com.swafel.inventory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import io.opentracing.Tracer;

@Service
public class InventoryService {

	@Autowired
	private Tracer tracer;

	private Map<Long, InventoryItem> items = new HashMap<>();

	public InventoryService() {

		items.put(1l, new InventoryItem(1, "Foo", 42));
		items.put(2l, new InventoryItem(2, "Bar", 1));
		items.put(3l, new InventoryItem(3, "Tzar", 3));
		items.put(4l, new InventoryItem(4, "Gar", 0));
	}

	public InventoryItem findById(long id) {

		InventoryItem ret = items.get(id);

		/*
		try {
			Thread.sleep(10L);
		} catch (InterruptedException e) {
			// NOP
		}*/

		return ret;
	}
}
