package tn.esprit.pi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.pi.domain.*;
import tn.esprit.pi.dto.ShopDTOs.*;
import tn.esprit.pi.repository.*;
import tn.esprit.pi.security.jwt.JwtService;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductRepository productRepository;
    @MockitoBean
    private ProductReviewRepository productReviewRepository;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private ShopRepository shopRepository;
    @MockitoBean
    private JwtService jwtService;

    private ObjectMapper objectMapper;
    private Product product;
    private Product product2;
    private User user;
    private User adminUser;
    private ProductReview review;
    private Shop shop;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        shop = new Shop();
        shop.setId(1L);
        shop.setName("Sports Store");

        product = new Product();
        product.setId(1L);
        product.setName("Football Jersey");
        product.setPrice(29.99);
        product.setStock(10);
        product.setCategory("Apparel");
        product.setSportType(SportType.FOOTBALL);
        product.setImage("jersey.jpg");
        product.setShop(shop);

        product2 = new Product();
        product2.setId(2L);
        product2.setName("Basketball Shoes");
        product2.setPrice(89.99);
        product2.setStock(5);
        product2.setCategory("Footwear");
        product2.setSportType(SportType.BASKETBALL);
        product2.setImage("shoes.jpg");
        product2.setShop(shop);

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setUsername("Test User");
        user.setRole(Role.PLAYER);

        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setEmail("admin@example.com");
        adminUser.setUsername("Admin User");
        adminUser.setRole(Role.ADMIN);

        review = new ProductReview();
        review.setId(1L);
        review.setProduct(product);
        review.setUser(user);
        review.setRating(5);
        review.setComment("Great product!");
        review.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void getAllProducts_ShouldReturnProductList() throws Exception {
        List<Product> products = Arrays.asList(product, product2);
        when(productRepository.findAll()).thenReturn(products);

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Football Jersey"))
                .andExpect(jsonPath("$[0].price").value(29.99))
                .andExpect(jsonPath("$[1].name").value("Basketball Shoes"));

        verify(productRepository).findAll();
    }

    @Test
    void getAllProducts_ShouldReturnEmptyListWhenNoProducts() throws Exception {
        when(productRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getProductById_ShouldReturnProduct() throws Exception {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Football Jersey"))
                .andExpect(jsonPath("$.sportType").value("FOOTBALL"))
                .andExpect(jsonPath("$.category").value("Apparel"))
                .andExpect(jsonPath("$.stock").value(10));

        verify(productRepository).findById(1L);
    }

    @Test
    void getProductById_ShouldReturnNotFoundWhenProductDoesNotExist() throws Exception {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/products/999"))
                .andExpect(status().isNotFound());

        verify(productRepository).findById(999L);
    }

    @Test
    void getProductById_ShouldHandleInvalidIdFormat() throws Exception {
        mockMvc.perform(get("/products/invalid"))
                .andExpect(status().isBadRequest());

        verify(productRepository, never()).findById(any());
    }

    @Test
    void searchProducts_ShouldReturnAllWhenNoFilters() throws Exception {
        List<Product> products = Arrays.asList(product, product2);
        Page<Product> page = new PageImpl<>(products, PageRequest.of(0, 10), 2);
        when(productRepository.searchProducts(null, null, null, null, null, any(PageRequest.class)))
                .thenReturn(page);

        mockMvc.perform(get("/products/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void searchProducts_ShouldFilterByName() throws Exception {
        List<Product> products = Arrays.asList(product);
        Page<Product> page = new PageImpl<>(products, PageRequest.of(0, 10), 1);
        when(productRepository.searchProducts(eq("Football"), any(), any(), any(), any(), any(PageRequest.class)))
                .thenReturn(page);

        mockMvc.perform(get("/products/search")
                        .param("name", "Football"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Football Jersey"));
    }

    @Test
    void searchProducts_ShouldFilterBySportType() throws Exception {
        List<Product> products = Arrays.asList(product2);
        Page<Product> page = new PageImpl<>(products, PageRequest.of(0, 10), 1);
        when(productRepository.searchProducts(any(), any(), eq(SportType.BASKETBALL), any(), any(), any(PageRequest.class)))
                .thenReturn(page);

        mockMvc.perform(get("/products/search")
                        .param("sportType", "BASKETBALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].sportType").value("BASKETBALL"));
    }

    @Test
    void searchProducts_ShouldFilterByPriceRange() throws Exception {
        List<Product> products = Arrays.asList(product);
        Page<Product> page = new PageImpl<>(products, PageRequest.of(0, 10), 1);
        when(productRepository.searchProducts(any(), any(), any(), eq(20.0), eq(50.0), any(PageRequest.class)))
                .thenReturn(page);

        mockMvc.perform(get("/products/search")
                        .param("minPrice", "20.0")
                        .param("maxPrice", "50.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void searchProducts_ShouldFilterByCategory() throws Exception {
        List<Product> products = Arrays.asList(product2);
        Page<Product> page = new PageImpl<>(products, PageRequest.of(0, 10), 1);
        when(productRepository.searchProducts(any(), eq("Footwear"), any(), any(), any(), any(PageRequest.class)))
                .thenReturn(page);

        mockMvc.perform(get("/products/search")
                        .param("category", "Footwear"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].category").value("Footwear"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void addProductReview_ShouldCreateReviewSuccessfully() throws Exception {
        ReviewRequest request = new ReviewRequest(5, "Great product!");
        
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(productReviewRepository.existsByProductAndUser(product, user)).thenReturn(false);
        when(productReviewRepository.save(any(ProductReview.class))).thenReturn(review);

        mockMvc.perform(post("/products/1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Review added successfully"));

        verify(productReviewRepository).save(any(ProductReview.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void addProductReview_ShouldReturnBadRequestForDuplicateReview() throws Exception {
        ReviewRequest request = new ReviewRequest(5, "Great product!");
        
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(productReviewRepository.existsByProductAndUser(product, user)).thenReturn(true);

        mockMvc.perform(post("/products/1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("You have already reviewed this product"));

        verify(productReviewRepository, never()).save(any(ProductReview.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void addProductReview_ShouldReturnBadRequestForInvalidRating() throws Exception {
        ReviewRequest request = new ReviewRequest(6, "Great product!"); // Invalid rating > 5
        
        mockMvc.perform(post("/products/1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addProductReview_ShouldReturnUnauthorizedWithoutAuth() throws Exception {
        ReviewRequest request = new ReviewRequest(5, "Great product!");
        
        mockMvc.perform(post("/products/1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getProductReviews_ShouldReturnReviewList() throws Exception {
        ProductReview review2 = new ProductReview();
        review2.setId(2L);
        review2.setProduct(product);
        review2.setUser(adminUser);
        review2.setRating(4);
        review2.setComment("Good quality");
        review2.setCreatedAt(LocalDateTime.now().minusDays(1));

        List<ProductReview> reviews = Arrays.asList(review, review2);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productReviewRepository.findByProductOrderByCreatedAtDesc(product)).thenReturn(reviews);

        mockMvc.perform(get("/products/1/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].rating").value(5))
                .andExpect(jsonPath("$[0].comment").value("Great product!"))
                .andExpect(jsonPath("$[0].username").value("Test User"))
                .andExpect(jsonPath("$[1].rating").value(4))
                .andExpect(jsonPath("$[1].comment").value("Good quality"));

        verify(productReviewRepository).findByProductOrderByCreatedAtDesc(product);
    }

    @Test
    void getProductReviews_ShouldReturnEmptyListWhenNoReviews() throws Exception {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productReviewRepository.findByProductOrderByCreatedAtDesc(product)).thenReturn(List.of());

        mockMvc.perform(get("/products/1/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getProductReviews_ShouldReturnNotFoundForInvalidProduct() throws Exception {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/products/999/reviews"))
                .andExpect(status().isNotFound());

        verify(productReviewRepository, never()).findByProductOrderByCreatedAtDesc(any());
    }

    @Test
    @WithMockUser(username = "admin@example.com")
    void createProduct_ShouldCreateProductSuccessfully() throws Exception {
        ProductRequest request = new ProductRequest(
                "New Product", 49.99, 20, "Equipment", "new-product.jpg", SportType.TENNIS
        );
        
        Product newProduct = new Product();
        newProduct.setId(3L);
        newProduct.setName("New Product");
        newProduct.setPrice(49.99);
        newProduct.setStock(20);
        newProduct.setShop(shop);

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(shopRepository.findById(1L)).thenReturn(Optional.of(shop));
        when(productRepository.save(any(Product.class))).thenReturn(newProduct);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Product"))
                .andExpect(jsonPath("$.price").value(49.99));

        verify(productRepository).save(any(Product.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void createProduct_ShouldReturnForbiddenForNonAdmin() throws Exception {
        ProductRequest request = new ProductRequest(
                "New Product", 49.99, 20, "Equipment", "new-product.jpg", SportType.TENNIS
        );
        
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @WithMockUser(username = "admin@example.com")
    void updateProduct_ShouldUpdateProductSuccessfully() throws Exception {
        ProductRequest request = new ProductRequest(
                "Updated Product", 39.99, 15, "Updated Category", "updated.jpg", SportType.FOOTBALL
        );
        
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(shopRepository.findById(1L)).thenReturn(Optional.of(shop));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        mockMvc.perform(put("/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(productRepository).save(any(Product.class));
    }

    @Test
    @WithMockUser(username = "admin@example.com")
    void deleteProduct_ShouldDeleteProductSuccessfully() throws Exception {
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        doNothing().when(productRepository).delete(product);

        mockMvc.perform(delete("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Product deleted successfully"));

        verify(productRepository).delete(product);
    }

    @Test
    void getProductsByShop_ShouldReturnProductsForShop() throws Exception {
        List<Product> products = Arrays.asList(product, product2);
        when(productRepository.findByShopId(1L)).thenReturn(products);

        mockMvc.perform(get("/products/shop/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(productRepository).findByShopId(1L);
    }
}