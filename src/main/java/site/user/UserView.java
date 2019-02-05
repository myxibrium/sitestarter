package site.user;

/**
 * Created by Repnox on 4/6/2018.
 */
public class UserView {

    private String username;

    private boolean verified;

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
