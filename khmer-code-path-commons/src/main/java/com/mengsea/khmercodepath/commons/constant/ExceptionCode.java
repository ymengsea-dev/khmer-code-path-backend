package com.mengsea.khmercodepath.commons.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ExceptionCode {
    OAUTH2_LOGIN_FAILED(LmsStatusCode.UNAUTHORIZED, "Unable to login with google"),
    USER_NOT_FOUND(LmsStatusCode.NOT_FOUND, "User not found"),
    USER_ALREADY_EXISTS(LmsStatusCode.CONFLICT, "User already exists"),
    USER_UNAUTHORIZED(LmsStatusCode.UNAUTHORIZED, "Unauthorized access"),
    INVALID_CREDENTIAL(LmsStatusCode.UNAUTHORIZED, "Invalid username or password"),
    ACCOUNT_INACTIVE(LmsStatusCode.UNAUTHORIZED, "Account is inactive"),
    ACCOUNT_LOOKED(LmsStatusCode.UNAUTHORIZED, "Account is locked"),
    REFRESH_TOKEN_EXPIRED(LmsStatusCode.REFRESH_TOKEN_EXPIRED, "Refresh token expired, login again"),
    REFRESH_TOKEN_NOT_FOUND(LmsStatusCode.REFRESH_TOKEN_EXPIRED, "Refresh token not found."),
    REFRESH_TOKEN_REVOKED(LmsStatusCode.REFRESH_TOKEN_EXPIRED, "Refresh token has been revoked"),
    UNAUTHORIZED(LmsStatusCode.UNAUTHORIZED, "Token is missing or invalid."),
    ACCESS_DENIED(LmsStatusCode.FORBIDDEN, "You do not have permission to perform this action."),
    VALIDATION_ERROR(LmsStatusCode.VALIDATION_FAILED, "Request validation error"),
    FILE_STORAGE_FAILED(LmsStatusCode.INTERNAL_SERVER_ERROR,
            "File storage failed. Ensure MinIO is running and credentials in .env match the MinIO container."),
    FILE_TOO_LARGE(LmsStatusCode.VALIDATION_FAILED,
            "File exceeds the maximum upload size (50MB per file)."),
    INTERNAL_SERVER_ERROR(LmsStatusCode.INTERNAL_SERVER_ERROR, "Internal server error"),
    TOKEN_EXPIRED(LmsStatusCode.TOKEN_EXPIRED, "Access token has expired"),
    PASSWORD_RESET_TOKEN_INVALID(LmsStatusCode.BAD_REQUEST, "Password reset token is invalid"),
    PASSWORD_RESET_TOKEN_EXPIRED(LmsStatusCode.BAD_REQUEST, "Password reset token has expired"),
    DUPLICATE_STUDENT_ID(LmsStatusCode.CONFLICT, "Student ID is already in use"),
    DUPLICATE_TEACHER_ID(LmsStatusCode.CONFLICT, "Teacher ID is already in use"),
    IMMUTABLE_USER_FIELD(LmsStatusCode.OPERATION_NOT_ALLOWED, "This field cannot be changed after creation"),
    USER_ALREADY_DELETED(LmsStatusCode.NOT_FOUND, "User not found or already removed"),
    UNSUPPORTED_IMPORT_FORMAT(LmsStatusCode.BAD_REQUEST, "Only CSV or XLSX files are supported"),
    IMPORT_FILE_EMPTY(LmsStatusCode.BAD_REQUEST, "Import file is empty or invalid"),
    CLASS_NOT_FOUND(LmsStatusCode.NOT_FOUND, "Class not found"),
    CLASS_CODE_CONFLICT(LmsStatusCode.CONFLICT, "Class code is already in use"),
    CLASS_DELETE_NOT_ALLOWED(LmsStatusCode.OPERATION_NOT_ALLOWED,
            "Class can be deleted only when it has no enrolled students and no lessons"),
    TEACHER_NOT_FOUND(LmsStatusCode.BAD_REQUEST, "Teacher user not found or is not a teacher"),
    STUDENT_NOT_FOUND(LmsStatusCode.BAD_REQUEST, "One or more student users were not found or are not students"),
    COURSE_NOT_FOUND(LmsStatusCode.NOT_FOUND, "Course not found"),
    DEPARTMENT_NOT_FOUND(LmsStatusCode.NOT_FOUND, "Department not found"),
    FACULTY_NOT_FOUND(LmsStatusCode.NOT_FOUND, "Faculty not found"),
    FACULTY_NAME_CONFLICT(LmsStatusCode.CONFLICT, "Faculty name is already in use at this school"),
    DEPARTMENT_NAME_CONFLICT(LmsStatusCode.CONFLICT, "Department name is already in use at this school"),
    SCHOOL_NOT_FOUND(LmsStatusCode.NOT_FOUND, "School not found"),
    SCHOOL_NOT_ASSIGNED(LmsStatusCode.BAD_REQUEST, "User is not assigned to a school"),
    SCHOOL_SLUG_CONFLICT(LmsStatusCode.CONFLICT, "School slug is already in use"),
    REGISTRATION_CLOSED(LmsStatusCode.OPERATION_NOT_ALLOWED, "Registration is closed for this school"),
    REGISTRATION_DOMAIN_NOT_ALLOWED(LmsStatusCode.OPERATION_NOT_ALLOWED,
            "This email domain is not allowed to register at this school"),
    REGISTRATION_DOMAIN_CONFLICT(LmsStatusCode.CONFLICT, "Registration domain is already in use"),
    REGISTRATION_DOMAIN_NOT_FOUND(LmsStatusCode.NOT_FOUND, "Registration domain not found"),
    CLASS_NOT_PUBLIC(LmsStatusCode.OPERATION_NOT_ALLOWED, "This class is not open for self-enrollment"),
    PUBLIC_COURSES_DISABLED(LmsStatusCode.OPERATION_NOT_ALLOWED,
            "Public courses are not enabled for your school"),
    PERMISSION_NOT_GRANTABLE(LmsStatusCode.OPERATION_NOT_ALLOWED, "This permission cannot be granted"),
    PERMISSION_TARGET_NOT_TEACHER(LmsStatusCode.BAD_REQUEST, "Permissions can only be customized for teachers"),
    ROLE_SELF_CHANGE(LmsStatusCode.OPERATION_NOT_ALLOWED, "You cannot change your own role"),
    LAST_ADMIN_REQUIRED(LmsStatusCode.OPERATION_NOT_ALLOWED, "At least one school administrator is required"),
    OPERATIONS_REQUEST_NOT_FOUND(LmsStatusCode.NOT_FOUND, "Faculty request not found"),
    ATTENDANCE_NOT_FOUND(LmsStatusCode.NOT_FOUND, "Attendance record not found"),
    GRADE_NOT_FOUND(LmsStatusCode.NOT_FOUND, "Grade not found"),
    INVALID_SESSION_ID(LmsStatusCode.BAD_REQUEST, "Invalid attendance session id"),
    LESSON_NOT_FOUND(LmsStatusCode.NOT_FOUND, "Lesson not found"),
    MATERIAL_NOT_FOUND(LmsStatusCode.NOT_FOUND, "Material not found"),
    LIBRARY_ITEM_NOT_FOUND(LmsStatusCode.NOT_FOUND, "Library item not found"),
    NOTIFICATION_NOT_FOUND(LmsStatusCode.NOT_FOUND, "Notification not found"),
    INVITATION_NOT_FOUND(LmsStatusCode.NOT_FOUND, "Class invitation not found"),
    INVITATION_ALREADY_RESPONDED(LmsStatusCode.OPERATION_NOT_ALLOWED, "Invitation has already been accepted or declined"),
    STUDENT_ALREADY_ENROLLED(LmsStatusCode.CONFLICT, "Student is already enrolled in this class"),
    AI_CONVERSATION_NOT_FOUND(LmsStatusCode.NOT_FOUND, "AI conversation not found"),
    MATERIAL_RAG_NOT_READY(LmsStatusCode.OPERATION_NOT_ALLOWED,
            "Material is not indexed for AI. Upload a supported file and try again."),
    MATERIAL_RAG_INDEX_FAILED(LmsStatusCode.INTERNAL_SERVER_ERROR,
            "Failed to index material for AI retrieval"),
    NOTE_NOT_FOUND(LmsStatusCode.NOT_FOUND, "Note not found"),
    QUIZ_NOT_FOUND(LmsStatusCode.NOT_FOUND, "Quiz not found"),
    QUIZ_ALREADY_SUBMITTED(LmsStatusCode.OPERATION_NOT_ALLOWED, "You have already submitted this quiz"),
    QUIZ_HAS_SUBMISSIONS(LmsStatusCode.OPERATION_NOT_ALLOWED,
            "This quiz already has student attempts. Duplicate it before editing.");

    private final LmsStatusCode statusCode;
    private final String message;

    public HttpStatus getHttpStatus() {
        return statusCode.getHttpStatus();
    }

    public String getCode() {
        return statusCode.getCode();
    }
}
