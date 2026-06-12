package com.example.myapplication;

public class Promotion {
    public String shopName, address, promoTitle, promoDescription, discount, validUntil;
    public double distanceKm;

    public Promotion(String shopName, String address, String promoTitle,
                     String promoDescription, String discount,
                     String validUntil, double distanceKm) {
        this.shopName = shopName;
        this.address = address;
        this.promoTitle = promoTitle;
        this.promoDescription = promoDescription;
        this.discount = discount;
        this.validUntil = validUntil;
        this.distanceKm = distanceKm;
    }
}