package vn.iotstar.bt_07.controller.api;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import vn.iotstar.bt_07.entity.Category;
import vn.iotstar.bt_07.entity.Product;
import vn.iotstar.bt_07.model.ProductDTO;
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

	@GetMapping(path = "/getProducts")
	public ResponseEntity<?> getProducts() {
		List<ProductDTO> list = new ArrayList<>();
		for (Product p : productService.findAll()) {
			ProductDTO dto = new ProductDTO();
			BeanUtils.copyProperties(p, dto);
			if (p.getCategory() != null) {
				dto.setCategoryId(p.getCategory().getCategoryId());
				dto.setCategoryName(p.getCategory().getCategoryName());
			}
			list.add(dto);
		}
		return new ResponseEntity<Response>(new Response(true, "Thành công", list), HttpStatus.OK);
	}

	@PutMapping(path = "/updateProduct", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> updateProduct(
			@Validated @RequestParam("productId") Long productId,
			@Validated @RequestParam("productName") String productName,
			@RequestParam("imageFile") MultipartFile imageFile,
			@Validated @RequestParam("unitPrice") Double unitPrice,
			@Validated @RequestParam("discount") Double discount,
			@Validated @RequestParam("description") String description,
			@Validated @RequestParam("categoryId") Long categoryId,
			@Validated @RequestParam("quantity") Integer quantity,
			@Validated @RequestParam("status") Short status) {
		Optional<Product> optProduct = productService.findById(productId);
		if (optProduct.isEmpty()) {
			return new ResponseEntity<Response>(new Response(false, "Không tìm thấy Product", null), HttpStatus.BAD_REQUEST);
		}
		Optional<Category> optCategory = categoryService.findById(categoryId);
		if (optCategory.isEmpty()) {
			return new ResponseEntity<Response>(new Response(false, "Không tìm thấy Category", null), HttpStatus.BAD_REQUEST);
		}
		Product product = optProduct.get();
		if (!imageFile.isEmpty()) {
			UUID uuid = UUID.randomUUID();
			String uuString = uuid.toString();
			product.setImages(storageService.getSorageFilename(imageFile, uuString));
			storageService.store(imageFile, product.getImages());
		}
		product.setProductName(productName);
		product.setUnitPrice(unitPrice);
		product.setDiscount(discount);
		product.setDescription(description);
		product.setQuantity(quantity);
		product.setStatus(status);
		product.setCategory(optCategory.get());
		productService.save(product);
		return new ResponseEntity<Response>(new Response(true, "Cập nhật Thành công", product), HttpStatus.OK);
	}

	@DeleteMapping(path = "/deleteProduct")
	public ResponseEntity<?> deleteProduct(@Validated @RequestParam("productId") Long productId) {
		Optional<Product> optProduct = productService.findById(productId);
		if (optProduct.isEmpty()) {
			return new ResponseEntity<Response>(new Response(false, "Không tìm thấy Product", null), HttpStatus.BAD_REQUEST);
		}
		productService.delete(optProduct.get());
		return new ResponseEntity<Response>(new Response(true, "Xóa Thành công", optProduct.get()), HttpStatus.OK);
	}
}
