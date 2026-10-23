package com.project.stylesync_ai.service;
import com.project.stylesync_ai.pricecomparison.PriceOffer;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PriceComparisonServiceTest {

    private final PriceComparisonService service =
            new PriceComparisonService();

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
        assertEquals(new BigDecimal("699.00"),
                result.get(0).getPrice());
        assertEquals("Myntra", result.get(1).getPlatform());
        assertEquals("Amazon", result.get(2).getPlatform());
    }

    @Test
    void comparePrices_shouldReturnSingleOffer() {

        List<PriceOffer> offers = List.of(
                new PriceOffer("T-Shirt", "Amazon",
                        new BigDecimal("899.00"), "url1")
        );

        List<PriceOffer> result = service.comparePrices(offers);

        assertEquals(1, result.size());
        assertEquals("Amazon", result.get(0).getPlatform());
    }

    @Test
    void comparePrices_shouldReturnEmptyListWhenNoOffers() {

        List<PriceOffer> result =
                service.comparePrices(List.of());

        assertEquals(0, result.size());
    }

    @Test
    void comparePrices_shouldRejectNullOffersList(){
        assertThrows(
                NullPointerException.class,
                () -> service.comparePrices(null)
        );
    }

    @Test
    void comparePrices_shouldRejectNullOffer(){
        List<PriceOffer> offers = java.util.Arrays.asList(
                new PriceOffer(
                        "T-Shirt",
                        "Amazon",
                        new BigDecimal("899.00"),
                        "url"
                ),
                null
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.comparePrices(offers)
        );
    }

    @Test
    void comparePrices_shouldRejectOffersWithNullPrice(){

        List<PriceOffer> offers = List.of(
                new PriceOffer(
                        "T-Shirt",
                        "Amazon",
                        null,
                        "url"
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.comparePrices(offers)
        );
    }
}
