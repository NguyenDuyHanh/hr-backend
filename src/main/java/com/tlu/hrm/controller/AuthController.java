package com.tlu.hrm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tlu.hrm.dto.request.LoginRequest;
import com.tlu.hrm.dto.request.RefreshTokenRequest;
import com.tlu.hrm.dto.request.UserDto;
import com.tlu.hrm.dto.response.ApiResponse;
import com.tlu.hrm.dto.response.LoginResponse;
import com.tlu.hrm.dto.response.TokenRefreshResponse;
import com.tlu.hrm.exception.CustomException;
import com.tlu.hrm.model.User;
import com.tlu.hrm.security.JwtTokenProvider;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserDetailsService userDetailsService;

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        
        if (refreshToken == null || !tokenProvider.validateToken(refreshToken)) {
            throw new CustomException("Refresh token không hợp lệ hoặc đã hết hạn", HttpStatus.UNAUTHORIZED);
        }

        String username = tokenProvider.getUsernameFromJWT(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        String newAccessToken = tokenProvider.generateAccessToken(userDetails);

        TokenRefreshResponse response = TokenRefreshResponse.builder()
                .accessToken(newAccessToken)
                .build();

        return ResponseEntity.ok(ApiResponse.success("Lấy mã token mới thành công", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        System.out.println("=== Login Attempt ===");
        System.out.println("=== Username received: '" + loginRequest.getUsername() + "'");
        System.out.println("=== Password length received: " + (loginRequest.getPassword() != null ? loginRequest.getPassword().length() : 0));

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);
        
        LoginResponse loginResponse = LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", loginResponse));
    }

    @GetMapping("/getCurrentUser")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new CustomException("Chưa đăng nhập hoặc phiên làm việc đã hết hạn", HttpStatus.UNAUTHORIZED);
        }
        
        String username = authentication.getName();
        User user = (User) userDetailsService.loadUserByUsername(username);
        UserDto userDto = new UserDto(user);
        
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin người dùng hiện tại thành công", userDto));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        // Hỗ trợ invalidate session/token ở đây nếu cần thiết. 
        // Hiện tại dùng stateless JWT nên phản hồi thành công trực tiếp để Client xóa token.
        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công", "Logout successful"));
    }
}
