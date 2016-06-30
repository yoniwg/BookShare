package com.hgyw.bookshare.logicAccess;

/**
 * Static factory for AccessManager
 */
public final class AccessManagerFactory {

    private static final AccessManager instance = AccessManagerImpl.INSTANCE;
    //static {Test.test(instance);}

    private AccessManagerFactory() {} // restrict instantiate

    public static AccessManager getInstance() {
        return instance;
    }

}
