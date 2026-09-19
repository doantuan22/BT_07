package vn.iotstar.bt_07.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import vn.iotstar.bt_07.service.IStorageService;

@Controller
public class CategoryPageController {

	@Autowired
	IStorageService storageService;

	@GetMapping({ "/admin", "/admin/" })
	public String list() {
		return "admin/category/list";
	}

	@GetMapping("/admin/categories/images/{filename:.+}")
	public ResponseEntity<Resource> serveImage(@PathVariable("filename") String filename) {
		if (filename.contains("..")) {
			return ResponseEntity.notFound().build();
		}
		try {
			Resource file = storageService.loadAsResource(filename);
			ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
			MediaTypeFactory.getMediaType(file).ifPresent(builder::contentType);
			return builder.header(HttpHeaders.CONTENT_DISPOSITION, "inline").body(file);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
}
