package com.mengsea.khmercodepath.api.classes.service;

import com.mengsea.khmercodepath.api.classes.payload.ClassInvitationPayload;

import java.util.List;
import java.util.Set;

public interface ClassInvitationService {

    void inviteStudents(Long classId, Set<String> studentIds);

    List<ClassInvitationPayload> listMyPendingInvitations();

    List<ClassInvitationPayload> listPendingInvitationsForClass(Long classId);

    ClassInvitationPayload acceptInvitation(Long invitationId);

    ClassInvitationPayload declineInvitation(Long invitationId);

    void cancelPendingInvitations(Long classId, List<String> studentIds);
}
