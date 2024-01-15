package com.company.model;

import com.company.jdbc.utilities.ConnectionDB;
import com.mysql.jdbc.Connection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

public class MouseCheeseGame {
    private GameCell[][] gameBoard;
    private int totalEarnedPoints = 0;
    private int colMousePosition;
    private int rowMousePosition;
    private boolean won;
    private boolean lost;
    private boolean testMode;

    /**
     * Crear un tablero de rows x cols y colocar el gato, el queso, una celda propicia y una celda no propicia.
     * @param rows nombre de files
     * @param cols nombre de columnes
     */
    //AÑADIR BD
    public MouseCheeseGame(int rows, int cols,boolean testMode, Connection con, int id, boolean jugadorNuevo){
        this.testMode = testMode;

        if (jugadorNuevo) {
            this.gameBoard = new GameCell[rows][cols];
            for (int i = 0; i < gameBoard.length; i++) {
                for (int j = 0; j < gameBoard[i].length; j++) {
                    gameBoard[i][j] = new GameCell();

                    //Sube las celGames a la BD con sus puntuaciones en cada posición
                    int points = gameBoard[i][j].getPoints();
                    String query = "INSERT INTO gamecell VALUES (0, " + id + ", false, false, false, " + points + ", " + i + ", " + j + ")";
                    try {
                        Statement stmt2 = con.createStatement();
                        stmt2.executeUpdate(query);
                    } catch (SQLException e) {
                        System.out.println("Error adding cells into gamecell Table" + e.getClass().getName());
                    }
                    //System.out.println(query);
                }
            }

            //añadimos el gato
            while(true) {
                int x = (int) (Math.random() * rows);
                int y = (int) (Math.random() * cols);
                if ((x > 0 || y > 0) && (x < rows || y > cols) && !(x == rows-1 && y == cols-1)  && !(gameBoard[x][y] instanceof Questionable)){
                    gameBoard[x][y].setCat();

                    //Añadimos el gato en la BD
                    String query="Update gamecell Set iscat=true Where idJugador=" + id + " AND posY=" + x + " AND posX=" + y;
                    try {
                        Statement stmt2= con.createStatement();
                        stmt2.executeUpdate(query);
                    } catch (SQLException e){
                        System.out.println("Error updating cell in gamecell Table" + e.getClass().getName());
                    }
                    //System.out.println(query);
                    break;
                }
            }

            //añadimos celda de pregunta propicia
            while(true) {
                int x = (int) (Math.random() * rows);
                int y = (int) (Math.random() * cols);
                if ((x > 0 || y > 0) && (x < rows || y > cols) && !(x == rows-1 && y == cols-1) && !(gameBoard[x][y] instanceof Questionable) && !gameBoard[x][y].isCat()){
                /*
                TODO el hecho de haber puesto '' dentro de los String question y answer podría provocar problemas al
                intentar acertar la pregunta puesto que ahora contiene '' dentro de la respuesta... Podría con substring
                solventar el problema? o trim modificando " " por "''", yo me entiendo
                */
                    try{
                        Random rnd=new Random();
                        int idQuestion=rnd.nextInt(4) + 1;

                        String query="Select question, answer From questionable Where idPregunta=" + idQuestion;
                        Statement stmt=con.createStatement();
                        ResultSet rs=stmt.executeQuery(query);

                        rs.next();
                        String question= "\'" + rs.getString(1) + "\'";
                        String answer=  "\'" + rs.getString(2) + "\'";

                        gameBoard[x][y] = new PropitiousCell(question, answer, con, id, x, y);
                    } catch (SQLException e){
                        System.out.println("No ha sido posible extraer las preguntas y respuestas de la BD, " + e.getClass().getName());
                    }
                    break;
                }
            }

            //añadimos celda de pregunta no propicia
            while(true) {
                int x = (int) (Math.random() * rows);
                int y = (int) (Math.random() * cols);
                if ((x > 0 || y > 0) && (x < rows || y > cols) && !(x == rows-1 && y == cols-1) && !(gameBoard[x][y] instanceof Questionable) && !gameBoard[x][y].isCat()){

                /*
                TODO UnpropitiousCell no implementado en BD, en sus métodos tampoco
                celda lo que haremos ahí es contar el tipo de puntuación que se obtiene de esta celda
                 */
                    gameBoard[x][y] = new UnpropitiousCell();
                    break;
                }
            }
            //añadimos el queso
            int xAux=cols-1;
            int yAux=rows-1;
            gameBoard[yAux][xAux].setCheese();

            //Añadimos el queso en la BD
            String query="Update gamecell Set ischeese=true Where idJugador=" + id + " AND posY=" + xAux + " AND posX=" + yAux;
            try {
                Statement stmt2= con.createStatement();
                stmt2.executeUpdate(query);
            } catch (SQLException e){
                System.out.println("Error updating cell in gamecell Table" + e.getClass().getName());
            }
            //System.out.println(query);


            if(testMode) {
                printSolutionBoard(gameBoard);
            }
        } else {
            gameBoard=new GameCell[rows][cols];

            //Descarga la puntuación actual
            int points=0;
            String queryPuntos="Select idcelda, iscat, ischeese, isDiscovered, points, posX, posY " +
                            "From gameCell " +
                            "Where idJugador=" + id + " ORDER BY posY, posX ASC";
            try{
                Statement stmtPuntos=con.createStatement();
                ResultSet rs=stmtPuntos.executeQuery(queryPuntos);

                for (int i=0; i<gameBoard.length; i++){
                    for(int j=0; j< gameBoard[i].length; j++){
                        gameBoard[i][j] = new GameCell();
                        rs.next();
                        int pointsAux=rs.getInt(5); //Variable solo para uso debugging
                        gameBoard[i][j].setCat(rs.getBoolean(2));
                        gameBoard[i][j].setCheese(rs.getBoolean(3));
                        gameBoard[i][j].setDiscovered(rs.getBoolean(4));
                        gameBoard[i][j].setPoints(rs.getInt(5));
                    }
                }
            } catch (SQLException e){
                System.out.println("No se ha podido descargar los puntos de la BD, " + e.getClass().getName());
            }
                    //Descarga las celdas de la BD
            if(testMode) {
                printSolutionBoard(gameBoard);
            }
        }
    }

    //Empieza colocando el ratón al inicio del juego COMPARTIR DATOS CON BD
    public void start(Connection con, int id, boolean bool) {
        if (bool) {
            colMousePosition = 0;
            rowMousePosition = 0;
            //Modificamos parámetros de entrada setDiscovered para comunicar con BD
            getCurrentCell().setDiscovered(con, id, colMousePosition, rowMousePosition);
            completeMouseMovement(con, id); //sube los puntos a la BD

            //Pone la posición del ratón en la primera casilla
            String query = "Update mousecheesegame Set colMousePosition=0, rowMousePosition=0 Where idJugador=" + id;
            try {
                Statement stmt = con.createStatement();
                stmt.executeUpdate(query);
            } catch (SQLException e) {
                System.out.println("Error reading mousecheesegame table, " + e.getClass().getName());
            }
        } else {
            try{
                //Descargamos de la BD la posición de la columna del ratón y la implementamos en el código
                String queryCol="Select colMousePosition From mousecheesegame Where idJugador=" + id;
                Statement stmtCol=con.createStatement();
                ResultSet rsCol=stmtCol.executeQuery(queryCol);

                rsCol.next();
                colMousePosition=rsCol.getInt(1);

                //Descargamos de la BD la posición de la fila del ratón y la implementamos en el código
                String queryRow="Select rowMousePosition From mousecheesegame Where idJugador=" + id;
                Statement stmtRow=con.createStatement();
                ResultSet rsRow=stmtRow.executeQuery(queryRow);

                rsRow.next();
                rowMousePosition=rsRow.getInt(1);
            } catch (SQLException e){
                System.out.println("Fallo al intentar acceder a la columna colMousePosition o rowMousePosition de mousecheesegame, " +
                                "" + e.getClass().getName());
            }
        }
    }

    public boolean hasWon(Connection con, int id) {
        if (won){
            borrarCeldasJugador(con, id);
            borrarPropitiousCell(con, id);
        }
        return won;
    }

    public boolean hasLost(Connection con, int id) {
        if (lost){
            borrarCeldasJugador(con, id);
            borrarPropitiousCell(con, id);
        }
        return lost;
    }

    private void borrarCeldasJugador(Connection con, int id){
        try{
            String query="DELETE FROM gamecell WHERE idJugador=" + id;
            Statement stmt=con.createStatement();
            stmt.executeUpdate(query);
        }catch (SQLException e){
            System.out.println("Fallo al intentar borrar las celdas de la partida, " + e.getClass().getName());
        }
    }

    private void borrarPropitiousCell(Connection con, int id){
        try {
            String query="Delete From propitiouscell Where idJugador=" + id;
            Statement stmt=con.createStatement();
            stmt.executeUpdate(query);
        } catch (SQLException e){
            System.out.println("No ha sido posible borrar los datos de propitiouscell, " + e.getClass().getName());
        }
    }

    public int getTotalEarnedPoints() {
        return totalEarnedPoints;
    }

    public int getColMousePosition() {
        return colMousePosition;
    }

    public int getRowMousePosition() {
        return rowMousePosition;
    }

    public String startMouseMovement(String movement, Connection con, int id) {
        switch (movement){
            case "l":
                if (colMousePosition == 0) { //Si está en la columna primera no podrá moverse
                    return null;
                }
                if(gameBoard[rowMousePosition][colMousePosition-1].isDiscovered()) { //Si esta descubierta movimiento inválido
                    return null;
                }
                colMousePosition--; //Si tod está correcto hace el movimiento, aquí DEBERÍA HACER BD?
                break;
            case "r":
                if (colMousePosition == gameBoard[rowMousePosition].length-1) { //Si está en la columna última no hay más para moverse
                    return null;
                }
                if(gameBoard[rowMousePosition][colMousePosition+1].isDiscovered()) { //Si esta descubierta movimiento inválido
                    return null;
                }
                colMousePosition++; //Si tod está correcto hace el movimiento, aquí ¿DEBERÍA HACER BD? pienso que sería mejor cuando comprueba de qué tipo es la casilla
                break;
            case "u":
                if (rowMousePosition == 0) { //Si está en la fila primera no hay más para moverse
                    return null;
                }
                if(gameBoard[rowMousePosition-1][colMousePosition].isDiscovered()) //Si esta descubierta movimiento inválido
                    return null;
                rowMousePosition--; //Si tod está correcto hace el movimiento, aquí DEBERÍA HACER BD?
                break;
            case "d":
                if (rowMousePosition == gameBoard.length-1) { //Si está en la fila última no hay más para moverse
                    return null;
                }
                if(gameBoard[rowMousePosition+1][colMousePosition].isDiscovered()) { //Si esta descubierta movimiento inválido
                    return null;
                }
                rowMousePosition++; //Si tod está correcto hace el movimiento, aquí DEBERÍA HACER BD?
                break;
        }
        getCurrentCell().setDiscovered(con, id, colMousePosition, rowMousePosition); //Toma la posición actual y le cambia a estado descubierto
        if (rowMousePosition == gameBoard.length-1 && colMousePosition == gameBoard[rowMousePosition].length-1 ){
            this.won = true;
            try {
                String query="Update mousecheesegame Set won=true Where idJugador=" + id;
                Statement stmt=con.createStatement();
                stmt.executeUpdate(query);
            } catch (SQLException e){
                System.out.println("No ha sido posible modificar el estado de la columna won de la tabla mousecheesegame, " + e.getClass().getName());
            }
            return null;
        }else if(getCurrentCell().isCat()){
            this.lost = true;
            try {
                String query="Update mousecheesegame Set lost=true Where idJugador=" + id;
                Statement stmt=con.createStatement();
                stmt.executeUpdate(query);
            } catch (SQLException e){
                System.out.println("No ha sido posible modificar el estado de la columna lost de la tabla mousecheesegame, " + e.getClass().getName());
            }
            return null;
        }else if(getCurrentCell() instanceof Questionable){ // Por aquí probablemente vaya el tema de las preguntas, para implentar en la BD digo
            return ((Questionable)getCurrentCell()).getQuestion();
        }
        completeMouseMovement(con, id); //Dentro de esta función se hace el tema de conseguir los puntos
        return null; //Retorna null para que cuando se dirija al controlador entre dentro del IF en referencia de que no es una celda pregunta
    }

    private GameCell getCurrentCell() {
        return gameBoard[rowMousePosition][colMousePosition];
    }

    public int completeMouseMovement(String userAnswer, Connection con, int id) {
        //TODO debería comprobar si la celda actual es PropitiousCell, para UnpropitiousCell no se podrá implementar
        if(getCurrentCell() instanceof PropitiousCell) {
            try {
                String query = "SELECT idcelda FROM propitiouscell WHERE idjugador=" + id + " AND idcelda IN(SELECT idcelda FROM gamecell)";
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query);

                if (rs.next()) {
                    this.totalEarnedPoints += 50;
                    if (((PropitiousCell) getCurrentCell()).submitAnswer(userAnswer)) {
                        try {
                            String query2 = "Update mousecheesegame Set totalEarnedPoints=" + this.totalEarnedPoints + "" +
                                    " Where idJugador=" + id;
                            Statement stmt2 = con.createStatement();
                            stmt2.executeUpdate(query2);
                        } catch (SQLException e) {
                            System.out.println("No se ha podido actualizar los puntos de la tabla mousecheesegame," + e.getClass().getName());
                        }
                        return 50;
                    } else {
                        System.out.println("En la BD no se ha encontrado la celda con pregunta, reinicie el juego manualmente");
                    }
                }
            }catch(SQLException e){
                System.out.println("No ha sido posible hacer la query en la tabla propitiouscell o en gamecell, " + e);
            }
        }

        //UnpropitiousCell no implementado en BD
        if(getCurrentCell() instanceof UnpropitiousCell) {
            if (!(((UnpropitiousCell) getCurrentCell()).submitAnswer(userAnswer))) {
                this.totalEarnedPoints -= 50;

                try {
                    String query2="Update mousecheesegame Set totalEarnedPoints=" + this.totalEarnedPoints + "" +
                            " Where idJugador=" + id;
                    Statement stmt2=con.createStatement();
                    stmt2.executeUpdate(query2);
                } catch (SQLException e){
                    System.out.println("No se ha podido actualizar los puntos de la tabla mousecheesegame," + e.getClass().getName());
                }

                return -50;
            }
        }
        /*
        por si la lío modificando el código...
        if(getCurrentCell() instanceof PropitiousCell) {
            if (((PropitiousCell) getCurrentCell()).submitAnswer(userAnswer)) {
                this.totalEarnedPoints += 50;
                return 50;
            }
        }
        if(getCurrentCell() instanceof UnpropitiousCell) {
            if (!(((UnpropitiousCell) getCurrentCell()).submitAnswer(userAnswer))) {
                this.totalEarnedPoints -= 50;
                return -50;
            }
        }
         */
        return 0;
    }

    //Modifica puntuación, COMPARTIR CON BD
    public int completeMouseMovement(Connection con, int id) {
        //Cambiar la llamada a función de abajo DE SER NECESARIO por query a BD
        this.totalEarnedPoints += getCurrentCell().getPoints();

        //Actualiza puntos de la partida del jugador
        try {
            String query="Update mousecheesegame Set totalEarnedPoints=" + this.totalEarnedPoints + "" +
                        " Where idJugador=" + id;
            Statement stmt=con.createStatement();
            stmt.executeUpdate(query);
        } catch (SQLException e){
            System.out.println("No se ha podido actualizar los puntos de la tabla mousecheesegame," + e.getClass().getName());
        }
        return getCurrentCell().getPoints();
    }

    public String getCurrentFigure() {
        return this.getCurrentCell().toString();
    }


    public static void printSolutionBoard(GameCell[][] gameBoard){
        for(int x = 0; x < gameBoard.length; x++){
            for(int y = 0; y < gameBoard[0].length ; y++){
                if(y > 0 && y < gameBoard[0].length)
                    System.out.print(" ");
                System.out.print(gameBoard[x][y]);
            }
            System.out.println();
        }
        System.out.println();
    }
}
