package site.security;

import org.springframework.stereotype.Component;
import site.user.AppUser;
import site.user.UserUtils;

@Component
public class Authorizer {

    public AppUser loggedInUser() {
        AppUser user = UserUtils.getCurrentAppUser();
        if (user == null) {
            throw new AccessException();
        } else {
            return user;
        }
    }

    public AppUser adminUser() {
        AppUser user = loggedInUser();
        if (user.getRoles().contains("ADMIN")) {
            return user;
        } else {
            throw new AccessException();
        }
    }

    public AppUser authorUser() {
        AppUser user = loggedInUser();
        if (user.getRoles().contains("ADMIN") || user.getRoles().contains("AUTHOR")) {
            return user;
        } else {
            throw new AccessException();
        }
    }

}
