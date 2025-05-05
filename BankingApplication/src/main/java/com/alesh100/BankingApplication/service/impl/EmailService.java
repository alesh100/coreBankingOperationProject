package com.alesh100.BankingApplication.service.impl;

import com.alesh100.BankingApplication.dto.EmailDetails;

public interface EmailService {
    void sendEmailAlert(EmailDetails emailDetails);
    void sendEmailAlertWithAttachment(EmailDetails emailDetails);
}
