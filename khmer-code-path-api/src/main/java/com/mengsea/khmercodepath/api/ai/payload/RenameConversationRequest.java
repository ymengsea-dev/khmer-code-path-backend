package com.mengsea.khmercodepath.api.ai.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RenameConversationRequest {

    @NotBlank
    @Size(max = 500)
    private String title;
}
