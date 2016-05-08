package hgyw.com.bookshare.dataAccess;

/**
 * Created by Yoni on 3/17/2016.
 */
public class DataAccessFactory {

    private enum DatabaseType { LISTS, SQL_LITE ,MY_SQL}

    private static DatabaseType currentDB = DatabaseType.LISTS;

    private static DataAccess crud;

    private DataAccessFactory() {}

    /**
     * @return
     */
    static synchronized public DataAccess getInstance(){
        if (crud == null) {
            switch (currentDB) {
                case LISTS:
                    crud = new DataAccessListImpl();
                   // new CrudTest((ListsCrudImpl) crud); // test.
                    break;
                case SQL_LITE:
                    //TODO
                    break;
                case MY_SQL:
                    //TODO
                    break;
            }
        }
        return crud;
    }
}
