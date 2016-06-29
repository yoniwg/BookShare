package com.hgyw.bookshare.dataAccess;

import com.annimon.stream.Stream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.hgyw.bookshare.entities.Entity;
import com.hgyw.bookshare.entities.IdReference;
import com.hgyw.bookshare.entities.ImageEntity;
import com.hgyw.bookshare.entities.reflection.EntityReflection;
import com.hgyw.bookshare.entities.reflection.Property;

/**
 * A {@link StreamCrud} implementation by lists.
 */
class ListsCrudImpl implements StreamCrud {

    protected ListsCrudImpl() {}

    // map from entity to its list
    private Map<Class<? extends Entity>, List<Entity>> entitiesMap = new HashMap<>();
    // map from entity to max id in the list (the next generated id will increase with 1)
    private Map<Class<? extends Entity>, Long> entitiesIdMap = new HashMap<>();


    @Override
    public void create(Entity item) {
        item.setDeleted(false);
        checkAreReferencesLegal(item);
        List<Entity> entityList = getListOrCreate(item.getClass());

        if (item.getId() != Entity.DEFAULT_ID) {
            throw new IllegalArgumentException("ID must be 0");
        }
        generateNewId(item);
        entityList.add(item.clone());
    }

    /**
     * Checks are item's references exist in database
     * @param item the entity item
     * @throws NoSuchElementException if {@code item} refer to another item that is not exists in database
     */
    private void checkAreReferencesLegal(Entity item) {
        try {
            Stream.of(EntityReflection.getReferringProperties(item.getClass()).entrySet())
                    .forEach(keyValue -> {
                        Class<? extends Entity> referredClass = keyValue.getKey();
                        Property referredIdProperty = keyValue.getValue();
                        long referredId = (long) referredIdProperty.get(item);
                        // Allow no image
                        if (referredId == 0 && referredClass == ImageEntity.class) return;
                        // Just to throw exception when referred is not found
                        retrieve(referredClass, referredId);
                    });
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("Id reference in " + item.getEntityType().getSimpleName() + " isn't exists: " + e.getMessage());
        }
    }

    /**
     * Get the list of the entity. The list will be created if it has not been yet.
     */
    private List<Entity> getListOrCreate(Class<? extends Entity> clazz) {
        List<Entity> entityList = entitiesMap.get(clazz);
        if (entityList == null) {
            entitiesMap.put(clazz, entityList = new ArrayList<>());
            entitiesIdMap.put(clazz, 0L);
        }
        return entityList;
    }

    /**
     * Generates new id for entity
     * Sets the item.id to the new id.
     */
    private void generateNewId(Entity item) {
        long newId = entitiesIdMap.get(item.getClass()) + 1;
        item.setId(newId);
        entitiesIdMap.put(item.getClass(), newId);
    }

    @Override
    public void update(Entity item) {
        checkAreReferencesLegal(item);
        item.setDeleted(false);
        List<Entity> entityList = getListOrCreate(item.getClass());
        boolean isRemoved = entityList.remove(item);
        if (!isRemoved) throw new NoSuchElementException("No element of " + item.toIdReference());
        entityList.add(item.clone());
    }

    @Override
    public void delete(IdReference idReference) {
        Entity retrievedItem = retrieveOriginal(idReference.getEntityType(), idReference.getId());
        // cannot be deleted twice
        if (retrievedItem.isDeleted()) throw createNoSuchEntityException(idReference.getEntityType(), idReference.getId());
        retrievedItem.setDeleted(true);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Entity> Stream<T> streamAll(Class<T> entityType) {
        List<Entity> entityList = entitiesMap.get(entityType);
        if (entityList != null) return Stream.of(entityList).map(e ->  (T) e.clone());
        return Stream.empty();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Entity> T retrieve(Class<T> entityClass, long entityId) {
        return (T) retrieveOriginal(entityClass, entityId).clone(); // clone for retrieve DB change
    }

    /**
     * retrieve the original item that is in database.
     */
    @SuppressWarnings("unchecked")
    private <T extends Entity> T retrieveOriginal(Class<? extends T> entityClass, long id) {
        List<Entity> entityList = entitiesMap.get(entityClass);
        if (entityList == null) throw createNoSuchEntityException(entityClass, id);
        Entity item =  Stream.of(entityList)
                .filter(e -> e.getId() == id)
                .findFirst().orElseThrow(() -> createNoSuchEntityException(entityClass, id));
        return (T) item;
    }

    /**
     * Creates a NoSuchEntityException
     */
    private static NoSuchElementException createNoSuchEntityException(Class<?> entityClass, long id) {
        return new NoSuchElementException("No entity " + entityClass.getSimpleName() + " with ID " + id);
    }


}
