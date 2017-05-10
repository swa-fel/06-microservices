package com.swafel.shop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.swafel.shop.model.CatalogItem;
import com.swafel.shop.model.InventoryItem;
import com.swafel.shop.model.ShopItem;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/")
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

	private ShopItem createShopItem(CatalogItem catalogItem, InventoryItem inventoryItem) {
		ShopItem item = new ShopItem();
		item.setId(catalogItem.getId());
		item.setName(catalogItem.getName());
		item.setPrice(catalogItem.getPrice());
		item.setAvailability(getAvailability(inventoryItem));
		return item;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/catalog", produces = "application/json")
	public List<CatalogItem> inventoryList() {
		return catalogService.listItems();
	}


	@RequestMapping(method = RequestMethod.GET, value = "/item/{id}", produces = "application/json")
	public ResponseEntity<ShopItem> getItem(@PathVariable long id) throws ExecutionException, InterruptedException {
		CatalogItem catalogItem = catalogService.getItem(id);
		InventoryItem inventoryItem = inventoryService.getItem(id);

		if (catalogItem == null) {
			return ResponseEntity.status(503).build();
		}

		ShopItem shopItem = createShopItem(catalogItem, inventoryItem);

		return ResponseEntity.ok(shopItem);
	}
}
