package com.catchthemoment.controller;

import com.catchthemoment.entity.User;
import com.catchthemoment.exception.ServiceProcessingException;
import com.catchthemoment.model.ForgotPassword;
import com.catchthemoment.model.UpdatePassword;
import com.catchthemoment.service.UserResetPasswordService;
import com.catchthemoment.util.SiteUrlUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.utility.RandomString;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/**
 * @author shele
 * @version 1.0.0
 * This controller commits for updating user's password via sending email to user
 * for changing email .
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class ForgotPasswordController implements ForgotPasswordControllerApiDelegate {

    private final UserResetPasswordService resetPasswordService;

    @Override
    public ResponseEntity<Void> resetPassword(UpdatePassword updatePasswordModel) {
        String token = updatePasswordModel.getToken();
        String password = updatePasswordModel.getPassword();
        Optional<User> userFromResetToken = resetPasswordService.getUserFromResetToken(token);
        if (userFromResetToken.isEmpty()) {
            log.error("something goes wrong within" + userFromResetToken);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            log.info("*** user changed password {} ***", userFromResetToken);
            resetPasswordService.updatePassword(userFromResetToken.get(), password);

        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * When user fill incoming input form , sending email starts to come .
     * This method  charges of for sending email to user
     *
     * @param forgotPassword (optional)
     * @return
     * @throws Exception
     */
    @Override
    public ResponseEntity<Void> forgotPassword(@ModelAttribute ForgotPassword forgotPassword) throws Exception {
        String email = forgotPassword.getEmail();
        String token = RandomString.make(20);
        try {
            resetPasswordService.updateResetPasswordToken(email, token);
            String resetPasswordLink = SiteUrlUtil.getSiteURL(((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()) + "/users/reset_password?token=" + token;
            resetPasswordService.sendResetPasswordEmail(email, resetPasswordLink);
            log.debug("*** email was sent to user: {} ***", resetPasswordLink);
        } catch (ServiceProcessingException e) {
            throw new RuntimeException(e);
        }
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    /**
     * Get reset password html form base input
     *
     * @param token token for request user to reset password (optional)
     * @return
     * @throws Exception
     */
    @Override
    public ResponseEntity<String> resetPasswordForm(@RequestParam String token) throws Exception {
        Optional<User> user = resetPasswordService.getUserFromResetToken(token);
        if (user.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
