package com.project.stylesync_ai.service;

import com.project.stylesync_ai.entity.PriceOfferEntity;
import com.project.stylesync_ai.pricecomparison.PriceOffer;
import com.project.stylesync_ai.repository.PriceRepository;
import com.project.stylesync_ai.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.Comparator;

@Service
public class PriceComparisonService {

    private final PriceRepository priceRepository;
    private final ProductRepository productRepository;

    public PriceComparisonService(
            PriceRepository priceRepository,
            ProductRepository productRepository) {
        this.priceRepository = priceRepository;
        this.productRepository = productRepository;
    }

    // Existing method used by the current /test endpoint.
    public List<PriceOffer> comparePrices(List<PriceOffer> offers) {
        Objects.requireNonNull(offers, "Offers list must not be null");

        if (offers.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(
                    "Offers list must not contain null values");
        }

        if (offers.stream().anyMatch(offer -> offer.getPrice() == null)) {
            throw new IllegalArgumentException(
                    "Offer price must not be null");
        }

        return offers.stream()
                .sorted(Comparator.comparing(PriceOffer::getPrice))
                .toList();
    }

    // New method: compare saved offers for a product.
    public List<PriceOffer> compareProductPrices(Long productId) {

        if (productId == null || productId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product ID must be greater than zero");
        }

        var product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found"));

        List<PriceOfferEntity> savedOffers =
                priceRepository.findByProduct_IdOrderByPriceAsc(productId);

        return savedOffers.stream()
                .map(offer -> new PriceOffer(
                        product.getName(),
                        offer.getPlatform(),
                        offer.getPrice(),
                        offer.getProductUrl()))
                .toList();
    }
}
