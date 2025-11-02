package com.interview.model;

import com.interview.constants.CategoryEnum;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


@Entity
@Table(name = "AUTO_PART")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutoPartEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @NotBlank(message = "Part name cannot be empty")
    @Column(name = "NAME", nullable = false)
    private String name;

    @NotBlank(message = "Manufacturer cannot be empty")
    @Column(name = "MANUFACTURER", nullable = false)
    private String manufacturer;

    @NotNull(message = "Price cannot be null")
    @Min(value = 0, message = "Price must be positive")
    @Column(name = "PRICE", nullable = false)
    private Double price;

    @Column(name = "CATEGORY")
    @Enumerated(EnumType.STRING)
    private CategoryEnum category;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock must be positive")
    @Column(name = "STOCK_QUANTITY", nullable = false)
    private Integer stockQuantity;
}