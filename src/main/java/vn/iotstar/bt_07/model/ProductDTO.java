package vn.iotstar.bt_07.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {
	private Long productId;
	private String productName;
	private int quantity;
	private double unitPrice;
	private String images;
	private String description;
	private double discount;
	private Date createDate;
	private short status;
	private Long categoryId;
	private String categoryName;
}
