package com.mengsea.khmercodepath.api.profile.payload;

import com.mengsea.khmercodepath.api.classes.payload.ClassSummaryPayload;
import com.mengsea.khmercodepath.api.progress.payload.ClassProgressPayload;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LearningClassPayload {
    ClassSummaryPayload summary;
    ClassProgressPayload progress;
    long pendingQuizzes;
}
