package com.project.stylesync_ai.controller;

import com.project.stylesync_ai.dto.PriceOfferRequest;
import com.project.stylesync_ai.entity.PriceOfferEntity;
import com.project.stylesync_ai.pricecomparison.PriceOffer;
import com.project.stylesync_ai.service.PriceComparisonService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/price-comparison")
public class PriceComparisonController {

    private final PriceComparisonService priceComparisonService;

    public PriceComparisonController(
            PriceComparisonService priceComparisonService) {
        this.priceComparisonService = priceComparisonService;
    }

    // Existing demo endpoint
    @GetMapping("/test")
    public List<PriceOffer> comparePrices(
            @RequestParam(required = false) String productName) {

        if (productName == null || productName.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product name must not be blank"
            );
        }

        productName = productName.trim();

        List<PriceOffer> offers = List.of(
                new PriceOffer(
                        productName,
                        "Amazon",
                        new BigDecimal("899.00"),
                        "https://example.com/amazon/tshirt"
                ),
                new PriceOffer(
                        productName,
                        "Flipkart",
                        new BigDecimal("699.00"),
                        "https://example.com/flipkart/tshirt"
                ),
                new PriceOffer(
                        productName,
                        "Myntra",
                        new BigDecimal("799.00"),
                        "https://example.com/myntra/tshirt"
                )
        );

        return priceComparisonService.comparePrices(offers);
    }

    // Existing database-backed price comparison endpoint
    @GetMapping("/{productId}")
    public List<PriceOffer> compareProductPrices(
            @PathVariable Long productId) {

        return priceComparisonService.compareProductPrices(productId);
    }

    // Admin: create a price offer for a product
    @PostMapping("/{productId}/offers")
    public ResponseEntity<PriceOfferEntity> createOffer(
            @PathVariable Long productId,
            @Valid @RequestBody PriceOfferRequest request) {

        PriceOfferEntity createdOffer =
                priceComparisonService.createOffer(productId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdOffer);
    }

    // Admin: update an existing price offer
    @PutMapping("/offers/{offerId}")
    public ResponseEntity<PriceOfferEntity> updateOffer(
            @PathVariable Long offerId,
            @Valid @RequestBody PriceOfferRequest request) {

        PriceOfferEntity updatedOffer =
                priceComparisonService.updateOffer(offerId, request);

        return ResponseEntity.ok(updatedOffer);
    }

    // Admin: delete an existing price offer
    @DeleteMapping("/offers/{offerId}")
    public ResponseEntity<Void> deleteOffer(
            @PathVariable Long offerId) {

        priceComparisonService.deleteOffer(offerId);

        return ResponseEntity.noContent().build();
    }
}