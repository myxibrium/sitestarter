package site.mail;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Repnox on 4/16/2018.
 */
public class Email {

    private final List<EmailAddress> to = new ArrayList<>();

    private EmailAddress from;

    private String subject;

    private String textContent;

    private String htmlContent;

    public void setFromEmail(String emailAddress) {
        from = new EmailAddress();
        from.setEmail(emailAddress);
    }

    public void setFromEmail(String emailAddress, String name) {
        from = new EmailAddress();
        from.setEmail(emailAddress);
        from.setName(name);
    }

    public void addRecipientEmail(String emailAddress) {
        EmailAddress e = new EmailAddress();
        e.setEmail(emailAddress);
        to.add(e);
    }

    public void addRecipientEmail(String emailAddress, String name) {
        EmailAddress e = new EmailAddress();
        e.setEmail(emailAddress);
        e.setName(name);
        to.add(e);
    }

    public List<EmailAddress> getTo() {
        return to;
    }

    public EmailAddress getFrom() {
        return from;
    }

    public void setFrom(EmailAddress from) {
        this.from = from;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public String getHtmlContent() {
        return htmlContent;
    }

    public void setHtmlContent(String htmlContent) {
        this.htmlContent = htmlContent;
    }
}
