package com.hgyw.bookshare.dataAccess;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.annimon.stream.Collectors;
import com.hgyw.bookshare.entities.Entity;
import com.hgyw.bookshare.entities.reflection.EntityReflection;
import com.hgyw.bookshare.entities.reflection.SqlLiteReflection;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by haim7 on 09/06/2016.
 */
public class SqlLiteDataAccess extends SqlDataAccess {

    private static final String DATABASE_NAME = "booksAppDataBase";
    private static final int DATABASE_VERSION = 1;
    private static final SqlLiteReflection sqlLiteReflection = new SqlLiteReflection();

    private final Context context;

    private final SQLiteOpenHelper openHelper;

    protected SqlLiteDataAccess(Context context) {
        super(SqlLiteReflection.ID_KEY_SQL, "_", null, null); //TODO
        this.context = context;
        openHelper = new SQLiteOpenHelper(context, DATABASE_NAME , null, DATABASE_VERSION) {
            private void createTableIfNotExists(SQLiteDatabase db, Class<? extends Entity> type) {
                String sqlTable = tableName(type);
                String sqlColumnsType = sqlLiteReflection.streamProperties(type)
                        .map(p -> {
                            String columnName = p.getName();
                            String columnType = sqlLiteReflection.getSqlLiteNameOf(p.getPropertyType());
                            boolean isPrimaryKey = p.getName().equals(SqlLiteReflection.ID_KEY_SQL);
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
        };
    }

    public long getLastID() {
        final String LAST_ID_QUERY = "SELECT last_insert_rowid()";
        Cursor cur = openHelper.getReadableDatabase().rawQuery(LAST_ID_QUERY, null);
        cur.moveToFirst();
        long ID = cur.getLong(0);
        cur.close();
        return ID;
    }


    @Override
    protected synchronized long executeCreateSql(String sql) {
        openHelper.getWritableDatabase().execSQL(sql);
        return getLastID();
    }

    @Override
    protected void executeSql(String sql) {
        openHelper.getWritableDatabase().execSQL(sql);
    }

    @Override
    protected <T> List<T> executeResultSql(Class<T> type, String sql) {
        List<T> items = new ArrayList<>();
        Cursor result = openHelper.getReadableDatabase().rawQuery(sql, null);

        if (result.moveToFirst()) do {
            T item = sqlLiteReflection.readObject(result, type);
            items.add(item);
        } while (result.moveToNext());
        result.close();

        return items;
    }



}
