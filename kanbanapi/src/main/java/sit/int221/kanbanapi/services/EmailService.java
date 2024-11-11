package sit.int221.kanbanapi.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import sit.int221.kanbanapi.databases.kanbandb.entities.Board;
import sit.int221.kanbanapi.databases.kanbandb.entities.Collab;
import sit.int221.kanbanapi.databases.kanbandb.repositories.BoardRepository;
import sit.int221.kanbanapi.databases.userdb.entities.User;
import sit.int221.kanbanapi.exceptions.BadRequestException;
import sit.int221.kanbanapi.exceptions.EmailSendFailed;
import sit.int221.kanbanapi.exceptions.ItemNotFoundException;

import java.io.IOException;

@Service
public class EmailService {
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;
    @Autowired
    private UserService userService;
    @Autowired
    private final JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String mailSenderEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendInvitationEmail(String recipientEmail, String accessRight, String boardId) {
        if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
            throw new BadRequestException("Recipient email is invalid or missing");
        }
        String inviterName = jwtUserDetailsService.getCurrentUser().getName();
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new ItemNotFoundException("Board "+ boardId + " does not exist"));
        String subject = String.format("%s has invited you to collaborate with %s access right on %s board", inviterName, accessRight, board.getBoardName());
        String invitationLink = String.format("https://intproj23.sit.kmutt.ac.th/kk2/board/%s/collab/invitations", boardId);
        String message = String.format(
                "<html>" +
                        "<body style='font-family: Arial, sans-serif;'>" +
                        "<div style='max-width: 600px; margin: auto; padding: 20px; border: 1px solid #dddddd;'>" +
                        "<h2 style='color: #333333;'>Hello,</h2>" +
                        "<p>%s has invited you to collaborate on the <strong>%s</strong> board with <strong>%s</strong> access rights.</p>" +
                        "<p style='margin: 20px 0;'>Please click the button below to accept the invitation:</p>" +
                        "<a href='%s' style='display: inline-block; padding: 10px 20px; color: white; background-color: #007bff; text-decoration: none; border-radius: 5px;'>Accept Invitation</a>" +
                        "<p style='margin-top: 20px;'>If you have any questions, feel free to contact us.</p>" +
                        "<p>Best regards,<br>ITBKK-KK2 Team</p>" +
                        "<hr style='border: none; border-top: 1px solid #dddddd; margin: 20px 0;'>" +
                        "<p style='font-size: 12px; color: #777;'>This is an automated message. Please do not reply.</p>" +
                        "</div>" +
                        "</body>" +
                        "</html>",
                inviterName, board.getBoardName(), accessRight, invitationLink
        );
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(mailSenderEmail, inviterName);
            helper.setReplyTo("noreply@intproj23.sit.kmutt.ac.th", "DO NOT REPLY");
            helper.setTo(recipientEmail);
            helper.setSubject(subject);
            helper.setText(message, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException | IOException e) {
            throw new EmailSendFailed("Mail send Failed");
        }

    }
}
