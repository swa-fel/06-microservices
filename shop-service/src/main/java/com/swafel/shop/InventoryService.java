package com.swafel.shop;

import com.swafel.shop.model.InventoryItem;

import feign.Param;
import feign.RequestLine;

public interface InventoryService {
	@RequestLine("GET /inventory/{id}")
	InventoryItem getItem(@Param("id") long id);
}
