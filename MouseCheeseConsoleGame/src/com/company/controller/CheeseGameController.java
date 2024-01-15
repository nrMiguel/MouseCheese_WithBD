package com.company.controller;

import com.company.model.MouseCheeseGame;
import com.mysql.jdbc.Connection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class CheeseGameController {
    Scanner input = new Scanner(System.in);
    int currentRowMousePosition = 0 ;
    int currentColMousePosition = 0;
    String currentCellFigure ="";
    int newRowMousePosition = 0 ;
    int newColMousePosition = 0;
    MouseCheeseGame game;
    private String[][] gameBoard;

    public void play(int rows, int cols, boolean testMode, Connection con)  {
        int id=0;
        boolean jugadorNuevo=false;

        //Selecciona la última partida generada o crea una nueva
        String jugadorActivo="Select idJugador From mousecheesegame WHERE won=false AND lost=false Order By idJugador Desc LIMIT 1";
        try {
            Statement stmtJA = con.createStatement();
            ResultSet rs = stmtJA.executeQuery(jugadorActivo);
            //Si hay una partida activa la continua
            if (rs.next()){
                id = rs.getInt(1);
                System.out.println("Muestra si ha cogido id de la tabla (hay un jugador sin acabar partida) ID: " + id);
                gameBoard = startBoard(rows,cols, con, id, jugadorNuevo);
                game = new MouseCheeseGame(rows,cols,testMode, con, id, jugadorNuevo);
                //Si no hay partida activa crea una nueva.
            } else {
                jugadorNuevo=true;
                gameBoard = startBoard(rows,cols, con, id, jugadorNuevo);

                System.out.println("No hay partida en curso, creando nueva...");
                try {
                    String jugadorQueryNuevo = "Insert INTO mousecheesegame VALUES(0, 0, 0, 0, false, false, true)";
                    Statement stmtJN = con.createStatement();
                    stmtJN.executeUpdate(jugadorQueryNuevo);
                } catch (SQLException e){
                    System.out.println("Fallo al intentar insertar un nuevo jugador en la BD, " + e.getClass().getName());
                }
                try {
                    String jugadorNCreado = "Select idJugador From mousecheesegame Order By idJugador Desc LIMIT 1";
                    Statement selectJN = con.createStatement();
                    ResultSet rs2 = selectJN.executeQuery(jugadorNCreado);
                    rs2.next();
                    id = rs2.getInt(1);
                } catch (SQLException e){
                    System.out.println("Fallo al intentar seleccionar el nuevo jugador insertado en la BD, " + e.getClass().getName());
                }
                game = new MouseCheeseGame(rows,cols,testMode, con, id, jugadorNuevo);
            }

        } catch (SQLException e){
            System.out.println("No ha sido posible capturar la idJugador al intentar accceder a la tabla mousecheesegame, " + e.getClass().getName());
        }

        game.start(con, id, jugadorNuevo);
        System.out.println("Bienvenido a Cheese Mouse Game!!");
        System.out.println("El objetivo es que el raton 'MM' llegue al queso 'CH'.");
        System.out.println("Para desplazar el raton pulse:");
        System.out.println("r: Derecha (right)");
        System.out.println("l: Izquierda (left)");
        System.out.println("u: Arriba (up)");
        System.out.println("d: Abajo (down)");
        System.out.print( printBoard()+"\n");
        while (!game.hasLost(con, id) && !game.hasWon(con, id)){
            System.out.print("Inserte desplazamiento:");
            String moveOutput = this.move(input.nextLine(), con, id);
            if(moveOutput != null){
                System.out.println(moveOutput);
            }
        }
        //Hall of fame
        try{
            String query="Select idJugador AS Jugador, totalEarnedPoints AS Puntos From mousecheesegame " +
                    " Where won=true Order By totalEarnedPoints DESC";
            Statement stmt=con.createStatement();
            ResultSet rs=stmt.executeQuery(query);

            System.out.println("Este es el salón de la fama");
            while (rs.next()){
                int jugador= rs.getInt(1);
                int puntuación= rs.getInt(2);

                System.out.println("---------------------------------");
                System.out.println("Jugador: " + jugador + " Puntuación - " + puntuación);
                System.out.println("---------------------------------");
            }
        } catch (SQLException e){
            System.out.println("No ha sido posible obtener el salón de la fama de la BD, " + e.getClass().getName());
        }
    }

    private String[][] startBoard(int rows, int cols,Connection con, int id, boolean bool) {
        String[][] board = new String[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                board[i][j] = "00";
            }
        }

        if(bool) {
            board[0][0] = "MM";
            board[rows - 1][cols - 1] = "CH";
        } else {
            //Buscamos la posición del ratón en BD si la partida fue empezada
            try{
                String query="Select rowMousePosition, colMousePosition From mousecheesegame Where idJugador=" + id;
                Statement stmt=con.createStatement();
                ResultSet rs=stmt.executeQuery(query);

                rs.next();
                int row=rs.getInt(1);
                int col=rs.getInt(2);
                board[row][col] = "MM";
                board[rows - 1][cols - 1] = "CH";
            } catch (SQLException e){
                System.out.println("No ha sido posible ccrear la la celda descubierta al intentar acceder a la BD, " +
                                e.getClass().getName());
            }
        }
        return board;
    }

    public String move(String movement, Connection con, int id) {
        return performMovement(game.startMouseMovement(movement, con, id), con, id);
    }

    private String performMovement(String question, Connection con, int id) {
        if(question == null) {
            //El movimiento de ratón ya ha sido hecho en el modelo, también ha tomado puntos, igual podría hacer la persistencia de datos en BD desde ahí
            newRowMousePosition = game.getRowMousePosition();
            newColMousePosition = game.getColMousePosition();
            if(newRowMousePosition != currentRowMousePosition || newColMousePosition != currentColMousePosition) {
                moveMouseToNewCell(con, id);
                //Los siguientes 2 IFs comprueba si has ganado o perdido
                if(currentCellFigure=="CH"){
                    return printBoard() + String.format("Felicidades, has ganado!!\nPuntuacion total: %3d",game.getTotalEarnedPoints());
                }
                if(currentCellFigure=="CC") {
                    gameBoard[currentRowMousePosition][currentColMousePosition]=currentCellFigure;
                    return printBoard() + "Has perdido";
                }
                //Aquí retorna el tablero con lo ocurrido por lo que lo que ha pasado anteriormente es donde debería comunicar los datos con la BD
                //Aquí podría tomar los datos de puntuación y en la función de la línea 84 (moveMouseToNewCell) Coger la posición del mouse - COMPARTIR BD (planteando aun)
                return printBoard() + String.format("Puntos acumulados: %3d\n",game.getTotalEarnedPoints());
            }
            return null;
        }

        System.out.print(question);
        //Preguntar al usuario
        String userAnswer = input.nextLine();
        game.completeMouseMovement(userAnswer, con, id);
        return performMovement(null, con, id);
    }

    //Mueve el ratón a nueva posición, ¿DEBERÍA COMPARTIR CON BD?
    private void moveMouseToNewCell(Connection con, int id) {
        gameBoard[newRowMousePosition][newColMousePosition]="MM";
        switch (currentCellFigure){
            case "--","++":
                gameBoard[currentRowMousePosition][currentColMousePosition]=currentCellFigure;
                break;
            default:
                gameBoard[currentRowMousePosition][currentColMousePosition]="·.";
                break;
        }
        currentCellFigure = game.getCurrentFigure(); //Esta no me queda del tot claro como funciona, veo que comprueba de qué tipo es la casilla y le pasa el String
        //Aquí cambia la posición del mouse actual, ¿DEBERIA COMUNICAR A BD?
        currentRowMousePosition = newRowMousePosition;
        currentColMousePosition = newColMousePosition;

        //Sube la posición del mouse a la BD
        try {
            String query="UPDATE mousecheesegame SET colMousePosition=" + currentColMousePosition + "," +
                        " rowMousePosition=" +  currentRowMousePosition + " WHERE idJugador=" + id;
            Statement stmt= con.createStatement();
            stmt.executeUpdate(query);
        } catch (SQLException e){
            System.out.println("No se ha podido mover de posición el mouse en la tabla mousecheesegame" + e.getClass().getName());
        }
    }

    private String printBoard() {
        String board ="";
        for(int x = 0; x < gameBoard.length; x++){
            for(int y = 0; y < gameBoard[0].length ; y++){
                if(y > 0 && y < gameBoard[0].length)
                    board += " ";
                board += (gameBoard[x][y]);
            }
            board +=("\n");
        }
        return board;
    }
}
