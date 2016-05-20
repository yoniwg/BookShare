package com.hgyw.bookshare.logicAccess;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.hgyw.bookshare.dataAccess.DataAccess;
import com.hgyw.bookshare.dataAccess.DataAccessFactory;
import com.hgyw.bookshare.entities.*;
import com.hgyw.bookshare.exceptions.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by haim7 on 09/05/2016.
 */
public class Test {

    public static void test(AccessManager accessManager) {

      //  DataAccess da = DataAccessFactory.getInstance();

        SupplierAccess sAccess = null;
        CustomerAccess cAccess = null;
        Book book = new Book();
        BookSupplier bookSupplier = new BookSupplier();
        BookQuery bookQuery;
        User customer = new User();
        customer.setUserType(UserType.CUSTOMER);

        Credentials firstSupplierCredentials = Credentials.create("s", "");
        Credentials secondSupplierCredentials = Credentials.create("s2", "");

        /////////////////////////////
        // new supplier
        User supplier = new User();
        supplier.setId(0);
        supplier.setUserType(UserType.SUPPLIER);
        supplier.setCredentials(firstSupplierCredentials);
        supplier.setFirstName("");
        supplier.setLastName("Feldhaime");
        supplier.setAddress("Israel");
        supplier.setEmail("admin@feldhaim.co.il");
        supplier.setPhoneNumber("03-4004004");
        try {
            accessManager.signUp(supplier);
        } catch (WrongLoginException e) {
            e.printStackTrace();
        }
        sAccess = accessManager.getSupplierAccess();

        book.setId(0);
        book.setTitle("The Fellowship of the Ring");
        book.setAuthor("J. R. R. Tolkien");
        sAccess.addBook(book);
        bookSupplier.setId(0);
        bookSupplier.setBookId(book.getId());
        bookSupplier.setPrice(new BigDecimal("49.99"));
        sAccess.addBookSupplier(bookSupplier);

        book.setId(0);
        book.setTitle("The Two Towers");
        book.setAuthor("J. R. R. Tolkien");
        sAccess.addBook(book);
        bookSupplier.setId(0);
        bookSupplier.setBookId(book.getId());
        bookSupplier.setPrice(new BigDecimal("89.99"));
        sAccess.addBookSupplier(bookSupplier);

        book.setId(0);
        book.setTitle("The Return of the King");
        book.setAuthor("J. R. R. Tolkien");
        sAccess.addBook(book);
        bookSupplier.setId(0);
        bookSupplier.setBookId(book.getId());
        bookSupplier.setPrice(new BigDecimal("79.99"));
        sAccess.addBookSupplier(bookSupplier);
        try {
            System.out.println("$$$Negative test: Trying to add again:");
            sAccess.addBookSupplier(bookSupplier);
        } catch (Exception e) {
            System.out.println(e.getClass().getSimpleName() + ": " + e.getMessage());
        }

        //////////////////////////////
        accessManager.signOut();
        //////////////////////////////

        // new supplier

        supplier.setId(0);
        supplier.setCredentials(secondSupplierCredentials);
        supplier.setFirstName("");
        supplier.setLastName("Yefe-Nof");
        supplier.setAddress("Jerusalem, Israel");
        supplier.setEmail("admin@yefenof.co.il");
        supplier.setPhoneNumber("03-5005005");
        try {
            accessManager.signUp(supplier);
        } catch (WrongLoginException e) {
            e.printStackTrace();
        }
        sAccess = accessManager.getSupplierAccess();

        for (int i = 1; i <= 7; i++) {
            book.setId(0);
            book.setTitle("Harry potter " + i);
            book.setAuthor("J.K.Rowling");
            sAccess.addBook(book);
            bookSupplier.setId(0);
            bookSupplier.setBookId(book.getId());
            bookSupplier.setPrice(BigDecimal.valueOf(100 + 10 * i));
            sAccess.addBookSupplier(bookSupplier);
        }

        // add bookSupplier for another book
        bookQuery = new BookQuery();
        bookQuery.setTitleQuery("The Two Towers");
        book = sAccess.findBooks(bookQuery).iterator().next(); // get first book from books match the quarry
        bookSupplier.setId(0);
        bookSupplier.setBookId(book.getId());
        bookSupplier.setPrice(BigDecimal.valueOf(77.88));
        sAccess.addBookSupplier(bookSupplier);

        // retrieve all current harry potter books adn change the details of books
        bookQuery = new BookQuery();
        bookQuery.setTitleQuery("HARRY POTTER");
        for (Book b : sAccess.findBooks(bookQuery)) {
            b.setAuthor("Joanne Jo Rowling");
            sAccess.updateBook(b);
        }

        // remove bookSupplier
        bookQuery = new BookQuery();
        bookQuery.setTitleQuery("HARRY POTTER 7");
        book = sAccess.findBooks(bookQuery).iterator().next();
        bookSupplier = sAccess.findBookSuppliers(book).iterator().next();
        sAccess.removeBookSupplier(bookSupplier);
        try {
            System.out.println("$$$Negative test: Try to remove again:");
            sAccess.removeBookSupplier(bookSupplier);
        } catch (Exception e) {
            System.out.println(e.getClass().getSimpleName() + ": " + e.getMessage());
        }

        //////////////////////////////
        accessManager.signOut();
        //////////////////////////////
        // resign as first supplier
        /////////////////////////////

        try {
            accessManager.signIn(firstSupplierCredentials);
        } catch (WrongLoginException e) {
            e.printStackTrace();
        }
        sAccess = accessManager.getSupplierAccess();
        supplier = sAccess.retrieveUserDetails();

        // retrieve books by query
        bookQuery = new BookQuery();
        bookQuery.setTitleQuery("HARRY POTTER");
        bookQuery.setBeginPrice(BigDecimal.valueOf(135));
        bookQuery.setEndPrice(BigDecimal.valueOf(165));
        // add bookSupplier for these book and current supplier
        final SupplierAccess saFinal = sAccess;
        Collection<Book> booksFound = sAccess.findBooks(bookQuery);
        System.out.println(" *** books found: "); for (Book b : booksFound) System.out.println(b);
        for (Book b : booksFound) {
            bookSupplier.setId(0);
            bookSupplier.setBookId(b.getId());
            bookSupplier.setSupplierId(supplier.getId());
            bookSupplier.setPrice(BigDecimal.valueOf(66.99));
            sAccess.addBookSupplier(bookSupplier);
        }

        // update supplier details
        supplier = sAccess.retrieveUserDetails();
        supplier.setPhoneNumber("03-3333333");
        sAccess.updateUserDetails(supplier);

        // retrieve supplier books
        System.out.println(" *** Books of supplier: " + supplier);
        for (BookSupplier bs : sAccess.retrieveMyBooks()) System.out.println(bs);

        bookSupplier = sAccess.retrieveMyBooks().iterator().next();
        bookSupplier.setPrice(BigDecimal.valueOf(1000));
        sAccess.updateBookSupplier(bookSupplier);

        //////////////////////////////
        accessManager.signOut();
        //////////////////////////////

        // new customer
        customer.setId(0);
        customer.setCredentials(Credentials.create("haim1", "12345"));
        customer.setEmail("haim763@gmail.com");
        customer.setFirstName("Haim");
        customer.setLastName("Greenstein");
        try {
            accessManager.signUp(customer);
        } catch (WrongLoginException e) {
            e.printStackTrace();
        }
        cAccess = accessManager.getCustomerAccess();

        // order query results
        bookQuery = new BookQuery();
        bookQuery.setAuthorQuery("Rowling");
        bookQuery.setBeginPrice(BigDecimal.valueOf(105));
        bookQuery.setEndPrice(BigDecimal.valueOf(145));
        final CustomerAccess finalCAccess = cAccess;
        Collection<Order> orders = Stream.of(cAccess.findBooks(bookQuery))
                .map(b -> finalCAccess.findBookSuppliers(b).iterator().next())
                .map(bs -> {
                    Order order = new Order();
                    order.setBookSupplierId(bs.getSupplierId());
                    order.setUnitPrice(bs.getPrice());
                    return order;
                }).collect(Collectors.toList());

       Transaction transaction = new Transaction();
        transaction.setCreditCard("231972947817861868");
        try {
            cAccess.performNewTransaction(transaction, orders);
        } catch (OrdersTransactionException e) {
            e.printStackTrace();
        }

        //
        System.out.println(" *** findSpecialOffers:");
        for (Book b : cAccess.findSpecialOffers(100)) {
            System.out.println(b);
        }

        // cancel order
        orders = cAccess.retrieveActiveOrders();
        cAccess.cancelOrder(orders.iterator().next().getId());
        try {
            System.out.println("$$$Negative test: ");
            cAccess.cancelOrder(orders.iterator().next().getId());
        } catch (Exception e) {
            System.out.println(e.getClass().getSimpleName() + ": " + e.getMessage());
        }

        // order rating
        OrderRating orderRating = new OrderRating();
        orderRating.setCommunication(Rating.GOOD);
        orderRating.setItemAsDescribed(Rating.BAD);
        cAccess.updateOrderRating(Stream.of(orders).skip(1).findFirst().get().getId(), orderRating);

        // book review
        BookReview bookReview = new BookReview();
        bookReview.setBookId(book.getId());
        bookReview.setTitle("Bad Book");
        bookReview.setDescription("The book is very very boring, and is not interesting at all.");
        cAccess.writeBookReview(bookReview);
        bookReview.setRating(Rating.POOR);
        cAccess.writeBookReview(bookReview);
        try {
            System.out.println(" $$$ Negative test: ");
            cAccess.writeBookReview(bookReview);
        } catch (Exception e) {
            System.out.println(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        try {
            System.out.println(" $$$ Negative test: ");
            cAccess.removeBookReview(new BookReview());
        } catch (Exception e) {
            System.out.println(e.getClass().getSimpleName() + ": " + e.getMessage());
        }

        System.out.println(" ** suppliers of book " + book.shortDescription() + ": ");
        for (BookSupplier bs : cAccess.findBookSuppliers(book)) System.out.println(bs);

        //////////////////////////////
        accessManager.signOut();
        //////////////////////////////

        // new customer
        customer.setId(0);
        customer.setCredentials(Credentials.create("yoni1", "54321"));
        customer.setEmail("yoni@gmail.com");
        customer.setFirstName("Yoni");
        customer.setLastName("Wiesberg");
        System.out.println(" ** username haim1: " + accessManager.isUserNameTaken("haim1"));
        System.out.println(" ** username yoni1: " + accessManager.isUserNameTaken("yoni1"));
        try {
            accessManager.signUp(customer);
        } catch (WrongLoginException e) {
            e.printStackTrace();
        }




        ///////////////////////////////////////////////////
        printWholeDatabase(DataAccessFactory.getInstance());
    }


    private static void printWholeDatabase(DataAccess dataAccess) {
        final List<Class<? extends Entity>> classes = Arrays.asList(
                User.class, User.class, Book.class,
                BookSupplier.class, Order.class, Transaction.class, BookReview.class

        );

        System.out.println("\n **************** Database Summury ***************");
        for (Class<? extends Entity> c : classes) System.out.println(
                " **** " + c.getSimpleName() + " List" + " **** " + "\n * "
                        + dataAccess.streamAll(c).map(Object::toString).collect(Collectors.joining("\n * "))
        );
    }
}
