package com.company.jdbc.utilities;

import com.company.jdbc.utilities.MYSQLConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionDB {
    private static Connection instance;

    private ConnectionDB(){};

    public static Connection getInstance() throws SQLException {
        if(instance == null) {
            String url = MYSQLConnection.url;
            String usr = MYSQLConnection.usr;
            String pwd = MYSQLConnection.pwd;
            instance = DriverManager.getConnection(url, usr, pwd);
        }
            return instance;
    }

    public static void closeConnection() throws SQLException {
        if(instance != null){
            instance.close();
        }
    }
}
