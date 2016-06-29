package com.hgyw.bookshare.dataAccess;

import android.content.Context;

import com.hgyw.bookshare.MyApplication;

/**
 * Static factory for {@link DataAccess} interface.
 */
public class DataAccessFactory {

    private enum DatabaseType {
        LISTS {
            DataAccess createDataAccess() {
                return new StreamCrudDataAccess(new ListsCrudImpl());
            }
        },
        SQL_LITE {
            DataAccess createDataAccess() {
                Context appContext = MyApplication.getAppContext();
                return new SqliteDataAccess(appContext);
            }
        },
        MY_SQL{
            DataAccess createDataAccess() {
                return new MysqlDataAccess();
            }
        }
        ;

        abstract DataAccess createDataAccess();
    }

    private static DatabaseType currentDB = DatabaseType.MY_SQL;

    private static DataAccess dataAccess;

    private DataAccessFactory() {}  // restrict instantiation

    /**
     * @return
     */
    static synchronized public DataAccess getInstance(){
        if (dataAccess == null) {
            dataAccess = currentDB.createDataAccess();
        }
        return dataAccess;

    }
}
