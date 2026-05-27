package com.itemstorage.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Неаутентифицированный пользователь перенаправляется на логин")
    void testUnauthenticatedUser_RedirectToLogin() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Аутентифицированный ADMIN имеет доступ к /admin/**")
    void testAdminUser_AccessToAdmin() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ANALYST")
    @DisplayName("Аналитик не имеет доступа к /storekeeper/**")
    void testAnalystUser_NoAccessToStorekeeper() throws Exception {
        mockMvc.perform(get("/storekeeper/cells"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "RECEPTIONIST")
    @DisplayName("Приёмщик имеет доступ к /receptionist/**")
    void testReceptionistUser_AccessToReceptionist() throws Exception {
        mockMvc.perform(get("/receptionist/create"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "STOREKEEPER")
    @DisplayName("Кладовщик имеет доступ к /storekeeper/**")
    void testStorekeeperUser_AccessToStorekeeper() throws Exception {
        mockMvc.perform(get("/storekeeper/cells"))
                .andExpect(status().isOk());
    }
}