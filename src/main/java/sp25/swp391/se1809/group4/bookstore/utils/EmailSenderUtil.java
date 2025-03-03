package sp25.swp391.se1809.group4.bookstore.utils;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailSenderUtil {
    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String toEmail, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message,true,"UTF-8");
            helper.setFrom("bookstoregrp4@gmail.com");
            helper.setTo(toEmail);
            helper.setText(body,true);
            helper.setSubject(subject);

            mailSender.send(message);
            System.out.printf("Mail Sent Successfully!");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
