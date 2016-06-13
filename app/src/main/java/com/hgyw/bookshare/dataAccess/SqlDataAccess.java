package com.hgyw.bookshare.dataAccess;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.hgyw.bookshare.entities.*;
import com.hgyw.bookshare.entities.reflection.ConvertersCollection;
import com.hgyw.bookshare.entities.reflection.EntityReflection;
import com.hgyw.bookshare.entities.reflection.OneSideConverter;
import com.hgyw.bookshare.entities.reflection.Properties;
import com.hgyw.bookshare.entities.reflection.Property;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Created by haim7 on 26/05/2016.
 */
abstract class SqlDataAccess implements DataAccess {

    protected final String ID_KEY = "id";
    protected final String SUB = "_";
    protected final String NON_DELETED_CONDITION;
    protected final ConvertersCollection sqlConverters;
    private final String idColumnAdditionalProperties;

    /**
     *
     * @param sqlConverters Converters from java to sql values for creating sql statement using
     * {@code converter.convert(value).toString()}.
     * @param idColumnAdditionalProperties Something like "primary key auto_increment" to add to the id column definition.
     */
    protected SqlDataAccess(ConvertersCollection sqlConverters, String idColumnAdditionalProperties) {
        this.sqlConverters = sqlConverters;
        this.idColumnAdditionalProperties = idColumnAdditionalProperties;
        NON_DELETED_CONDITION = "deleted=" + sqlValue(false);
    }


    protected String tableName(Class<? extends Entity> userClass) {
        return userClass.getSimpleName().toLowerCase() + "_" + "table";
    }

    public void createTableIfNotExists(Class<? extends Entity> type) {
        String sqlTable = tableName(type);
        String sqlColumnTypeList = Stream.of(getProperties(type).values())
                .map(p -> {
                    String columnName = p.getName();
                    String columnType = sqlConverters.findConverter(p.getPropertyType()).getSqlTypeName();
                    boolean isPrimaryKey = p.getName().equals(ID_KEY);
                    String primaryKey = isPrimaryKey ? idColumnAdditionalProperties : "";
                    return columnName + " " + columnType + " " + primaryKey;
                })
                .collect(Collectors.joining(","));
        String sql = "CREATE TABLE IF NOT EXISTS " + sqlTable + "(" + sqlColumnTypeList + ")";
        executeSql(sql);
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
        if (fromDate != null) conditions.add("trn.date >= " + sqlValue(fromDate));
        if (toDate != null) conditions.add("trn.date <= " + sqlValue(toDate));

        String sql = joining + " WHERE " + Stream.of(conditions).collect(Collectors.joining(" AND "));
        return executeResultSql(Order.class, sql);
    }

    @Override
    public List<Book> findBooks(BookQuery query) {
        List<String> conditions = new ArrayList<>(2);

        if (!query.getTitleQuery().isEmpty()) conditions.add("bks.title LIKE " + sqlStringContains(query.getTitleQuery()));
        if (!query.getAuthorQuery().isEmpty()) conditions.add("bks.author LIKE " + sqlStringContains(query.getAuthorQuery()));
        String genreValues = Stream.of(query.getGenreSet()).map(this::sqlValue).collect(Collectors.joining(","));
        conditions.add("bks.genre IN (" + genreValues + ")");
        conditions.add("bks." + NON_DELETED_CONDITION);
        // TODO price
        //if (query.getBeginPrice() != null) conditions.add("MIN(bsp.price) >= " + sqlValue(query.getBeginPrice()));

        String conditionsString = Stream.of(conditions).collect(Collectors.joining(" AND "));
        String sql = "SELECT * FROM " + tableName(Book.class) + " bks " +
                "INNER JOIN " + tableName(BookSupplier.class) + " bsp ON (bsp.bookId = bks." + ID_KEY + ") " +
                "WHERE " + conditionsString;
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

    private String sqlQuote(String string){
        return '\'' + string + '\'';
    }

    /**
     * Calls sqlQuote(String) if needed.
     */
    private String sqlValue(Object value){
        if (value == null) return "'null'";

        OneSideConverter converter = sqlConverters.findConverter(value.getClass());
        String sqlType = converter.getSqlTypeName();

        String sqlValue = converter.convert(value).toString();
        if (sqlType.isEmpty() && value instanceof String || !sqlType.isEmpty() && (sqlType.contains("CHAR") || sqlType.contains("TEXT"))){
            sqlValue = sqlQuote(sqlValue);
        }
        return sqlValue;
    }

    /**
     * Call this method instead of sqlValue(Object), for condition '_ LIKE [sqlStringContains(string)]'.
     */
    private String sqlStringContains(String string) {
        return sqlQuote("%" + string + "%");
    }


    protected long createItemDb(Entity item) {
        Collection<Property> properties = getProperties(item.getClass()).values();
        List<String> fieldNames = new ArrayList<>(properties.size());
        List<String> sqlValues = new ArrayList<>(properties.size());
        Stream.of(properties)
                .filter(p -> !p.getName().equals(ID_KEY))
                .forEach(p -> {
                    fieldNames.add(p.getName());
                    sqlValues.add(sqlValue(p.get(item)));
                });

        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)",
                tableName(item.getClass()),
                Stream.of(fieldNames).collect(Collectors.joining(",")),
                Stream.of(sqlValues).collect(Collectors.joining(","))
        );
        System.out.println("Starting createItem: " + sql);
        return executeCreateSql(sql);
    }


    protected void updateItemDb(Entity item) {
        Collection<Property> properties = getProperties(item.getClass()).values();
        String keyValues = Stream.of(properties)
                .filter(p -> !p.getName().equals(ID_KEY))
                .map(p -> p.getName() + "=" + sqlValue(p.get(item)))
                .collect(Collectors.joining(","));
        String statement = String.format("UPDATE %s SET %s WHERE %s=%s",
                tableName(item.getClass()), keyValues, ID_KEY, item.getId());
        System.out.println("Starting updateItem: " + statement);
        executeSql(statement);
    }


    private final Map<Class<?>, Map<String,Property>> propertiesMaps = new HashMap<>();

    protected Map<String,Property> getProperties(Class<?> aClass) {
        Map<String,Property> properties = propertiesMaps.get(aClass);
        if (properties == null) {
            Collection<Property> propertyCollection = Properties.getFlatProperties(aClass, SUB, sqlConverters::canConvertFrom);
            properties = Stream.of(propertyCollection).collect(Collectors.toMap(Property::getName, o->o));
            propertiesMaps.put(aClass, properties);
        }
        return properties;
    }

    @Override
    public void delete(IdReference item) {
        String sql = String.format("UPDATE %s SET %s=%s WHERE %s=%s",
                tableName(item.getEntityType()),
                "deleted", sqlValue(true),
                ID_KEY, item.getId()
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
