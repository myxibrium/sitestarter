package site.user;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;
import site.common.BasicResponse;
import site.mail.Email;
import site.mail.Mailer;
import site.security.AccessException;
import site.security.Authorizer;

import javax.servlet.http.HttpServletResponse;

@RestController
public class AppUserController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    private Authorizer authorizer;

    @Autowired
    private Mailer mailer;

    @Autowired
    private AppUserDao appUserDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @RequestMapping(method = RequestMethod.POST, value = "/api/login")
    public void login(@RequestBody AppUser inputUser) {
        if (inputUser == null || StringUtils.isBlank(inputUser.getUsername()) || StringUtils.isBlank(inputUser.getPassword())) {
            throw new AccessException();
        }

        AppUser userDetails;
        try {
            userDetails = appUserDao.loadUserByUsername(inputUser.getUsername());
        } catch (UsernameNotFoundException e) {
            throw new AccessException();
        }

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, inputUser.getPassword());
        try {
            authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        } catch (Exception e) {
            throw new AccessException();
        }

        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/user")
    public @ResponseBody UserView getUser() {
        AppUser user = authorizer.loggedInUser();

        UserView userView = new UserView();
        userView.setUsername(user.getUsername());
        userView.setVerified(user.isVerified());
        return userView;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/api/user")
    public void postUser(HttpServletRequest request, @RequestBody AppUser inputUser) throws Exception {
        // Prevent normal people from registering.
        authorizer.adminUser();

        AppUser user = new AppUser();
        user.setUsername(inputUser.getUsername());
        user.setEmail(inputUser.getEmail());
        user.setPassword(inputUser.getPassword());

        appUserDao.newUser(user);

        String urlProtocol = request.getProtocol().contains("https") ? "https://" : "http://";
        String verificationUrl = urlProtocol + request.getServerName() + "/#!/verify/"+user.getVerificationCode();

        Email verificationEmail = new Email();
        verificationEmail.addRecipientEmail(inputUser.getEmail());
        verificationEmail.setSubject("Email Verification Email");
        verificationEmail.setTextContent("Your email verification code is: "+user.getVerificationCode()+"\r\nOr follow this link: "+verificationUrl);
        verificationEmail.setHtmlContent("Your email verification code is: "+user.getVerificationCode()+"<br/><a href='"+verificationUrl+"'>Or follow this link.</a>");
        mailer.sendEmail(verificationEmail);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/user/email/verify/{id}")
    public @ResponseBody BasicResponse verifyEmail(@PathVariable String id) {
        boolean success = appUserDao.verifyEmail(id);
        return success ? new BasicResponse("SUCCESS") : new BasicResponse("FAIL");
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/user/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        new SecurityContextLogoutHandler().logout(request, response, auth);
    }

}
