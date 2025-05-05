package com.alesh100.BankingApplication.service.impl;

import com.alesh100.BankingApplication.dto.EmailDetails;
import com.alesh100.BankingApplication.entity.Transaction;
import com.alesh100.BankingApplication.entity.User;
import com.alesh100.BankingApplication.repository.TransactionRepository;
import com.alesh100.BankingApplication.repository.UserRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.query.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.zip.DataFormatException;

@Component
@AllArgsConstructor
@Slf4j
public class BankStatement {

    @Autowired
    TransactionRepository transactionRepository;
    @Autowired
    UserRepository userRepository;

    @Autowired
    EmailService emailService;

    private static final String FILE = "C:\\Users\\PC\\Downloads\\MyStatement.pdf";

    private static final  Font fontWhite = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL , BaseColor.WHITE);
    private static final  Font fontBlack = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL , BaseColor.BLACK);

    public List<Transaction> listOfTransaction (String accountNumber, String startDate,
                                                String endDate) throws FileNotFoundException, DocumentException {
        User user = userRepository.findByAccountNumber(accountNumber);
        LocalDateTime start = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE).plusDays(1).atStartOfDay();
        String customerName = user.getFirstName() + " " + user.getLastName() + " " + user.getMiddleName();
        List<Transaction> transactionList = transactionRepository.findAll().stream().filter(transaction -> transaction.getAccountNumber().equals(accountNumber))
                .filter(transaction -> transaction.getCreatedAt() != null &&
                        !transaction.getCreatedAt().isBefore(start) && !transaction.getModifiedAt().isAfter(end)).toList();
        Rectangle statementSize = new Rectangle(PageSize.A4);
        Document document = new Document(statementSize);
        log.info("");
        OutputStream outputStreamWriter =new FileOutputStream(FILE);
        PdfWriter.getInstance(document, outputStreamWriter);

        document.open();
        PdfPTable bankInfoTable = new PdfPTable(1);
        PdfPCell bankName = new PdfPCell(new Phrase("Alesh Bank", fontWhite));
        bankName.setBorder(0);
        bankName.setBackgroundColor(BaseColor.BLUE);
        bankName.setPadding(20f);

        PdfPCell bankAddress = new PdfPCell(new Phrase("7, Unity Avenue, Ayobo", fontBlack));
        bankAddress.setBorder(0);
        bankInfoTable.addCell(bankName);
        bankInfoTable.addCell(bankAddress);

        PdfPTable customerInfo = new PdfPTable(2);
        PdfPCell selectedCommencementDate = new PdfPCell(new Phrase("Start Date: " + startDate, fontBlack));
        selectedCommencementDate.setBorder(0);
        PdfPCell statement = new PdfPCell(new Phrase("STATEMENT OF ACCOUNT", fontBlack));
        statement.setBorder(0);

        PdfPCell stopDate = new PdfPCell(new Phrase("Stop Date: " + endDate, fontBlack));
        stopDate.setBorder(0);
        PdfPCell name = new PdfPCell(new Phrase("Customer Name: " + customerName, fontBlack));
        name.setBorder(0);
        PdfPCell space = new PdfPCell();
        space.setBorder(0);
        PdfPCell address = new PdfPCell(new Phrase("Address: " + user.getAddress(), fontBlack));
        address.setBorder(0);
        customerInfo.addCell(selectedCommencementDate);
        customerInfo.addCell(statement);
        customerInfo.addCell(stopDate);
        customerInfo.addCell(name);
        customerInfo.addCell(space);
        customerInfo.addCell(address);

        PdfPTable transactionTable = new PdfPTable(4);
        PdfPCell date = new PdfPCell(new Phrase("DATE:" , fontWhite));
        date.setBorder(0);
        date.setBackgroundColor(BaseColor.BLUE);
        PdfPCell transactionType = new PdfPCell(new Phrase("Transaction Type", fontWhite));
        transactionType.setBackgroundColor(BaseColor.BLUE);
        transactionType.setBorder(0);
        PdfPCell transactionAmount = new PdfPCell(new Phrase("Transaction Amount", fontWhite));
        transactionAmount.setBackgroundColor(BaseColor.BLUE);
        transactionAmount.setBorder(0);
        PdfPCell transactiOnStatus = new PdfPCell(new Phrase("STATUS", fontWhite));
        transactiOnStatus.setBackgroundColor(BaseColor.BLUE);
        transactiOnStatus.setBorder(0);

        transactionTable.addCell(date);
        transactionTable.addCell(transactionType);
        transactionTable.addCell(transactionAmount);
        transactionTable.addCell(transactiOnStatus);
        transactionList.forEach(transaction -> {
            transactionTable.addCell(new Phrase(transaction.getCreatedAt().toString()));
            transactionTable.addCell(new Phrase(transaction.getTransactionType()));
            transactionTable.addCell(new Phrase(transaction.getAmount().toString()));
            transactionTable.addCell(new Phrase(transaction.getStatus()));
        });


        document.add(bankInfoTable);
        document.add(customerInfo);
        document.add(transactionTable);
        document.close();
        EmailDetails emailDetails =  EmailDetails.builder()
                .recipient(user.getEmail())
                .subject("BANK STATEMENT")
                .messageBody("Your account Statement from " + startDate + " to " + endDate)
                .attachment(FILE)
                .build();
        emailService.sendEmailAlertWithAttachment(emailDetails);

        return transactionList;
    }
}
