package com.alesh100.BankingApplication.service.impl;

import com.alesh100.BankingApplication.dto.EmailDetails;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.util.Objects;


@Service
public class EmailServiceImpl implements EmailService{

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;
    @Override
    public void sendEmailAlert(EmailDetails emailDetails) {
        try{
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setFrom(senderEmail);
            simpleMailMessage.setTo(emailDetails.getRecipient());
            simpleMailMessage.setText(emailDetails.getMessageBody());
            simpleMailMessage.setSubject(emailDetails.getSubject());
            javaMailSender.send(simpleMailMessage);
            System.out.println("mail send successfully");
        } catch (MailException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendEmailAlertWithAttachment(EmailDetails emailDetails) {

          MimeMessage mimeMessage = javaMailSender.createMimeMessage();
          MimeMessageHelper mimeMessageHelper;
          try{
              mimeMessageHelper = new MimeMessageHelper (mimeMessage, true);
              mimeMessageHelper.setFrom(senderEmail);
              mimeMessageHelper.setTo(emailDetails.getRecipient());
              mimeMessageHelper.setSubject(emailDetails.getSubject());
              mimeMessageHelper.setText(emailDetails.getMessageBody());
              FileSystemResource file = new FileSystemResource(new File(emailDetails.getAttachment()));
              mimeMessageHelper.addAttachment(Objects.requireNonNull(file.getFilename()), file);
              javaMailSender.send(mimeMessage);
          } catch (MessagingException e) {
              throw new RuntimeException(e);
          }
    }
}
