package org.example.testtask.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatchException;
import org.example.testtask.dto.PatchedUserDTO;
import org.example.testtask.entity.UserEntity;
import org.example.testtask.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private List<UserEntity> listOfUsers;

    @InjectMocks
    private UserServiceImpl userService;

    private UserEntity user;

    @BeforeEach
    public void init() {
        user = UserEntity.builder()
                .email("test@example.com")
                .firstName("TestFirstName")
                .lastName("TestLastName")
                .birthDate(LocalDate.now().minusYears(25))
                .build();
    }

    @Test
    void createUser_shouldAddUserToList() {
        UserEntity result = userService.createUser(user);

        assertEquals(user, result);
    }

    @Test
    void updateUserByEmail_existingUser_shouldUpdateAndReturnUpdatedUser() {
        UserEntity updatedUser = UserEntity.builder().build();
        when(listOfUsers.stream()).thenReturn(Stream.of(user));

        UserEntity result = userService.updateUserByEmail(user.getEmail(), updatedUser);

        assertEquals(updatedUser, result);
    }

    @Test
    void updateUserByEmail_nonExistingUser_shouldThrowException() {
        when(listOfUsers.stream()).thenReturn(Stream.empty());

        ResponseStatusException exception = assertThrowsExactly(ResponseStatusException.class,
                () -> userService.updateUserByEmail(user.getEmail(), user));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void findByEmail_existingUser_returnOptionalContainingUser() {
        when(listOfUsers.stream()).thenReturn(Stream.of(user));

        Optional<UserEntity> result = userService.findByEmail(user.getEmail());

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    void findByEmail_nonExistingUser_shouldReturnEmptyOptional() {
        when(listOfUsers.stream()).thenReturn(Stream.empty());

        Optional<UserEntity> result = userService.findByEmail(user.getEmail());

        assertTrue(result.isEmpty());
    }

    @Test
    void deleteUserByEmail_existingUser_shouldRemoveUserFromList() {
        when(listOfUsers.stream()).thenReturn(Stream.of(user));
        when(listOfUsers.remove(user)).thenReturn(true);

        boolean result = userService.deleteUserByEmail(user.getEmail());

        assertTrue(result);
    }

    @Test
    void deleteUserByEmail_nonExistingUser_shouldThrowException() {
        when(listOfUsers.stream()).thenReturn(Stream.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.deleteUserByEmail(user.getEmail()));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void findUsersForBirthDateRange_usersExistInRange_shouldReturnListOfUsers() {
        LocalDate from = LocalDate.now().minusYears(30);
        LocalDate to = LocalDate.now().minusYears(20);
        when(listOfUsers.stream()).thenReturn(Stream.of(user));

        List<UserEntity> result = userService.findUsersForBirthDateRange(from, to);

        assertEquals(1, result.size());
        assertEquals(user, result.get(0));
    }

    @Test
    void patchUser_existingUser_shouldPatchUser() throws JsonProcessingException, JsonPatchException {
        String newEmail = "test@example.com";
        LocalDate newBirthDate = user.getBirthDate().minusYears(2);
        PatchedUserDTO patchedUserDTO = PatchedUserDTO.builder()
                .email(newEmail)
                .birthDate(newBirthDate)
                .build();
        String patchDetails = patchedUserDTO.toJson();


        when(listOfUsers.stream()).thenReturn(Stream.of(user));

        UserEntity result = userService.patchUser(user.getEmail(), patchDetails);
        assertEquals(result.getEmail(), newEmail);
        assertEquals(result.getBirthDate(), newBirthDate);
        assertEquals(result.getFirstName(), user.getFirstName());
        assertEquals(result.getLastName(), user.getLastName());
    }
}