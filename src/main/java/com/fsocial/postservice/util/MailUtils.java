package com.fsocial.postservice.util;

import com.fsocial.postservice.dto.response.MailInformation;
import com.fsocial.postservice.enums.AccountErrorCode;
import com.fsocial.postservice.enums.MailTemplate;
import com.fsocial.postservice.exception.AppException;
import com.fsocial.postservice.exception.StatusCode;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MailUtils {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.from}")
    private String fromEmail;

    @Value("${app.resend.key}")
    private String resendApiKey;

    @Value("${app.resend.form}")
    private String resendFrom;

    public void sendOtp(String toEmail, String otpCode) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Your OTP Code");
            helper.setText(buildOtpEmail(otpCode), true);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.error("Có lỗi xảy ra khi gửi mail: {}", e.getMessage());
            throw new AppException(AccountErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    public String buildOtpEmail(String otpCode) {
        return MailTemplate.OTP.getTemplate().replace("{otp}", otpCode);
    }

    public void sendOtpResend (MailInformation mailInformation, String otp){
        String html = buildOtpEmail(otp);
        mailInformation.setHtml(html);

        Resend resend = new Resend(resendApiKey);
        CreateEmailOptions emailOptions = CreateEmailOptions.builder()
                .from(resendFrom)
                .to(mailInformation.getTo())
                .subject(mailInformation.getSubject())
                .html(mailInformation.getHtml())
                .build();

        try {
            CreateEmailResponse response  = resend.emails().send(emailOptions);
            System.out.println("Send mail successfull: " +  response.getId());
        }catch (ResendException e ){
            throw  new AppException(e.getMessage(),StatusCode.SEND_MAIL_FAIL);
        }

    }

}
