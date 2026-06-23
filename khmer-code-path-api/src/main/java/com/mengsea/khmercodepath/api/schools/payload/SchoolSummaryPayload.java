package com.mengsea.khmercodepath.api.schools.payload;

import com.mengsea.khmercodepath.commons.constant.SchoolStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolSummaryPayload {
    private Long id;
    private String name;
    private String slug;
    private SchoolStatus status;
    private boolean registrationOpen;
}
