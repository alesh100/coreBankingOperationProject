package com.alesh100.BankingApplication.repository;

import com.alesh100.BankingApplication.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, String> {

}
