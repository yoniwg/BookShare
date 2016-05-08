package hgyw.com.bookshare.logicAccess;

/**
 * Created by haim7 on 24/03/2016.
 */
public class AccessManagerFactory {

    private static final AccessManager instance = AccessManagerImpl.INSTANCE;

    private AccessManagerFactory() {}

    public static AccessManager getInstance() {
        return instance;
    }
}
