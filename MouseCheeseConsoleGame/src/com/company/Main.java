package com.company;

import com.company.controller.CheeseGameController;
import com.company.jdbc.utilities.ConnectionDB;
import com.mysql.jdbc.Connection;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {

    public static void main(String[] args) {
        // load and register JDBC driver for MySQL
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Connection con = null;
        int id=1;

        boolean showSolutionBoard = true;
        CheeseGameController game = new CheeseGameController();

        //try-with-resources
        try {
             con = (Connection) ConnectionDB.getInstance();
             game.play(4, 4, showSolutionBoard, con);
        } catch (SQLException e){
            e.printStackTrace();
        } finally {
            try {
               con.close();
            } catch (SQLException e){
                System.out.println("No se ha podido cerrar la conexión");
            }
        }


    }
}
