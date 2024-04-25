package org.example.testtask.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatchException;
import org.example.testtask.entity.UserEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserService {
    UserEntity createUser(UserEntity userEntity);

    UserEntity updateUserByEmail(String email, UserEntity newUserData);

    Optional<UserEntity> findByEmail(String email);

    boolean deleteUserByEmail(String email);

    List<UserEntity> findUsersForBirthDateRange(LocalDate from, LocalDate to);

    UserEntity patchUser(String email, String patchDetails) throws JsonPatchException, JsonProcessingException;
}
