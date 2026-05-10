package com.mengsea.khmercodepath.api.users.service;

import com.mengsea.khmercodepath.api.users.payload.CreateUserRequest;
import com.mengsea.khmercodepath.api.users.payload.UpdateUserRequest;
import com.mengsea.khmercodepath.api.users.payload.UserDetailPayload;
import com.mengsea.khmercodepath.api.users.payload.UserImportResultPayload;
import com.mengsea.khmercodepath.api.users.payload.UserPagePayload;
import com.mengsea.khmercodepath.api.users.payload.UserStatusRequest;
import com.mengsea.khmercodepath.commons.constant.Role;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface UserManagementService {

    UserPagePayload listUsers(String name, String email, Role role, Boolean isActive, boolean includeDeleted, Pageable pageable);

    UserDetailPayload getUser(String id);

    UserDetailPayload createUser(CreateUserRequest request);

    UserDetailPayload updateUser(String id, UpdateUserRequest request);

    void deleteUser(String id);

    UserDetailPayload updateStatus(String id, UserStatusRequest request);

    UserImportResultPayload importUsers(MultipartFile file);
}
