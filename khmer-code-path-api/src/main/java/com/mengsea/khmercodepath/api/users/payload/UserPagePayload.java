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
public class UserPagePayload {
    private List<UserDetailPayload> items;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;
}
