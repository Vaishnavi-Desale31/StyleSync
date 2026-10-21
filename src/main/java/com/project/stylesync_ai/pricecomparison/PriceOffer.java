package com.project.stylesync_ai.pricecomparison;

import java.math.BigDecimal;

public class PriceOffer {

    private String productName;

    private String platform;

    private BigDecimal price;

    private String productUrl;

    public PriceOffer(String productName,
                      String platform,
                      BigDecimal price,
                      String productUrl) {
        this.productName = productName;
        this.platform = platform;
        this.price = price;
        this.productUrl = productUrl;
    }

    public String getProductName() {
        return productName;
    }

    public String getPlatform() {
        return platform;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getProductUrl() {
        return productUrl;
    }
}
