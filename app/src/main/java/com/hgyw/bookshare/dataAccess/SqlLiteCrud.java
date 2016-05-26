package com.hgyw.bookshare.dataAccess;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.hgyw.bookshare.entities.Entity;
import com.hgyw.bookshare.entities.IdReference;
import com.hgyw.bookshare.entities.reflection.Converter;
import com.hgyw.bookshare.entities.reflection.Converters;
import com.hgyw.bookshare.entities.reflection.EntityReflection;
import com.hgyw.bookshare.entities.reflection.PropertiesReflection;
import com.hgyw.bookshare.entities.reflection.PropertiesConvertManager;
import com.hgyw.bookshare.entities.reflection.SqlAndroidReflection;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Created by haim7 on 24/05/2016.
 */
public class SqlLiteCrud extends SQLiteOpenHelper implements Crud {

    private static final String DATABASE_NAME = "booksAppDataBase";
    private static final int DATABASE_VERSION = 1;

    private static final String PRIMARY_KEY_STRING = "id";

    private static final List<Converter> sqlLiteConverter = Arrays.asList(new Converter[]{
            Converters.ofIdentity(Integer.class, "INTEGER"),
            Converters.ofIdentity(Long.class, "BIGINT"),
            Converters.ofIdentity(String.class, "TEXT"),
            Converters.simple(Boolean.class, Integer.class, b -> b?1:0, i -> i==1, "INTEGER"),
            Converters.simple(BigDecimal.class, String.class, Object::toString, BigDecimal::new, "TEXT"),
            Converters.simple(Date.class, Long.class, Date::getTime, Date::new, "BIGINT"),
            Converters.simple(java.sql.Date.class, Long.class, Date::getTime, java.sql.Date::new, "BIGINT"),
    });

    private static String tableName(Class aClass) {
        return aClass.getSimpleName() + "_" + "table";
    }

    private final PropertiesConvertManager convertManager = PropertiesReflection.newPropertiesConvertManager("__", sqlLiteConverter);

    public SqlLiteCrud(Context context) {
        super(context, DATABASE_NAME , null, DATABASE_VERSION);
    }


    @Override
    public void create(Entity item) {
        if (item.getId() != Entity.DEFAULT_ID) throw new IllegalArgumentException("The item id should be " + Entity.DEFAULT_ID + " on create new item.");
        String sqlTable = tableName(item.getEntityType());
        ContentValues contentValues = SqlAndroidReflection.writeObject(item, convertManager);
        long newId = getWritableDatabase().insert(sqlTable, null, contentValues);
        if (newId != -1 ) item.setId(newId);
        else throw new RuntimeException("SqlLite cannot write new item from unknown reason.");
    }

    @Override
    public void update(Entity item) {
        String sqlTable = tableName(item.getEntityType());
        ContentValues contentValues = SqlAndroidReflection.writeObject(item, convertManager);
        int numberOfAffected = getWritableDatabase().update(sqlTable, contentValues, "id = ? ", new String[]{item.getId() + ""});
        if (numberOfAffected == 0) throwNoFountException(item.getEntityType(), item.getId());
    }

    @Override
    public void delete(IdReference itemReference) {
        Entity item = retrieve(itemReference.getEntityType(), itemReference.getId());
        item.setDeleted(true);
        update(item);
    }

    @Override
    public <T extends Entity> Stream<T> streamAll(Class<T> entityType) {
        ArrayList<T> items = new ArrayList<>();

        String sql = "select * from " + tableName(entityType);
        Cursor result = getReadableDatabase().rawQuery(sql, null );

        if (result.moveToFirst()) do {
            T item = SqlAndroidReflection.readObject(result, entityType, convertManager);
            items.add(item);
        } while (result.moveToNext());
        result.close();

        return Stream.of(items);
    }

    @Override
    public <T extends Entity> T retrieve(Class<T> entityType, long id) {
        String sql =  "select * from " + tableName(entityType) + " where id=" + id;
        Cursor result = getReadableDatabase().rawQuery(sql, null);
        if (!result.moveToFirst()) throwNoFountException(entityType, id);
        T item = SqlAndroidReflection.readObject(result, entityType, convertManager);
        result.close();
        return item;
    }

    private <T extends Entity> void throwNoFountException(Class<T> entityType, long id) {
        String message = "The item of " + entityType + " with id " + id + " is not exists in database.";
        throw new NoSuchElementException(message);
    }

    ////////////////////////////////
    // SQLiteOpenHelper merhods
    ////////////////////////////////

    private void createTableIfNotExists(SQLiteDatabase db, Class<? extends Entity> type) {
        String sqlTable = tableName(type);
        String sqlColumnsType = convertManager.streamFlatProperties(type)
                .map(p -> {
                    String columnName = p.getName();
                    String columnType = convertManager.findConverter(p.getPropertyType()).getConvertTypeName();
                    boolean isPrimaryKey = p.getName().equals(PRIMARY_KEY_STRING);
                    if (isPrimaryKey) {
                        columnType = "INTEGER";
                        return p.getName() + " " + columnType + " primary key autoincrement";
                    } else {
                        return columnName + " " + columnType;
                    }
                })
                .collect(Collectors.joining(","));
        String sql = "CREATE TABLE IF NOT EXISTS " + sqlTable + "(" + sqlColumnsType + ")";
        db.execSQL(sql);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (Class type : EntityReflection.getEntityTypes()) {
            createTableIfNotExists(db, type);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        clearDataBase(db);
        onCreate(db);
    }

    private void clearDataBase(SQLiteDatabase db) {
        for (String tableName : getAllTables(db)) {
            db.execSQL("DROP TABLE IF EXISTS " + tableName);
        }
    }

    private List<String> getAllTables(SQLiteDatabase db) {
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        List<String> tableNames = new ArrayList<>();
        if (c.moveToFirst()) do {
            String tableName = c.getString(0);
            tableNames.add(tableName);
        } while (c.moveToNext() );
        return tableNames;
    }
}
