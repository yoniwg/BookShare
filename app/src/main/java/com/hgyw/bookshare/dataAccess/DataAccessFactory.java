package com.hgyw.bookshare.dataAccess;

import android.content.Context;

import com.hgyw.bookshare.MyApplication;

/**
 * Created by Yoni on 3/17/2016.
 */
public class DataAccessFactory {

    private static final boolean PSEUDO_DELAY_TEST = false;

    private enum DatabaseType {
        LISTS {
            DataAccess createDataAccess() {
                return new StreamCrudDataAccess(new ListsCrudImpl());
            }
        },
        SQL_LITE {
            DataAccess createDataAccess() {
                Context appContext = MyApplication.getAppContext();
                return new StreamCrudDataAccess(new SqlLiteStreamCrud(appContext));
            }
        },
        SQL_LITE2 {  // TODO!
            DataAccess createDataAccess() {
                Context appContext = MyApplication.getAppContext();
                return new SqlLiteDataAccess(appContext);
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

    private DataAccessFactory() {}

    /**
     * @return
     */
    static synchronized public DataAccess getInstance(){
        if (dataAccess == null) {
            dataAccess = currentDB.createDataAccess();
            if (PSEUDO_DELAY_TEST) dataAccess = new DelayDataAccess(dataAccess);
        }
        return dataAccess;

    }
}
