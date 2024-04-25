package org.example.testtask.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatchException;
import jakarta.validation.Valid;
import org.example.testtask.dto.PatchedUserDTO;
import org.example.testtask.entity.UserEntity;
import org.example.testtask.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    @Value("${user.minimum.age}")
    private int minimumAge;
    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<String> createUser(@Valid @RequestBody UserEntity userEntity) {
        LocalDate minimumBirthDate = LocalDate.now().minusYears(minimumAge);
        if (userEntity.getBirthDate().isAfter(minimumBirthDate)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Users must be older than " + minimumAge + " years old to register.");
        }
        userService.createUser(userEntity);
        return ResponseEntity.status(HttpStatus.CREATED).body("User successfully created");
    }

    @PatchMapping("/{email}")
    public ResponseEntity<String> partiallyUpdateUser(@PathVariable String email, @Valid @RequestBody PatchedUserDTO request) {
        if (userService.findByEmail(email).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            userService.patchUser(email, request.toJson());
        } catch (JsonPatchException | JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to apply JSON Patch: " + e.getMessage());
        }

        return ResponseEntity.ok("User updated successfully");
    }

    @PutMapping("/{email}")
    public ResponseEntity<String> fullUpdateUser(@PathVariable String email, @Valid @RequestBody UserEntity updatedUser) {
        if (userService.findByEmail(email).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        userService.updateUserByEmail(email, updatedUser);
        return ResponseEntity.ok("User updated successfully");
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<String> deleteUser(@PathVariable String email) {
        if (userService.findByEmail(email).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        userService.deleteUserByEmail(email);
        return ResponseEntity.ok("User deleted successfully");
    }

    @GetMapping
    public ResponseEntity<List<UserEntity>> searchUsersByBirthDateRange(@RequestParam("from") LocalDate from,
                                                                        @RequestParam("to") LocalDate to) {
        if (from.isAfter(to)) {
            return ResponseEntity.badRequest().body(null);
        }

        List<UserEntity> users = userService.findUsersForBirthDateRange(from, to);
        return ResponseEntity.ok(users);
    }
}
