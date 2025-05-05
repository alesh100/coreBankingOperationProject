package com.alesh100.BankingApplication.service.impl;

import com.alesh100.BankingApplication.config.JWTTokenProvider;
import com.alesh100.BankingApplication.dto.*;
import com.alesh100.BankingApplication.entity.Role;
import com.alesh100.BankingApplication.entity.User;
import com.alesh100.BankingApplication.repository.UserRepository;
import com.alesh100.BankingApplication.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthenticatedAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;

@Service
public class UserServiceImpl implements UserService{
    DecimalFormat decimalFormat = new DecimalFormat("'\u20A6', ###,##0:00");
    @Autowired
    UserRepository userRepository;

    @Autowired
    EmailService emailService;

    @Autowired
    TransactionService transactionService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JWTTokenProvider jwtTokenProvider;

    @Override
    public BankResponse createAccount(UserRequest userRequest) {
        /*
         * Creating an account saving new user in db
         * check if user already has an account
         */
        if(userRepository.existsByEmail(userRequest.getEmail())){
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_EXISTS_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_EXISTS_MESSAGE)
                    .accountInfo(null)
                    .build();
        }
        User newUser = User.builder()
                .firstName(userRequest.getFirstName())
                .lastName(userRequest.getLastName())
                .middleName(userRequest.getMiddleName())
                .gender(userRequest.getGender())
                .address(userRequest.getAddress())
                .stateOfOrigin(userRequest.getStateOfOrigin())
                .accountNumber(AccountUtils.generateAccountNumber())
                .accountBalance(BigDecimal.ZERO)
                .email(userRequest.getEmail())
                .password(passwordEncoder.encode(userRequest.getPassword()))
                .phoneNumber(userRequest.getPhoneNumber())
                .alternativePhoneNumber(userRequest.getAlternativePhoneNumber())
                .status("ACTIVE")
                .role(Role.ROLE_ADMIN)
                .build();
        User savedUser = userRepository.save(newUser);
//send email alert
        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(savedUser.getEmail())
                .subject("ACCOUNT CREATION")
                .messageBody("congratulation you account has been created! \n your account number: " +
                        savedUser.getAccountNumber() + " \n Name: " + savedUser.getFirstName() + " " + savedUser.getLastName() +
                        " " + savedUser.getMiddleName())
                .build();
        emailService.sendEmailAlert(emailDetails);

        return BankResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_CREATION_SUCCESS_CODE)
                .responseMessage(AccountUtils.ACCOUNT_CREATION_SUCCESS_MESSAGE)
                .accountInfo(AccountInfo.builder()
                        .accountNumber(savedUser.getAccountNumber())
                        .accountBalance(savedUser.getAccountBalance())
                        .accountName(savedUser.getFirstName() + " " +
                                savedUser.getLastName() + " " + savedUser.getMiddleName())
                        .build())
                .build();
    }

    @Override
    public BankResponse balanceEnquiry(EnquiryRequest enquiryRequest) {
        //check if the provided account number exist in db
        boolean isAccountNumber = userRepository.existsByAccountNumber(enquiryRequest.getAccountNumber());
        if(!isAccountNumber){
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_EXISTS_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_MESSAGE)
                    .accountInfo(null)
                    .build();
        }
        User foundUser = userRepository.findByAccountNumber(enquiryRequest.getAccountNumber());
        return BankResponse.builder()
                .responseCode("004")
                .responseMessage("Account exist")
                .accountInfo(AccountInfo.builder()
                        .accountNumber(enquiryRequest.getAccountNumber())
                        .accountBalance(foundUser.getAccountBalance())
                        .accountName(foundUser.getFirstName() + " " + foundUser.getLastName() + " "
                        + foundUser.getMiddleName())
                        .build())
                .build();
    }

    @Override
    public String nameEnquiry(EnquiryRequest enquiryRequest) {
        boolean isAccountExist = userRepository.existsByAccountNumber(enquiryRequest.getAccountNumber());
        if(!isAccountExist){
            return "Account do not exist";
        }
        User foundUser = userRepository.findByAccountNumber(enquiryRequest.getAccountNumber());
        return foundUser.getFirstName() + " " + foundUser.getLastName() + foundUser.getMiddleName();

    }

    @Override
    public BankResponse creditAccount(CreditDebitRequest creditDebitRequest) {
        //check if the provided account number exist in db
        boolean isAccountNumber = userRepository.existsByAccountNumber(creditDebitRequest.getAccountNumber());
        if(!isAccountNumber){
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_EXISTS_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_MESSAGE)
                    .accountInfo(null)
                    .build();
        }
        User userToCredit = userRepository.findByAccountNumber(creditDebitRequest.getAccountNumber());
        userToCredit.setAccountBalance(userToCredit.getAccountBalance().add(creditDebitRequest.getAmount()));
        userRepository.save(userToCredit);
        TransactionDto transactionDto = TransactionDto.builder()
                .accountNumber(userToCredit.getAccountNumber())
                .amount(creditDebitRequest.getAmount())
                .transactionType("CREDIT")
                .build();
        transactionService.savedTransaction(transactionDto);

        return BankResponse.builder()
                .responseCode("005")
                .responseMessage("Successfully credited")
                .accountInfo(AccountInfo.builder()
                        .accountName(userToCredit.getFirstName() + " " + userToCredit.getLastName()
                        + " " + userToCredit.getMiddleName())
                        .accountBalance(userToCredit.getAccountBalance())
                        .accountNumber(userToCredit.getAccountNumber())
                        .build())
                .build();

    }

    @Override
    public BankResponse debitAccount(CreditDebitRequest creditDebitRequest) {
        //check if the account exist
        //check if the amount you which to withdraw is not more than current account number
        boolean isAccountNumber = userRepository.existsByAccountNumber(creditDebitRequest.getAccountNumber());
        if (!isAccountNumber) {
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_EXISTS_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_MESSAGE)
                    .accountInfo(null)
                    .build();
        }
        User userToDebit = userRepository.findByAccountNumber(creditDebitRequest.getAccountNumber());
        if (userToDebit.getAccountBalance().compareTo(creditDebitRequest.getAmount()) < 0){
            return BankResponse.builder()
                    .responseCode("006")
                    .responseMessage("amount not enough")
                    .accountInfo(null)
                    .build();
    }
        else
            userToDebit.setAccountBalance(userToDebit.getAccountBalance().subtract(creditDebitRequest.getAmount()));
        userRepository.save(userToDebit);
        TransactionDto transactionDto = TransactionDto.builder()
                .accountNumber(userToDebit.getAccountNumber())
                .amount(creditDebitRequest.getAmount())
                .transactionType("DEBIT")
                .build();
        transactionService.savedTransaction(transactionDto);
        return BankResponse.builder()
                .responseCode("007")
                .responseMessage("debit successfully")
                .accountInfo(AccountInfo.builder()
                        .accountNumber(creditDebitRequest.getAccountNumber())
                        .accountName(userToDebit.getFirstName() + " " + userToDebit.getLastName() + " " +
                                userToDebit.getMiddleName())
                        .accountBalance(userToDebit.getAccountBalance())
                        .build())
                .build();
    }

    @Override
    public BankResponse transfer(TransferRequest transferRequest) {
        /*
        Check if the receiver account number exist
        check if the sender have enough money
        debit the sender
        send email alert
        credit the receiver
        send email alert
         */
        boolean isReceiverAccountNumber = userRepository.existsByAccountNumber(transferRequest.getReceiverAccountNumber());
        if(!isReceiverAccountNumber){
            return  BankResponse.builder()
                    .responseCode("006")
                    .responseMessage("receiver account doesn't exist")
                    .accountInfo(null)
                    .build();
        }
        User senderAccount = userRepository.findByAccountNumber(transferRequest.getSenderAccountNumber());
        if(transferRequest.getAmount().compareTo(senderAccount.getAccountBalance()) > 0){
            return BankResponse.builder()
                    .responseCode("007")
                    .responseMessage("not enough money")
                    .accountInfo(null)
                    .build();
        }
        senderAccount.setAccountBalance(senderAccount.getAccountBalance().subtract(transferRequest.getAmount()));
        userRepository.save(senderAccount);
        transactionService.savedTransaction(TransactionDto.builder()
                .accountNumber(senderAccount.getAccountNumber())
                .amount(transferRequest.getAmount())
                .transactionType("DEBIT")
                .build());

        User receiverAccount = userRepository.findByAccountNumber(transferRequest.getReceiverAccountNumber());
        receiverAccount.setAccountBalance(receiverAccount.getAccountBalance().add(transferRequest.getAmount()));

        userRepository.save(receiverAccount);
        transactionService.savedTransaction(TransactionDto.builder()
                .accountNumber(receiverAccount.getAccountNumber())
                .amount(transferRequest.getAmount())
                .transactionType("CREDIT")
                .build());

        EmailDetails creditAlert = EmailDetails.builder()
                .recipient(receiverAccount.getEmail())
                .subject("Credit Alert")
                .messageBody("some of " + decimalFormat.format(transferRequest.getAmount()))
                .build();
        emailService.sendEmailAlert(creditAlert);
        EmailDetails debitAlert = EmailDetails.builder()
                .recipient(senderAccount.getEmail())
                .subject("Debit Alert")
                .messageBody("some of " + decimalFormat.format(transferRequest.getAmount()))
                .build();
        emailService.sendEmailAlert(debitAlert);


        return BankResponse.builder()
                .responseCode("008")
                .responseMessage("transfer successfully")
                .accountInfo(null)
                .build();
    }

//    public BankResponse login(LoginDto loginDto) {
//        Authentication authentication = null;
//        authentication =authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword())
//        );
//        EmailDetails loginAlert = EmailDetails.builder()
//                .subject("Login Success")
//                .recipient(loginDto.getEmail())
//                .messageBody("You just login our account")
//                .build();
//        emailService.sendEmailAlert(loginAlert);
//        return BankResponse.builder()
//                .responseCode("Login success")
//                .responseMessage(jwtTokenProvider.generateToken(authentication))
//                .build();
//    }

    @Override
    public BankResponse login(LoginDto loginDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword())
            );

            EmailDetails loginAlert = EmailDetails.builder()
                    .subject("Login Success")
                    .recipient(loginDto.getEmail())
                    .messageBody("You just logged in to your account")
                    .build();
            emailService.sendEmailAlert(loginAlert);

            return BankResponse.builder()
                    .responseCode("00")
                    .responseMessage(jwtTokenProvider.generateToken(authentication))
                    .build();

        } catch (BadCredentialsException ex) {
            return BankResponse.builder()
                    .responseCode("01")
                    .responseMessage("Invalid email or password")
                    .build();

        } catch (Exception ex) {
            return BankResponse.builder()
                    .responseCode("02")
                    .responseMessage("Login failed: " + ex.getMessage())
                    .build();
        }
    }

    //balance enquiry, name enquiry,

}
