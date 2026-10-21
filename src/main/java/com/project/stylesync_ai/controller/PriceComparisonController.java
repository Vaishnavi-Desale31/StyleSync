package com.project.stylesync_ai.controller;
import org.springframework.web.bind.annotation.RequestParam;
import com.project.stylesync_ai.pricecomparison.PriceOffer;
import com.project.stylesync_ai.pricecomparison.PriceComparisonService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/price-comparison")
public class PriceComparisonController {

    private final PriceComparisonService priceComparisonService;

    public PriceComparisonController(PriceComparisonService priceComparisonService) {
        this.priceComparisonService = priceComparisonService;
    }

    @GetMapping("/test")
    public List<PriceOffer> comparePrices(
            @RequestParam(defaultValue = "Premium Black T-Shirt")
             String productName) {

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

}
