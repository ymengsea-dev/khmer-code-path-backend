package com.mengsea.khmercodepath.api.classes.payload;

import com.mengsea.khmercodepath.commons.constant.ClassVisibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassVisibilityOptionPayload {
    private ClassVisibility value;
    private String label;
    private String description;
}
