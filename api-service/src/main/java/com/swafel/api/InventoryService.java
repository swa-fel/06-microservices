package com.swafel.api;

import com.netflix.hystrix.HystrixCommand;

import feign.Param;
import feign.RequestLine;

public interface InventoryService {
	@RequestLine("GET /inventory/{id}")
	HystrixCommand<InventoryItem> getItem(@Param("id") long id);
}
