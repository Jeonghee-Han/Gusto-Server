package com.umc.gusto.domain.store.repository;

import com.umc.gusto.domain.store.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StoreRepository extends JpaRepository<Store, Long> {
//    @Query("SELECT s FROM Store s WHERE s.town.townCode = :townCode AND s.storeId IN :storeIds")
//    List<Store> findByTownCodeAndStoreIds(String townCode, List<Long> storeIds);

    List<Store> findTop5ByStoreNameContains(String keyword);

    @Query("SELECT s FROM Store s WHERE s.storeStatus = 'ACTIVE' AND s.storeId < :cursorId " +
            "AND (REPLACE(s.storeName, ' ', '') LIKE LOWER(CONCAT('%', REPLACE(:keyword, ' ', ''), '%')) " +
            "OR REPLACE(s.categoryString, ' ', '') LIKE LOWER(CONCAT('%', REPLACE(:keyword, ' ', ''), '%')))")
    Page<Store> searchByStoreNameContains(String keyword, Long cursorId, PageRequest pageRequest);
}
