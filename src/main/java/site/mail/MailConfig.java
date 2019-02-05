package site.mail;

import com.mailjet.client.MailjetClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Created by Repnox on 4/11/2018.
 */
@Component
public class MailConfig {

    @Autowired
    private MailjetMailer mailjetMailer;

    @Bean
    @Primary
    public Mailer mailer() {
        return mailjetMailer;
    }

}