package com.swafel.shop;

import com.swafel.shop.model.CatalogItem;

import java.util.List;

import feign.Param;
import feign.RequestLine;

public interface CatalogService {
	@RequestLine("GET /catalog/{id}")
	CatalogItem getItem(@Param("id") long id);

	@RequestLine("GET /catalog")
	List<CatalogItem> listItems();
}
