package site.mail;

import java.util.List;

/**
 * Created by Repnox on 4/16/2018.
 */
public interface Mailer {

    void sendEmail(Email email) throws Exception;

}
