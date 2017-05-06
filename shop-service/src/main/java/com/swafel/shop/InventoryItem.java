package com.swafel.shop;

public class InventoryItem {
	private long id;
	private String name;
	private long count;

	public InventoryItem() {}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	static final InventoryItem NONE = new InventoryItem();
}
