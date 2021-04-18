package com.example.cacheapi.products;

import javax.persistence.*;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "product_no")
@Entity
public class Product {

	@Id
	private Integer product_no;
	private String product_name;
	private String brand_name;
	private long product_price;
	private int category_no;

	@Override
	public String toString() {
		return "Product [product_no=" + product_no + ",product_name=" + product_name + ",brand_name=" + brand_name
				+ ",product_price=" + product_price + ",category_no=" + category_no + "]";
	}
}
