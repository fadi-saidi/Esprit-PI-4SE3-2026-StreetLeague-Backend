package tn.esprit.pi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi.domain.*;
import tn.esprit.pi.repository.PlayerMerchRepository;
import tn.esprit.pi.repository.ProductRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class PlayerMerchService {

    private final PlayerMerchRepository playerMerchRepository;
    private final ProductRepository productRepository;
    private final WalletService walletService;

    /**
     * Process sale of player merchandise - credits seller's wallet
     */
    public void processPlayerMerchSale(Product product, int quantitySold, double totalSaleAmount) {
        // Find the player merchandise that created this product
        PlayerMerch playerMerch = playerMerchRepository.findAll().stream()
                .filter(pm -> pm.getProduct() != null && pm.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);
        
        if (playerMerch != null && playerMerch.getSeller() != null && playerMerch.getSeller().getUser() != null) {
            User seller = playerMerch.getSeller().getUser();
            
            // Calculate sale amount per unit
            double unitPrice = totalSaleAmount / quantitySold;
            
            // Process wallet credit for seller
            walletService.processPlayerMerchSale(seller, totalSaleAmount, product.getName());
            
            // Update stock in player merch record (optional - for tracking)
            if (playerMerch.getStock() != null) {
                playerMerch.setStock(Math.max(0, playerMerch.getStock() - quantitySold));
                playerMerchRepository.save(playerMerch);
            }
        }
    }

    /**
     * Check if a product is player merchandise
     */
    public boolean isPlayerMerchandise(Product product) {
        return playerMerchRepository.findAll().stream()
                .anyMatch(pm -> pm.getProduct() != null && pm.getProduct().getId().equals(product.getId()));
    }

    /**
     * Get player merchandise by product
     */
    public PlayerMerch getPlayerMerchByProduct(Product product) {
        return playerMerchRepository.findAll().stream()
                .filter(pm -> pm.getProduct() != null && pm.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);
    }
}