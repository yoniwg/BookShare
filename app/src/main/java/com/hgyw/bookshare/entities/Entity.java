package com.hgyw.bookshare.entities;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

import com.annimon.stream.function.Function;
import com.hgyw.bookshare.entities.reflection.Property;
import com.hgyw.bookshare.entities.reflection.Properties;

/**
 * Created by Yoni on 3/15/2016.
 */
public abstract class Entity extends IdReference implements Cloneable, Serializable
{
    public static int DEFAULT_ID = 0;

    private long id = DEFAULT_ID;
    private boolean deleted;

    /**
     * Get the id.
     * @return long value of id
     */
    public long getId() {
        return id;
    }

    /**
     * Set the id.
     * @param id new long value of id
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Do shallow copy on this entity
     */
    @Override
    public Entity clone() {
        try {
            return (Entity) super.clone();
        }
        catch (CloneNotSupportedException  e) {
            throw new InternalError();
        }
    }

    /**
     * Prints public getters of this object.
     * @return String described the this object.
     */
    @Override
    public String toString() {
        List<Property> props = Properties.getProperties(this.getClass());

        Property idProperty = null;
        for (Property p : props) if (p.getName().equalsIgnoreCase("id")) {idProperty = p; break;}
        assert idProperty != null;
        props.remove(idProperty);

        StringBuilder str = new StringBuilder();
        for (Property p : props) {
            Object value = p.get(this);
            if (value instanceof String) value = "'" + value + "'";
            if (value instanceof Entity)
                value = "(" + ((Entity) value).shortDescription() + ")";
            if (str.length() != 0) str.append(", ");
            str.append(p.getName()).append("=").append(value);
        }
        return getClass().getSimpleName() + "(id=" + idProperty.get(this) + "){" + str + "}";
    }

    public String shortDescription() {
        return "id=" + getId();
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean isDeleted) {
        this.deleted = isDeleted;
    }

    @Override
    public Class<? extends Entity> getEntityType() {
        return getClass();
    }

    public static <T extends Entity> Comparator<? super T> defaultComparator(Class<T> referringClass) {
        if (referringClass == Transaction.class) {
            return  (a,b) -> -((Transaction) a).getDate().compareTo(((Transaction) b).getDate());
        } else if (referringClass == Book.class) {
            return  (a,b) -> ((Book) a).getTitle().compareTo(((Book) b).getTitle());
        } else {
            return  (a,b) -> a.getId() < b.getId() ? 1 : a.getId() == b.getId() ? 0 : -1;
        }
    }
}
