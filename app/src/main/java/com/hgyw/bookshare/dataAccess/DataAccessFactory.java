package com.hgyw.bookshare.dataAccess;

/**
 * Created by Yoni on 3/17/2016.
 */
public class DataAccessFactory {

    private enum DatabaseType { LISTS, SQL_LITE ,MY_SQL}

    private static DatabaseType currentDB = DatabaseType.LISTS;

    private static DataAccess dataAccess;

    private DataAccessFactory() {}

    /**
     * @return
     */
    static synchronized public DataAccess getInstance(){
        if (dataAccess == null) {
            switch (currentDB) {
                case LISTS:
                    dataAccess = new DataAccessListImpl();
                   // new CrudTest((ListsCrudImpl) crud); // test.
                    break;
                case SQL_LITE:
                    //TODO
                    break;
                case MY_SQL:
                    //TODO
                    break;
            }
            //dataAccess = new DelayDataAccess(dataAccess); // delay test
        }
        return dataAccess;

    }
}
