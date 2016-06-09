package com.hgyw.bookshare.dataAccess;

import android.util.Base64;

import com.fasterxml.jackson.databind.util.Converter;
import com.hgyw.bookshare.entities.Entity;
import com.hgyw.bookshare.entities.reflection.Converters;
import com.hgyw.bookshare.entities.reflection.ConvertersCollection;
import com.hgyw.bookshare.entities.reflection.JsonReflection;
import com.hgyw.bookshare.entities.reflection.Parser;
import com.hgyw.bookshare.entities.reflection.Properties;
import com.hgyw.bookshare.entities.reflection.Property;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by haim7 on 26/05/2016.
 */
class MysqlDataAccess extends SqlDataAccess implements DataAccess {

    private static final String SERVER_URL = "http://yweisber.vlab.jct.ac.il";
    private static final String GET_DATA_URL = SERVER_URL + "/" + "getData.php";
    private static final ConvertersCollection sqlConverters = new ConvertersCollection(
            Converters.ofIdentity(String.class),
            Converters.ofIdentity(Integer.class),
            Converters.ofIdentity(Long.class),
            Converters.ofIdentity(Double.class),
            Converters.fullConverter(Boolean.class, Integer.class, b->(b)?1:0, i->i==1),
            Converters.fullConverter(byte[].class, String.class, arr -> Base64.encodeToString(arr, 0), str -> Base64.decode(str,0)),
            Converters.fullConverter(BigDecimal.class, String.class, Object::toString, BigDecimal::new, BigDecimal.ZERO),
            Converters.fullConverterInherit(Date.class, Long.class, Date::getTime, Converters::newDate, type -> Converters.newDate(type, 0)),
            Converters.fullConverterInherit(Enum.class, Integer.class, Enum::ordinal, (type, i) -> type.getEnumConstants()[i], type -> type.getEnumConstants()[0])
    );

    protected MysqlDataAccess() {
        super("id", JsonReflection.SUB_PROPERTY_SEPARATOR, sqlConverters);
    }

    private Object getFromJson(JSONObject jsonObject, Class<?> propertyType, String key) throws JSONException {
        if (propertyType == Integer.class) {
            return jsonObject.getInt(key);
        } else if (propertyType == Long.class) {
            return jsonObject.getLong(key);
        } else if (propertyType == Boolean.class) {
            return jsonObject.getBoolean(key);
        } else if (propertyType == Double.class) {
            return jsonObject.getDouble(key);
        } else if (propertyType == String.class) {
            return jsonObject.getString(key);
        } else {
            throw new RuntimeException("Cannot convert from jsonObject to " + propertyType.getName());
        }
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


    private String sendStatementHttpPost1(String statement)  {
        try {
            HttpAsync hr = new HttpAsync(new URL(GET_DATA_URL),
                    Collections.singletonMap("statement", statement),
                    HttpAsync.POST);
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


    protected  <T> List<T> executeResultSql(Class<T> type, String statement) {
        try {
            System.out.println("Starting ask sql: " + statement);
            String result = sendStatementHttpPost(statement);
            JSONArray jsonItems = new JSONArray(result);
            int itemsCount = jsonItems.length();
            List<T> items = new ArrayList<>(itemsCount);
            for (int i = 0; i < itemsCount; i++) {
                T item = readObject(type, jsonItems.getJSONObject(i));
                items.add(item);
            }
            return items;
        }
        catch (JSONException e) {
            throw new DataAccessIoException("Error in create JSON objects, on asking sql from remote server.", e);
        }
    }

    private  <T> T readObject(Class<T> type, JSONObject jsonObject) {
        T item = Converters.tryNewInstanceOrThrow(type);
        for (Property p : getProperties(type)) {
            Object propertyValue;
            try {
                Class<?> propertyType = p.getPropertyType();
                Parser propertyFromJsonParser = sqlConverters.findParser(propertyType);
                Class<?> jsonType = propertyFromJsonParser.getConvertType();

                Object jsonValue = getFromJson(jsonObject, jsonType, p.getName());
                propertyValue = jsonValue.equals(JSONObject.NULL) ? null : propertyFromJsonParser.parse(propertyType, jsonValue);
            }
            catch (JSONException e) {throw new RuntimeException(e);}
            p.set(item, propertyValue);
        }
        return item;
    }

    @Override
    protected long executeCreateSql(String  sql) {
        String result = sendStatementHttpPost(sql);
        return Long.parseLong(result);
    }

    @Override
    protected  void executeSql(String sql) {
        sendStatementHttpPost(sql);
    }

}
