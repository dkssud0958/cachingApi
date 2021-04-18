package com.example.cacheapi.products;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.net.*;
import java.util.*;

import org.springframework.http.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;

import com.example.cacheapi.cache.*;
import com.example.cacheapi.categories.*;

import javassist.*;

@Controller
@RequestMapping(value = "/api/products", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProductController {

	private final ProductRepository productRepository;
	
	private final CacheService cacheService;

	public ProductController(ProductRepository productRepository, CacheService cacheService) {
		this.productRepository = productRepository;
		this.cacheService = cacheService;
	}

	@PostMapping("/{product_no}")
	public ResponseEntity<Product> createProduct(@PathVariable Integer	product_no, @RequestBody Product product) {
		product.setProduct_no(product_no);
		Product newProduct = this.productRepository.save(product);
		URI createdUri = linkTo(CategoryController.class).slash(newProduct.getProduct_no()).toUri();
		return ResponseEntity.created(createdUri).body(product);
	}
	
	@GetMapping("/{product_no}")
	public ResponseEntity<ProductDto> selectProduct(@PathVariable Integer product_no) throws NotFoundException {
		//ĳ�� ���� ȣ��
		Optional<ProductDto> optionalproduct = cacheService.getProductInfo(product_no);
		if(optionalproduct.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok().body(optionalproduct.get());
	}
	
	@GetMapping("/category/{category_no}")
	public ResponseEntity<List<ProductDto>> selectProductListwithCategory(@PathVariable Integer category_no) throws NotFoundException {
		//캐시서비스 호출
		List<ProductDto> productList = cacheService.findProductsByCategoryNo(category_no);
		if(null == productList || productList.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok().body(productList);
	}

	@PutMapping("/{product_no}")
	public ResponseEntity updateProductInfo(@PathVariable Integer product_no, @RequestBody Product product) throws NotFoundException {
		Optional<Product> originProduct = productRepository.findById(product_no);
		if(originProduct.isPresent()) {
			product.setProduct_no(product_no);
			this.productRepository.save(product);
			return ResponseEntity.ok(null);
		}
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{product_no}")
	public ResponseEntity deleteProduct(@PathVariable Integer product_no) {
		try {
			this.productRepository.deleteById(product_no);			
		}catch (Exception e) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(null);
	}
}
