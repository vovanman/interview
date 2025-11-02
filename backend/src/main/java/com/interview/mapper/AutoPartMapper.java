package com.interview.mapper;

import com.interview.dto.AutoPartResponse;
import com.interview.model.AutoPartEntity;

public class AutoPartMapper {

    public static AutoPartResponse entityToResponse(AutoPartEntity e) {
        if (e == null) return null;
        return AutoPartResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .manufacturer(e.getManufacturer())
                .price(e.getPrice())
                .category(e.getCategory() == null ? null : e.getCategory().name())
                .stockQuantity(e.getStockQuantity())
                .createDate(e.getCreateDate())
                .updateDate(e.getUpdateDate())
                .status(e.getStatus() == null ? null : e.getStatus().name())
                .build();
    }

}
