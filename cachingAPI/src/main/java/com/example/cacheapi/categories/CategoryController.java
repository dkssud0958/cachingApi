package com.example.cacheapi.categories;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.net.*;
import java.util.*;

import org.springframework.hateoas.*;
import org.springframework.http.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;

import com.example.cacheapi.cache.*;

import javassist.*;

@Controller
@RequestMapping(value = "/api/categories", produces = MediaTypes.HAL_JSON_VALUE)
public class CategoryController {

	private final CategoryRepository categoryRepository;

	private final CacheService cacheService;

	public CategoryController(CategoryRepository categoryRepository, CacheService cacheService) {
		this.categoryRepository = categoryRepository;
		this.cacheService = cacheService;
	}

	@PostMapping("/{category_no}")
	public ResponseEntity<Category> createCategory(@PathVariable Integer category_no, @RequestBody Category category) {
		category.setCategory_no(category_no);
		Category newCategory = this.categoryRepository.save(category);
		URI createdUri = linkTo(CategoryController.class).slash(newCategory.getCategory_no()).toUri();
		return ResponseEntity.created(createdUri).body(category);
	}

	@GetMapping("/{category_no}")
	public ResponseEntity<String> getCategory(@PathVariable Integer category_no) throws NotFoundException {
		String category_name = cacheService.findCategoryGroupName(category_no);
		if(null == category_name) return ResponseEntity.notFound().build();
		return ResponseEntity.ok().body(category_name);
	}

	@PutMapping("/{category_no}")
	public ResponseEntity updateCategory(@PathVariable Integer category_no, @RequestBody Category category) throws NotFoundException {
		Optional<Category> originCategory = categoryRepository.findById(category_no);
		if(originCategory.isPresent()) {
			category.setCategory_no(category_no);
			this.categoryRepository.saveAndFlush(category);	
			return ResponseEntity.ok(null);
		}
		return ResponseEntity.notFound().build();
	}
		
	@DeleteMapping("/{category_no}")
	public ResponseEntity deleteCategory(@PathVariable Integer category_no) {
		try {
			this.categoryRepository.deleteById(category_no);			
		}catch (Exception e) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(null);
	}
}
