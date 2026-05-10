package com.mengsea.khmercodepath.api.users.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserImportResultPayload {
    private int created;
    private int failed;
    private List<UserImportErrorPayload> errors;
}
