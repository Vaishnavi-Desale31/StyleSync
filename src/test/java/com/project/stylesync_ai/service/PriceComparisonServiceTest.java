package com.project.stylesync_ai.service;

import com.project.stylesync_ai.entity.PriceOfferEntity;
import com.project.stylesync_ai.entity.Product;
import com.project.stylesync_ai.pricecomparison.PriceOffer;
import com.project.stylesync_ai.repository.PriceRepository;
import com.project.stylesync_ai.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceComparisonServiceTest {

    @Mock
    private PriceRepository priceRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private PriceComparisonService service;

    @Test
    void comparePrices_shouldSortOffersByAscendingPrice() {
        List<PriceOffer> offers = List.of(
                new PriceOffer("T-Shirt", "Amazon",
                        new BigDecimal("899.00"), "url1"),
                new PriceOffer("T-Shirt", "Flipkart",
                        new BigDecimal("699.00"), "url2"),
                new PriceOffer("T-Shirt", "Myntra",
                        new BigDecimal("799.00"), "url3")
        );

        List<PriceOffer> result = service.comparePrices(offers);

        assertEquals(3, result.size());
        assertEquals("Flipkart", result.get(0).getPlatform());
        assertEquals(new BigDecimal("699.00"), result.get(0).getPrice());
        assertEquals("Myntra", result.get(1).getPlatform());
        assertEquals("Amazon", result.get(2).getPlatform());
    }

    @Test
    void comparePrices_shouldReturnSingleOffer() {
        List<PriceOffer> result = service.comparePrices(List.of(
                new PriceOffer("T-Shirt", "Amazon",
                        new BigDecimal("899.00"), "url1")
        ));

        assertEquals(1, result.size());
        assertEquals("Amazon", result.get(0).getPlatform());
    }

    @Test
    void comparePrices_shouldReturnEmptyListWhenNoOffers() {
        assertTrue(service.comparePrices(List.of()).isEmpty());
    }

    @Test
    void comparePrices_shouldRejectNullOffersList() {
        assertThrows(NullPointerException.class,
                () -> service.comparePrices(null));
    }

    @Test
    void comparePrices_shouldRejectNullOffer() {
        List<PriceOffer> offers = Arrays.asList(
                new PriceOffer("T-Shirt", "Amazon",
                        new BigDecimal("899.00"), "url"),
                null
        );

        assertThrows(IllegalArgumentException.class,
                () -> service.comparePrices(offers));
    }

    @Test
    void comparePrices_shouldRejectOffersWithNullPrice() {
        List<PriceOffer> offers = List.of(
                new PriceOffer("T-Shirt", "Amazon", null, "url")
        );

        assertThrows(IllegalArgumentException.class,
                () -> service.comparePrices(offers));
    }

    @Test
    void compareProductPrices_shouldReturnOffersForExistingProduct() {
        Product product = new Product(
                "Black T-Shirt", "Cotton shirt", 799.0,
                "StyleSync", "image.jpg", "L", "Black", null
        );

        PriceOfferEntity flipkart = new PriceOfferEntity(
                product, "Flipkart",
                new BigDecimal("699.00"), "https://example.com/flipkart"
        );

        PriceOfferEntity amazon = new PriceOfferEntity(
                product, "Amazon",
                new BigDecimal("899.00"), "https://example.com/amazon"
        );

        when(productRepository.findById(2L))
                .thenReturn(Optional.of(product));

        when(priceRepository.findByProduct_IdOrderByPriceAsc(2L))
                .thenReturn(List.of(flipkart, amazon));

        List<PriceOffer> result = service.compareProductPrices(2L);

        assertEquals(2, result.size());
        assertEquals("Black T-Shirt", result.get(0).getProductName());
        assertEquals("Flipkart", result.get(0).getPlatform());
        assertEquals(new BigDecimal("699.00"), result.get(0).getPrice());
        assertEquals("Amazon", result.get(1).getPlatform());

        verify(productRepository).findById(2L);
        verify(priceRepository).findByProduct_IdOrderByPriceAsc(2L);
    }

    @Test
    void compareProductPrices_shouldReturnEmptyListWhenNoOffersExist() {
        Product product = new Product(
                "Black T-Shirt", "Cotton shirt", 799.0,
                "StyleSync", "image.jpg", "L", "Black", null
        );

        when(productRepository.findById(2L))
                .thenReturn(Optional.of(product));

        when(priceRepository.findByProduct_IdOrderByPriceAsc(2L))
                .thenReturn(List.of());

        assertTrue(service.compareProductPrices(2L).isEmpty());
    }

    @Test
    void compareProductPrices_shouldReturnNotFoundForMissingProduct() {
        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.compareProductPrices(999L)
        );

        assertEquals(404, exception.getStatusCode().value());
        verifyNoInteractions(priceRepository);
    }

    @Test
    void compareProductPrices_shouldRejectInvalidProductId() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.compareProductPrices(0L)
        );

        assertEquals(400, exception.getStatusCode().value());
        verifyNoInteractions(productRepository, priceRepository);
    }
}
