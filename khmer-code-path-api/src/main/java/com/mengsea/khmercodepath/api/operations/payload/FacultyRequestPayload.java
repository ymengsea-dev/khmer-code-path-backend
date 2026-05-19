package com.mengsea.khmercodepath.api.operations.payload;

import com.mengsea.khmercodepath.commons.constant.RequestIconType;
import com.mengsea.khmercodepath.commons.constant.RequestStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FacultyRequestPayload {
    private Long id;
    private String title;
    private String requester;
    private String detail;
    private RequestIconType icon;
    private RequestStatus status;
}
