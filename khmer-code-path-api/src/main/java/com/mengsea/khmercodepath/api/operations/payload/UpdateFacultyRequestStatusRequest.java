package com.mengsea.khmercodepath.api.operations.payload;

import com.mengsea.khmercodepath.commons.constant.RequestStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateFacultyRequestStatusRequest {

    @NotNull
    private RequestStatus status;

    @Size(max = 2000)
    private String adminComment;
}
