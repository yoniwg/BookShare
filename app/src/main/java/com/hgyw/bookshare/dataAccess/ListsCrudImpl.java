package com.hgyw.bookshare.dataAccess;

import com.annimon.stream.Stream;
import com.annimon.stream.function.Function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.hgyw.bookshare.entities.Entity;
import com.hgyw.bookshare.entities.IdReference;
import com.hgyw.bookshare.entities.reflection.EntityReflection;
import com.hgyw.bookshare.entities.reflection.Property;

/**
 * Created by Yoni on 3/17/2016.
 */
class ListsCrudImpl implements Crud {

    protected ListsCrudImpl() {}

    private Map<Class<? extends Entity>, Long> entitiesIdMap = new HashMap<>();
    private Map<Class<? extends Entity>, List<Entity>> entitiesMap = new HashMap<>();

    @Override
    public void create(Entity item) {
        item.setDeleted(false);
        checkAreReferencesLegal(item);
        List<Entity> entityList = getListOrCreate(item.getClass());

        if (item.getId() != 0) {
            throw new IllegalArgumentException("ID must be 0");
        }
        generateNewId(item);
        entityList.add(item.clone());
    }

    private void checkAreReferencesLegal(Entity item) {
        try {
            Stream.of(EntityReflection.getReferringProperties(item.getClass()).entrySet())
                    .forEach(keyValue -> {
                        Class<? extends Entity> referredClass = keyValue.getKey();
                        Property referredIdProperty = keyValue.getValue();
                        retrieve(referredClass, (Long) referredIdProperty.get(item)); // Just to throw exception when referred is not found
                    });
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("Id reference in " + item.getEntityType().getSimpleName() + " isn't exists: " + e.getMessage());
        }
    }

    private List<Entity> getListOrCreate(Class<? extends Entity> clazz) {
        List<Entity> entityList = entitiesMap.get(clazz);
        if (entityList == null) {
            entitiesMap.put(clazz, entityList = new ArrayList<Entity>());
            entitiesIdMap.put(clazz, 0L);
        }
        return entityList;
    }

    private void generateNewId(Entity entity) {
        long entityId = entitiesIdMap.get(entity.getClass()) + 1;
        entity.setId(entityId);
        entitiesIdMap.put(entity.getClass(), entityId);
    }

    @Override
    public void update(Entity item) {
        checkAreReferencesLegal(item);
        item.setDeleted(false);
        List<Entity> entityList = entitiesMap.get(item.getClass());
        entityList.remove(item);
        entityList.add(item.clone());
    }

    @Override
    public void delete(IdReference idReference) {
        Entity retrievedItem = retrieveNonDeletedOriginalEntity(idReference);
        retrievedItem.setDeleted(true);
    }

    private Entity retrieveNonDeletedOriginalEntity(IdReference idReference) {
        Entity retrievedItem = retrieveOriginalEntity(idReference.getEntityType(), idReference.getId());
        if (retrievedItem.isDeleted()) throw createNoSuchEntityException(idReference.getEntityType(), idReference.getId());
        return retrievedItem;
    }

    /**
     * Stream all items of specified entity.
     * @param <T> The type of entity
     * @return Stream of all entities.
     */
    public <T extends Entity> Stream<T> streamAll(Class<T> entityType) {
        List<Entity> entityList = entitiesMap.get(entityType);
        if (entityList != null) return Stream.of(entityList).map(e ->  (T) e.clone());
        return Stream.empty();
    }

    @Override
    public <T extends Entity> T retrieve(Class<T> entityClass, long entityId) {
        return (T) retrieveOriginalEntity(entityClass, entityId).clone();
    }


    private <T extends Entity> T retrieveOriginalEntity(Class<? extends T> entityClass, long id) {
        List<Entity> entityList = entitiesMap.get(entityClass);
        if (entityList == null) throw createNoSuchEntityException(entityClass, id);
        Entity item =  Stream.of(entityList)
                .filter(e -> e.getId() == id)
                .findFirst().orElseThrow(() -> createNoSuchEntityException(entityClass, id));
        return (T) item;

    }

    private static NoSuchElementException createNoSuchEntityException(Class<?> entityClass, long id) {
        return new NoSuchElementException("No entity " + entityClass.getSimpleName() + " with ID " + id);
    }


}
