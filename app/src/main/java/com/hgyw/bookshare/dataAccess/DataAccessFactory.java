package com.hgyw.bookshare.dataAccess;

import com.hgyw.bookshare.MyApplication;

/**
 * Created by Yoni on 3/17/2016.
 */
public class DataAccessFactory {

    private static final boolean PSEUDO_DELAY_TEST = false;

    private enum DatabaseType {
        LISTS {
            DataAccess createDataAccess() {
                return new StreamableCrudDataAccess(new ListsCrudImpl());
            }
        },
        SQL_LITE {
            DataAccess createDataAccess() {
                return new StreamableCrudDataAccess(new SqlLiteCrud(MyApplication.getAppContext()));
            }
        },
        MY_SQL{
            DataAccess createDataAccess() {
                throw new UnsupportedOperationException("No implementation yet.");
            }
        }
        ;

        abstract DataAccess createDataAccess();
    }

    private static DatabaseType currentDB = DatabaseType.SQL_LITE;

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
