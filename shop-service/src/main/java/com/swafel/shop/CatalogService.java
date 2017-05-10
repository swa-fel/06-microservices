package com.swafel.shop;

import com.netflix.hystrix.HystrixCommand;
import com.swafel.shop.model.CatalogItem;

import java.util.List;

import feign.Param;
import feign.RequestLine;

public interface CatalogService {
	@RequestLine("GET /catalog/{id}")
	HystrixCommand<CatalogItem> getItem(@Param("id") long id);

	@RequestLine("GET /catalog")
	HystrixCommand<List<CatalogItem>> listItems();
}
