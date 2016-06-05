package com.hgyw.bookshare.entities;

import com.annimon.stream.Stream;
import com.hgyw.bookshare.entities.reflection.EntityProperty;
import com.hgyw.bookshare.entities.reflection.EntityReference;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by haim7 on 10/05/2016.
 */
public class BookSummary {

    private BigDecimal minPrice = BigDecimal.ZERO;
    private BigDecimal maxPrice = BigDecimal.ZERO;
    @EntityProperty(disable = true)
    private Map<Rating, Integer> ratingMap = Collections.emptyMap();

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public Map<Rating, Integer> getRatingMap() {
        return Collections.unmodifiableMap(ratingMap);
    }

    public void setRatingMap(Map<Rating, Integer> ratingMap) {
        this.ratingMap = new HashMap<>(ratingMap);
        this.ratingMap.remove(Rating.EMPTY);
    }

    public float clacMeanRating() {
        if (ratingMap.isEmpty()) return 0.0f;
        int startsSum =  Stream.of(ratingMap.entrySet())
                .map(kv -> kv.getKey().getStars() * kv.getValue())
                .reduce(0, (i,j) -> i+j);
        return (float) startsSum / getRatingMap().size();
    }

    public int sumOfRates() {
        return Stream.of(ratingMap.values()).reduce(0, (i,j) -> i+j);
    }
}
