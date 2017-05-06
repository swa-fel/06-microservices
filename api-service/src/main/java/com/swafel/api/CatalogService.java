package com.swafel.api;

import com.netflix.hystrix.HystrixCommand;

import feign.Param;
import feign.RequestLine;

public interface CatalogService {
	@RequestLine("GET /catalog/{id}")
	HystrixCommand<CatalogItem> getItem(@Param("id") long id);
}
