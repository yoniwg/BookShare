package com.hgyw.bookshare.dataAccess;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.annimon.stream.function.Supplier;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.Entity;
import com.hgyw.bookshare.entities.IdReference;
import com.hgyw.bookshare.entities.reflection.EntityReflection;
import com.hgyw.bookshare.entities.reflection.PropertiesReflection;
import com.hgyw.bookshare.entities.reflection.Property;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Created by haim7 on 24/05/2016.
 */
public class SqlLiteCrud implements Crud {

    private final List<Class<? extends Entity>> entityTypes = EntityReflection.getEntityTypes();
    private final SQLiteDatabase db;
    private static final String DB_NAME = "booksAppDataBase";

    public SqlLiteCrud(Context context) {
        db  = context.openOrCreateDatabase(DB_NAME,Context.MODE_PRIVATE,null);
        entityTypes.add(Book.class);
        for (Class<? extends Entity> type : entityTypes) {
            String sqlTable = type.getSimpleName();
            String sqlColumns = SqlReflection.sqlColumnType(type);
            String sql = "CREATE TABLE IF NOT EXISTS " + sqlTable + "(" + sqlColumns + ")";
            db.execSQL(sql);
        }
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
