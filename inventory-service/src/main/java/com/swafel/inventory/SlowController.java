package com.swafel.inventory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/slow")
public class SlowController {

	@Autowired
	InventoryService inventoryService;

	@RequestMapping(method = RequestMethod.GET, value="/{time}", produces = "text/plain")
	public String slow(@PathVariable int time) {
		inventoryService.setSlow(time);
		return "OK";
	}
}
