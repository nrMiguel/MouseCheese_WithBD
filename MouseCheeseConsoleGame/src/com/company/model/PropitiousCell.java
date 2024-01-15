package com.company.model;

import com.mysql.jdbc.Connection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PropitiousCell extends GameCell implements Questionable {
    private String question;
    private String[] answers;

    public PropitiousCell(String question, String answers, Connection con, int id, int x, int y) {
        int idcelda=0;

        //Buscamos la idcelda a la que colocaremos la celda tipo PropitiousCell
        try{
            String query="Select idcelda From gamecell where idJugador=" + id + " AND posY=" + y + " AND posX=" + x;
            Statement stmt=con.createStatement();
            ResultSet rs=stmt.executeQuery(query);
            rs.next();
            idcelda=rs.getInt(1);

            //Añadimos PropitiousCell en la BD
            try {
                String queryInsert="INSERT INTO propitiouscell VALUES (" + idcelda + ", " + question + ", " + answers + ", " + id + ")";
                Statement stmt2= con.createStatement();
                stmt2.executeUpdate(queryInsert);
            } catch (SQLException e){
                System.out.println("Error inserting PropitiousCell into propitiouscell Table" + e.getClass().getName());
            }
        } catch (Exception e) {
            System.out.println("Error trying to obtain idcelda from gameCell" + e.getClass().getName());
        }
        this.question = question;
        this.answers = answers.toLowerCase().split(",");

    }

    @Override
    public String getQuestion() {
        return "Para ganar 50 puntos. "+this.question;
    }

    @Override
    public boolean submitAnswer(String answer) {
        for (String a :this.answers) {
            if(a.equals(answer.toLowerCase()))
                return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "++";
    }
}
