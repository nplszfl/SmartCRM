package com.smartcrm.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailGenerateResponse {
    private String leadId;
    private String subjectA;
    private String subjectB;
    private String variantA;
    private String variantB;
    private String templateUsed;
}