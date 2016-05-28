package com.hgyw.bookshare.dataAccess;

import com.annimon.stream.Optional;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookQuery;
import com.hgyw.bookshare.entities.BookSummary;
import com.hgyw.bookshare.entities.Credentials;
import com.hgyw.bookshare.entities.Entity;
import com.hgyw.bookshare.entities.IdReference;
import com.hgyw.bookshare.entities.Order;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.entities.reflection.JsonReflection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by haim7 on 26/05/2016.
 */
class RemoteSqlDataAccess implements DataAccess {

    private static final String SUB = "_";
    private static final String GET_DATA_URI = "http://yweisber.vlab.jct.ac.il/getData.php";
    private static final String WRITE_DATA_URI = "http://yweisber.vlab.jct.ac.il/writeData.php";
    private static final JsonReflection jsonReflection = new JsonReflection();

    @Override
    public Optional<User> retrieveUserWithCredentials(Credentials credentials) {
        String sql = MessageFormat.format("SELECT * FROM {0} WHERE {1}={2} AND {3}={4}",
                tableName(User.class),
                "credentials" + SUB + "username",
                credentials.getUsername(),
                "credentials" + SUB + "password",
                credentials.getPassword()
                );
        List<User> sqlResult = askSql(User.class, sql);
        return sqlResult.isEmpty() ? Optional.empty() : Optional.of(sqlResult.get(0));
    }

    private Object tableName(Class<User> userClass) {
        return userClass.getSimpleName()+"_"+"table";
    }

    private static <T> Optional<T> optionalOf(List<T> list) {
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public boolean isUsernameTaken(String username) {
        return false;
    }

    @Override
    public List<User> findInterestedInBook(Book book, User userAsked) {
        return null;
    }

    @Override
    public List<Order> retrieveOrders(User customer, User supplier, Date fromDate, Date toDate, boolean onlyOpen) {
        return null;
    }

    @Override
    public List<Book> findBooks(BookQuery query) {
        return null;
    }

    @Override
    public List<Book> findSpecialOffers(User user, int limit) {
        return null;
    }

    @Override
    public <T extends Entity> List<T> findEntityReferTo(Class<T> referringClass, IdReference... referredItems) {
        return null;
    }

    @Override
    public BookSummary getBookSummary(Book book) {
        return null;
    }

    @Override
    public void create(Entity item) {

    }

    @Override
    public void update(Entity item) {

    }

    @Override
    public void delete(IdReference item) {

    }

    @Override
    public <T extends Entity> T retrieve(Class<T> entityClass, long id) {
        return null;
    }

    @Override
    public Entity retrieve(IdReference idReference) {
        return retrieve(idReference.getEntityType(), idReference.getId());
    }

    private <T extends Entity> List<T> askSql(Class<T> type, String statement) {
        try {
            String result = Http.post(GET_DATA_URI, Collections.singletonMap("statement", statement));
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
            throw new RuntimeException("Error in ask sql from remote server.", e);
        } catch (JSONException e) {
            throw new RuntimeException("Error in create JSON objects, on asking sql from remote server.", e);
        }
    }

    private <T extends Entity> List<T> sendSql(Class<T> type, String statement) {
        try {
            String result = Http.post(GET_DATA_URI, Collections.singletonMap("json", statement));
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
            throw new RuntimeException("Error in ask sql from remote server.", e);
        } catch (JSONException e) {
            throw new RuntimeException("Error in create JSON objects, on asking sql from remote server.", e);
        }
    }

}
