package com.mengsea.khmercodepath.api.classes.service;

import com.mengsea.khmercodepath.api.classes.payload.AssignStudentsRequest;
import com.mengsea.khmercodepath.api.classes.payload.ClassDetailPayload;
import com.mengsea.khmercodepath.api.classes.payload.ClassPagePayload;
import com.mengsea.khmercodepath.api.classes.payload.CreateClassRequest;
import com.mengsea.khmercodepath.api.classes.payload.RemoveStudentsRequest;
import com.mengsea.khmercodepath.api.classes.payload.UpdateClassRequest;
import com.mengsea.khmercodepath.api.users.payload.UserDetailPayload;
import com.mengsea.khmercodepath.commons.constant.ClassStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ClassManagementService {

    ClassPagePayload listClasses(
            String search,
            String teacherId,
            String semester,
            Integer academicYear,
            ClassStatus status,
            Pageable pageable
    );

    ClassDetailPayload getClass(Long id);

    ClassDetailPayload createClass(CreateClassRequest request);

    ClassDetailPayload updateClass(Long id, UpdateClassRequest request);

    void deleteClass(Long id);

    void assignStudents(Long id, AssignStudentsRequest request);

    void removeStudents(Long id, RemoveStudentsRequest request);

    List<UserDetailPayload> listClassStudents(Long id);
}
