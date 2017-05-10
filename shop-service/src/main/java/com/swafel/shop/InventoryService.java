package com.swafel.shop;

import com.netflix.hystrix.HystrixCommand;
import com.swafel.shop.model.InventoryItem;

import feign.Param;
import feign.RequestLine;

public interface InventoryService {
	@RequestLine("GET /inventory/{id}")
	HystrixCommand<InventoryItem> getItem(@Param("id") long id);
}
