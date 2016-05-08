package hgyw.com.bookshare.entities;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by haim7 on 04/05/2016.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface EntityReference {
    Class value();
}
