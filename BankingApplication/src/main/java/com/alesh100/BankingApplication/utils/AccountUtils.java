package com.alesh100.BankingApplication.utils;

import java.time.Year;

public class AccountUtils {


    public static final String ACCOUNT_EXISTS_CODE = "001";
    public static final String ACCOUNT_EXISTS_MESSAGE = "This user alraedy has an created";
    public static final String ACCOUNT_CREATION_SUCCESS_CODE = "002";
    public static final String ACCOUNT_CREATION_SUCCESS_MESSAGE = "Account created successfully";

    public static final String ACCOUNT_NOT_CODE = "003";
    public static final String ACCOUNT_NOT_MESSAGE = "Account number not exist";

    public static String generateAccountNumber(){
        /*
         * 2025 + random 6 digit
         */


        Year currentYear = Year.now();
        int min = 100000;
        int max =900000;

        //generate a random number min and max
        int randomNumber =min + (int)(Math.random()  * max);
        //convert te current year and random number to string
        return currentYear + "" + randomNumber;

    }
}
