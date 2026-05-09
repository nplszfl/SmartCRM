package com.smartcrm.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationAnalyzeRequest {

    @NotBlank(message = "Conversation ID is required")
    private String conversationId;

    @NotBlank(message = "Speaker name is required")
    private String speakerName;

    @NotBlank(message = "Speaker role is required")
    private String speakerRole;

    @NotBlank(message = "Transcript is required")
    @Size(min = 50, message = "Transcript must be at least 50 characters")
    private String transcript;

    @Builder.Default
    private String conversationType = "sales_call";
}