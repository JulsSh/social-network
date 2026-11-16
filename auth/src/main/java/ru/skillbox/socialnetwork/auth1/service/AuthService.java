package ru.skillbox.socialnetwork.auth1.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skillbox.socialnetwork.auth1.dto.RefreshRequest;
import ru.skillbox.socialnetwork.auth1.dto.RegistrationDto;
import ru.skillbox.socialnetwork.auth1.dto.TokenResponse;
import ru.skillbox.socialnetwork.auth1.entity.User;
import ru.skillbox.socialnetwork.auth1.enums.Role;
import ru.skillbox.socialnetwork.auth1.enums.TokenType;
import ru.skillbox.socialnetwork.auth1.exceptions.InvalidTokenException;
import ru.skillbox.socialnetwork.auth1.exceptions.PasswordsDoNotMatchException;
import ru.skillbox.socialnetwork.auth1.exceptions.UserAlreadyExistsException;
import ru.skillbox.socialnetwork.auth1.exceptions.UserNotFoundException;
import ru.skillbox.socialnetwork.auth1.repository.UserRepository;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    @Transactional
    public String registerNewUser(RegistrationDto registrationDto) {
        if (!registrationDto.password1().equals(registrationDto.password2())){
            throw new PasswordsDoNotMatchException();
        }
        if (userRepository.existsByEmail(registrationDto.email())) {
            throw new UserAlreadyExistsException(registrationDto.email());
        }
        User newUser = User.builder()
                .email(registrationDto.email())
                .password(passwordEncoder.encode(registrationDto.password1()))
                .firstName(registrationDto.firstName())
                .lastName(registrationDto.lastName())
                .role(Role.USER)
                .createdAt(Instant.now())
                .modifiedAt(Instant.now())
                .build();
        userRepository.save(newUser);
        return "ok";
    }

    @Transactional(readOnly = true)
    public TokenResponse refreshToken(RefreshRequest request){
       if(!jwtService.isValid(request.refreshToken(), TokenType.REFRESH)){
           throw new InvalidTokenException();
       }
      UUID userId = jwtService.extractUserId(request.refreshToken());

       User myUser = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        return  new TokenResponse(jwtService.generateAccessToken(myUser), jwtService.generateRefreshToken(myUser));
    }
}
