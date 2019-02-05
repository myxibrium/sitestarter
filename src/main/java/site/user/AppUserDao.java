package site.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by Repnox on 2/25/2018.
 */
@Component
public class AppUserDao implements UserDetailsService {

    private static String INSERT_USER = "insert into APP_USER " +
            "(id, created_dt, username, password, email, is_verif, email_verif_cd) values " +
            "(? , ?         , ?       , ?       , ?    , ?       , ?             )";

    private static String DELETE_EXISTING_ROLES = "delete from USER_ROLE " +
            "where user_id = ?";


    private static String INSERT_USER_ROLE = "insert into USER_ROLE " +
            "(user_id, role) values " +
            "(?      , ?      )";

    private static String SELECT_BY_USERNAME = "select * from APP_USER where username = ?";

    private static String SELECT_ROLES_BY_USER_ID = "select * from USER_ROLE where user_id = ?";

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    TransactionTemplate transactionTemplate;

    public void newUser(AppUser user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setId(UUID.randomUUID().toString());
        user.setVerificationCode(UUID.randomUUID().toString());
        jdbcTemplate.update(INSERT_USER,
                user.getId(),
                new Date(),
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.isVerified(),
                user.getVerificationCode());
    }

    public void updateUserRoles(AppUser user) {
        transactionTemplate.execute(new TransactionCallback<Void>() {
            @Override
            public Void doInTransaction(TransactionStatus transactionStatus) {
                jdbcTemplate.update(DELETE_EXISTING_ROLES, user.getId());
                for (String role : user.getRoles()) {
                    jdbcTemplate.update(INSERT_USER_ROLE, user.getId(), role);
                }
                return null;
            }
        });
    }

    @Override
    public AppUser loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser appUser = jdbcTemplate.query(SELECT_BY_USERNAME,
                new Object[] {username},
                resultSet -> {
                    if (resultSet.next()) {
                        AppUser appUser1 = new AppUser();
                        appUser1.setId(resultSet.getString("id"));
                        appUser1.setEmail(resultSet.getString("email"));
                        appUser1.setPassword(resultSet.getString("password"));
                        appUser1.setUsername(resultSet.getString("username"));
                        appUser1.setVerified(resultSet.getBoolean("is_verif"));
                        return appUser1;
                    }
                    return null;
                }
        );
        if (appUser == null) {
            throw new UsernameNotFoundException("Username not found.");
        } else {
            List<String> roles = jdbcTemplate.query(SELECT_ROLES_BY_USER_ID, new Object[]{appUser.getId()}, new ResultSetExtractor<List<String>>() {
                @Override
                public List<String> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
                    List<String> roles = new ArrayList<>();
                    while(resultSet.next()) {
                        roles.add(resultSet.getString("role"));
                    }
                    return roles;
                }
            });
            appUser.getRoles().addAll(roles);
        }
        return appUser;
    }

    public boolean verifyEmail(String emailVerificationCode) {
        int rowsUpdated = jdbcTemplate.update("update APP_USER set is_verif = 1 where email_verif_cd = ?", emailVerificationCode);
        return rowsUpdated > 0;
    }
}
