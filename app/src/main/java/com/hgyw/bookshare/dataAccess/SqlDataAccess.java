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
import com.hgyw.bookshare.entities.reflection.ConvertersCollection;
import com.hgyw.bookshare.entities.reflection.EntityReflection;
import com.hgyw.bookshare.entities.reflection.Properties;
import com.hgyw.bookshare.entities.reflection.Property;

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
    protected final String NON_DELETED_CONDITION;
    protected final ConvertersCollection sqlConverters;


    protected SqlDataAccess(String id, String sub, ConvertersCollection sqlConverters) {
        ID_KEY = id;
        SUB = sub;
        this.sqlConverters = sqlConverters;
        NON_DELETED_CONDITION = "deleted=" + sqlValue(false);
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
        List<User> sqlResult = executeResultSql(User.class, sql);
        return sqlResult.isEmpty() ? Optional.empty() : Optional.of(sqlResult.get(0));
    }

    @Override
    public boolean isUsernameTaken(String username) {
        String sql = String.format("SELECT * from %s WHERE %s AND %s=%s ",
                tableName(User.class),
                NON_DELETED_CONDITION,
                "credentials" + SUB + "username", sqlValue(username)
        );
        List<User> sqlResult = executeResultSql(User.class, sql);
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
        return executeResultSql(Order.class, sql);
    }

    @Override
    public List<Book> findBooks(BookQuery query) {
        List<String> conditions = new ArrayList<>(2);
        if (!query.getTitleQuery().isEmpty()) conditions.add("bks.title LIKE " + sqlValue(query.getTitleQuery()));
        if (!query.getAuthorQuery().isEmpty()) conditions.add("bks.author LIKE " + sqlValue(query.getAuthorQuery()));
        String genreOrdinals = Stream.of(query.getGenreSet()).map(this::sqlValue).collect(Collectors.joining(","));
        conditions.add("bks.genre IN (" + genreOrdinals + ")");
        conditions.add("bks." + NON_DELETED_CONDITION); // TODO price
        String conditionsString = Stream.of(conditions).collect(Collectors.joining(" AND "));

        String sql = "SELECT * FROM " + tableName(Book.class) + " bks WHERE %s " + conditionsString;
        return executeResultSql(Book.class, sql);
    }

    @Override
    public List<Book> findSpecialOffers(User user, int limit) {
        String sql = "SELECT * FROM " + tableName(Book.class) + " WHERE " + NON_DELETED_CONDITION; // TODO
        return executeResultSql(Book.class, sql);
    }

    @Override
    public <T extends Entity> List<T> findEntityReferTo(Class<T> referringClass, IdReference... referredItems) {
        String conditions = Stream.of(referredItems)
                .map(id -> {
                    Property p = EntityReflection.getReferringProperties(referringClass, id.getEntityType());
                    return p.getName() + "=" + id.getId();
                }).collect(Collectors.joining(" AND "));
        String sql = String.format("SELECT * FROM %s WHERE %s AND %s", tableName(referringClass), NON_DELETED_CONDITION, conditions);
        return executeResultSql(referringClass, sql);
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
        List<T> result = executeResultSql(entityClass, sql);
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

    private String sqlValue(Object value) {
        if (value == null) return "null";
        value = sqlConverters.convert(value);
        if (value instanceof String){
            return '\'' + value.toString() + '\'';
        }
        return value.toString();
    }


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

    protected Collection<Property> getProperties(Class<?> aClass) {
        return Properties.getFlatProperties(aClass, SUB, sqlConverters::canConvertFrom);
    }

    @Override
    public void delete(IdReference item) {
        String sql = String.format("UPDATE %s SET %s=%s WHERE %s=%s",
                tableName(item.getEntityType()),
                "deleted",
                sqlValue(true),
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
                sqlValue(BigDecimal.ZERO),
                tableName(BookSupplier.class),
                NON_DELETED_CONDITION,
                "bookId", book.getId()
        );
        List<BookSummary> results = executeResultSql(BookSummary.class, sql);
        if (results.isEmpty()) throw new NoSuchElementException("No book with id " + book.getId());
        return results.get(0);
    }


    protected abstract void executeSql(String sql);

    protected abstract long executeCreateSql(String sql);

    protected abstract <T> List<T> executeResultSql(Class<T> type, String statement);

}
