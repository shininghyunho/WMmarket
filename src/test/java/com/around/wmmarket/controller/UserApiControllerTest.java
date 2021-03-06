package com.around.wmmarket.controller;

import com.around.wmmarket.controller.dto.UserSaveRequestDto;
import com.around.wmmarket.controller.dto.UserSigninRequestDto;
import com.around.wmmarket.domain.user.Role;
import com.around.wmmarket.domain.user.SignedUser;
import com.around.wmmarket.domain.user.User;
import com.around.wmmarket.domain.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserApiControllerTest {
    @LocalServerPort int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockHttpSession session;

    private MockMvc mvc;

    @Before
    public void setup(){
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        session = new MockHttpSession();
    }

    @After
    public void tearDown(){
        userRepository.deleteAll();
    }

    /////////////////////////////////////////////////////////////////////////// TEST
    @Test
    public void UserSave() throws Exception{
        // given
        String testEmail="test_email";
        String testPassword="test_password";
        String testNickname="test_nickname";
        Role testRole=Role.USER;
        UserSaveRequestDto requestDto = UserSaveRequestDto.builder()
                .email(testEmail)
                .password(testPassword)
                .nickname(testNickname)
                .role(testRole)
                .build();
        String url = "http://localhost:"+port+"/api/v1/user";
        // when
        mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(new ObjectMapper().writeValueAsString(requestDto)))
            .andExpect(status().isOk());
        log.info("request : "+new ObjectMapper().writeValueAsString(requestDto));
        // then
        List<User> allUser = userRepository.findAll();
        assertThat(allUser.get(0).getEmail()).isEqualTo(testEmail);
        log.info("user password = "+allUser.get(0).getPassword());
        assertThat(allUser.get(0).getNickname()).isEqualTo(testNickname);
        assertThat(allUser.get(0).getRole()).isEqualTo(testRole);
    }

    @Test
    public void UserSignIn() throws Exception{
        // given
        String testEmail="test_email2";
        String testPassword="test_password2";
        String testNickname="test_nickname2";
        Role testRole=Role.USER;
        userRepository.save(User.builder()
                .email(testEmail)
                .password(passwordEncoder.encode(testPassword))
                .nickname(testNickname)
                .role(testRole)
                .build());

        UserSigninRequestDto requestDto = UserSigninRequestDto.builder()
                .email(testEmail)
                .password(testPassword)
                .build();
        String url = "http://localhost:"+port+"/api/v1/user/signIn";
        // when
        mvc.perform(post(url)
                .session(session)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(new ObjectMapper().writeValueAsString(requestDto))).andExpect(status().isOk());
        // then
        Object object = session.getAttribute("SPRING_SECURITY_CONTEXT");
        SecurityContextImpl context = (SecurityContextImpl) object;
        SignedUser signedUser = (SignedUser) context.getAuthentication().getPrincipal();
        assertThat(testEmail).isEqualTo(signedUser.getUsername());
        assertThat(passwordEncoder.matches(testPassword,signedUser.getPassword())).isTrue();
    }

    @Test
    public void UserExist() throws Exception{
        // given
        String testEmail="test_email3";
        String testPassword="test_password3";
        String testNickname="test_nickname3";
        Role testRole=Role.USER;
        String anotherEmail="another_email";
        userRepository.save(User.builder()
                .email(testEmail)
                .password(passwordEncoder.encode(testPassword))
                .nickname(testNickname)
                .role(testRole)
                .build());
        String url = "http://localhost:"+port+"/api/v1/user/isExist";
        // when
        /// ???????????? User ??????
        /*MvcResult ret1 = mvc.perform(get(url)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(new ObjectMapper().writeValueAsString(testEmail)))
                .andExpect(status().isOk())
                .andReturn();*/
        MvcResult ret1 = mvc.perform(get(url)
                .param("email",testEmail))
                .andExpect(status().isOk())
                .andReturn();
        // ???????????? ?????? User ??????
        MvcResult ret2 = mvc.perform(get(url)
                .param("email",anotherEmail))
                .andExpect(status().isOk())
                .andReturn();

        // then
        assertThat(ret1.getResponse().getContentAsString()).isEqualTo("true");
        assertThat(ret2.getResponse().getContentAsString()).isEqualTo("false");
    }
}
