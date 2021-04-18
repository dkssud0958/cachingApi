package com.example.cacheapi.categories;

import javax.persistence.*;

import lombok.*;

@Builder @AllArgsConstructor @NoArgsConstructor
@Getter @Setter @EqualsAndHashCode(of = "category_no")
@Entity
public class Category {
	
	@Id
	private Integer category_no;
	private String category_name;
	private Integer parent_no;
	private int depth;

}
