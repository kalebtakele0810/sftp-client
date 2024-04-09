package et.kaleb.sftpclient.service;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

@Service
public class EmailHandlerServiceImpl implements EmailHandlerService {

    private Logger logger = LoggerFactory.getLogger(EmailHandlerService.class);

    // Recipient's email ID needs to be mentioned.
    @Value("${smtp.to}")
    private String to;

    // Sender's email ID needs to be mentioned
    @Value("${smtp.from}")
    private String from;

    @Value("${smtp.username}")
    private String username;//change accordingly

    @Value("${smtp.password}")
    private String password;//change accordingly

    @Value("${smtp.host}")
    private String emailHost;

    @Value("${smtp.port}")
    private Integer port;

    @Value("${sftp.file.extension}")
    private String fileExtension;

    @Value("${smtp.starttls.enable}")
    private boolean starttlsEnable;

    @Autowired
    private FileTransferService fileTransferService;

    @Override
    public boolean send(File file) throws JSchException, SftpException {
        //

        // Check if FTPFile is a regular file


        if (file.isFile()) {
            logger.info("Sending and renaming file: " + file.getName() + " " + FileUtils.byteCountToDisplaySize(file.length()));
            boolean isEmailSent = connectEmail(file);

            //rename file after email sent to avoid repetitive file sending
            if (isEmailSent) {
                fileTransferService.renameFTPFile(file.getName(), "_SENT_" + file.getName());
            }
            return true;
        }

        return false;
    }

    private boolean connectEmail(File file) {

        boolean isSent = false;
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", starttlsEnable);
        props.put("mail.smtp.host", emailHost);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.ssl.trust", emailHost);
        props.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");
        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        try {
            // Create a default MimeMessage object.
            Message message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));

            // Set Subject: header field
            message.setSubject("Hourly Authorized Transaction Report " + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().getTime()));

            // Create the message part
            BodyPart messageBodyPart = new MimeBodyPart();

            // Now set the actual message
            messageBodyPart.setContent(getEmailBody(), "text/html; charset=utf-8");
            // Create a multipar message
            Multipart multipart = new MimeMultipart();

            // Set text message part
            multipart.addBodyPart(messageBodyPart);

            // Part two is attachment
            messageBodyPart = new MimeBodyPart();
            String filename = file.getAbsolutePath();
            DataSource source = new FileDataSource(filename);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(FilenameUtils.removeExtension(file.getName()) + "." + fileExtension);
            multipart.addBodyPart(messageBodyPart);

            // Send the complete message parts
            message.setContent(multipart);

            // Send message
            Transport.send(message);
            isSent = true;
            logger.info("Email sent successfully for " + file.getName());
            return isSent;

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

    }

    private String getEmailBody() {
        ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.getDefault());
        String pattern = bundle.getString("body");
        String regex = "\\$\\{([^}]*)\\}";
        return pattern.replaceFirst(regex, new SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().getTime()));

    }
}
