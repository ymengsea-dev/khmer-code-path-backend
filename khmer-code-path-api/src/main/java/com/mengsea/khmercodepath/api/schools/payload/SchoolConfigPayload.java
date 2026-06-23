package com.mengsea.khmercodepath.api.schools.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolConfigPayload {
    private String pageTitle;
    private String pageDescription;
    private List<SchoolConfigTabPayload> tabs;
    private SchoolProfileConfigPayload profile;
}
