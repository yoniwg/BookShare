package com.hgyw.bookshare.dataAccess;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.hgyw.bookshare.entities.Entity;
import com.hgyw.bookshare.entities.IdReference;
import com.hgyw.bookshare.entities.reflection.EntityReflection;
import com.hgyw.bookshare.entities.reflection.SqlliteReflection;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Created by haim7 on 24/05/2016.
 */
@Deprecated
class SqlLiteStreamCrud extends SQLiteOpenHelper implements StreamCrud {

    private static final String DATABASE_NAME = "booksAppDataBase";
    private static final int DATABASE_VERSION = 1;
    private final SqlliteReflection sqlLiteReflection = new SqlliteReflection();

    /**
     * return the sqlLite table-name for a class.
     */
    private static String tableName(Class<?> aClass) {
        return aClass.getSimpleName() + "_" + "table";
    }

    /** Constructor */
    public SqlLiteStreamCrud(Context context) {
        super(context, DATABASE_NAME , null, DATABASE_VERSION);
    }


    @Override
    public void create(Entity item) {
        if (item.getId() != Entity.DEFAULT_ID) throw new IllegalArgumentException("The item id should be " + Entity.DEFAULT_ID + " on create new item.");
        item.setDeleted(false);

        String sqlTable = tableName(item.getEntityType());
        ContentValues contentValues = sqlLiteReflection.writeObject(item);

        long newId = getWritableDatabase().insert(sqlTable, null, contentValues);
        if (newId != -1 ) item.setId(newId);
        else throw new SQLException("SqlLite cannot write new item from unknown reason. The id accepdet is -1."); // TODO better treatment
    }

    @Override
    public void update(Entity item) {
        item.setDeleted(false);
        updateDatabase(item);
    }


    // update object to database with none validation.
    private void updateDatabase(Entity item) {
        String sqlTable = tableName(item.getEntityType());
        ContentValues contentValues = sqlLiteReflection.writeObject(item);
        int numberOfAffected = getWritableDatabase().update(sqlTable, contentValues, SqlliteReflection.ID_KEY_SQL + " = " + item.getId(), null);
        if (numberOfAffected == 0) throwNoFountException(item.getEntityType(), item.getId());
    }

    @Override
    public void delete(IdReference itemReference) {
        Entity item = retrieve(itemReference.getEntityType(), itemReference.getId());
        item.setDeleted(true);
        updateDatabase(item);
    }

    @Override
    public <T extends Entity> Stream<T> streamAll(Class<T> entityType) {
        ArrayList<T> items = new ArrayList<>();

        String sql = "select * from " + tableName(entityType);
        Cursor result = getReadableDatabase().rawQuery(sql, null );

        if (result.moveToFirst()) do {
            T item = sqlLiteReflection.readObject(result, entityType);
            items.add(item);
        } while (result.moveToNext());
        result.close();

        return Stream.of(items);
    }

    @Override
    public <T extends Entity> T retrieve(Class<T> entityType, long id) {
        String sql =  "select * from " + tableName(entityType) + (" where " + SqlliteReflection.ID_KEY_SQL + "=") + id;
        Cursor result = getReadableDatabase().rawQuery(sql, null);
        if (!result.moveToFirst()) throwNoFountException(entityType, id);
        T item = sqlLiteReflection.readObject(result, entityType);
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
        String sqlColumnsType = sqlLiteReflection.streamProperties(type)
                .map(p -> {
                    String columnName = p.getName();
                    String columnType = sqlLiteReflection.getSqlLiteNameOf(p.getPropertyType());
                    boolean isPrimaryKey = p.getName().equals(SqlliteReflection.ID_KEY_SQL);
                    String primaryKey = isPrimaryKey ? " primary key autoincrement" : "";
                    return columnName + " " + columnType + primaryKey;
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
