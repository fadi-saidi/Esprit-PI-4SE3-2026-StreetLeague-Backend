package tn.esprit.pi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.pi.domain.*;
import tn.esprit.pi.dto.ShopDTOs.*;
import tn.esprit.pi.repository.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@CrossOrigin("*")
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepository;
    private final ProductReviewRepository productReviewRepository;
    private final UserRepository userRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.base-url}")
    private String baseUrl;

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "name", "price", "stock");

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Sort buildSort(String sortBy, String sortDir) {
        String field = SORTABLE_FIELDS.contains(sortBy) ? sortBy : "id";
        return sortDir.equalsIgnoreCase("desc") ? Sort.by(field).descending() : Sort.by(field).ascending();
    }

    private ProductResponse toProductResponse(Product p) {
        Integer count = productRepository.countReviewsByProductId(p.getId());
        Double avg = productRepository.avgRatingByProductId(p.getId());
        return new ProductResponse(
                p.getId(), p.getName(), p.getPrice(), p.getStock(),
                p.getCategory(), p.getImage(), p.getSportType(),
                count != null ? count : 0,
                avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0
        );
    }

    private ReviewResponse toReviewResponse(ProductReview r) {
        return new ReviewResponse(
                r.getId(), r.getRating(), r.getComment(),
                r.getCreatedAt() != null ? r.getCreatedAt().toString() : null,
                r.getUser() != null ? r.getUser().getId() : null,
                r.getUser() != null ? r.getUser().getUsername() : null
        );
    }

    private Map<String, Object> toPageResponse(Page<Product> page) {
        Map<String, Object> response = new HashMap<>();
        response.put("content", page.getContent().stream().map(this::toProductResponse).toList());
        response.put("totalElements", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());
        response.put("currentPage", page.getNumber());
        response.put("pageSize", page.getSize());
        return response;
    }

    // ─── Public ───────────────────────────────────────────────────────────────

    @GetMapping
    public Map<String, Object> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        return toPageResponse(productRepository.findAll(PageRequest.of(page, pageSize, buildSort(sortBy, sortDir))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(p -> ResponseEntity.ok(toProductResponse(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public Map<String, Object> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) SportType sportType,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        return toPageResponse(productRepository.searchProducts(name, category, sportType, minPrice, maxPrice,
                PageRequest.of(page, pageSize, buildSort(sortBy, sortDir))));
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<List<ReviewResponse>> getProductReviews(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(p -> ResponseEntity.ok(
                        productReviewRepository.findByProductOrderByCreatedAtDesc(p)
                                .stream().map(this::toReviewResponse).toList()))
                .orElse(ResponseEntity.notFound().build());
    }

    // ─── Authenticated ────────────────────────────────────────────────────────

    @PostMapping("/{id}/reviews")
    public ResponseEntity<?> addReview(@PathVariable Long id,
                                       @Valid @RequestBody ReviewRequest request,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) return ResponseEntity.notFound().build();

        return userRepository.findByEmail(userDetails.getUsername())
                .map(user -> {
                    if (productReviewRepository.existsByProductAndUser(product, user))
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(Map.of("error", "You already reviewed this product"));
                    ProductReview review = new ProductReview();
                    review.setProduct(product);
                    review.setUser(user);
                    review.setRating(request.rating());
                    review.setComment(request.comment());
                    review.setCreatedAt(LocalDateTime.now());
                    return ResponseEntity.ok((Object) toReviewResponse(productReviewRepository.save(review)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/reviews/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable Long id, @PathVariable Long reviewId,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return productReviewRepository.findById(reviewId)
                .map(review -> {
                    User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
                    if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).<Void>build();
                    boolean isOwner = review.getUser() != null && review.getUser().getId().equals(user.getId());
                    boolean isAdmin = user.getRole().name().equals("ADMIN");
                    if (!isOwner && !isAdmin) return ResponseEntity.status(HttpStatus.FORBIDDEN).<Void>build();
                    productReviewRepository.delete(review);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ─── Admin ────────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        Product product = new Product();
        product.setName(request.name());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setCategory(request.category());
        product.setImage(request.image());
        product.setSportType(request.sportType());
        return ResponseEntity.status(HttpStatus.CREATED).body(toProductResponse(productRepository.save(product)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id,
                                                         @Valid @RequestBody ProductRequest request) {
        return productRepository.findById(id)
                .map(product -> {
                    product.setName(request.name());
                    product.setPrice(request.price());
                    product.setStock(request.stock());
                    product.setCategory(request.category());
                    product.setImage(request.image());
                    product.setSportType(request.sportType());
                    return ResponseEntity.ok(toProductResponse(productRepository.save(product)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadImage(
            @RequestParam(required = false) MultipartFile file,
            @RequestParam(required = false) String imageUrl) {

        if (imageUrl != null && !imageUrl.isBlank()) {
            if (!imageUrl.matches("^https?://.*"))
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid image URL"));
            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
        }
        if (file == null || file.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("error", "No file or URL provided"));
        if (!ALLOWED_TYPES.contains(file.getContentType()))
            return ResponseEntity.badRequest().body(Map.of("error", "Only JPEG, PNG, WEBP, GIF allowed"));

        try {
            Path dir = Paths.get(uploadDir).toAbsolutePath();
            Files.createDirectories(dir);
            String ext = file.getOriginalFilename() != null && file.getOriginalFilename().contains(".")
                    ? file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."))
                    : ".jpg";
            String filename = UUID.randomUUID() + ext;
            Files.copy(file.getInputStream(), dir.resolve(filename));
            return ResponseEntity.ok(Map.of("imageUrl", baseUrl + "/uploads/products/" + filename));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to save file"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(product -> {
                    productRepository.delete(product);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/low-stock")
    public List<ProductResponse> getLowStockProducts(@RequestParam(defaultValue = "5") Integer threshold) {
        return productRepository.findAll().stream()
                .filter(p -> p.getStock() != null && p.getStock() < threshold)
                .map(this::toProductResponse)
                .toList();
    }
}
