package com.alesh100.BankingApplication.service.impl;

import com.alesh100.BankingApplication.dto.TransactionDto;
import com.alesh100.BankingApplication.entity.Transaction;
import com.alesh100.BankingApplication.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TransactionImpl implements TransactionService{
    @Autowired
    TransactionRepository transactionRepository;

    @Override
    public void savedTransaction(TransactionDto transactionDto) {
        Transaction transaction = Transaction.builder()
                .transactionType(transactionDto.getTransactionType())
                .accountNumber(transactionDto.getAccountNumber())
                .amount(transactionDto.getAmount())
                .status("ACTIVE")
                .build();
        transactionRepository.save(transaction);
    }
}
