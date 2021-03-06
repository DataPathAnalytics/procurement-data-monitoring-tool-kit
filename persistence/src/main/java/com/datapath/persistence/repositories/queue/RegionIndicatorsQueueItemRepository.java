package com.datapath.persistence.repositories.queue;

import com.datapath.persistence.entities.queue.RegionIndicatorsQueueItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegionIndicatorsQueueItemRepository extends JpaRepository<RegionIndicatorsQueueItem, Long> {

    Page<RegionIndicatorsQueueItem> findAllByRegionInOrderByMaterialityScoreDesc(List<String> regions, Pageable pageable);

    Page<RegionIndicatorsQueueItem> findAllByTenderScoreLessThanAndRegionInOrderByMaterialityScoreDesc(Double maxImpact,
                                                                                                       List<String> regions,
                                                                                                       Pageable pageable);

    Page<RegionIndicatorsQueueItem> findAllByTenderScoreGreaterThanEqualAndRegionInOrderByMaterialityScoreDesc(Double minImpact,
                                                                                                               List<String> regions,
                                                                                                               Pageable pageable);

    Page<RegionIndicatorsQueueItem> findAllByTenderScoreGreaterThanEqualAndTenderScoreLessThanAndRegionInOrderByMaterialityScoreDesc(Double minImpact,
                                                                                                                                     Double maxImpact,
                                                                                                                                     List<String> regions,
                                                                                                                                     Pageable pageable);

    Page<RegionIndicatorsQueueItem> findAllByOrderByMaterialityScoreDesc(Pageable pageable);

    Page<RegionIndicatorsQueueItem> findAllByTenderScoreLessThanOrderByMaterialityScoreDesc(Double maxImpact,
                                                                                            Pageable pageable);

    Page<RegionIndicatorsQueueItem> findAllByTenderScoreGreaterThanEqualOrderByMaterialityScoreDesc(Double minImpact,
                                                                                                    Pageable pageable);

    Page<RegionIndicatorsQueueItem> findAllByTenderScoreGreaterThanEqualAndTenderScoreLessThanOrderByMaterialityScoreDesc(Double minImpact,
                                                                                                                          Double maxImpact,
                                                                                                                          Pageable pageable);

    Integer countByTopRiskIsTrueAndTenderScoreLessThanAndRegionIn(Double maxImpact, List<String> regions);

    Integer countByTopRiskIsTrueAndTenderScoreGreaterThanEqualAndRegionIn(Double minImpact, List<String> regions);

    Integer countByTopRiskIsTrueAndTenderScoreGreaterThanEqualAndTenderScoreLessThanAndRegionIn(Double minImpact,
                                                                                                Double maxImpact,
                                                                                                List<String> regions);


    Integer countByTopRiskIsTrueAndTenderScoreLessThan(Double maxImpact);

    Integer countByTopRiskIsTrueAndTenderScoreGreaterThanEqual(Double minImpact);

    Integer countByTopRiskIsTrueAndTenderScoreGreaterThanEqualAndTenderScoreLessThan(Double minImpact,
                                                                                     Double maxImpact);

    Integer countByTopRiskIsTrueAndRegionIn(List<String> regions);

    Integer countByTopRiskIsTrue();

    @Query("SELECT DISTINCT i.region FROM IndicatorsQueueItem i")
    List<String> findDistinctRegions();

    @Query("SELECT DISTINCT i.region FROM IndicatorsQueueItem i WHERE i.tenderScore < ?1")
    List<String> findDistinctRegionsByTenderScoreLessThan(Double maxImpact);

    @Query("SELECT DISTINCT i.region FROM IndicatorsQueueItem i WHERE i.tenderScore >= ?1")
    List<String> findDistinctRegionsByTenderScoreGreaterThanEqual(Double minImpact);

    @Query("SELECT DISTINCT i.region FROM IndicatorsQueueItem i WHERE i.tenderScore >= ?1 AND i.tenderScore < ?2")
    List<String> findDistinctRegionsByTenderScoreGreaterThanEqualAndTenderScoreLessThan(Double minImpact, Double maxImpact);


    @Query(value = "select tender_outer_id, top_risk from region_indicators_queue_item", nativeQuery = true)
    List<Object[]> getAllTendersIdsAndTops();
}
