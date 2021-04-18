package com.example.cacheapi.categories;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.Test;
import org.junit.jupiter.api.*;
import org.junit.runner.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.context.*;
import org.springframework.test.context.junit4.*;
import org.springframework.test.web.servlet.*;

import com.fasterxml.jackson.databind.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class CategoryControllerTests {

	@Autowired
	MockMvc mockMvc;
	
	@Autowired
	ObjectMapper objectMapper;

	@Test
	@Order(1)
	@DisplayName("移댄뀒怨좊━ 異붽� �뀒�뒪�듃")
	public void createCategory() throws Exception {
		Category category = Category.builder()
				.category_name("�냼�뭹�븯�쐞 移댄뀒怨좊━11")
				.parent_no(Integer.valueOf(4))
				.depth(2)
				.build();

		mockMvc.perform(post("/api/categories/11")
				.content(objectMapper.writeValueAsString(category)))
				.andDo(print())
				.andExpect(status().isCreated());
	}

	@Test
	@Order(2)
	@DisplayName("移댄뀒怨좊━紐� 蹂�寃� �뀒�뒪�듃")
	public void updateCategory() throws Exception {
		Category category = Category.builder()
				.category_name("移댄뀒怨좊━11 �씠由꾨�寃�")
				.build();

		mockMvc.perform(patch("/api/categories/11")
				.content(objectMapper.writeValueAsString(category)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("category_name").value("移댄뀒怨좊━11 �씠由꾨�寃�"));
	}

	@Order(3)
	@Test
	@DisplayName("移댄뀒怨좊━ �궘�젣 �뀒�뒪�듃")
	public void deleteCategory() throws Exception {

		mockMvc.perform(delete("/api/categories/11"))
				.andDo(print())
				.andExpect(status().isOk());

	}

	@Order(4)
	@Test
	@DisplayName("移댄뀒怨좊━紐� 議고쉶 �뀒�뒪�듃")
	public void getCategory() throws Exception {

		mockMvc.perform(get("/api/categories/4"))
				.andDo(print())
				.andExpect(status().isOk());

	}
	
	@Order(5)
	@Test
	@DisplayName("移댄뀒怨좊━紐� 議고쉶 �뀒�뒪�듃&罹먯떆�뿉�꽌 議고쉶 �뀒�뒪�듃")
	public void getCategory2() throws Exception {

		mockMvc.perform(get("/api/categories/4"))
				.andDo(print())
				.andExpect(status().isOk());

	}
}
