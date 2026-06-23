package com.mengsea.khmercodepath.api.schools.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolConfigTabPayload {
    private String id;
    private String label;
}
