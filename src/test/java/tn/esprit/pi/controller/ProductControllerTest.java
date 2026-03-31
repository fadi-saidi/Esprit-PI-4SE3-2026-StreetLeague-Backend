package tn.esprit.pi.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tn.esprit.pi.domain.Product;
import tn.esprit.pi.domain.User;
import tn.esprit.pi.repository.ProductRepository;
import tn.esprit.pi.repository.ProductReviewRepository;
import tn.esprit.pi.repository.UserRepository;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock private ProductRepository productRepository;
    @Mock private ProductReviewRepository productReviewRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserDetails userDetails;

    @InjectMocks private ProductController productController;

    private MockMvc mockMvc;
    private Product product;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
        product = new Product();
        product.setId(1L);
        product.setName("Ballon de Foot");
        product.setPrice(25.0);
        product.setStock(10);
    }

    @Test
    void getAllProducts_Success() throws Exception {
        Page<Product> page = new PageImpl<>(Collections.singletonList(product));
        when(productRepository.findAll(any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Ballon de Foot"));
    }

    @Test
    void getProductById_NotFound() throws Exception {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/products/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProduct_Success() throws Exception {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        mockMvc.perform(delete("/products/1"))
                .andExpect(status().isOk());
    }
}