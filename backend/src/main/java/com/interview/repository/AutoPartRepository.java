package com.interview.repository;

import com.interview.constants.CategoryEnum;
import com.interview.model.AutoPartEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AutoPartRepository extends JpaRepository<AutoPartEntity, Long> {
    Optional<List<AutoPartEntity>> findByCategory(CategoryEnum category);
    Optional<List<AutoPartEntity>> findByName(String name);
    Page<AutoPartEntity> findByCategoryAndManufacturerContainingIgnoreCase(CategoryEnum category, String manufacturer, Pageable pageable);
}