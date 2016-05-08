package hgyw.com.bookshare.dataAccess;

import com.annimon.stream.Stream;
import com.annimon.stream.function.Function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import hgyw.com.bookshare.entities.Entity;
import hgyw.com.bookshare.entities.IdReference;
import hgyw.com.bookshare.entities.reflection.Property;
import hgyw.com.bookshare.entities.reflection.PropertiesReflection;

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
        List<Entity> entityList = getListOrCreate(item.getClass());

        if (item.getId() != 0) {
            throw new IllegalArgumentException("ID must be 0");
        }
        generateNewId(item);
        entityList.add(item.clone());
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

    public <T extends Entity> Stream<T> streamAll(Class<? extends T> entityType) {
        List<Entity> entityList = entitiesMap.get(entityType);
        if (entityList != null) return Stream.of(entityList).map(e ->  (T) e.clone());
        return Stream.empty();
    }

    @Override
    public <T extends Entity> T retrieve(Class<? extends T> entityClass, long entityId) {
        return (T) retrieveOriginalEntity(entityClass, entityId).clone();
    }


    @Override
    public Entity retrieve(IdReference idReference) {
        return retrieve(idReference.getEntityType(), idReference.getId());
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

    @Override
    public <T extends Entity, R extends Entity> Function<T, R> retriever(Class<R> referredClass, Function<T, Long> referenceFunction) {
        return t -> retrieve(referredClass, referenceFunction.apply(t));
    }

}
