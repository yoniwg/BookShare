package com.hgyw.bookshare.dataAccess;

import com.annimon.stream.Stream;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookQuery;
import com.hgyw.bookshare.entities.BookReview;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.Entity;
import com.hgyw.bookshare.entities.IdReference;
import com.hgyw.bookshare.entities.ImageEntity;
import com.hgyw.bookshare.entities.Order;
import com.hgyw.bookshare.entities.OrderRating;
import com.hgyw.bookshare.entities.Transaction;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.entities.reflection.PropertiesReflection;
import com.hgyw.bookshare.entities.reflection.Property;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * Created by Yoni on 5/27/2016.
 */
public class MySqlCrud implements Crud {

    private static final String DB_URL = "http://yweisber.vlab.jct.ac.il/getData.php";

/*
    private static final Map<Class,String> sqlTypes = new HashMap<>();
    static {
        sqlTypes.put(Integer.class, "INTEGER");
        sqlTypes.put(int.class, "INTEGER");
        sqlTypes.put(Long.class, "BIGINT");
        sqlTypes.put(long.class, "BIGINT");
        sqlTypes.put(String.class, "TEXT");
        sqlTypes.put(Boolean.class, "BOOLEAN");
        sqlTypes.put(boolean.class, "BOOLEAN");
        sqlTypes.put(BigDecimal.class, "DECIMAL");
        sqlTypes.put(Date.class, "DATE");
        sqlTypes.put(java.sql.Date.class, "DATE");
        sqlTypes.put(byte[].class, "BLOB");
    }

    public static void createAll(){
        createOnce(Book.class);
        createOnce(BookReview.class);
        createOnce(BookSupplier.class);
        createOnce(ImageEntity.class);
        createOnce(Order.class);
        createOnce(Transaction.class);
        createOnce(User.class);
    }

    public static void createOnce(Class<? extends Entity> klass) {
        StringBuilder statement = new StringBuilder("CREATE TABLE `BookSharing`.");
        statement.append("`" + klass.getSimpleName().toLowerCase() + "_table` (");
        statement.append("`id` BIGINT NOT NULL , ");
        List<Property> properties = PropertiesReflection.getProperties(klass);
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
        HttpRequest hr = null;
        try {
            hr = new HttpRequest(new URL(DB_URL), request, HttpRequest.POST);
            hr.sendRequest();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
*/

    private static String getTableName(Class<? extends Entity> klass){
        return klass.getSimpleName().toLowerCase() + "_table";
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
    public <T extends Entity> Stream<T> streamAll(Class<T> entityType) {
        return null;
    }

    @Override
    public <T extends Entity> T retrieve(Class<T> entityClass, long id) {
        return null;
    }
}
