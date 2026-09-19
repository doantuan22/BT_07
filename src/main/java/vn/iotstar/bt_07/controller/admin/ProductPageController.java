package vn.iotstar.bt_07.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProductPageController {

	@GetMapping({ "/admin/products", "/admin/products/" })
	public String list() {
		return "admin/product/list";
	}
}
