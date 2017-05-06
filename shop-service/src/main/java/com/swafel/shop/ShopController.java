package com.swafel.shop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RestController
@RequestMapping("/shop")
public class ShopController {

	@Autowired
	private InventoryService inventoryService;

	@Autowired
	private CatalogService catalogService;

	private String getAvailability(InventoryItem item) {
		if (item != null) {
			if (item.getCount() > 0) {
				return "in stock!";
			}
			else {
				return "out of stock";
			}
		}

		return "inventory information not available at this time";
	}


	@RequestMapping(method = RequestMethod.GET, value = "/item/{id}", produces = "application/json")
	public ResponseEntity<ShopItem> getItem(@PathVariable long id) throws ExecutionException, InterruptedException {
		Future<InventoryItem> futureInventoryItem = inventoryService.getItem(id).queue();
		Future<CatalogItem> futureCatalogItem = catalogService.getItem(id).queue();

		CatalogItem catalogItem = futureCatalogItem.get();

		if (catalogItem == null) {
			return ResponseEntity.status(503).build();
		}

		InventoryItem inventoryItem = futureInventoryItem.get();

		ShopItem shopItem = new ShopItem();
		shopItem.setId(id);
		shopItem.setName(catalogItem.getName());
		shopItem.setPrice(catalogItem.getPrice());

		shopItem.setAvailability(getAvailability(inventoryItem));

		return ResponseEntity.ok(shopItem);
	}

/*
	@RequestMapping(method = RequestMethod.GET, value = "/item/{id}", produces = "application/json")
	public ResponseEntity<ShopItem> getItem(@PathVariable long id) throws ExecutionException, InterruptedException {
		//Future<InventoryItem> futureInventoryItem = inventoryService.getItem(id).queue();
		//Future<CatalogItem> futureCatalogItem = catalogService.getItem(id).queue();

		InventoryItem inventoryItem =  inventoryService.getItem(id).execute();
		CatalogItem catalogItem = catalogService.getItem(id).execute();

		if (catalogItem == null ) {
			return ResponseEntity.status(503).build();
		}

		ShopItem shopItem = new ShopItem();
		shopItem.setId(id);
		shopItem.setName(catalogItem.getName());
		shopItem.setPrice(catalogItem.getPrice());

		shopItem.setAvailability(getAvailability(inventoryItem));

		return ResponseEntity.ok(shopItem);
	}*/
}
