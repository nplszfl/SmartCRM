package com.smartcrm.common.dto;

import lombok.Data;
import lombok.Builder;
import java.io.Serializable;
import java.util.List;

/**
 * Page response DTO for pagination.
 */
@Data
@Builder
public class PageResponse<T> implements Serializable {

    private Long total;
    private Integer page;
    private Integer pageSize;
    private Integer totalPages;
    private List<T> data;

    public static <T> PageResponse<T> of(Long total, Integer page, Integer pageSize, List<T> data) {
        return PageResponse.<T>builder()
                .total(total)
                .page(page)
                .pageSize(pageSize)
                .totalPages((int) Math.ceil((double) total / pageSize))
                .data(data)
                .build();
    }
}