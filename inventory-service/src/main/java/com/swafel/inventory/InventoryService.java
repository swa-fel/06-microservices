package com.swafel.inventory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.opentracing.Tracer;

@Service
public class InventoryService {

	@Autowired
	private Tracer tracer;

	private Map<Long, InventoryItem> items = new HashMap<>();

	private int slow = 0;

	public int getSlow() {
		return slow;
	}

	public void setSlow(int slow) {
		this.slow = slow;
	}

	public InventoryService() {

		items.put(1l, new InventoryItem(1, "Foo", 42));
		items.put(2l, new InventoryItem(2, "Bar", 1));
		items.put(3l, new InventoryItem(3, "Tzar", 3));
		items.put(4l, new InventoryItem(4, "Gar", 0));
	}

	public List<InventoryItem> listItems() {
		return new LinkedList<>(items.values());
	}

	public InventoryItem findById(long id) {

		InventoryItem ret = items.get(id);

		if (slow > 0) {

			try {
				Thread.sleep(slow);
			} catch (InterruptedException e) {
				// NOP
			}
		}

		return ret;
	}
}
