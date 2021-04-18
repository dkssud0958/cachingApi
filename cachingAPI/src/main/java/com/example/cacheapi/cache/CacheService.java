package com.example.cacheapi.cache;

import java.util.*;

import org.modelmapper.*;
import org.springframework.boot.*;
import org.springframework.stereotype.*;

import com.example.cacheapi.categories.*;
import com.example.cacheapi.products.*;

@Component
public class CacheService implements ApplicationRunner {

	private final CategoryRepository categoryRepository;

	private final ProductRepository productRepository;

	private final ModelMapper modelMapper;

	public CacheService(CategoryRepository categoryRepository, ProductRepository productRepository,
			ModelMapper modelMapper) {
		this.categoryRepository = categoryRepository;
		this.productRepository = productRepository;
		this.modelMapper = modelMapper;
	}

	public static final Map<Integer, String> categoryCache = new LinkedHashMap<Integer, String>();
	// 상품 캐시 사이즈 500개로 설정
	public static final LRUCacheMap<Integer, ProductDto> productCache = LRUCacheMap.newInstance(500);

	//캐시유효시간을 설정하여 LRU의 단점인 지속적으로 사용되는 데이터 갱신을 목적으로 함
	private final long cacheDuration = 30 * 1000L; // 캐시유효시간 30초
	private long categoryGroupsLoadTime;
	private long productGroupsLoadTime;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		System.out.println("서버 기동 시 캐시에 기본 데이터 저장");
		//카테고리(카테고리번호, 카테고리명은 전체 메모리 로드
		//캐시 만료시간 설정하여 만료 시 전체 재로드
		initializeCache("category");

		//상품 초기 적재
		List<Product> productList = productRepository.getMultiProduct();
		productCachePut(productList);
		productGroupsLoadTime = System.currentTimeMillis();

		System.out.println("productCache  : " + Arrays.asList(productCache));
	}

	//상품리스트 캐시 저장(초기화, 갱신)
	public void productCachePut(List<Product> productList) {
		for (Product product : productList) {
			ProductDto productDto = modelMapper.map(product, ProductDto.class);
			productDto.setCategory_name(categoryCache.get(product.getCategory_no()));
			productCache.put(product.getProduct_no(), productDto);
		}
	}

	public void initializeCache(String type) {
		long now = System.currentTimeMillis();
		if (type.equals("category")) {
			synchronized (categoryCache) {
				categoryCache.clear();
				for (Object[] ob : categoryRepository.getCategory()) {
					categoryCache.put((Integer) ob[0], (String) ob[1]);
				}
				System.out.println("initialize categories : " + Arrays.asList(categoryCache));
			}

			//캐시 로드된 시간 갱신
			categoryGroupsLoadTime = now;
		} else if (type.equals("product")) {
			//기존 저장된 캐시에서 PRODUCT_NO Array로 변환하여 저장
			Integer [] product_nos = productCache.keySet().toArray(new Integer[500]);

			//캐시 삭제
			productCache.clear();

			//PRODUCT_NO Array로 캐시 정보 갱신
			productCachePut(productRepository.getProductListRefresh(product_nos));
			System.out.println("refresh product : " + Arrays.asList(productCache));

			//캐시 로드된 시간 갱신
			productGroupsLoadTime = now;
		}
	}

	//카테고리번호로 카테고리명 조회 시 사용
	public String findCategoryGroupName(Integer category_no) {
		long now = System.currentTimeMillis();
		//카테고리 캐시가 비었거나 캐시유효시간 초과 시 캐시 삭제 후 재 로드
		if (categoryCache.isEmpty() || now - categoryGroupsLoadTime > cacheDuration) {
			initializeCache("category");
		}
		return categoryCache.get(category_no);
	}

	//카테고리번호로 상품 목록 조회 시 사용
	//카테고리 번호에 해당하는 상품은 캐시버퍼를 넘길 확률이 높으므로 전체 클리어 후 저장
	public List<ProductDto> findProductsByCategoryNo(Integer category_no) {
		List<Product> productlist = this.productRepository.getProductListWithCategory(category_no);
		if (!productlist.isEmpty()) {
			List<ProductDto> listDto = new ArrayList<ProductDto>();
			productCache.clear();
			//데이터 로드 중 캐시가 삭제될 것을 방지하기 위해 캐시 로드시간 갱신
			productGroupsLoadTime = System.currentTimeMillis();
			for (Product product : productlist) {
				ProductDto productDto = modelMapper.map(product, ProductDto.class);
				productDto.setCategory_name(categoryCache.get(product.getCategory_no()));
				productCache.put(product.getProduct_no(), productDto);
				listDto.add(productDto);
			}
			System.out.println("productCache  : " + Arrays.asList(productCache));
			productGroupsLoadTime = System.currentTimeMillis();
			return listDto;
		}
		return null;
	}

	public Optional<ProductDto> getProductInfo(Integer product_no) {
		long now = System.currentTimeMillis();
		if (now - productGroupsLoadTime > cacheDuration) {
			initializeCache("product");
		}
		if (productCache.get(product_no) != null) {
			System.out.println("캐시에서 상품 정보 리턴(product_no=" + product_no + ")");
			return Optional.of(productCache.get(product_no));
		} else {
			Optional<Product> product = productRepository.findById(product_no);
			if (product.isPresent()) {
				ProductDto productDto = modelMapper.map(product.get(), ProductDto.class);
				productDto.setCategory_name(categoryCache.get(product.get().getCategory_no()));
				productCache.put(product.get().getProduct_no(), productDto);
				return Optional.of(productDto);
			}
			return null;
		}
	}

	//상품 캐시에 적용
	//LRU(Least Recently Used) 알고리즘 사용하였으며, 아래의 코드 대로 가장 오랫동안 사용되지 않은 데이터 삭제
	//LinkedHashMap의 특성 중 accessOrder를 true로 사용하여 캐시에 있던 데이터 조회 시 최근 사용한 것으로 인지
	//Cache miss 발생 시 removeEldestEntry 메소드 이용하여 가장 오랫동안 사용되지 않은 데이터 삭제
	//계속 사용되는 데이터는 갱신되지 않을 수 있어 캐시만료시간 설정하여,
	//기존 캐시 데이터 기반으로 갱신 진행
	private static class LRUCacheMap<K, V> extends LinkedHashMap<K, V> {

		private static final long serialVersionUID = 1L;
		private final int capacity;

		public LRUCacheMap(int capacity) {
			super(capacity, 0.75F, true); // 순서모드 사용
			this.capacity = capacity;
		}

		public static <K, V> LRUCacheMap<K, V> newInstance(int size) {
			return new LRUCacheMap<K, V>(size);
		}

		@Override
		protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
			return size() > capacity;
		}

		@Override
		public V put(K key, V value) {
			return super.put(key, value);
		}

		@Override
		public V get(Object key) {
			return super.getOrDefault(key, null);
		}
	}

}
