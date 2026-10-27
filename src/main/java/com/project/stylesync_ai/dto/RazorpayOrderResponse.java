package com.project.stylesync_ai.dto;

public class RazorpayOrderResponse {

    private Long localOrderId;
    private String razorpayOrderId;
    private long amount;
    private String currency;
    private String keyId;

    public RazorpayOrderResponse(
            Long localOrderId,
            String razorpayOrderId,
            long amount,
            String currency,
            String keyId) {
        this.localOrderId = localOrderId;
        this.razorpayOrderId = razorpayOrderId;
        this.amount = amount;
        this.currency = currency;
        this.keyId = keyId;
    }

    public Long getLocalOrderId() {
        return localOrderId;
    }

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public long getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getKeyId() {
        return keyId;
    }
}