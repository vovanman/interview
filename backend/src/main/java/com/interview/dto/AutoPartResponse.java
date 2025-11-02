package com.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutoPartResponse {
    private Long id;
    private String name;
    private String manufacturer;
    private Double price;
    private String category;
    private Integer stockQuantity;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
    private String status;
}

