package com.redditapp.service;

import com.redditapp.dto.RegisterRequest;
import com.redditapp.model.NotificationEmail;
import com.redditapp.model.User;
import com.redditapp.model.VerificationToken;
import com.redditapp.repository.UserRepository;
import com.redditapp.repository.VerificationTokenRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.UUID;

import static com.redditapp.util.Constants.ACTIVATION_EMAIL;

@Service
@AllArgsConstructor
@Transactional
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final MailContentBuilder mailContentBuilder;
    private final MailService mailService;

    @Transactional
    public void signup(RegisterRequest registerRequest){
        User user = new User();
        user.setUserName(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setCreated(Instant.now());
        user.setEnabled(false);
        userRepository.save(user);

        generateVerificationToken(user);
        String token = generateVerificationToken(user);
        String message = mailContentBuilder.build("Thank you for signing up to Spring Reddit, please click on the below url to activate your account : "
                + ACTIVATION_EMAIL + "/" + token);

        mailService.sendMail(new NotificationEmail("Please Activate your account", user.getEmail(), message));
    }

    private String generateVerificationToken(User user) {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setuser(user);
        verificationTokenRepository.save(verificationToken);
        return token;
    }
}
