package com.smartcrm.common.dto;

import lombok.Data;
import lombok.Builder;
import java.io.Serializable;

/**
 * Page request DTO for pagination.
 */
@Data
@Builder
public class PageRequest implements Serializable {

    private Integer page = 1;
    private Integer pageSize = 20;
    private String sortBy;
    private String sortOrder = "desc";

    public int getOffset() {
        return (page - 1) * pageSize;
    }
}