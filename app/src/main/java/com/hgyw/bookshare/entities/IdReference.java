package com.hgyw.bookshare.entities;

import java.util.Objects;

/**
 * Created by haim7 on 04/05/2016.
 */
public abstract class IdReference {

    public abstract long getId();

    public abstract Class<? extends Entity> getEntityType();

    /**
     * @param entityClass not null class of <? extends Entity>
     * @param id entityId
     * @return IdReference
     */
    public static IdReference of(Class<? extends Entity> entityClass, long id) {
        Objects.requireNonNull(entityClass);
        return new IdReference() {
            @Override
            public long getId() {
                return id;
            }

            @Override
            public Class<? extends Entity> getEntityType() {
                return entityClass;
            }
        };
    }

    /**
     * Returns true if and only id the object o is of the same class of this, and the id's are equals.
     * @param o the object to equal.
     * @return boolean value indicates whether the objects are equals.
     */
    @Override
    public final boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) return false;
        Entity other = (Entity) o;
        return this.getEntityType() == other.getEntityType() && this.getId() == other.getId();
    }

    /**
     * Hash code method.
     * @return hash code.
     */
    @Override
    public final int hashCode() {
        return Long.valueOf(getId()).hashCode();
    }

    @Override
    public String toString() {
        return "{" + "id=" + getId() + ", entityType=" + getEntityType() +"}";
    }


    public final IdReference toIdReference() {
        if (this.getClass() == IdReference.class) return this;
        return IdReference.of(getEntityType(), getId());
    }
}
