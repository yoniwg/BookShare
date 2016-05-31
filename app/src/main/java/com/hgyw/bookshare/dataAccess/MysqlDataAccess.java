package com.hgyw.bookshare.dataAccess;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookQuery;
import com.hgyw.bookshare.entities.BookSummary;
import com.hgyw.bookshare.entities.Credentials;
import com.hgyw.bookshare.entities.Entity;
import com.hgyw.bookshare.entities.IdReference;
import com.hgyw.bookshare.entities.Order;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.entities.reflection.EntityReflection;
import com.hgyw.bookshare.entities.reflection.JsonReflection;
import com.hgyw.bookshare.entities.reflection.Property;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;

/**
 * Created by haim7 on 26/05/2016.
 */
class MysqlDataAccess implements DataAccess {

    private static final String SUB = JsonReflection.SUB_PROPERTY_SEPARATOR;
    private static final String SERVER_URL = "http://yweisber.vlab.jct.ac.il";
    private static final String GET_DATA_URL = SERVER_URL + "/" + "getData.php";
    private static final String WRITE_DATA_URL = SERVER_URL + "/" + "/writeData.php";
    private static final JsonReflection jsonReflection = new JsonReflection();
    public static final String ID = "id";

    static String tableName(Class<?> userClass) {
        return userClass.getSimpleName().toLowerCase()+"_"+"table";
    }

    /*{printTablesAndPropertiesPhpMap();
        User user = new User();
        user.setFirstName("Haim");
        user.setCredentials(new Credentials("jsaajs", "1234"));
        user.setBirthday(new java.sql.Date(0));
        System.out.println(jsonReflection.writeObject(user).toString());
        ObjectMapper om = new ObjectMapper();
        try {
            String s = om.writeValueAsString(user);
            System.out.println(s);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        MySqlCrud.createAll();

    }

    private void printTablesAndPropertiesPhpMap() {
        String entities = Stream.of(EntityReflection.getEntityTypes())
                .map(type -> {
                    String tableName = tableName(type);
                    String properties = Stream.of(jsonReflection.getProperties(type).values())
                            .map(p -> quote(p.getName()) + " => " + quote(p.getPropertyType().getSimpleName()) )
                            .collect(Collectors.joining(", "));
                    return quote(tableName) + " => array(" + properties + ")";
                })
                .collect(Collectors.joining(",\n\t"));
        String phpArray = "$tables = array(" + entities + ");";
        System.out.println(phpArray);
    }*/



    @Override
    public Optional<User> retrieveUserWithCredentials(Credentials credentials) {
        String sql = String.format("SELECT * from %s WHERE %s=%s AND %s=%s",
                tableName(User.class),
                "credentials" + SUB + "username", sqlValue(credentials.getUsername()),
                "credentials" + SUB + "password", sqlValue(credentials.getPassword())
        );
        List<User> sqlResult = retrieveEntityFromDb(User.class, sql);
        return sqlResult.isEmpty() ? Optional.empty() : Optional.of(sqlResult.get(0));
    }

    @Override
    public boolean isUsernameTaken(String username) {
        String sql = String.format("SELECT * from %s WHERE %s=%s",
                tableName(User.class),
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
        return Collections.emptyList();
    }

    @Override
    public List<Book> findBooks(BookQuery query) {
        String sql = String.format("SELECT * from %s", tableName(Book.class)); // TODO
        return retrieveEntityFromDb(Book.class, sql);
    }

    @Override
    public List<Book> findSpecialOffers(User user, int limit) {
        return findBooks(new BookQuery()); // TODO
    }

    @Override
    public <T extends Entity> List<T> findEntityReferTo(Class<T> referringClass, IdReference... referredItems) {
        String conditions = Stream.of(referredItems)
                .map(id -> {
                    Property p = EntityReflection.getReferringProperties(referringClass, id.getEntityType());
                    return p.getName() + "=" + id.getId();
                }).collect(Collectors.joining(" AND "));
        String sql = String.format("SELECT * FROM %s WHERE %s", tableName(referringClass), conditions);
        return retrieveEntityFromDb(referringClass, sql);
    }

    @Override
    public BookSummary getBookSummary(Book book) {
        return new BookSummary(); // TODO
    }

    @Override
    public void create(Entity item) {
        if (item.getId() != 0) throw new IllegalArgumentException("Created item should have id 0.");
        item.setDeleted(false);
        long newId = createItem(item);
        item.setId(newId);
    }

    @Override
    public void update(Entity item) {
        if (item.getId() == 0) throw new IllegalArgumentException("Updated item should not have id 0.");
        item.setDeleted(false);
        updateItem(item);
    }

    @Override
    public void delete(IdReference item) {
        String sql = String.format("UPDATE %s SET %s=%s WHERE %s=%s",
                tableName(item.getEntityType()),
                "deleted",
                true,
                ID,
                item.getId()
        );
        sendStatementHttpPost(sql);
    }

    @Override
    public <T extends Entity> T retrieve(Class<T> entityClass, long id) {
        String sql = String.format("SELECT * FROM %s WHERE %s=%s",
                tableName(entityClass),
                ID,
                id
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
        if (value instanceof String){
            return '\'' + value.toString() + '\'';
        }
        return value.toString();
    }

    private String sendStatementHttpPost(String statement)  {
        HttpRequest hr = null;
        try {
            hr = new HttpRequest(new URL(GET_DATA_URL),
                    Collections.singletonMap("statement", statement),
                    HttpRequest.POST);
            hr.sendRequest();
            return hr.getReply();
        } catch (InterruptedException | IOException | ExecutionException e) {
            throw new RuntimeException("Error in statementHttpPost: " + e.getMessage(), e);
        }

    }

    private <T extends Entity> List<T> retrieveEntityFromDb(Class<T> type, String statement) {
        try {
            System.out.println("Starting ask sql: " + statement);
            String result = sendStatementHttpPost(statement);
            JSONArray jsonItems = new JSONArray(result);
            int itemsCount = jsonItems.length();
            List<T> items = new ArrayList<>(itemsCount);
            for (int i = 0; i < itemsCount; i++) {
                T item = jsonReflection.readObject(type, jsonItems.getJSONObject(i));
                items.add(item);
            }
            return items;
        }
        catch (JSONException e) {
            throw new RuntimeException("Error in create JSON objects, on asking sql from remote server.", e);
        }
    }

    private long createItem(Entity item) {
        Collection<Property> properties = jsonReflection.getProperties(item.getClass()).values();
        String fields = Stream.of(properties)
                .map(Property::getName)
                .collect(Collectors.joining(","));
        String values = Stream.of(properties)
                .map(p -> sqlValue(p.get(item)))
                .collect(Collectors.joining(","));
        String statement = String.format("INSERT INTO %s (%s) VALUES (%s)",
                tableName(item.getClass()), fields, values);
        System.out.println("Starting createItem: " + statement);
        String result = sendStatementHttpPost(statement);
        return Long.parseLong(result);
    }

    private void updateItem(Entity item) {
        Collection<Property> properties = jsonReflection.getProperties(item.getClass()).values();
        String keyValues = Stream.of(properties)
                .filter(p-> !p.getName().equals(ID))
                .map(p-> p.getName() + "=" + sqlValue(p.get(item)))
                .collect(Collectors.joining(","));
        String statement = String.format("UPDATE %s SET (%s) WHERE %s=%s",
                tableName(item.getClass()), keyValues, ID, item.getId());
        System.out.println("Starting updateItem: " + statement);
        sendStatementHttpPost(statement);
    }




}
