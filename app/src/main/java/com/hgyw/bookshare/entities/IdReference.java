package com.hgyw.bookshare.entities;

import java.util.Objects;

/**
 * class that provides getId() and getEntityType() methods.
 * This class has final {@link IdReference#equals(Object)} method, guaranty that every sub-class
 * equals by the same conditions.
 */
public abstract class IdReference {

    public abstract long getId();

    public abstract Class<? extends Entity> getEntityType();

    /**
     * Factory method for simple immutable IdReference.
     * @param entityClass not null instance of Entity
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

    @Override
    public final int hashCode() {
        return Long.valueOf(getId()).hashCode();
    }

    @Override
    public String toString() {
        return "{" + "id=" + getId() + ", entityType=" + getEntityType() +"}";
    }

    /**
     * @return IdReference refer to type and id of this object, by {@link IdReference#of(Class, long)}
     * method. if this object created by that method, it will returns itself.
     */
    public final IdReference toIdReference() {
        if (this.getClass() == IdReference.class) return this;
        return IdReference.of(getEntityType(), getId());
    }
}
