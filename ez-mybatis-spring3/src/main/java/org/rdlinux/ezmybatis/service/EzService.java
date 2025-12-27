package org.rdlinux.ezmybatis.service;

import org.rdlinux.ezmybatis.core.EzQuery;
import org.rdlinux.ezmybatis.dto.DcDTO;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * Base service interface
 *
 * @param <MdType> Entity type
 * @param <PkType> Primary key type
 */
public interface EzService<MdType, PkType extends Serializable> {
    /**
     * Query data by conditions
     *
     * @param param Query parameters
     */
    List<MdType> query(EzQuery<MdType> param);

    /**
     * Query total count by conditions
     *
     * @param param Query parameters
     */
    int queryCount(EzQuery<MdType> param);

    /**
     * Query data and total count by conditions
     *
     * @param param Query parameters
     */
    <RetType> DcDTO<RetType> queryDataAndCount(EzQuery<RetType> param);

    /**
     * Get entity by ID
     *
     * @param id Primary key
     */
    MdType getById(PkType id);

    /**
     * Get entities by multiple IDs
     *
     * @param ids Collection of primary keys
     */
    List<MdType> getByIds(Collection<PkType> ids);

    /**
     * Get entities by field
     *
     * @param field Entity field name
     * @param value Field value
     */
    List<MdType> getByField(String field, Object value);

    /**
     * Get one entity by field
     *
     * @param field Entity field name
     * @param value Field value
     */
    MdType getOneByField(String field, Object value);

    /**
     * Get entities by column
     *
     * @param column Table column name
     * @param value  Column value
     */
    List<MdType> getByColumn(String column, Object value);

    /**
     * Get one entity by column
     *
     * @param column Table column name
     * @param value  Column value
     */
    MdType getOneByColumn(String column, Object value);

    /**
     * Update entity, only non-null fields will be updated
     */
    int update(MdType model);

    /**
     * Batch update entities, only non-null fields will be updated
     */
    int batchUpdate(Collection<MdType> models);

    /**
     * Replace entity, all fields will be updated
     */
    int replace(MdType model);

    /**
     * Batch replace entities, all fields will be updated
     */
    int batchReplace(Collection<MdType> models);

    /**
     * Delete entity by ID
     */
    int deleteById(PkType id);

    /**
     * Batch delete entities by IDs
     */
    int deleteByIds(Collection<PkType> ids);

    /**
     * Delete entities by field
     *
     * @param field Entity field name
     * @param value Field value
     */
    int deleteByField(String field, Object value);

    /**
     * Delete entities by column
     *
     * @param column Table column name
     * @param value  Column value
     */
    int deleteByColumn(String column, Object value);

    /**
     * Delete entity
     *
     * @param model Entity to delete
     */
    int delete(MdType model);

    /**
     * Batch delete entities
     *
     * @param models Collection of entities to delete
     */
    int batchDelete(Collection<MdType> models);

    /**
     * Save entity
     *
     * @param model Entity to save
     */
    int save(MdType model);

    /**
     * Batch save entities
     *
     * @param models Collection of entities to save
     */
    int batchSave(Collection<MdType> models);
}
