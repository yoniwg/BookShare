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
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Created by haim7 on 26/05/2016.
 */
class MysqlDataAccess implements DataAccess {

    private static final String SUB = JsonReflection.SUB_PROPERTY_SEPARATOR;
    private static final String SERVER_URL = "http://yweisber.vlab.jct.ac.il";
    private static final String GET_DATA_URL = SERVER_URL + "/" + "getData.php";
    private static final String WRITE_DATA_URL = SERVER_URL + "/" + "/writeData.php";
    private static final JsonReflection jsonReflection = new JsonReflection();

    static String tableName(Class<?> userClass) {
        return userClass.getSimpleName()+"_"+"table";
    }

    {printTablesAndPropertiesPhpMap();}
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
    }


    private static String quote(String tableName) {
        return '\'' + tableName + '\'';
    }

    static String sqlQuote(String username) {
        return quote(username);
    }

    @Override
    public Optional<User> retrieveUserWithCredentials(Credentials credentials) {
        String sql = String.format("SELECT * from %s WHERE %s=%s %s=%s",
                tableName(User.class),
                "credentials" + SUB + "username", sqlQuote(credentials.getUsername()),
                "credentials" + SUB + "password", sqlQuote(credentials.getPassword())
        );
        List<User> sqlResult = getSql(User.class, sql);
        return sqlResult.isEmpty() ? Optional.empty() : Optional.of(sqlResult.get(0));
    }

    @Override
    public boolean isUsernameTaken(String username) {
        String sql = String.format("SELECT * from %s WHERE %s=%s",
                tableName(User.class),
                "credentials" + SUB + "username", sqlQuote(username)
        );
        List<User> sqlResult = getSql(User.class, sql);
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
        return getSql(Book.class, sql);
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
        return getSql(referringClass, sql);
    }

    @Override
    public BookSummary getBookSummary(Book book) {
        return new BookSummary(); // TODO
    }

    @Override
    public void create(Entity item) {
        if (item.getId() != 0) throw new IllegalArgumentException("Created item should have id 0.");
        item.setDeleted(false);
        writeItem(item);
    }

    @Override
    public void update(Entity item) {
        if (item.getId() == 0) throw new IllegalArgumentException("Updated item should not have id 0.");
        item.setDeleted(false);
        writeItem(item);
    }

    @Override
    public void delete(IdReference item) {
        String sql = String.format("UPDATE %s SET %s=%s WHERE %s=%s",
                tableName(item.getEntityType()),
                "deleted",
                true,
                "id",
                item.getId()
        );
        executeSql(sql);
    }

    @Override
    public <T extends Entity> T retrieve(Class<T> entityClass, long id) {
        String sql = String.format("SELECT * FROM %s WHERE %s=%s",
                tableName(entityClass),
                "id",
                id
        );
        List<T> result = getSql(entityClass, sql);
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
    
    private void executeSql(String statement) {
        try {
            System.out.println("Starting execute sql: " + statement);
            String result = Http.post(GET_DATA_URL, Collections.singletonMap("statement", statement));
        } catch (IOException e) {
            throw new RuntimeException("Error in read sql from remote server.", e);
        }
    }
    
    private <T extends Entity> List<T> getSql(Class<T> type, String statement) {
        try {
            System.out.println("Starting ask sql: " + statement);
            String result = Http.post(GET_DATA_URL, Collections.singletonMap("statement", statement));
            
            JSONArray jsonItems = new JSONObject(result).getJSONArray("products");
            int itemsCount = jsonItems.length();
            List<T> items = new ArrayList<>(itemsCount);
            for (int i = 0; i < itemsCount; i++) {
                T item = jsonReflection.readObject(type, jsonItems.getJSONObject(i));
                items.add(item);
            }
            return items;
        }
        catch (IOException e) {
            throw new RuntimeException("Error in read sql from remote server.", e);
        } catch (JSONException e) {
            throw new RuntimeException("Error in create JSON objects, on asking sql from remote server.", e);
        }
    }

    private void writeItem(Entity item) {
        String jsonString = jsonReflection.writeObject(item).toString();
        try {
            System.out.println("Starting send json.");
            String result = Http.post(WRITE_DATA_URL, Collections.singletonMap("json", jsonString));
        } catch (IOException e) {
            throw new RuntimeException("Error in write sql to remote server.", e);
        }
    }

}
