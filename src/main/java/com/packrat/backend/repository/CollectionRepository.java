package com.packrat.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.packrat.backend.dto.CollectionResponse;
import com.packrat.backend.entity.Collection;

public interface CollectionRepository extends JpaRepository<Collection, UUID>{

    /**
     * One aggregate query for the overview dashboard (ADR-022). Amounts are summed without currency
     * conversion (ADR-006); totalPriceNow only counts items that have one and is null when none do (ADR-017).
     */
    @Query("""
            select new com.packrat.backend.dto.CollectionResponse(
                c.id, c.user.id, c.name, count(i), coalesce(sum(i.pricePaid), 0), sum(i.priceNow))
            from Collection c left join Item i on i.collection = c
            where c.user.id = :userId
            group by c.id, c.user.id, c.name
            order by c.name""")
    List<CollectionResponse> findCollectionsWithTotalsByUserId(UUID userId);
}
