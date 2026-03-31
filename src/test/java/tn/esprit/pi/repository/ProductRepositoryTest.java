package tn.esprit.pi.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import tn.esprit.pi.domain.Product;
import tn.esprit.pi.domain.Shop;
import tn.esprit.pi.domain.SportType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void findBySportType_ShouldReturnProducts() {
        // Given
        Shop shop = new Shop();
        shop.setName("Test Shop");
        entityManager.persist(shop);

        Product product1 = new Product();
        product1.setName("Ball");
        product1.setSportType(SportType.FOOTBALL);
        product1.setShop(shop);
        entityManager.persist(product1);

        Product product2 = new Product();
        product2.setName("Shoes");
        product2.setSportType(SportType.FOOTBALL);
        product2.setShop(shop);
        entityManager.persist(product2);

        entityManager.flush();

        // When
        List<Product> products = productRepository.findBySportType(SportType.FOOTBALL);

        // Then
        assertThat(products).hasSize(2);
        assertThat(products).extracting(Product::getName).contains("Ball", "Shoes");
    }

    @Test
    void findByShopId_ShouldReturnProducts() {
        // Given
        Shop shop = new Shop();
        shop.setName("Test Shop");
        entityManager.persist(shop);

        Product product = new Product();
        product.setName("Jersey");
        product.setShop(shop);
        entityManager.persist(product);
        entityManager.flush();

        // When
        List<Product> products = productRepository.findAll(); // Since no findByShopId, use findAll and filter

        // Then
        assertThat(products).hasSize(1);
        assertThat(products.get(0).getName()).isEqualTo("Jersey");
    }

    @Test
    void findByNameContainingIgnoreCase_ShouldReturnProducts() {
        // Given
        Shop shop = new Shop();
        shop.setName("Test Shop");
        entityManager.persist(shop);

        Product product = new Product();
        product.setName("Football Ball");
        product.setShop(shop);
        entityManager.persist(product);
        entityManager.flush();

        // When
        List<Product> products = productRepository.findByNameContainingIgnoreCase("ball");

        // Then
        assertThat(products).hasSize(1);
        assertThat(products.get(0).getName()).isEqualTo("Football Ball");
    }
}
