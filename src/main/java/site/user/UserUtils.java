package site.user;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Created by Repnox on 4/5/2018.
 */
public class UserUtils {

    public static AppUser getCurrentAppUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated() && authentication.getPrincipal() instanceof AppUser) {
            return (AppUser) authentication.getPrincipal();
        } else {
            return null;
        }
    }

}
