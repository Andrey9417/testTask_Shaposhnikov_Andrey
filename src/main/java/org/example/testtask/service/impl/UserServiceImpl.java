package org.example.testtask.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import org.example.testtask.entity.UserEntity;
import org.example.testtask.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private List<UserEntity> listOfUsers = new ArrayList<>();

    @Override
    public UserEntity createUser(UserEntity userEntity) {
        listOfUsers.add(userEntity);
        return userEntity;
    }

    @Override
    public UserEntity updateUserByEmail(String email, UserEntity newUserData) {
        UserEntity user = findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found with email: " + email));
        updateUser(user, newUserData);
        return newUserData;
    }

    @Override
    public Optional<UserEntity> findByEmail(String email) {
        return listOfUsers.stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst();
    }

    private void updateUser(UserEntity oldUser, UserEntity updatedUser) {
        listOfUsers.remove(oldUser);
        listOfUsers.add(updatedUser);
    }

    @Override
    public boolean deleteUserByEmail(String email) {
        UserEntity user = findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found with email: " + email));
        return listOfUsers.remove(user);
    }

    @Override
    public List<UserEntity> findUsersForBirthDateRange(LocalDate from, LocalDate to) {
        return listOfUsers.stream()
                .filter(user -> user.getBirthDate().isAfter(from) && user.getBirthDate().isBefore(to))
                .collect(Collectors.toList());
    }

    @Override
    public UserEntity patchUser(String email, String patchDetails) throws JsonPatchException, JsonProcessingException {
        UserEntity oldUser = findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found with email: " + email));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        JsonNode patchNode = objectMapper.readTree(patchDetails);
        JsonMergePatch patch = JsonMergePatch.fromJson(patchNode);
        JsonNode originalObjNode = objectMapper.valueToTree(oldUser);
        TreeNode patchedObjNode = patch.apply(originalObjNode);
        UserEntity updatedUser = objectMapper.treeToValue(patchedObjNode, UserEntity.class);
        updateUser(oldUser, updatedUser);
        return updatedUser;
    }
}
