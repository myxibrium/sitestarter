package site.mail;

import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.resource.Contact;
import com.mailjet.client.resource.Email;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Created by Repnox on 4/16/2018.
 */
@Component
public class MailjetMailer implements Mailer {

    @Value("${mailjet.api.username}")
    private String mailjetUsername;

    @Value("${mailjet.api.password}")
    private String mailjetPassword;

    @Value("${email.from.address}")
    private String fromAddress;

    @Value("${email.from.name}")
    private String fromName;

    private MailjetClient mailjetClient;

    @PostConstruct
    public void setup() {
        mailjetClient = new MailjetClient(mailjetUsername, mailjetPassword);
    }

    @Override
    public void sendEmail(site.mail.Email email) throws Exception {
        MailjetRequest mailjetRequest;
        JSONArray recipients;

        recipients = new JSONArray();

        for (EmailAddress addr : email.getTo()) {
            recipients.put(new JSONObject()
                    .put(Contact.EMAIL, addr.getEmail())
                    .put(Contact.NAME, addr.getName())
            );
        }

        if (email.getFrom() == null) {
            email.setFromEmail(fromAddress, fromName);
        }

        mailjetRequest = new MailjetRequest(Email.resource)
                .property(Email.FROMNAME, email.getFrom().getName())
                .property(Email.FROMEMAIL, email.getFrom().getEmail())
                .property(Email.SUBJECT, email.getSubject())
                .property(Email.TEXTPART, email.getTextContent())
                .property(Email.HTMLPART, email.getHtmlContent())
                .property(Email.RECIPIENTS, recipients)
                .property(Email.MJCUSTOMID, "JAVA-Email");

        mailjetClient.post(mailjetRequest);
    }
}
