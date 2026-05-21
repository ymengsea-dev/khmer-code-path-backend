package com.mengsea.khmercodepath.api.ai.payload;

import com.mengsea.khmercodepath.commons.constant.AiSectionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateConversationRequest {

    @NotNull
    private AiSectionType sectionType = AiSectionType.GENERAL;

    @Size(max = 128)
    private String sectionRef;

    @Size(max = 500)
    private String title;
}
