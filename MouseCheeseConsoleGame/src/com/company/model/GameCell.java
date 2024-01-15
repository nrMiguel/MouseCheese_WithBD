package com.company.model;

import com.mysql.jdbc.Connection;

import java.sql.SQLException;
import java.sql.Statement;

public class GameCell {
    private int points;
    private boolean isCat;
    private boolean isCheese;
    private boolean isDiscovered;

    public GameCell() {
        this.points = (int) ((Math.random()*3)+1) * 10;
    }

    public int getPoints() {
        return points;
    }

    public boolean isCat() {
        return isCat;
    }

    public boolean isDiscovered() { return isDiscovered; }

    public void setPoints(int points) {
        this.points = points;
    }

    public void setCat() {
        isCat = true;
    }

    public void setCat(Boolean bool) { isCat=bool; }

    public void setCheese() { isCheese = true; }

    public void setCheese(Boolean bool){ isCheese=bool; }

    public void setDiscovered(Connection con, int id, int x, int y) {
        isDiscovered = true;
        try {
            String query="Update gamecell Set isDiscovered=true Where idJugador=" + id +" AND posX=" + x + " AND posY=" + y;
            Statement stmt=con.createStatement();
            stmt.executeUpdate(query);
        } catch (SQLException e){
            System.out.println("No se ha podido actualizar la celda para que sea descubierta" + e.getClass().getName());
        }
    }

    public void setDiscovered(Boolean bool){ isDiscovered=bool; }

    @Override
    public String toString() {
        if(isCheese)
            return "CH";
        if(isCat)
            return "CC";
        else
            return String.format("%d",points);
    }
}
