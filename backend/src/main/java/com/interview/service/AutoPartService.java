package com.interview.service;


import com.interview.constants.CategoryEnum;
import com.interview.dto.AutoPartQueryRequest;
import com.interview.dto.AutoPartRequest;
import com.interview.dto.AutoPartResponse;
import com.interview.dto.AutoPartTotalValuePerCategoryResponse;
import com.interview.exception.ResourceNotFoundException;
import com.interview.mapper.AutoPartMapper;
import com.interview.model.AutoPartEntity;
import com.interview.repository.AutoPartRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class AutoPartService {

    private final AutoPartRepository autoPartRepository;

    public AutoPartService(AutoPartRepository repository) {
        this.autoPartRepository = repository;
    }

    public AutoPartResponse create(AutoPartRequest dto) {
        CategoryEnum category = dto.getCategory() == null ? null : CategoryEnum.valueOf(dto.getCategory());
        AutoPartEntity part = AutoPartEntity.builder()
                .name(dto.getName())
                .manufacturer(dto.getManufacturer())
                .price(dto.getPrice())
                .category(category)
                .stockQuantity(dto.getStockQuantity())
                .build();

        //set update and create dates to null so they are auto-generated
        part.setCreateDate(null);
        part.setUpdateDate(null);

        AutoPartEntity saved = autoPartRepository.save(part);
        return AutoPartMapper.entityToResponse(saved);
    }

    public AutoPartResponse getById(Long id) {
        AutoPartEntity e = autoPartRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AutoPart not found with id " + id));
        return AutoPartMapper.entityToResponse(e);
    }

    public List<AutoPartResponse> getByName(String name) {
        List<AutoPartEntity> list = autoPartRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("AutoPart not found with name " + name));
        return list.stream().map(AutoPartMapper::entityToResponse).collect(Collectors.toList());
    }

    public List<AutoPartResponse> getByCategory(String category) {
        CategoryEnum cat = CategoryEnum.valueOf(category);

        List<AutoPartEntity> list = autoPartRepository.findByCategory(cat)
                .orElseThrow(() -> new ResourceNotFoundException("AutoPart not found with category " + category));
        return list.stream().map(AutoPartMapper::entityToResponse).collect(Collectors.toList());
    }

    public List<AutoPartResponse> getAll() {
        return autoPartRepository.findAll().stream().map(AutoPartMapper::entityToResponse).collect(Collectors.toList());
    }

    public Page<AutoPartResponse> getAllPaged(int page, int size) {
        Page<AutoPartEntity> p = autoPartRepository.findAll(PageRequest.of(page, size));
        List<AutoPartResponse> content = p.getContent().stream().map(AutoPartMapper::entityToResponse).collect(Collectors.toList());
        return new PageImpl<>(content, PageRequest.of(p.getNumber(), p.getSize()), p.getTotalElements());
    }

    /**
     *  Aggregate total value per category = sum(price * stockQuantity)
     */
    public List<AutoPartTotalValuePerCategoryResponse> getTotalValuePerCategory() {
        List<AutoPartEntity> all = autoPartRepository.findAll();
        Map<String, Double> totals = all.stream()
                .filter(e -> e.getPrice() != null && e.getStockQuantity() != null)
                .collect(Collectors.groupingBy(
                        e -> e.getCategory() == null ? "UNSPECIFIED" : e.getCategory().name(),
                        Collectors.summingDouble(e -> e.getPrice() * e.getStockQuantity())
                ));

        return totals.entrySet().stream()
                .map(entry -> AutoPartTotalValuePerCategoryResponse.builder()
                        .value(entry.getValue())
                        .category(entry.getKey())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Find by example using Spring Data Example matcher
     */
    public List<AutoPartResponse> findByExample(AutoPartRequest dto) {
        AutoPartEntity probe = AutoPartEntity.builder()
                .name(dto.getName())
                .manufacturer(dto.getManufacturer())
                .price(dto.getPrice())
                .category(dto.getCategory() == null ? null : CategoryEnum.valueOf(dto.getCategory()))
                .stockQuantity(dto.getStockQuantity())
                .build();

        ExampleMatcher matcher = ExampleMatcher.matchingAll()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase();

        Example<AutoPartEntity> example = Example.of(probe, matcher);
        List<AutoPartEntity> list = autoPartRepository.findAll(example);
        return list.stream().map(AutoPartMapper::entityToResponse).collect(Collectors.toList());
    }

    public Map<String, Object> query(AutoPartQueryRequest queryRequest) {

        Pageable pageable = PageRequest.of(
                queryRequest.getPagination() != null ? queryRequest.getPagination().getPage() : 0,
                queryRequest.getPagination() != null ? queryRequest.getPagination().getSize() : 10,
                Sort.by(
                        Sort.Direction.fromString(
                                queryRequest.getPagination() != null ? queryRequest.getPagination().getDirection() : "asc"
                        ),
                        queryRequest.getPagination() != null ? queryRequest.getPagination().getSortBy() : "id"
                )
        );

        AutoPartQueryRequest.Filter f = queryRequest.getFilter();
        String category = (f != null && f.getCategory() != null) ? f.getCategory() : "";
        String manufacturer = (f != null && f.getManufacturer() != null) ? f.getManufacturer() : "";

        CategoryEnum categoryEnum = CategoryEnum.valueOf(category);
        Page<AutoPartEntity> page = autoPartRepository
                .findByCategoryAndManufacturerContainingIgnoreCase(categoryEnum, manufacturer, pageable);

        List<AutoPartEntity> filtered = page.getContent().stream()
                .filter(p -> f == null || (
                        (f.getMinPrice() == null || p.getPrice() >= f.getMinPrice()) &&
                                (f.getMaxPrice() == null || p.getPrice() <= f.getMaxPrice()) &&
                                (f.getMinStock() == null || p.getStockQuantity() >= f.getMinStock()) &&
                                (f.getMaxStock() == null || p.getStockQuantity() <= f.getMaxStock())
                ))
                .collect(Collectors.toList());

        Map<String, Object> aggregationResult = new HashMap<>();
        if (queryRequest.getAggregation() != null) {
            AutoPartQueryRequest.Aggregation agg = queryRequest.getAggregation();
            for (String field : agg.getFields()) {
                Double value = computeAggregation(filtered, field, agg.getFunction());
                aggregationResult.put(field + "_" + agg.getFunction(), value);
            }
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("page", page.getNumber());
        response.put("size", page.getSize());
        response.put("totalElements", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());
        response.put("parts", filtered);
        if (!aggregationResult.isEmpty()) {
            response.put("aggregations", aggregationResult);
        }

        return response;
    }

    private Double computeAggregation(List<AutoPartEntity> parts, String field, String function) {
        List<Double> values = parts.stream()
                .map(p -> {
                    switch (field) {
                        case "price": return p.getPrice();
                        case "stockQuantity": return p.getStockQuantity().doubleValue();
                        default: return 0.0;
                    }
                })
                .collect(Collectors.toList());

        switch (function.toLowerCase()) {
            case "sum": return values.stream().reduce(0.0, Double::sum);
            case "avg": return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            case "min": return values.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
            case "max": return values.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
            case "count": return (double) values.size();
            default: return null;
        }
    }

    // Total count of parts
    public long count() {
        return autoPartRepository.count();
    }

    // Count by example
    public long countByExample(AutoPartRequest dto) {
        AutoPartEntity probe = AutoPartEntity.builder()
                .name(dto.getName())
                .manufacturer(dto.getManufacturer())
                .price(dto.getPrice())
                .category(dto.getCategory() == null ? null : CategoryEnum.valueOf(dto.getCategory()))
                .stockQuantity(dto.getStockQuantity())
                .build();

        ExampleMatcher matcher = ExampleMatcher.matchingAll()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase();

        Example<AutoPartEntity> example = Example.of(probe, matcher);
        return autoPartRepository.count(example);
    }

    public AutoPartResponse update(Long id, AutoPartRequest dto) {
        AutoPartEntity part = autoPartRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AutoPart not found with id " + id));
        part.setName(dto.getName());
        part.setManufacturer(dto.getManufacturer());
        part.setPrice(dto.getPrice());
        part.setCategory(dto.getCategory() == null ? null : CategoryEnum.valueOf(dto.getCategory()));
        part.setStockQuantity(dto.getStockQuantity());
        //set update and create dates to null so they are auto-generated
        part.setCreateDate(null);
        part.setUpdateDate(null);
        AutoPartEntity saved = autoPartRepository.save(part);
        return AutoPartMapper.entityToResponse(saved);
    }

    /**
     * Hard delete an auto part by id
     * @param id
     */
    public void delete(Long id) {
        if (!autoPartRepository.existsById(id)) {
            throw new ResourceNotFoundException("AutoPart not found with id " + id);
        }
        autoPartRepository.deleteById(id);
    }
}

