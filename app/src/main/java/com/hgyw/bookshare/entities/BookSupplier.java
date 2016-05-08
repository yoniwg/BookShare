package hgyw.com.bookshare.entities;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by Yoni on 3/15/2016.
 */
public class BookSupplier extends Entity  {

    @EntityReference(Supplier.class)
    private long supplierId;
    @EntityReference(Book.class)
    private long bookId;
    private BigDecimal price = BigDecimal.ZERO;
    private int amountAvailable = 100;


    public int getAmountAvailable() {
        return amountAvailable;
    }

    public void setAmountAvailable(int amountAvailable) {
        this.amountAvailable = amountAvailable;
    }

    public long getBookId() {
        return bookId;
    }

    public void setBookId(long bookId) {
        this.bookId = bookId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price.setScale(3, RoundingMode.HALF_UP);
    }

    public long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(long supplierId) {
        this.supplierId = supplierId;
    }
}
