package com.mengsea.khmercodepath.api.profile.service;

import com.mengsea.khmercodepath.api.profile.payload.MyLearningPayload;
import com.mengsea.khmercodepath.api.profile.payload.ProfileSummaryPayload;

public interface ProfileSummaryService {

    MyLearningPayload getMyLearning();

    ProfileSummaryPayload getProfileSummary();
}
