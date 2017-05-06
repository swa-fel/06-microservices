package com.swafel.api;

import com.netflix.hystrix.HystrixCommand;

import java.util.List;

import feign.Param;
import feign.RequestLine;

public interface CatalogService {
	@RequestLine("GET /catalog/{id}")
	HystrixCommand<CatalogItem> getItem(@Param("id") long id);

	@RequestLine("GET /catalog")
	List<CatalogItem> listItems();
}
