package cn.hrbzhongjiebang;

import cn.hrbzhongjiebang.user.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @Transactional
class WebSecurityRegressionTest {
    @Autowired MockMvc mvc;
    @Autowired AuthService auth;
    private static final String API="/zhongjiebang-demo/api/v1";
    @Test void anonymousWritesAndPasswordChangesRequireAuthentication() throws Exception {
        mvc.perform(post(API+"/houses").contentType("application/json").content("{}")).andExpect(status().isUnauthorized());
        mvc.perform(post(API+"/auth/password").contentType("application/json").content("{\"password\":\"StrongPass123\"}")).andExpect(status().isUnauthorized());
        mvc.perform(get(API+"/houses/1").header("X-User-Id","1")).andExpect(status().isUnauthorized());
    }
    @Test void publicSearchAndRealLoginWork() throws Exception {
        auth.register("13812340001","StrongPass123");
        mvc.perform(get(API+"/houses").param("keyword","' OR 1=1 --").param("size","999")).andExpect(status().isOk()).andExpect(jsonPath("size").value(50));
        mvc.perform(post(API+"/auth/login/password").contentType("application/json")
                .content("{\"phone\":\"13812340001\",\"password\":\"StrongPass123\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("token").isNotEmpty());
    }
    @Test void blankAndOversizeUtf8PasswordsAreRejected() throws Exception {
        mvc.perform(post(API+"/auth/register").contentType("application/json").content("{\"phone\":\"13812340002\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post(API+"/auth/register").contentType("application/json")
                .content("{\"phone\":\"13812340003\",\"password\":\""+"密".repeat(25)+"\"}"))
                .andExpect(status().isBadRequest());
    }
    @Test void changingPasswordRevokesTheSession() throws Exception {
        String token=auth.register("13812340004","StrongPass123").token();
        mvc.perform(post(API+"/auth/password").header("Authorization","Bearer "+token).contentType("application/json")
                .content("{\"password\":\"ChangedPass123\"}")).andExpect(status().isOk());
        mvc.perform(get(API+"/houses/1").header("Authorization","Bearer "+token)).andExpect(status().isUnauthorized());
    }
}
