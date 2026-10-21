
package com.project.stylesync_ai.pricecomparison;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class PriceComparisonService {

    public List<PriceOffer> comparePrices(List<PriceOffer> offers) {
        return offers.stream()
                .sorted(Comparator.comparing(PriceOffer::getPrice))
                .toList();
    }
}
