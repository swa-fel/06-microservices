package com.swafel.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RestController
@RequestMapping("/")
public class ApiController {

	@Autowired
	private InventoryService inventoryService;

	@Autowired
	private CatalogService catalogService;

	@RequestMapping(method = RequestMethod.GET, value = "/catalog/{id}", produces = "application/json")
	public ResponseEntity<CatalogItem> getCatalogItem(@PathVariable long id) throws ExecutionException, InterruptedException {
		CatalogItem ret = catalogService.getItem(id).execute();
		if (ret != null) {
			return ResponseEntity.ok(ret);
		}
		else {
			return ResponseEntity.status(503).build();
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/catalog", produces = "application/json")
	public List<CatalogItem> inventoryList() {
		return catalogService.listItems();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/inventory/{id}", produces = "application/json")
	public ResponseEntity<InventoryItem> getInventoryItem(@PathVariable long id) throws ExecutionException, InterruptedException {
		InventoryItem ret = inventoryService.getItem(id).execute();
		if (ret != null) {
			return ResponseEntity.ok(ret);
		}
		else {
			return ResponseEntity.status(503).build();
		}
	}

}
