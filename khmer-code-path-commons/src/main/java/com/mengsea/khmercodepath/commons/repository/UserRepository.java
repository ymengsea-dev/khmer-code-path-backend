package com.mengsea.khmercodepath.commons.repository;

import com.mengsea.khmercodepath.commons.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndDeletedFalse(String email);

    Optional<User> findByUuidAndDeletedFalse(String uuid);

    boolean existsByStudentIdAndDeletedFalse(String studentId);

    boolean existsByTeacherIdAndDeletedFalse(String teacherId);

    boolean existsByStudentIdAndDeletedFalseAndUuidNot(String studentId, String uuid);

    boolean existsByTeacherIdAndDeletedFalseAndUuidNot(String teacherId, String uuid);

    List<User> findAllByUuidInAndDeletedFalse(Collection<String> uuids);
}
