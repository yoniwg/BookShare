package com.hgyw.bookshare.entities;

import android.support.annotation.NonNull;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.hgyw.bookshare.entities.reflection.EntityProperty;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Summary details for book
 */
public class BookSummary {

    private BigDecimal minPrice = BigDecimal.ZERO;
    private BigDecimal maxPrice = BigDecimal.ZERO;

    @EntityProperty(disable = true)
    private final Map<Rating, Integer> ratingMap = new EnumMap<>(Rating.class);

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

    /**
     * @return Immutable rating map backed by this object.
     */
    public Map<Rating, Integer> getRatingMap() {
        return Collections.unmodifiableMap(ratingMap);
    }

    public void setRating(@NonNull Rating rating, int count) {
        Objects.requireNonNull(rating);
        if (rating == Rating.EMPTY) throw new IllegalArgumentException("The rating should not EMPTY.");
        if (count < 0) throw new IllegalArgumentException("The count should not be small than 0.");
        ratingMap.put(rating, count);
    }

    /**
     * Replace current rating with new one.
     * One can provide EMPTY rating as old in order to add new rating.
     * @param oldRating - old rating to replace - may be EMPTY
     * @param newRating - new rating to put - may not be EMPTY
     */
    public void changeRating(@NonNull Rating oldRating, @NonNull Rating newRating) {
        Objects.requireNonNull(oldRating);
        Objects.requireNonNull(newRating);
        if (newRating == Rating.EMPTY)
            throw new IllegalArgumentException("The rating should not EMPTY.");
        Integer oldRatingAmount = ratingMap.get(oldRating);
        Integer newRatingAmount = ratingMap.get(newRating);
        if (newRatingAmount == null) newRatingAmount = 0;
        if (oldRating == Rating.EMPTY) {
            setRating(newRating, newRatingAmount + 1);
        } else {
            if (oldRatingAmount == null)
                throw new IllegalStateException("cannot change rating which doesn't exist already");
            setRating(oldRating, oldRatingAmount - 1);
            setRating(newRating, newRatingAmount + 1);
        }
    }

    public float calcMeanRating() {
        if (ratingMap.isEmpty()) return 0f;
        return Stream.of(ratingMap.entrySet())
                .filter(kv -> kv.getValue() > 0)
                .map(kv -> kv.getKey().getStars() * kv.getValue())
                .collect(Collectors.averaging(Number::doubleValue))
                .floatValue();
    }

    public int sumOfRates() {
        return Stream.of(ratingMap.values()).reduce(0, (i,j) -> i+j);
    }
}
