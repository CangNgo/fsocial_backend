package com.fsocial.postservice.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum MailTemplate {
    OTP("<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "  <meta charset=\"UTF-8\" />\n" +
            "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />\n" +
            "  <title>OTP Verification</title>\n" +
            "</head>\n" +
            "<body style=\"margin:0;padding:0;background-color:#fdf6f0;font-family:'Segoe UI',Tahoma,Geneva,Verdana,sans-serif;\">\n" +
            "  <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color:#fdf6f0;padding:40px 20px;\">\n" +
            "    <tr>\n" +
            "      <td align=\"center\">\n" +
            "        <table role=\"presentation\" width=\"500\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);\">\n" +
            "          <!-- Header -->\n" +
            "          <tr>\n" +
            "            <td style=\"background:linear-gradient(135deg,#ff6d00,#ff9100);padding:32px 40px;text-align:center;\">\n" +
            "              <div style=\"width:56px;height:56px;margin:0 auto 16px;background-color:rgba(255,255,255,0.25);border-radius:50%;line-height:56px;font-size:28px;\">&#128272;</div>\n" +
            "              <h1 style=\"margin:0;color:#ffffff;font-size:22px;font-weight:700;letter-spacing:-0.3px;\">Verification Code</h1>\n" +
            "              <p style=\"margin:8px 0 0;color:rgba(255,255,255,0.85);font-size:14px;\">Enter this code to verify your identity</p>\n" +
            "            </td>\n" +
            "          </tr>\n" +
            "          <!-- Body -->\n" +
            "          <tr>\n" +
            "            <td style=\"padding:36px 40px 16px;\">\n" +
            "              <p style=\"margin:0 0 24px;color:#555;font-size:15px;line-height:1.6;text-align:center;\">\n" +
            "                Hello, please use the following code to complete your verification:\n" +
            "              </p>\n" +
            "              <!-- OTP Display -->\n" +
            "              <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\">\n" +
            "                <tr>\n" +
            "                  <td align=\"center\" style=\"padding:8px 0 24px;\">\n" +
            "                    <div style=\"display:inline-block;background-color:#fff5ee;border:2px solid #ff6d00;border-radius:12px;padding:18px 40px;letter-spacing:14px;font-size:32px;font-weight:700;color:#e65100;font-family:'Courier New',monospace;\">{otp}</div>\n" +
            "                  </td>\n" +
            "                </tr>\n" +
            "              </table>\n" +
            "              <p style=\"margin:0;color:#999;font-size:13px;text-align:center;\">\n" +
            "                &#9201; This code will expire in <strong style=\"color:#e65100;\">5 minutes</strong>\n" +
            "              </p>\n" +
            "            </td>\n" +
            "          </tr>\n" +
            "          <!-- Divider -->\n" +
            "          <tr>\n" +
            "            <td style=\"padding:0 40px;\"><hr style=\"border:none;border-top:1px solid #f0e0d0;margin:28px 0;\" /></td>\n" +
            "          </tr>\n" +
            "          <!-- Footer -->\n" +
            "          <tr>\n" +
            "            <td style=\"padding:0 40px 32px;\">\n" +
            "              <p style=\"margin:0;color:#aaa;font-size:12px;line-height:1.6;text-align:center;\">\n" +
            "                If you did not request this code, please ignore this email or contact support.<br/>\n" +
            "                &copy; 2026 Your Company. All rights reserved.\n" +
            "              </p>\n" +
            "            </td>\n" +
            "          </tr>\n" +
            "        </table>\n" +
            "      </td>\n" +
            "    </tr>\n" +
            "  </table>\n" +
            "</body>\n" +
            "</html>");
    final String template;

    MailTemplate(String template) {
        this.template = template;
    }
}
