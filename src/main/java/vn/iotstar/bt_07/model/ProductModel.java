package vn.iotstar.bt_07.model;

import java.util.Date;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductModel {
	private Long productId;
	private String productName;
	private int quantity;
	private double unitPrice;
	private String images;
	private MultipartFile imageFile;
	private String description;
	private double discount;
	private Date createDate;
	private short status;
	private Long categoryId;
}
