package com.mengsea.khmercodepath.api.schools.service;

import com.mengsea.khmercodepath.api.schools.payload.SchoolConfigPayload;
import com.mengsea.khmercodepath.api.schools.payload.SchoolDetailPayload;
import com.mengsea.khmercodepath.api.schools.payload.UpdateSchoolRequest;

public interface SchoolManagementService {

    SchoolDetailPayload getMySchool();

    SchoolDetailPayload updateMySchool(UpdateSchoolRequest request);

    SchoolConfigPayload getSchoolConfig();
}
