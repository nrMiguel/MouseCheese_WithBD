package com.company.model;

import com.mysql.jdbc.Connection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UnpropitiousCell extends GameCell implements Questionable{
    public String getQuestion() {
        return "Inserte un número entre 1 and 3, si no lo adivina, perderà 50 puntos:";
    }

    @Override
    public boolean submitAnswer(String answer) {
        int intValue = 0;
        try {
            intValue = Integer.parseInt(answer);
        } catch (NumberFormatException e) {
            return false;
        }
        int badLuckNumber = (int) (Math.random()*3)+1;
        return intValue == badLuckNumber;
    }

    @Override
    public String toString() {
        return "--";
    }
}
