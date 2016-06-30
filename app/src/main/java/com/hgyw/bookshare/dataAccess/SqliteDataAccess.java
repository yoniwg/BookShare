package com.hgyw.bookshare.dataAccess;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;

import com.hgyw.bookshare.entities.Entity;
import com.hgyw.bookshare.entities.reflection.Converters;
import com.hgyw.bookshare.entities.reflection.ConvertersCollection;
import com.hgyw.bookshare.entities.reflection.EntityReflection;
import com.hgyw.bookshare.entities.reflection.FullConverter;
import com.hgyw.bookshare.entities.reflection.Property;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Implementation of SqlDataAccess using SQLite
 */
public class SqliteDataAccess extends SqlDataAccess {

    private static final String DATABASE_NAME = "booksAppDataBase";
    private static final int DATABASE_VERSION = 1;

    private final SQLiteOpenHelper openHelper;

    protected SqliteDataAccess(Context context) {
        super(new ConvertersCollection(
                Converters.ofIdentity(String.class).withSqlType("TEXT"),
                Converters.ofIdentity(Long.class).withSqlType("INTEGER"),
                Converters.ofIdentity(Integer.class).withSqlType("INTEGER"),
                Converters.fullConverter(Boolean.class, Integer.class, b->b?1:0, i -> i!=0).withSqlType("INTEGER"),
                Converters.fullConverter(byte[].class, String.class, arr -> Base64.encodeToString(arr, 0), str -> Base64.decode(str,0)).withSqlType("TEXT"),
                Converters.fullConverter(BigDecimal.class, String.class, Object::toString, BigDecimal::new, BigDecimal.ZERO).withSqlType("TEXT"),
                Converters.fullConverterInherit(Date.class, Long.class, Date::getTime,
                        Converters::newInstance,
                        type -> Converters.newInstance(type, System.currentTimeMillis()))
                        .withSqlType("INTEGER"),
                Converters.fullConverterInherit(Enum.class, Integer.class, Enum::ordinal,
                        (type, i) -> type.getEnumConstants()[i],
                        type -> type.getEnumConstants()[0])
                        .withSqlType("INTEGER")
        ), "primary key autoincrement");

        openHelper = new SQLiteOpenHelper(context, DATABASE_NAME , null, DATABASE_VERSION) {

            @Override
            public void onCreate(SQLiteDatabase db) {
                for (Class<? extends Entity> type : EntityReflection.getEntityTypes()) {
                    database = db; // for avoiding recurcive call to getWritableDatabase()
                    createTableIfNotExists(type);
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
                c.close();
                return tableNames;
            }
        };
    }

    private SQLiteDatabase database;
    private synchronized SQLiteDatabase getDatabase() {
        if (database == null || !database.isOpen()) {
            database = openHelper.getWritableDatabase();
        }
        return database;
    }

    public long getLastID() {
        final String LAST_ID_QUERY = "SELECT last_insert_rowid()";
        Cursor cur = getDatabase().rawQuery(LAST_ID_QUERY, null);
        cur.moveToFirst();
        long ID = cur.getLong(0);
        cur.close();
        return ID;
    }


    @Override
    protected synchronized long executeCreateSql(String sql) {
        getDatabase().execSQL(sql);
        return getLastID();
    }

    @Override
    protected void executeSql(String sql) {
        getDatabase().execSQL(sql);
    }

    private static <T> T genericGet(Cursor cursor, int column, Class<T> type) {
        type = Converters.toUnboxedType(type);
        if (type == int.class) return (T) (Object) cursor.getInt(column);
        if (type == long.class) return (T) (Object) cursor.getLong(column);
        if (type == double.class) return (T) (Object) cursor.getDouble(column);
        if (type == float.class) return (T) (Object) cursor.getFloat(column);
        if (type == String.class) return (T) (Object) cursor.getString(column);
        if (type == byte[].class) return (T) (Object) cursor.getBlob(column);
        if (type == boolean.class) return (T) (Object) (!cursor.isNull(column) && cursor.getShort(column) != 0);
        throw new RuntimeException("No method in " + Cursor.class + " to apply object of " + type);
    }

    public <T> T readObject(Cursor cursor, Class<T> type) {
        T newItem = Converters.newInstance(type);
        Map<String, Property> propertiesMap = getProperties(type);
        for (int i = 0; i < cursor.getColumnCount(); i++) {
            String propertyName = cursor.getColumnName(i);
            Property p = propertiesMap.get(propertyName);
            if (p == null) {
                String message = String.format("No property of cursor column '%s' in properties of %s.", propertyName, type);
                throw new RuntimeException(message);
            }
            FullConverter parser = sqlConverters.findFullConverter(p.getPropertyType());

            Object value;
            if (cursor.isNull(i)) { value = null; }
            else {
                Object cursorValue = genericGet(cursor, i, parser.getConvertType());
                value = parser.parse(p.getPropertyType(), cursorValue);
            }
            p.set(newItem, value);
        }
        return newItem;
    }

    @Override
    protected <T> List<T> executeResultSql(Class<T> type, String sql) {
        List<T> items = new ArrayList<>();
        Cursor result = getDatabase().rawQuery(sql, null);

        if (result.moveToFirst()) do {
            T item = readObject(result, type);
            items.add(item);
        } while (result.moveToNext());
        result.close();

        return items;
    }

}
