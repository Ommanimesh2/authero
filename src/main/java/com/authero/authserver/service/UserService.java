package com.authero.authserver.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.authero.authserver.dto.LoginDto;
import com.authero.authserver.dto.SignupDto;
import com.authero.authserver.dto.github.GithubTokenResponseDto;
import com.authero.authserver.dto.github.GithubUserEmailResponseDto;
import com.authero.authserver.dto.github.GithubUserResponseDto;
import com.authero.authserver.dto.github.SignUpWithGithubDto;
import com.authero.authserver.dto.google.GoogleUserDto;
import com.authero.authserver.dto.google.SignUpWithGoogleDto;
import com.authero.authserver.enums.Provider;
import com.authero.authserver.enums.RoleEnum;
import com.authero.authserver.models.Role;
import com.authero.authserver.models.User;
import com.authero.authserver.repository.RoleRepository;
import com.authero.authserver.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    private final RestTemplate restTemplate = new RestTemplate();

    // Github OAuth
    @Value("${oauth2.github.clientId}")
    private String GITHUB_CLIENT_ID;

    @Value("${oauth2.github.clientSecret}")
    private String GITHUB_CLIENT_SECRET;

    @Value("${oauth2.github.clientSecret}")
    private String GITHUB_TOKEN_URL;

    @Value("${oauth2.github.clientSecret}")
    private String GITHUB_USER_URL;

    // Google OAuth
    @Value("${oauth2.google.clientId}")
    private String googleClientId;

    @Value("${oauth2.google.clientSecret}")
    private String googleClientSecret;

    @Value("${oauth2.google.tokenUrl}")
    private String googleTokenUrl;

    @Value("${oauth2.google.userInfoUrl}")
    private String googleUserInfoUrl;

    public User signup(SignupDto signupDto) {
        Optional<Role> optionalRole = roleRepository.findByName(RoleEnum.USER);

        if (optionalRole.isEmpty()) {
            return null;
        }

        User user = User.builder()
                .fullName(signupDto.getFullName())
                .email(signupDto.getEmail())
                .password(passwordEncoder.encode(signupDto.getPassword()))
                .role(optionalRole.get())
                .build();

        return userRepository.save(user);
    }

    public User signUpWithGithub(SignUpWithGithubDto signUpWithGithubDto) {
        String accessToken = getGithubAccessToken(signUpWithGithubDto.getCode());
        log.info("Github access token: " + accessToken);
        if (accessToken == null) {
            throw new IllegalStateException("Failed to retrieve GitHub access token");
        }

        GithubUserResponseDto githubUserResponseDto = getGithubUserDetails(accessToken);

        if (githubUserResponseDto == null || githubUserResponseDto.getEmail() == null) {
            throw new IllegalStateException("Failed to retrieve GitHub user details");
        }

        Optional<User> existingUser = userRepository.findByEmail(githubUserResponseDto.getEmail());
        log.info(existingUser.toString());
        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        Optional<Role> optionalRole = roleRepository.findByName(RoleEnum.USER);

        if (optionalRole.isEmpty()) {
            throw new IllegalStateException("User role not found");
        }

        User newUser = User.builder()
                .fullName(githubUserResponseDto.getName())
                .email(githubUserResponseDto.getEmail())
                .provider(Provider.GITHUB)
                .password(passwordEncoder.encode("OAuthLogin"))
                .role(optionalRole.get())
                .build();

        return userRepository.save(newUser);
    }

    public User signUpWithGoogle(SignUpWithGoogleDto signUpWithGoogleDto) {
        String accessToken = getGithubAccessToken(signUpWithGoogleDto.getCode());
        log.info("Google access token: " + accessToken);
        if (accessToken == null) {
            throw new IllegalStateException("Failed to retrieve Google access token");
        }

        GoogleUserDto googleUserDto = getGoogleUserDetails(accessToken);

        if (googleUserDto == null || googleUserDto.getEmail() == null) {
            throw new IllegalStateException("Failed to retrieve Google user details");
        }

        Optional<User> existingUser = userRepository.findByEmail(googleUserDto.getEmail());

        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        Optional<Role> optionalRole = roleRepository.findByName(RoleEnum.USER);

        if (optionalRole.isEmpty()) {
            throw new IllegalStateException("User role not found");
        }

        User newUser = User.builder()
                .fullName(googleUserDto.getName())
                .email(googleUserDto.getEmail())
                .provider(Provider.GOOGLE)
                .password(passwordEncoder.encode("OAuthLogin"))
                .role(optionalRole.get())
                .build();

        return userRepository.save(newUser);
    }

    private String getGithubAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED); // Ensure correct content type

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", GITHUB_CLIENT_ID);
        params.add("client_secret", GITHUB_CLIENT_SECRET);
        params.add("code", code);
        params.add("scope", "user:email");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        log.info("Requesting GitHub token with code: " + code);

        ResponseEntity<GithubTokenResponseDto> response = restTemplate.postForEntity(
                GITHUB_TOKEN_URL,
                request,
                GithubTokenResponseDto.class);

        log.info("GitHub response: " + response);

        if (response.getBody() != null && response.getBody().getAccess_token() != null) {
            log.info("GitHub access token: " + response.getBody().getAccess_token());
            return response.getBody().getAccess_token();
        } else {
            log.error("Failed to retrieve GitHub access token: " + response);
            return null;
        }
    }

    private GithubUserResponseDto getGithubUserDetails(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            // Fetch basic user details from GitHub
            ResponseEntity<GithubUserResponseDto> response = restTemplate.exchange(
                    GITHUB_USER_URL,
                    HttpMethod.GET,
                    entity,
                    GithubUserResponseDto.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                GithubUserResponseDto user = response.getBody();

                // If the user's email is null, fetch the email from the GitHub emails API
                if (user != null && user.getEmail() == null) {
                    String email = fetchGithubUserPrimaryEmail(accessToken);

                    if (email != null) {
                        user.setEmail(email); // Set the primary email
                    }
                }

                return user; // Return the user details with the fetched email
            } else {
                log.error("Failed to fetch GitHub user details. Status Code: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error fetching GitHub user details: " + e.getMessage(), e);
        }

        return null;
    }

    // Additional method to fetch the primary email if the email is missing
    private String fetchGithubUserPrimaryEmail(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            // Fetch user emails
            ResponseEntity<GithubUserEmailResponseDto[]> response = restTemplate.exchange(
                    "https://api.github.com/user/emails",
                    HttpMethod.GET,
                    entity,
                    GithubUserEmailResponseDto[].class); // Note the array type for the response

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Find the primary, verified email from the list
                for (GithubUserEmailResponseDto emailDto : response.getBody()) {
                    if (emailDto.isPrimary() && emailDto.isVerified()) {
                        return emailDto.getEmail(); // Return the primary, verified email
                    }
                }
            } else {
                log.error("Failed to fetch GitHub user emails. Status Code: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error fetching GitHub user emails: " + e.getMessage(), e);
        }

        return null; // Return null if no primary email was found or an error occurred
    }

    public String getGoogleAccessToken(String code, String redirectUri) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.add("code", code);
        params.add("grant_type", "authorization_code");
        params.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(googleTokenUrl, request, Map.class);
        if (response.getBody() != null) {
            return response.getBody().get("access_token").toString();
        }
        return null;
    }

    public GoogleUserDto getGoogleUserDetails(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<GoogleUserDto> response = restTemplate.exchange(
                googleUserInfoUrl,
                HttpMethod.GET,
                entity,
                GoogleUserDto.class);

        return response.getBody();
    }

    public User login(LoginDto loginDto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getEmail(),
                        loginDto.getPassword()));

        return userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow();
    }

    public User createAdministrator(SignupDto signupDto) {
        Optional<Role> optionalRole = roleRepository.findByName(RoleEnum.ADMIN);

        if (optionalRole.isEmpty()) {
            return null;
        }
        User user = User.builder()
                .fullName(signupDto.getFullName())
                .email(signupDto.getEmail())
                .password(passwordEncoder.encode(signupDto.getPassword()))
                .role(optionalRole.get())
                .build();

        return userRepository.save(user);
    }

    public List<User> allUsers() {
        List<User> users = new ArrayList<>();

        userRepository.findAll().forEach(users::add);

        return users;
    }
}
