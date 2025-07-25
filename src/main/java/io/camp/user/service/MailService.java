package io.camp.user.service;

import io.camp.common.exception.ExceptionCode;
import io.camp.common.exception.user.CustomException;
import io.camp.common.exception.user.MailSendFailedException;
import io.camp.common.exception.user.VerifyCodeNotFoundException;
import io.camp.user.model.email.AuthCode;
import io.camp.user.repository.AuthCodeRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender javaMailSender;
    private final AuthCodeRepository authCodeRepository;

    private static final String senderEmail = "ahnju1103@gmail.com";
    private int generateCode() {
        return (int) (Math.random() * 90000) + 100000;
    }


    private MimeMessage createMail(String mail, int code) {
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            message.setFrom(senderEmail);
            message.setRecipients(MimeMessage.RecipientType.TO, mail);
            message.setSubject("[The Camp] 이메일 인증");

            String body = """
            <h3>요청하신 인증 번호입니다.</h3>
            <h1>%s</h1>
            <h3>감사합니다.</h3>
            """.formatted(code);

            message.setText(body, "UTF-8", "html");
        } catch (MessagingException e) {
            throw new MailSendFailedException(ExceptionCode.MAIL_SEND_FAILED); // 예외 처리
        }

        return message;
    }

    @Async
    public int sendMail(String mail, int code) {
        try {
            MimeMessage message = createMail(mail, code);
            javaMailSender.send(message);
        } catch (Exception e) {
            log.error("인증 메일 발송 실패: email={}, code={}", mail, code, e);
            throw new MailSendFailedException(ExceptionCode.MAIL_SEND_FAILED); // 예외 처리
        }
        return code;
    }
    @Transactional
    public void requestAuthCode(String email) {
        int code = generateCode();
        saveAuthCode(email, code);

        try {
            sendMail(email, code); // 메일 발송 실패해도 DB 저장 유지
        } catch (Exception e) {
            log.error("메일 발송 실패: email={}, code={}", email, code, e);
        }
    }


    @Transactional
    public void saveAuthCode(String email, int code) {
        AuthCode authCode = new AuthCode();
        authCode.setEmail(email);
        authCode.setCode(code);
        authCode.setCreatedAt(LocalDateTime.now());
        authCode.setExpiresAt(LocalDateTime.now().plusMinutes(3));

        authCodeRepository.deleteByEmail(email);
        authCodeRepository.save(authCode);
    }

    public boolean verifyAuthCode(String email, int code) {
        Optional<AuthCode> authCodeOpt = authCodeRepository.findByEmail(email);
        if (authCodeOpt.isPresent()) {
            AuthCode authCode = authCodeOpt.get();

            // 만료된 인증 코드 삭제
            if (authCode.getExpiresAt().isBefore(LocalDateTime.now())) {
                authCodeRepository.delete(authCode);
                throw new VerifyCodeNotFoundException(ExceptionCode.VERIFY_CODE_EXPIRED);
            }

            // 인증 코드가 일치하고 유효하다면 삭제하고 성공 반환
            if (authCode.getCode() == code) {
                authCodeRepository.delete(authCode);
                return true;
            }
        }
        throw new VerifyCodeNotFoundException(ExceptionCode.VERIFY_CODE_NOTFOUND);
    }

    @Scheduled(fixedRate = 5 * 60 * 1000) //매 5분마다 실행
    public void deleteExpiredAuthCodes() {
        LocalDateTime now = LocalDateTime.now();
        authCodeRepository.deleteByExpiresAtBefore(now);
    }

    public String generateTemporaryPassword() {
        int length = 10;
        String charSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        StringBuilder tempPassword = new StringBuilder(length);
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(charSet.length());
            tempPassword.append(charSet.charAt(index));
        }

        return tempPassword.toString();
    }

    public void sendTemporaryPassword(String email, String tempPassword) {
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            message.setFrom(senderEmail);
            message.setRecipients(MimeMessage.RecipientType.TO, email);
            message.setSubject("[The Camp] 임시 비밀번호 안내");

            String body = """
            <h3>요청하신 임시 비밀번호입니다.</h3>
            <h1>%s</h1>
            <h3>로그인 후 비밀번호를 변경해 주세요.</h3>
            """.formatted(tempPassword);

            message.setText(body, "UTF-8", "html");
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new MailSendFailedException(ExceptionCode.MAIL_SEND_FAILED);
        }
    }
}
