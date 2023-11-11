package ee.taltech.zoomalyzer.util;

import ee.taltech.zoomalyzer.entities.Recording;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

@Component
public class EmailService {
    private final Properties props;
    private static final Logger logger = Logger.getLogger(EmailService.class.getSimpleName());

    public EmailService() {
        props = loadProperties();
    }

    private void sendMail(String content, String recipientEmail) throws MessagingException {
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(props.getProperty("mailtramp.username"), props.getProperty("mailtramp.password"));
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress("noreply@zoomalyzer.com"));
        message.setRecipients(
                Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
        message.setSubject("Mail Subject");

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(content, "text/html; charset=utf-8");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);

        message.setContent(multipart);

        Transport.send(message);
        logger.info("Sent email to " + recipientEmail);
    }

    public void sendSavedRecordingEmail(Recording recording) throws MessagingException {
        sendMail(
                generateEmailContent(recording),
                recording.getUserEmail()
        );
    }

    public String generateEmailContent(Recording recording) {
        return String.format(
                "Your recording has been scheduled. Details and the link to your download can be found <a href=\"%s%s?token=%s\">here</a>.",
                props.getProperty("frontend.recording.url"), recording.getId(), recording.getToken());
    }

    private Properties loadProperties() {
        Properties properties = new Properties();
        String fileName = "application.properties";
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                System.out.println("Sorry, unable to find " + fileName);
                return properties;
            }

            properties.load(input);
        } catch (Exception ex) {
            throw new RuntimeException("Unable to load application.properties file in EmailService");
        }

        return properties;
    }
}
