package org.example.testtask.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.testtask.dto.PatchedUserDTO;
import org.example.testtask.entity.UserEntity;
import org.example.testtask.service.UserService;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = UserController.class)
@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

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
    void createUser_returnCreated() throws Exception {
        when(userService.createUser(user)).thenReturn(user);

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)));

        response.andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    void createUser_returnBadRequest_whenDateOfBirthIsNotValid() throws Exception {
        user.setBirthDate(LocalDate.now().minusYears(3));
        when(userService.createUser(user)).thenReturn(user);

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)));

        response.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void createUser_returnBadRequest_whenEmailIsNotValid() throws Exception {
        user.setEmail("wrong_email");
        when(userService.createUser(user)).thenReturn(user);

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)));

        response.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void partiallyUpdateUser_returnOk() throws Exception {
        PatchedUserDTO patchedUserDTO = PatchedUserDTO.builder()
                .email("newTest@email.com")
                .build();

        when(userService.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders
                .patch("/users/{email}", user.getEmail())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchedUserDTO)));

        response.andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void partiallyUpdateUser_returnBadRequest_whenBirthDateIsNotValid() throws Exception {
        PatchedUserDTO patchedUserDTO = PatchedUserDTO.builder()
                .email("newTest@email.com")
                .birthDate(LocalDate.now().plusDays(1))
                .build();

        when(userService.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders
                .patch("/users/{email}", user.getEmail())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchedUserDTO)));

        response.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void deleteUser_existingUser_returnOk() throws Exception {
        String email = user.getEmail();
        when(userService.findByEmail(email)).thenReturn(Optional.of(UserEntity.builder().build()));

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.delete("/users/{email}", email));

        response.andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void deleteUser_nonExistingUser_returnNotFound() throws Exception {
        String email = "nonexistent@example.com";
        when(userService.findByEmail(email)).thenReturn(Optional.empty());

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.delete("/users/{email}", email));

        response.andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void searchUsersByBirthDateRange_validRange_returnUsers() throws Exception {
        LocalDate from = LocalDate.now().minusYears(5);
        LocalDate to = LocalDate.now().minusYears(1);

        List<UserEntity> users = List.of(user);
        when(userService.findUsersForBirthDateRange(from, to)).thenReturn(users);

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.get("/users")
                .param("from", from.toString())
                .param("to", to.toString()));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].email", CoreMatchers.is(user.getEmail())))
                .andExpect(jsonPath("$[0].firstName", CoreMatchers.is(user.getFirstName())))
                .andExpect(jsonPath("$[0].lastName", CoreMatchers.is(user.getLastName())))
                .andExpect(jsonPath("$[0].birthDate", CoreMatchers.is(user.getBirthDate().toString())));
    }

    @Test
    void searchUsersByBirthDateRange_invalidRange_returnBadRequest() throws Exception {
        LocalDate from = LocalDate.now().minusYears(1);
        LocalDate to = LocalDate.now().minusYears(5);

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.get("/users")
                .param("from", from.toString())
                .param("to", to.toString()));

        response.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void fullUpdateUser_existingUser_returnOk() throws Exception {
        when(userService.findByEmail(user.getEmail())).thenReturn(Optional.of(UserEntity.builder().build()));

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.put("/users/{email}", user.getEmail())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)));

        response.andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void fullUpdateUser_nonExistingUser_returnNotFound() throws Exception {
        when(userService.findByEmail(user.getEmail())).thenReturn(Optional.empty());

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.put("/users/{email}", user.getEmail())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)));

        response.andExpect(MockMvcResultMatchers.status().isNotFound());
    }
}