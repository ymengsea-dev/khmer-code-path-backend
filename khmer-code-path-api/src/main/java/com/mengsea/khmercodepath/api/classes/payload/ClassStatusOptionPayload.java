package com.mengsea.khmercodepath.api.classes.payload;

import com.mengsea.khmercodepath.commons.constant.ClassStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClassStatusOptionPayload {
    private ClassStatus value;
    private String label;
}
