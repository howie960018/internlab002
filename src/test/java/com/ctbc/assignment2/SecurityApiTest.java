package com.ctbc.assignment2;

import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.service.CourseBeanService;
import com.ctbc.assignment2.service.CourseCategoryBeanService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @MockBean
    private CourseBeanService courseService;

    @MockBean
    private CourseCategoryBeanService categoryService;

    @Test
    public void loginWithValidCredentialsReturnsJwtToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        org.junit.jupiter.api.Assertions.assertTrue(json.get("accessToken").asText().startsWith("ey"));
    }

    @Test
    public void loginWithInvalidCredentialsReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getApiWithoutJwtReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/course/all"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getApiWithUserTokenReturnsOk() throws Exception {
        when(courseService.findAll()).thenReturn(List.of());
        String token = loginAndGetToken("user", "user123");

        mockMvc.perform(get("/api/course/all")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    public void nonGetApiWithUserTokenReturnsForbidden() throws Exception {
        String token = loginAndGetToken("user", "user123");

        mockMvc.perform(post("/api/course")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseName\":\"JWT課程\",\"price\":100.0}"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void nonGetApiWithAdminTokenReturnsOk() throws Exception {
        CourseBean saved = new CourseBean();
        saved.setId(1L);
        saved.setCourseName("JWT課程");
        saved.setPrice(100.0);
        when(courseService.save(any())).thenReturn(saved);

        String token = loginAndGetToken("admin", "admin123");

        mockMvc.perform(post("/api/course")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseName\":\"JWT課程\",\"price\":100.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseName").value("JWT課程"));
    }

    @Test
    public void expiredJwtReturnsUnauthorized() throws Exception {
        String expiredToken = createExpiredToken("user");

        mockMvc.perform(get("/api/course/all")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void malformedJwtReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/course/all")
                        .header("Authorization", "Bearer not-a-token"))
                .andExpect(status().isUnauthorized());
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        // 先呼叫登入 API，取得真實 JWT
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("accessToken").asText();
    }

    private String createExpiredToken(String username) {
        // 建立過期的 JWT，用來測試 401
        Date now = new Date();
        Date expired = new Date(now.getTime() - 1000L);
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", List.of("ROLE_USER"))
                .setIssuedAt(now)
                .setExpiration(expired)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
