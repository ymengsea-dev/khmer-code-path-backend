package com.mengsea.khmercodepath.api.ai.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendChatMessageRequest {

    @NotBlank
    @Size(max = 16_000)
    private String content;
}
