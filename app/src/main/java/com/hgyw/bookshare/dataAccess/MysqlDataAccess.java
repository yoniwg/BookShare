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
class MysqlDataAccess extends SqlDataAccess implements DataAccess {

    private static final String SERVER_URL = "http://yweisber.vlab.jct.ac.il";
    private static final String GET_DATA_URL = SERVER_URL + "/" + "getData.php";
    private static final JsonReflection jsonReflection = new JsonReflection();

    protected MysqlDataAccess() {
        super("id", JsonReflection.SUB_PROPERTY_SEPARATOR);
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

    private String sendStatementHttpPost1(String statement)  {
        try {
            HttpRequest hr = new HttpRequest(new URL(GET_DATA_URL),
                    Collections.singletonMap("statement", statement),
                    HttpRequest.POST);
            hr.sendRequest();
            return hr.getReply();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error in statementHttpPost: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new DataAccessIoException("Error in statementHttpPost: " + e.getMessage(), e);
        }

    }

    private String sendStatementHttpPost(String statement)  {
        try {
            return Http.post(GET_DATA_URL, Collections.singletonMap("statement", statement));
        } catch (IOException e) {
            throw new DataAccessIoException("Error in statementHttpPost: " + e.getMessage(), e);
        }
    }

    ///////////////////////////////
    // DataAccess implementation
    ///////////////////////////////


    @Override
    public void delete(IdReference item) {
        String sql = String.format("UPDATE %s SET %s=%s WHERE %s=%s",
                tableName(item.getEntityType()),
                "deleted",
                true,
                ID_KEY,
                item.getId()
        );
        sendStatementHttpPost(sql);
    }

    protected  <T> List<T> retrieveEntityFromDb(Class<T> type, String statement) {
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
            throw new DataAccessIoException("Error in create JSON objects, on asking sql from remote server.", e);
        }
    }

    @Override
    protected long createItemDb(Entity item) {
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
        String newId = sendStatementHttpPost(statement);
        return Long.parseLong(newId);
    }

    @Override
    protected void updateItemDb(Entity item) {
        Collection<Property> properties = jsonReflection.getProperties(item.getClass()).values();
        String keyValues = Stream.of(properties)
                .filter(p -> !p.getName().equals(ID_KEY))
                .map(p -> p.getName() + "=" + sqlValue(p.get(item)))
                .collect(Collectors.joining(","));
        String statement = String.format("UPDATE %s SET %s WHERE %s=%s",
                tableName(item.getClass()), keyValues, ID_KEY, item.getId());
        System.out.println("Starting updateItem: " + statement);
        sendStatementHttpPost(statement);
    }

}
