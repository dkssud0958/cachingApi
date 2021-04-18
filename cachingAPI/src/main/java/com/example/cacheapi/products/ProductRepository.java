package com.example.cacheapi.products;

import java.util.*;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.*;

public interface ProductRepository extends JpaRepository<Product, Integer>{

	@Query(
			value =	"  SELECT TOP 500 * "
					+ "  FROM PRODUCT "
					+ "ORDER BY PRODUCT_NO DESC"
					, nativeQuery = true)
		List<Product> getMultiProduct();
	
	@Query(
			value = "  SELECT *"
					+ "  FROM PRODUCT"
					+ " WHERE CATEGORY_NO = :category_no"
					, nativeQuery = true)
		List<Product> getProductListWithCategory(@Param("category_no") Integer category_no);

	@Query(
			value = "  SELECT *"
					+ "  FROM PRODUCT"
					+ " WHERE PRODUCT_NO IN :product_nos"
					, nativeQuery = true)
	List<Product> getProductListRefresh(@Param("product_nos") Integer [] product_nos);

}
