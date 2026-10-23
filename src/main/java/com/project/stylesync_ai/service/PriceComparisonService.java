
package com.project.stylesync_ai.service;

import com.project.stylesync_ai.pricecomparison.PriceOffer;
import org.springframework.stereotype.Service;
import java.util.Objects;
import java.util.Comparator;
import java.util.List;

@Service
public class PriceComparisonService {

    public List<PriceOffer> comparePrices(List<PriceOffer> offers) {
       Objects.requireNonNull(offers, "Offers list must not be null");

       if(offers.stream().anyMatch(Objects::isNull)){
           throw new IllegalArgumentException(
                   "Offers list must not contain null values"
           );
       }

       if(offers.stream().anyMatch(offer -> offer.getPrice() == null)){
           throw new IllegalArgumentException(
                   "Offer price must not be null"
           );
       }

       return offers.stream()
               .sorted(Comparator.comparing(PriceOffer::getPrice))
               .toList();
    }
}
