package tn.esprit.pi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi.domain.*;
import tn.esprit.pi.dto.ShopDTOs.*;
import tn.esprit.pi.repository.*;
import tn.esprit.pi.service.WalletService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin("*")
@RequestMapping("/player-merch")
@RequiredArgsConstructor
public class PlayerMerchController {

    private final PlayerMerchRepository playerMerchRepository;
    private final PlayerProfileRepository playerProfileRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final WalletService walletService;

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private PlayerMerchResponse toPlayerMerchResponse(PlayerMerch merch) {
        return new PlayerMerchResponse(
                merch.getId(),
                merch.getName(),
                merch.getDescription(),
                merch.getPrice(),
                merch.getStock(),
                merch.getCategory(),
                merch.getImage(),
                merch.getSportType(),
                merch.getStatus(),
                merch.getSeller() != null && merch.getSeller().getUser() != null 
                    ? merch.getSeller().getUser().getUsername() : null,
                merch.getSeller() != null ? merch.getSeller().getId() : null,
                merch.getSubmittedAt() != null ? merch.getSubmittedAt().toString() : null,
                merch.getApprovedAt() != null ? merch.getApprovedAt().toString() : null,
                merch.getRejectionReason()
        );
    }

    private Map<String, Object> toPageResponse(Page<PlayerMerch> page) {
        Map<String, Object> response = new HashMap<>();
        response.put("content", page.getContent().stream().map(this::toPlayerMerchResponse).toList());
        response.put("totalElements", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());
        response.put("currentPage", page.getNumber());
        response.put("pageSize", page.getSize());
        return response;
    }

    // ─── Public Endpoints ─────────────────────────────────────────────────────

    @GetMapping("/approved")
    public Map<String, Object> getApprovedMerch(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int pageSize) {
        Page<PlayerMerch> approvedMerch = playerMerchRepository.findByStatusOrderBySubmittedAtDesc(
                MerchStatus.APPROVED, PageRequest.of(page, pageSize));
        return toPageResponse(approvedMerch);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlayerMerchResponse> getMerchById(@PathVariable Long id) {
        return playerMerchRepository.findById(id)
                .map(merch -> ResponseEntity.ok(toPlayerMerchResponse(merch)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ─── Player Endpoints ─────────────────────────────────────────────────────

    @PostMapping("/submit")
    public ResponseEntity<?> submitMerch(@Valid @RequestBody PlayerMerchRequest request,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<User> userOpt = userRepository.findByEmail(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        User user = userOpt.get();
        if (!user.getRole().equals(Role.PLAYER)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only players can submit merchandise"));
        }
        
        PlayerProfile playerProfile = playerProfileRepository.findById(user.getId()).orElse(null);
        if (playerProfile == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Player profile not found"));
        }
        
        PlayerMerch merch = new PlayerMerch();
        merch.setName(request.name());
        merch.setDescription(request.description());
        merch.setPrice(request.price());
        merch.setStock(request.stock());
        merch.setCategory(request.category());
        merch.setImage(request.image());
        merch.setSportType(request.sportType());
        merch.setSeller(playerProfile);
        merch.setSubmittedAt(LocalDateTime.now());
        merch.setStatus(MerchStatus.PENDING);
        
        PlayerMerch saved = playerMerchRepository.save(merch);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toPlayerMerchResponse(saved));
    }

    @GetMapping("/my-submissions")
    public ResponseEntity<List<PlayerMerchResponse>> getMySubmissions(Authentication authentication) {
        System.out.println("=== DEBUG: getMySubmissions called ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Principal: " + (authentication != null ? authentication.getName() : "null"));
        
        if (authentication == null || authentication.getName() == null) {
            System.out.println("ERROR: Authentication is null or has no name");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        String email = authentication.getName();
        Optional<User> userOpt = userRepository.findByEmail(email);
        System.out.println("User lookup result: " + userOpt.isPresent());
        if (userOpt.isEmpty()) {
            System.out.println("ERROR: User not found for email: " + email);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        User user = userOpt.get();
        System.out.println("Found user: ID=" + user.getId() + ", email=" + user.getEmail() + ", role=" + user.getRole());
        
        PlayerProfile playerProfile = playerProfileRepository.findById(user.getId()).orElse(null);
        System.out.println("PlayerProfile lookup result: " + (playerProfile != null ? "found" : "not found"));
        if (playerProfile == null) {
            System.out.println("ERROR: PlayerProfile not found for user ID: " + user.getId());
            return ResponseEntity.notFound().build();
        }
        
        List<PlayerMerch> submissions = playerMerchRepository.findBySellerOrderBySubmittedAtDesc(playerProfile);
        System.out.println("Found " + submissions.size() + " submissions");
        List<PlayerMerchResponse> responses = submissions.stream().map(this::toPlayerMerchResponse).toList();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMerch(@PathVariable Long id,
                                         @Valid @RequestBody PlayerMerchRequest request,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<PlayerMerch> merchOpt = playerMerchRepository.findById(id);
        if (merchOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        PlayerMerch merch = merchOpt.get();
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null || !merch.getSeller().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You can only update your own merchandise"));
        }
        
        if (merch.getStatus() != MerchStatus.PENDING) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Can only update pending merchandise"));
        }
        
        merch.setName(request.name());
        merch.setDescription(request.description());
        merch.setPrice(request.price());
        merch.setStock(request.stock());
        merch.setCategory(request.category());
        merch.setImage(request.image());
        merch.setSportType(request.sportType());
        
        return ResponseEntity.ok(toPlayerMerchResponse(playerMerchRepository.save(merch)));
    }

    // ─── Admin Endpoints ──────────────────────────────────────────────────────

    @GetMapping("/admin/pending")
    public Map<String, Object> getPendingMerch(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int pageSize) {
        Page<PlayerMerch> pendingMerch = playerMerchRepository.findByStatusOrderBySubmittedAtDesc(
                MerchStatus.PENDING, PageRequest.of(page, pageSize));
        return toPageResponse(pendingMerch);
    }

    @GetMapping("/admin/stats")
    public ResponseEntity<Map<String, Object>> getAdminStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("pending", playerMerchRepository.countByStatus(MerchStatus.PENDING));
        stats.put("approved", playerMerchRepository.countByStatus(MerchStatus.APPROVED));
        stats.put("rejected", playerMerchRepository.countByStatus(MerchStatus.REJECTED));
        return ResponseEntity.ok(stats);
    }

    @PutMapping("/admin/{id}/approve")
    public ResponseEntity<?> approveMerch(@PathVariable Long id,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<PlayerMerch> merchOpt = playerMerchRepository.findById(id);
        if (merchOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        PlayerMerch merch = merchOpt.get();
        User admin = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (admin == null || !admin.getRole().equals(Role.ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin access required"));
        }
        
        if (merch.getStatus() != MerchStatus.PENDING) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Can only approve pending merchandise"));
        }
        
        // Create product from approved merch
        Product product = new Product();
        product.setName(merch.getName());
        product.setPrice(merch.getPrice());
        product.setStock(merch.getStock());
        product.setCategory(merch.getCategory());
        product.setImage(merch.getImage());
        product.setSportType(merch.getSportType());
        
        // Find or create a default shop for player merchandise
        Shop playerShop = shopRepository.findAll().stream()
                .filter(s -> s.getName().equals("Player Marketplace"))
                .findFirst()
                .orElseGet(() -> {
                    Shop newShop = new Shop();
                    newShop.setName("Player Marketplace");
                    newShop.setDescription("Player-submitted merchandise");
                    return shopRepository.save(newShop);
                });
        
        product.setShop(playerShop);
        Product savedProduct = productRepository.save(product);
        
        // Update merch status
        merch.setStatus(MerchStatus.APPROVED);
        merch.setApprovedBy(admin);
        merch.setApprovedAt(LocalDateTime.now());
        merch.setProduct(savedProduct);
        
        return ResponseEntity.ok(toPlayerMerchResponse(playerMerchRepository.save(merch)));
    }

    @PutMapping("/admin/{id}/reject")
    public ResponseEntity<?> rejectMerch(@PathVariable Long id,
                                         @Valid @RequestBody MerchApprovalRequest request,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<PlayerMerch> merchOpt = playerMerchRepository.findById(id);
        if (merchOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        PlayerMerch merch = merchOpt.get();
        User admin = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (admin == null || !admin.getRole().equals(Role.ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin access required"));
        }
        
        if (merch.getStatus() != MerchStatus.PENDING) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Can only reject pending merchandise"));
        }
        
        merch.setStatus(MerchStatus.REJECTED);
        merch.setApprovedBy(admin);
        merch.setApprovedAt(LocalDateTime.now());
        merch.setRejectionReason(request.reason());
        
        return ResponseEntity.ok(toPlayerMerchResponse(playerMerchRepository.save(merch)));
    }
}