package hgyw.com.bookshare.dataAccess;

import com.annimon.stream.Collectors;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import hgyw.com.bookshare.entities.Book;
import hgyw.com.bookshare.entities.BookReview;
import hgyw.com.bookshare.entities.BookSupplier;
import hgyw.com.bookshare.entities.Credentials;
import hgyw.com.bookshare.entities.Customer;
import hgyw.com.bookshare.entities.Entity;
import hgyw.com.bookshare.entities.Order;
import hgyw.com.bookshare.entities.OrderRating;
import hgyw.com.bookshare.entities.OrderStatus;
import hgyw.com.bookshare.entities.Rating;
import hgyw.com.bookshare.entities.Supplier;
import hgyw.com.bookshare.entities.Transaction;
import hgyw.com.bookshare.entities.User;

/**
 * Created by haim7 on 24/03/2016.
 */
class ListsCrudImplTest {
/*
    private final ListsCrudImpl crud;

    public CrudTest(ListsCrudImpl crud) {
        this.crud = crud;
        addData();
        printWholeDatabase();
    }

    private void printWholeDatabase() {
        final List<Class<? extends Entity>> classes = Arrays.asList(
                Customer.class, Supplier.class, Book.class,
                BookSupplier.class, Order.class, Transaction.class, BookReview.class

        );

        System.out.println("\n **************** Database Summury ***************");
        for (Class<? extends Entity> c : classes) System.out.println(
                " **** " + c.getSimpleName() + " List" + " **** " + "\n * "
                        + crud.streamAll(c).map(Object::toString).collect(Collectors.joining("\n * "))
        );
    }

    private void addData() {

        // ****************************************************** //

        User user;
        user = new Customer();

        user.setId(0);
        user.setCredentials(Credentials.create("root", "1234"));
        user.setFirstName("Haim");
        user.setLastName("Greenstein");
        user.setAddress("Petach Tikve");
        user.setBirthday(java.sql.Date.valueOf("1992-3-23"));
        user.setEmail("haim123@gmail.com");
        user.setPhoneNumber("050-1234567");
        crud.create(user);

        user.setId(0);
        user.setFirstName("Yoni");
        user.setLastName("Wiseberg");
        user.setAddress("Bney Brak");
        user.setBirthday(java.sql.Date.valueOf("1991-6-24"));
        user.setEmail("yw32@gmail.com");
        user.setPhoneNumber("052-1234567");
        crud.create(user);

        user.setId(0);
        user.setFirstName("Israel");
        user.setLastName("Israeli");
        user.setAddress("Tel Aviv");
        user.setBirthday(java.sql.Date.valueOf("1976-12-3"));
        user.setEmail("dsfsf@walla.co.il");
        user.setPhoneNumber("03-6438326");
        crud.create(user);

        user = new Supplier();

        user.setId(0);
        user.setFirstName("");
        user.setLastName("Feldhaime");
        user.setAddress("Israel");
        user.setEmail("admin@feldhaim.co.il");
        user.setPhoneNumber("03-4004004");
        crud.create(user);

        user.setId(0);
        user.setFirstName("");
        user.setLastName("Yefe-Nof");
        user.setAddress("Jerusalem, Israel");
        user.setEmail("admin@yefenof.co.il");
        user.setPhoneNumber("03-5005005");
        crud.create(user);

        // ****************************************************** //

        Book b = new Book();

        b.setId(0);
        b.setTitle("The Fellowship of the Ring");
        b.setAuthor("J. R. R. Tolkien");
        crud.create(b);

        b.setId(0);
        b.setTitle("The Two Towers");
        b.setAuthor("J. R. R. Tolkien");
        crud.create(b);

        b.setId(0);
        b.setTitle("The Return of the King");
        b.setAuthor("J. R. R. Tolkien");
        crud.create(b);

        for (int i = 1; i <= 7; i++) {
            b.setId(0);
            b.setTitle("Hary potter " + i);
            b.setAuthor("J.K.Rolling");
            crud.create(b);
        }

        // ****************************************************** //

        BookSupplier bs = new BookSupplier();
        BookReview br = new BookReview();

        long suppliersCount = crud.streamAll(Supplier.class).count();
        long booksCount = crud.streamAll(Book.class).count();
        for (int i = 1; i <= booksCount; i++) {
            // add BookSupplier's randomly
            final double probabilityThatSupplierHaveBook = 2.0 / suppliersCount;
            for (int j = 1; j <= suppliersCount; j++)
                if (Math.random() < probabilityThatSupplierHaveBook) {
                    bs.setId(0);
                    bs.setSupplierId(crud.retrieve(Supplier.class, j));
                    bs.setBookId(crud.retrieve(Book.class, i));
                    bs.setPrice(BigDecimal.valueOf(30 + Math.random() * 50).setScale(3, BigDecimal.ROUND_HALF_UP));
                    crud.create(bs);
                }
            // add Review to each book
            for (int j =0; j < 3; j++) {
                br.setId(0);
                br.setBookId(crud.retrieve(Book.class, i));
                br.setTitle(generateRandomString(10));
                br.setDescription(generateRandomString(5));
                br.setRating(Rating.values()[(int) (Math.random() * 5)]);
                br.setCustomerId(getRandomItem(Customer.class));
                crud.create(br);
            }

        }

        // ****************************************************** //

        Order order;
        Transaction transaction = new Transaction();

        for (int j=0; j < 6; j++) {
            transaction.setId(0);
            transaction.setDate(getRandomDate(-4, -2));
            transaction.setCustomerId(getRandomItem(Customer.class));
            crud.create(transaction);
            for (int i = 0; i < 3; i++) {
                order = new Order();
                order.setBookSupplierId(getRandomItem(BookSupplier.class));
                order.setTransactionId(transaction);
                order.setOrderStatus(OrderStatus.values()[((int) (Math.random() * OrderStatus.values().length))]);
                OrderRating or = new OrderRating();
                or.setCommunication(Rating.values()[((int) (Math.random() * Rating.values().length))]);
                or.setItemAsDescribed(Rating.values()[((int) (Math.random() * Rating.values().length))]);
                or.setShippingTime(Rating.values()[((int) (Math.random() * Rating.values().length))]);
                order.setOrderRating(or);
                order.setAmount((int) (1 + Math.random() * 10));
                order.computePriceByBookSupplier();
                crud.create(order);
            }
        }

        // ****************************************************** //

    }


    // Retrieve random entity item This should be an expensive method!
    private  <T extends Entity> T getRandomItem(Class<T> clazz) {
        long index = (long) (crud.streamAll(clazz).count() * Math.random());
        return crud.streamAll(clazz).skip(index).findFirst().orElseThrow(() -> new NoSuchElementException("TESTING MESSEGE: the stream of entity is empty"));
    }

    private static String generateRandomString(int size) {
        StringBuilder str = new StringBuilder(size);
        boolean newWord = true;
        for (int i = 0; i < size; i++) {
            if (!newWord &&  Math.random() < 0.25) {
                str.append(' ');
                newWord = true;
            }
            else {
                char c = (char) ((int)(Math.random()*26) + (newWord ? 'A' : 'a'));
                str.append(c);
                newWord = false;
            }
        }
        return str.toString().trim();
    }

    private Date getRandomDate(double fromDays, double toDays) {
        return new Date(System.currentTimeMillis() + (long)( 24*60*60*1000* (fromDays +  (toDays - fromDays)*Math.random())));
    }

*/
}
