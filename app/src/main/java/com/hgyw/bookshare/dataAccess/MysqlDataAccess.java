package com.hgyw.bookshare.dataAccess;

import android.util.Base64;

import com.hgyw.bookshare.entities.reflection.Converters;
import com.hgyw.bookshare.entities.reflection.ConvertersCollection;
import com.hgyw.bookshare.entities.reflection.Parser;
import com.hgyw.bookshare.entities.reflection.Property;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
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

    protected MysqlDataAccess() {
        super(new ConvertersCollection(
                Converters.ofIdentity(String.class).withSqlName("TEXT"),
                Converters.fullConverter(Long.class, String.class, Object::toString, Long::new).withSqlName("BIGINT"),
                Converters.fullConverter(Integer.class, String.class, Object::toString, Integer::new).withSqlName("INT"),
                Converters.fullConverter(Boolean.class, String.class, b->b?"1":"0", i->i.equals("1")).withSqlName("TINYINT"),
                Converters.fullConverter(byte[].class, String.class, arr -> Base64.encodeToString(arr, 0), str -> Base64.decode(str,0)).withSqlName("TINYTEXT"),
                Converters.fullConverter(BigDecimal.class, String.class, Object::toString, BigDecimal::new, BigDecimal.ZERO).withSqlName("DECIMAL(6,2)"),
                Converters.fullConverterInherit(Date.class, String.class,
                        d -> String.valueOf(d.getTime()),
                        (type, str) -> Converters.newInstance(type, new Long(str)),
                        type -> Converters.newInstance(type, 0)
                ).withSqlName("BIGINT"),
                Converters.fullConverterInherit(Enum.class, String.class,
                        e -> String.valueOf(e.ordinal()),
                        (type, str) -> type.getEnumConstants()[new Integer(str)],
                        type -> type.getEnumConstants()[0]
                ).withSqlName("TINYINT")
        ), "PRIMARY KEY AUTO_INCREMENT");
    }


    /*public static void createOnce(Class<? extends Entity> klass) {
        StringBuilder statement = new StringBuilder("CREATE TABLE `BookSharing`.");
        statement.append("`" + klass.getSimpleName().toLowerCase() + "_table` (");
        statement.append("`id` BIGINT NOT NULL AUTO_INCREMENT, ");
        Collection<Property> properties = new JsonReflection().getProperties(klass).values();
        for (Property property : properties) {
            if (property.getName() == "id") continue;
            statement.append("`" + property.getName() + "` ");
            String type;
            if ((type = sqlTypes.get(property.getPropertyType())) == null){
                type = "TEXT";
            }
            statement.append(type + " ");
            statement.append("NULL , ");
        }
        statement.append("PRIMARY KEY (`id`))");
        statement.append(" ENGINE = InnoDB;");
        HashMap<String,String> request = new HashMap<>();
        request.put("statement", statement.toString());
        HttpAsync hr = null;
        try {
            hr = new HttpAsync(new URL(DB_URL), request, HttpAsync.POST);
            hr.sendRequest();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    /////////////////////////////
    // Http
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

    @Override
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
        T item = Converters.newInstance(type);
        for (Property p : getProperties(type).values()) {
            Object propertyValue;
            try {
                Class<?> propertyType = p.getPropertyType();
                Parser parser = sqlConverters.findParser(propertyType);

                String stringValue = jsonObject.getString(p.getName());
                // The null value is created by json, so we have to compare it with json's null:
                if (stringValue.equals(JSONObject.NULL.toString())) propertyValue = null;
                else propertyValue = parser.parse(propertyType, stringValue);
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
