package site.db;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import site.user.AppUser;
import site.user.AppUserDao;

import javax.annotation.PostConstruct;
import java.util.Arrays;

/**
 * Created by Repnox on 2/25/2018.
 */
@Configuration
public class DefaultUserConfig {

    @Value("${site.admin.password}")
    private String adminPassword;

    private final Logger logger = LoggerFactory.getLogger(DefaultUserConfig.class);

    @Autowired
    AppUserDao appUserDao;

    // Ensure flyway migration has occurred first by depending on this bean.
    @Autowired
    Flyway flyway;

    @PostConstruct
    public void init() {
        AppUser admin = new AppUser();
        admin.setUsername("admin");
        admin.setPassword(adminPassword);
        admin.setVerified(true);
        admin.getRoles().addAll(Arrays.asList("ADMIN"));
        admin.setEmail("nobody@nowhere.com");

        try {
            appUserDao.newUser(admin);
            appUserDao.updateUserRoles(admin);
        } catch (Exception e) {
            logger.info("Skipped creating admin user - because it already exists?");
        }
    }

}
