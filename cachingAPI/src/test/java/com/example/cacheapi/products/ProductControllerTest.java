package com.example.cacheapi.products;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.*;
import org.junit.runner.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.context.*;
import org.springframework.hateoas.*;
import org.springframework.http.*;
import org.springframework.test.context.junit4.*;
import org.springframework.test.web.servlet.*;

import com.fasterxml.jackson.databind.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerTest {

	@Autowired
	MockMvc mockMvc;
	
	@Autowired
	ObjectMapper objectMapper;

	@Test
	public void createProduct() throws Exception {
		Product product = Product.builder()
				.product_no(10001)
				.product_name("�깉 �긽�뭹 10001")
				.brand_name("�뿤�씪")
				.category_no(2)
				.product_price(29000)
				.build();

		mockMvc.perform(post("/api/products/10001").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(product))).andDo(print()).andExpect(status().isCreated());
	}
	
	@Test
	public void getProduct() throws Exception {
		Product product = Product.builder()
				.product_no(500)
				.build();

		mockMvc.perform(get("/api/products/500").contentType(MediaType.APPLICATION_JSON)
				).andDo(print()).andExpect(status().isCreated());
	}
}
