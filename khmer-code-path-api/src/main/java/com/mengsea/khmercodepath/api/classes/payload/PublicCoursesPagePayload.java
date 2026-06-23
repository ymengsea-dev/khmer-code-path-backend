package com.mengsea.khmercodepath.api.classes.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicCoursesPagePayload {
    private List<PublicCourseSummaryPayload> items;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;
}
