package com.devu.backend.service;

import com.devu.backend.config.auth.token.RefreshToken;
import com.devu.backend.repository.RefreshTokenRepository;
import com.devu.backend.config.auth.token.TokenService;
import com.devu.backend.controller.user.UserDTO;
import com.devu.backend.entity.User;
import com.devu.backend.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.Cookie;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private EmailService emailService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private CookieService cookieService;

    @Mock
    private RefreshTokenRepository tokenRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Spy
    private BCryptPasswordEncoder passwordEncoder;


    private User createUser() {
        String email = "test@test.com";
        User user = User.builder()
                .email(email)
                .username("test")
                .emailConfirm(false)
                .build();
        Long fakeUserId = 1L;
        ReflectionTestUtils.setField(user, "id", fakeUserId);
        return user;
    }

    @Test
    @DisplayName("Create User - Success")
    void saveUser() throws Exception {
        //given
        User user = createUser();

        //Mocking
        given(userRepository.save(any(User.class))).willReturn(user);
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

        //when
        User save = userService.createUserBeforeEmailValidation(user.getEmail());

        //then
        User findUser = userRepository.findById(user.getId()).get();
        assertEquals(user.getEmail(), findUser.getEmail());
        assertEquals(user.getId(), findUser.getId());
        assertEquals(user.getUsername(), findUser.getUsername());
    }

    @Test
    @DisplayName("update Email Confirm")
    void updateUserConfirm() {
        //given
        User user = createUser();
        ReflectionTestUtils.setField(user, "emailAuthKey", "test");
        userRepository.save(user);

        given(userRepository.findByEmailAuthKey("test")).willReturn(Optional.of(user));
        //when
        userService.updateUserConfirm("test");
        //then
        User findUser = userRepository.findByEmailAuthKey("test").get();
        assertTrue(findUser.isEmailConfirm());
        assertEquals(user.getEmail(), findUser.getEmail());
    }

    @Test
    @DisplayName("Update User")
    void updateUser() throws Exception {
        //given
        User user = createUser();
        ReflectionTestUtils.setField(user, "emailConfirm", true);
        userRepository.save(user);
        UserDTO updateDto = UserDTO.builder()
                .email(user.getEmail())
                .password("hcshcs")
                .username("test")
                .build();
        //Mocking
        given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
        //when
        userService.createUserAfterEmailValidation(updateDto);
        //then
        User findUser = userRepository.findByEmail(user.getEmail()).get();
        assertEquals(updateDto.getUsername(), findUser.getUsername());
        assertNotEquals(updateDto.getUsername(),findUser.getPassword());
    }

    @Test
    @DisplayName("logout")
    void logoutProcess() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        given(cookieService.getCookie(request, "X-AUTH-REFRESH-TOKEN"))
                .willReturn(new Cookie("testName", "testKey"));
        RefreshToken refreshToken = new RefreshToken(5L, "testToken");
        given(refreshTokenRepository.findByRefreshToken("testKey"))
                .willReturn(refreshToken);
        given(cookieService.deleteCookie("X-AUTH-REFRESH-TOKEN"))
                .willReturn(ResponseCookie.from("testName", "testValue").build());

        userService.logoutProcess(request, response);

        verify(refreshTokenRepository).delete(refreshToken);
        verify(cookieService).deleteCookie("X-AUTH-REFRESH-TOKEN");
    }
}