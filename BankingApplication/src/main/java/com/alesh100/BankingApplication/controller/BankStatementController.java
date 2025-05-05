package com.alesh100.BankingApplication.controller;

import com.alesh100.BankingApplication.entity.Transaction;
import com.alesh100.BankingApplication.service.impl.BankStatement;
import com.itextpdf.text.DocumentException;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileNotFoundException;
import java.util.List;

@RestController
@RequestMapping("/bankStatement")
@AllArgsConstructor
public class BankStatementController {
    @Autowired
    BankStatement bankStatement;

    @GetMapping
    public List<Transaction> bankStatement(@RequestParam String accountNumber,
                                           @RequestParam String startDate,
                                           @RequestParam String endDate) throws DocumentException, FileNotFoundException {
      return bankStatement.listOfTransaction(accountNumber, startDate, endDate);
    }
}
