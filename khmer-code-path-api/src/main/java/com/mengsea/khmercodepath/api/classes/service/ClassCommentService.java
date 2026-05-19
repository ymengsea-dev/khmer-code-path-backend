package com.mengsea.khmercodepath.api.classes.service;

import com.mengsea.khmercodepath.api.classes.payload.ClassCommentPayload;
import com.mengsea.khmercodepath.api.classes.payload.CreateClassCommentRequest;
import com.mengsea.khmercodepath.commons.domain.ClassComment;

import java.util.List;

public interface ClassCommentService {

    List<ClassCommentPayload> listComments(Long classId);

    ClassCommentPayload createComment(Long classId, CreateClassCommentRequest request);

    ClassCommentPayload toPayload(ClassComment comment);
}
