package com.example.cacheapi.categories;

import java.util.*;

import org.springframework.data.jpa.repository.*;

public interface CategoryRepository extends JpaRepository<Category, Integer>{
	
	@Query(
		value =	"WITH CATEGORYS(CATEGORY_NO, CATEGORY_NAME) AS ("
				+ "   SELECT CATEGORY_NO, CATEGORY_NAME FROM CATEGORY WHERE PARENT_NO IS NULL OR PARENT_NO = 0"
				+ "   UNION ALL"
				+ "   SELECT CATEGORY2.CATEGORY_NO, IFNULL(CATEGORY.CATEGORY_NAME || '-', '') || CATEGORY2.CATEGORY_NAME"
				+ "     FROM CATEGORY INNER JOIN CATEGORY AS CATEGORY2  ON CATEGORY.CATEGORY_NO = CATEGORY2.PARENT_NO"
				+ " )"
				+ "SELECT * FROM CATEGORYS"
				, nativeQuery = true)
	List<Object[]> getCategory();
}
