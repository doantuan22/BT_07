package vn.iotstar.bt_07.controller.api;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import vn.iotstar.bt_07.entity.Category;
import vn.iotstar.bt_07.entity.Product;
import vn.iotstar.bt_07.model.ProductModel;
import vn.iotstar.bt_07.model.Response;
import vn.iotstar.bt_07.service.ICategoryService;
import vn.iotstar.bt_07.service.IProductService;
import vn.iotstar.bt_07.service.IStorageService;

@RestController
@RequestMapping(path = "/api/product")
public class ProductApiController {

	@Autowired
	IProductService productService;
	@Autowired
	ICategoryService categoryService;
	@Autowired
	IStorageService storageService;

	@GetMapping
	public ResponseEntity<?> getAllProduct() {
		return new ResponseEntity<Response>(new Response(true, "Thành công", productService.findAll()), HttpStatus.OK);
	}

	@PostMapping(path = "/addProduct", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> saveOrUpdate(
			@Validated @RequestParam("productName") String productName,
			@RequestParam("imageFile") MultipartFile productImages,
			@Validated @RequestParam("unitPrice") Double productPrice,
			@Validated @RequestParam("discount") Double promotionalPrice,
			@Validated @RequestParam("description") String productDescription,
			@Validated @RequestParam("categoryId") Long categoryId,
			@Validated @RequestParam("quantity") Integer quantity,
			@Validated @RequestParam("status") Short status) {
		Optional<Product> optProduct = productService.findByProductName(productName);
		if (optProduct.isPresent()) {
			return new ResponseEntity<Response>(
					new Response(false, "Sản phẩm này đã tồn tại trong hệ thống", optProduct.get()),
					HttpStatus.BAD_REQUEST);
		} else {
			Product product = new Product();
			Timestamp timestamp = new Timestamp(new Date(System.currentTimeMillis()).getTime());
			try {
				ProductModel proModel = new ProductModel();
				proModel.setProductName(productName);
				proModel.setImageFile(productImages);
				proModel.setUnitPrice(productPrice);
				proModel.setDiscount(promotionalPrice);
				proModel.setDescription(productDescription);
				proModel.setCategoryId(categoryId);
				proModel.setQuantity(quantity);
				proModel.setStatus(status);

				BeanUtils.copyProperties(proModel, product);
				Category cateEntity = new Category();
				cateEntity.setCategoryId(proModel.getCategoryId());
				product.setCategory(cateEntity);
				if (!proModel.getImageFile().isEmpty()) {
					UUID uuid = UUID.randomUUID();
					String uuString = uuid.toString();
					product.setImages(storageService.getSorageFilename(proModel.getImageFile(), uuString));
					storageService.store(proModel.getImageFile(), product.getImages());
				}
				product.setCreateDate(timestamp);
				Product saved = productService.save(product);
				return new ResponseEntity<Response>(new Response(true, "Thành công", saved), HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
				return new ResponseEntity<Response>(new Response(false, "Thêm sản phẩm thất bại: " + e.getMessage(), null),
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}
}
