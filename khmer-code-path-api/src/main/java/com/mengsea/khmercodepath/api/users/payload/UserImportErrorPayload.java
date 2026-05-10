package com.mengsea.khmercodepath.api.users.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserImportErrorPayload {
    private int row;
    private String message;
}
