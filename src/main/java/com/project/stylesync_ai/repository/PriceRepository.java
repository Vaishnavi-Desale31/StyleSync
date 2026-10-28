package com.project.stylesync_ai.repository;

import com.project.stylesync_ai.entity.PriceOfferEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PriceRepository
        extends JpaRepository<PriceOfferEntity, Long> {

    List<PriceOfferEntity> findByProduct_IdOrderByPriceAsc(
            Long productId);
}
