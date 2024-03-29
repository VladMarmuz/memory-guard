package com.catchthemoment.service;

import com.catchthemoment.entity.User;
import com.catchthemoment.exception.ServiceProcessingException;
import com.catchthemoment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.validation.constraints.NotNull;

import java.util.Optional;

import static com.catchthemoment.exception.ApplicationErrorEnum.MAIL_INCORRECT;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserResetPasswordService {

    private final UserRepository repository;
    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String mailAddress;
    @Value("${spring.application.name}")
    private String sender;

    @Transactional(rollbackFor = Exception.class)
    public void updateResetPasswordToken(@NotNull String email, String token) throws ServiceProcessingException {
        var user = repository.findUserByEmail(email).orElseThrow(
                () -> new ServiceProcessingException(MAIL_INCORRECT));
            user.setResetPasswordToken(token);
            repository.save(user);
    }

    public Optional<User> getUserFromResetToken(String token) {
        return repository.findUserByResetPasswordToken(token);

    }

    @Transactional
    public void updatePassword(@NotNull User reqUser, String newPassword) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(newPassword);
        reqUser.setPassword(encodedPassword);
        reqUser.setResetPasswordToken(null);
        repository.save(reqUser);
    }

    public void sendResetPasswordEmail(String mailRecipient, String link) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(mailAddress);
        helper.setTo(mailRecipient);

        String subject = "Here's the link to reset your password";
        String content = """ 
                <p>Hello,</p>
                <p>You have requested to reset your password.</p>
                <p>Click the link below to change your password:</p>
                <p><a href=\"" + link + "\">Change my password</a></p>
                <br>
                <p>Ignore this email if you do remember your password,
                or you have not made the request.</p>
                """;

        helper.setSubject(subject);
        helper.setText(content, true);
        javaMailSender.send(message);
    }


}
