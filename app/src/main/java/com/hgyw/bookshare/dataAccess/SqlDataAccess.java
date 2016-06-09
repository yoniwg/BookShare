package com.hgyw.bookshare.dataAccess;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookQuery;
import com.hgyw.bookshare.entities.BookSummary;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.Credentials;
import com.hgyw.bookshare.entities.Entity;
import com.hgyw.bookshare.entities.IdReference;
import com.hgyw.bookshare.entities.Order;
import com.hgyw.bookshare.entities.Transaction;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.entities.reflection.EntityReflection;
import com.hgyw.bookshare.entities.reflection.JsonReflection;
import com.hgyw.bookshare.entities.reflection.Property;
import com.hgyw.bookshare.entities.reflection.SqlLiteReflection;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Created by haim7 on 26/05/2016.
 */
abstract class SqlDataAccess implements DataAccess {

    protected final String ID_KEY;
    protected final String SUB;
    protected final String NON_DELETED_CONDITION = "deleted=" + sqlValue(false);
    private static final SqlLiteReflection sqlLiteReflection = new SqlLiteReflection();

    protected SqlDataAccess(String id, String sub) {
        ID_KEY = id;
        SUB = sub;
    }

    protected String tableName(Class<?> userClass) {
        return userClass.getSimpleName().toLowerCase()+"_"+"table";
    }

    @Override
    public Optional<User> retrieveUserWithCredentials(Credentials credentials) {
        String sql = String.format("SELECT * from %s WHERE %s AND %s=%s AND %s=%s",
                tableName(User.class),
                NON_DELETED_CONDITION,
                "credentials" + SUB + "username", sqlValue(credentials.getUsername()),
                "credentials" + SUB + "password", sqlValue(credentials.getPassword())
        );
        List<User> sqlResult = retrieveEntityFromDb(User.class, sql);
        return sqlResult.isEmpty() ? Optional.empty() : Optional.of(sqlResult.get(0));
    }

    @Override
    public boolean isUsernameTaken(String username) {
        String sql = String.format("SELECT * from %s WHERE %s AND %s=%s ",
                tableName(User.class),
                NON_DELETED_CONDITION,
                "credentials" + SUB + "username", sqlValue(username)
        );
        List<User> sqlResult = retrieveEntityFromDb(User.class, sql);
        return !sqlResult.isEmpty();
    }

    @Override
    public List<User> findInterestedInBook(Book book, User userAsked) {
        return Collections.emptyList(); // TODO
    }

    @Override
    public List<Order> retrieveOrders(User customer, User supplier, Date fromDate, Date toDate, boolean onlyOpen) {
        String joining = "SELECT ord.* FROM " + tableName(Order.class) + " ord " +
                "INNER JOIN " + tableName(Transaction.class) + " trn ON (trn." + ID_KEY + " = ord." + "transactionId" + ") " +
                "INNER JOIN " + tableName(BookSupplier.class) + " bsp ON (bsp." + ID_KEY + " = ord." + "bookSupplierId" + ") ";

        List<String> conditions = new ArrayList<>(2);
        if (customer != null) conditions.add("trn.customerId = " + customer.getId());
        if (supplier != null) conditions.add("bsp.supplierId = " + supplier.getId());
        if (fromDate != null) conditions.add("trn.date > " + sqlValue(fromDate));
        if (toDate != null) conditions.add("trn.date < " + sqlValue(toDate));

        String sql = joining + " WHERE " + Stream.of(conditions).collect(Collectors.joining(" AND "));
        return retrieveEntityFromDb(Order.class, sql);
    }

    @Override
    public List<Book> findBooks(BookQuery query) {
        List<String> conditions = new ArrayList<>(2);
        if (!query.getTitleQuery().isEmpty()) conditions.add("bks.title LIKE " + sqlValue(query.getTitleQuery()));
        if (!query.getAuthorQuery().isEmpty()) conditions.add("bks.author LIKE " + sqlValue(query.getAuthorQuery()));
        String genreOrdinals = Stream.of(query.getGenreSet()).map(g -> Integer.toString(g.ordinal())).collect(Collectors.joining(","));
        conditions.add("bks.genre IN (" + genreOrdinals + ")");
        conditions.add("bks." + NON_DELETED_CONDITION); // TODO price

        String sql = String.format("SELECT * from %s bks WHERE %s ",
                tableName(Book.class),
                Stream.of(conditions).collect(Collectors.joining(" AND "))
        );
        return retrieveEntityFromDb(Book.class, sql);
    }

    @Override
    public List<Book> findSpecialOffers(User user, int limit) {
        String sql = String.format("SELECT * from %s WHERE %s", tableName(Book.class), NON_DELETED_CONDITION); // TODO
        return retrieveEntityFromDb(Book.class, sql);
    }

    @Override
    public <T extends Entity> List<T> findEntityReferTo(Class<T> referringClass, IdReference... referredItems) {
        String conditions = Stream.of(referredItems)
                .map(id -> {
                    Property p = EntityReflection.getReferringProperties(referringClass, id.getEntityType());
                    return p.getName() + "=" + id.getId();
                }).collect(Collectors.joining(" AND "));
        String sql = String.format("SELECT * FROM %s WHERE %s AND %s", tableName(referringClass), NON_DELETED_CONDITION, conditions);
        return retrieveEntityFromDb(referringClass, sql);
    }

    @Override
    public void create(Entity item) {
        if (item.getId() != 0) throw new IllegalArgumentException("Created item should have id 0.");
        item.setDeleted(false);
        long newId = createItemDb(item);
        item.setId(newId);
    }

    @Override
    public void update(Entity item) {
        if (item.getId() == 0) throw new IllegalArgumentException("Updated item should not have id 0.");
        item.setDeleted(false);
        updateItemDb(item);
    }

    @Override
    public <T extends Entity> T retrieve(Class<T> entityClass, long id) {
        String sql = String.format("SELECT * FROM %s WHERE %s=%s",
                tableName(entityClass),
                ID_KEY, id
        );
        List<T> result = retrieveEntityFromDb(entityClass, sql);
        if (result.isEmpty()) throw new NoSuchElementException("No item " + id + " of " + entityClass.getSimpleName() +  " in database.");
        return result.get(0);
    }

    @Override
    public Entity retrieve(IdReference idReference) {
        return retrieve(idReference.getEntityType(), idReference.getId());
    }

    /////////////////////////////
    // SQL Execution Methods
    /////////////////////////////

    private static String sqlValue(Object value) {
        if (value == null) return "null";
        if (value instanceof String) return '\'' + value.toString() + '\'';
        if (value instanceof Date) return Long.toString(((Date) value).getTime());
        if (value instanceof Boolean) return ((Boolean) value) ? "1" : "0";
        return value.toString();
    }


    protected abstract <T> List<T> retrieveEntityFromDb(Class<T> type, String statement);

    protected long createItemDb(Entity item) {
        Collection<Property> properties = getProperties(item.getClass());
        List<String> fieldNames = new ArrayList<>(properties.size());
        List<String> values = new ArrayList<>(properties.size());
        Stream.of(properties)
                .filter(p -> !p.getName().equals(ID_KEY))
                .forEach(p -> {
                    fieldNames.add(p.getName());
                    values.add(sqlValue(p.get(item)));
                });

        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)",
                tableName(item.getClass()),
                Stream.of(fieldNames).collect(Collectors.joining(",")),
                Stream.of(values).collect(Collectors.joining(","))
        );
        System.out.println("Starting createItem: " + sql);
        return executeCreateSql(sql);
    }


    protected void updateItemDb(Entity item) {
        Collection<Property> properties = getProperties(item.getClass());
        String keyValues = Stream.of(properties)
                .filter(p -> !p.getName().equals(ID_KEY))
                .map(p -> p.getName() + "=" + sqlValue(p.get(item)))
                .collect(Collectors.joining(","));
        String statement = String.format("UPDATE %s SET %s WHERE %s=%s",
                tableName(item.getClass()), keyValues, ID_KEY, item.getId());
        System.out.println("Starting updateItem: " + statement);
        executeSql(statement);
    }

    protected abstract Collection<Property> getProperties(Class<?> aClass);

    protected abstract long executeCreateSql(String sql);

    protected abstract void executeSql(String sql);

    @Override
    public void delete(IdReference item) {
        String sql = String.format("UPDATE %s SET %s=%s WHERE %s=%s",
                tableName(item.getEntityType()),
                "deleted",
                true,
                ID_KEY,
                item.getId()
        );
        executeSql(sql);
    }


    @Override
    public BookSummary getBookSummary(Book book) {
        String sqlFormat = "SELECT COALESCE(MIN({0}),{1}) AS minPrice, COALESCE(MAX({0}),{1}) AS maxPrice " + "FROM {2} " + "WHERE {3} AND {4}={5}";
        String sql = MessageFormat.format(sqlFormat,
                "price",
                BigDecimal.ZERO,
                tableName(BookSupplier.class),
                NON_DELETED_CONDITION,
                "bookId", book.getId()
        );
        List<BookSummary> results = retrieveEntityFromDb(BookSummary.class, sql);
        if (results.isEmpty()) throw new NoSuchElementException("No book with id " + book.getId());
        return results.get(0);
    }

}
