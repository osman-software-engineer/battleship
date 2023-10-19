package battleship;

import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static final int BOARD_SIZE = 10;

    public static void main(String[] args) {
        Board gameBoard = new Board(BOARD_SIZE, BOARD_SIZE);
        gameBoard.createFogOfWar();
        gameBoard.printFogOfWar();
        Ship[] ships = new Ship[5];
        initializeShips(ships);
        Scanner scanner = new Scanner(System.in);
        String userInput;
        Coordinates shipCoordinates = null;
        for (Ship ship : ships) {
            System.out.printf("Enter the coordinates of the %s (%s cells):\n", ship.getName(), ship.getSize());
            userInput = scanner.nextLine();
            // Validate Ship Location is within the dimensions of game board.
            while (!gameBoard.isShipLocationCorrect(userInput)) {
                System.out.println("Error! Wrong ship location! Try again:");
                System.out.printf("Enter the coordinates of the %s (%s cells):\n", ship.getName(), ship.getSize());
                userInput = scanner.nextLine();
                continue;
            }
            // Validate Ship location is not diagonal
            shipCoordinates = getShipCoordinates(userInput);
            while (gameBoard.getShipOrientation(shipCoordinates).equalsIgnoreCase("diagonal") || !gameBoard.isShipLocationCorrect(userInput)) {
                System.out.println("Error! Wrong ship location! Try again:");
                userInput = scanner.nextLine();
                shipCoordinates = getShipCoordinates(userInput);
                continue;
            }
            // Validate Ship Length is Correct
            while (!gameBoard.isShipLengthCorrect(shipCoordinates, ship)) {
                System.out.printf("Error! Wrong length of the %s! Try again:\n", ship.getName());
                userInput = scanner.nextLine();
                shipCoordinates = getShipCoordinates(userInput);
                //Again Validate Ship Location is within game board dimensions
                while (!gameBoard.isShipLocationCorrect(userInput)) {
                    System.out.println("Error! Wrong ship location! Try again:");
                    userInput = scanner.nextLine();
                    shipCoordinates = getShipCoordinates(userInput);
                }
                continue;
            }
            // Validate Ship Closeness to Other Ship
            while (gameBoard.isShipTooCloseToOtherShip(shipCoordinates)) {
                System.out.println("Error! You placed it too close to another one. Try again:");
                userInput = scanner.nextLine();
                shipCoordinates = getShipCoordinates(userInput);
                // Again Validate Ship Location is within game board dimensions
                while (!gameBoard.isShipLocationCorrect(userInput) || gameBoard.getShipOrientation(shipCoordinates).equalsIgnoreCase("diagonal")) {
                    System.out.println("Error! Wrong ship location! Try again:");
                    userInput = scanner.nextLine();
                    shipCoordinates = getShipCoordinates(userInput);
                }
                continue;
            }
            gameBoard.placeShip(shipCoordinates); // Place ship on board
            ship.setUserInputCoordinates(shipCoordinates);
            ship.setOrientation(gameBoard.getShipOrientation(shipCoordinates));
            gameBoard.printFogOfWar();
        }
        // The Game Starts
        System.out.println("The game starts!");
        Board fogBoard = new Board(BOARD_SIZE, BOARD_SIZE);
        fogBoard.createFogOfWar();
        fogBoard.printFogOfWar();
        System.out.println("Take a shot!");
        Coordinates shotCoordinates = null;
        while (true) {
            userInput = scanner.nextLine();
            while (!gameBoard.isShotLocationCorrect(userInput)) {
                System.out.println("Error! You entered the wrong coordinates! Try again:");
                userInput = scanner.nextLine();
                shotCoordinates = getShotCoordinates(userInput);
                continue;
            }
            shotCoordinates = getShotCoordinates(userInput);
            if (gameBoard.getCoordinatesStatus(shotCoordinates).equalsIgnoreCase("occupied")) {
                gameBoard.takeShot(shotCoordinates);
                fogBoard.takeShot(shotCoordinates);
                fogBoard.printFogOfWar();
                System.out.println("You hit a ship! Try again:");
                gameBoard.printFogOfWar();
                // Deduct Health
                //1) Identify which ship got hit
                //2) Reduce its health
                for (Ship ship : ships) {
                    shipCoordinates = ship.getUserInputCoordinates();
                    int shotRow, shotCol;
                    Coordinates shipAddress = gameBoard.getShipAddress(shipCoordinates, ship);
                    int shipStartRow, shipStartCol, shipEndRow, shipEndCol;
                    if ("horizontal".equalsIgnoreCase(ship.getOrientation())) {
                        shotRow = Board.getRowLabelIndex(shotCoordinates.getX1());
                        shotCol = Integer.parseInt(shotCoordinates.getX2());
                        shipStartRow = Integer.parseInt(shipAddress.getX1());
                        shipStartCol = Integer.parseInt(shipAddress.getX2());
                        shipEndRow = Integer.parseInt(shipAddress.getY1());
                        shipEndCol = Integer.parseInt(shipAddress.getY2());
                    } else {
                        shotRow = Board.getRowLabelIndex(shotCoordinates.getX1());
                        shotCol = Board.getColumnLabelIndex(shotCoordinates.getX2());
                        shipStartRow = Integer.parseInt(shipAddress.getX1());
                        shipStartCol = Integer.parseInt(shipAddress.getX2());
                        shipEndRow = Integer.parseInt(shipAddress.getY1());
                        shipEndCol = Integer.parseInt(shipAddress.getY2());
                    }
                    if (!"sank".equalsIgnoreCase(ship.getStatus()) && isShipHitByIndices(shotRow, shotCol, shipStartRow, shipStartCol, shipEndRow, shipEndCol)) {
                        ship.setHealth(ship.getHealth() - 1);
                    }
                    if (ship.getHealth() == 0 && !"sank".equalsIgnoreCase(ship.getStatus())) {
                        System.out.println("You sank a ship! Specify a new target:");
                        ship.setStatus("sank");
                    }
                }


            } else {
                gameBoard.missedShot(shotCoordinates);
                fogBoard.missedShot(shotCoordinates);
                fogBoard.printFogOfWar();
                System.out.println("You missed. Try again:");
                gameBoard.printFogOfWar();
            }
            // Check health of Ships
            if (ships[0].getHealth() == 0 && ships[1].getHealth() == 0 && ships[2].getHealth() == 0 && ships[3].getHealth() == 0 && ships[4].getHealth() == 0) {
                System.out.println("You sank the last ship. You won. Congratulations!");
                break;
            }
        }

    }

    public static boolean isShipHitByIndices(int shotRow, int shotCol, int shipStartRow, int shipStartCol, int shipEndRow, int shipEndCol) {
        return shipStartRow <= shotRow && shotRow <= shipEndRow && shipStartCol <= shotCol && shotCol <= shipEndCol;
    }

    private static Coordinates getShipCoordinates(String userInput) {
        String[] splitUserInput = userInput.split(" ");
        String x1 = splitUserInput[0].substring(0, 1);
        String x2 = splitUserInput[0].substring(1);
        String y1 = splitUserInput[1].substring(0, 1);
        String y2 = splitUserInput[1].substring(1);
        return new Coordinates(x1, x2, y1, y2);
    }

    private static Coordinates getShotCoordinates(String userInput) {
        String x1 = userInput.substring(0, 1);
        String x2 = userInput.substring(1);
        return new Coordinates(x1, x2);
    }

    private static void initializeShips(Ship[] ships) {
        Ship aircraftCarrier = new Ship(ShipType.AIRCRAFT_CARRIER, 5, 5, null);
        Ship battleship = new Ship(ShipType.BATTLESHIP, 4, 4, null);
        Ship submarine = new Ship(ShipType.SUBMARINE, 3, 3, null);
        Ship cruiser = new Ship(ShipType.CRUISER, 3, 3, null);
        Ship destroyer = new Ship(ShipType.DESTROYER, 2, 2, null);
        ships[0] = aircraftCarrier;
        ships[1] = battleship;
        ships[2] = submarine;
        ships[3] = cruiser;
        ships[4] = destroyer;
    }
}


class Board {

    public static final String EMPTY_CHAR = "~";
    public static final String SHIP_CHAR = "X";
    public static final String MISS_CHAR = "M";

    public String[][] getField() {
        return field;
    }

    private final String[][] field;

    private final int rowSize;

    private final int colSize;

    public Board(int row, int col) {
        this.field = new String[row][col];
        this.rowSize = row;
        this.colSize = col;
    }

    public void createFogOfWar() {
        for (int row = 0; row < this.rowSize; row++) {
            for (int col = 0; col < this.colSize; col++) {
                this.field[row][col] = EMPTY_CHAR;
            }
        }
    }

    public void printFogOfWar() {
        char rowLabel = 'A';
        int colLabel = 1;
        // Print Column Label
        System.out.print("  ");
        for (int i = 0; i < 10; i++) {
            System.out.print(colLabel++ + " ");
        }
        System.out.println();
        // Print Row Label
        for (int row = 0; row < this.rowSize; row++) {
            System.out.print(rowLabel++ + " ");
            for (int col = 0; col < this.colSize; col++) {
                System.out.print(this.field[row][col] + " ");
            }
            System.out.println();
        }
    }

    public boolean isShipLocationCorrect(String userInput) {
        String regex = "[A-J](10|[1-9])\\s[A-J](10|[1-9])";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(userInput);
        return matcher.matches();
    }

    public String getCoordinatesStatus(Coordinates coordinates) {
        int x1, x2;
        x1 = getRowLabelIndex(coordinates.getX1());
        x2 = getColumnLabelIndex(coordinates.getX2());
        if ("O".equalsIgnoreCase(this.field[x1][x2])) {
            return "occupied";
        } else if (SHIP_CHAR.equalsIgnoreCase(this.field[x1][x2])){
            return "occupied";
        }else {
            return "empty";
        }
    }

    public void takeShot(Coordinates coordinates) {
        int x1, x2;
        x1 = getRowLabelIndex(coordinates.getX1());
        x2 = getColumnLabelIndex(coordinates.getX2());
        this.field[x1][x2] = SHIP_CHAR;

    }

    public void missedShot(Coordinates coordinates) {
        int x1, x2;
        x1 = getRowLabelIndex(coordinates.getX1());
        x2 = getColumnLabelIndex(coordinates.getX2());
        this.field[x1][x2] = MISS_CHAR;
    }

    public boolean isShotLocationCorrect(String userInput) {
        String regex = "[A-J](10|[1-9])";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(userInput);
        return matcher.matches();
    }

    public boolean isShipLengthCorrect(Coordinates coordinates, Ship ship) {
        int x1, x2, y1, y2;
        String shipOrientation = getShipOrientation(coordinates);
        if (shipOrientation.equalsIgnoreCase("horizontal")) {
            x2 = Integer.parseInt(coordinates.getX2());
            y2 = Integer.parseInt(coordinates.getY2());
            if (x2 < y2) {
                x2 = Integer.parseInt(coordinates.getX2()) - 1;
                y2 = Integer.parseInt(coordinates.getY2());
            } else {
                x2 = Integer.parseInt(coordinates.getX2());
                y2 = Integer.parseInt(coordinates.getY2()) - 1;
            }
            return (Math.abs(Math.abs(x2 - y2) - ship.getSize()) == 0);
        } else if (shipOrientation.equalsIgnoreCase("vertical")) {
            x1 = getRowLabelIndex(coordinates.getX1());
            y1 = getRowLabelIndex(coordinates.getY1());
            if (x1 < y1) {
                x1 = getRowLabelIndex(coordinates.getX1()) - 1;
                y1 = getRowLabelIndex(coordinates.getY1());
            } else {
                x1 = getRowLabelIndex(coordinates.getX1());
                y1 = getRowLabelIndex(coordinates.getY1()) - 1;
            }
            return (Math.abs(Math.abs(x1 - y1) - ship.getSize()) == 0);
        } else {
            return false;
        }
    }

    public String getShipOrientation(Coordinates coordinates) {
        if (!coordinates.getX1().equalsIgnoreCase(coordinates.getY1()) && !coordinates.getX2().equalsIgnoreCase(coordinates.getY2())) {
            return "diagonal";
        } else if (coordinates.getX1().equals(coordinates.getY1())) {
            return "horizontal";
        } else {
            return "vertical";
        }
    }

    public Coordinates getShipAddress(Coordinates shipCoordinates, Ship ship) {
        String shipOrientation = getShipOrientation(shipCoordinates);
        int x1, x2, y1 = 0, y2;
        if (shipOrientation.equalsIgnoreCase("horizontal")) {
            ship.setOrientation("horizontal");
            // Parsing Coordinates to Array indices
            x1 = getRowLabelIndex(shipCoordinates.getX1());
            x2 = Integer.parseInt(shipCoordinates.getX2());
            y1 = getRowLabelIndex(shipCoordinates.getY1());
            y2 = Integer.parseInt(shipCoordinates.getY2());
            ship.setArrayIndices(new Coordinates(String.valueOf(x1), String.valueOf(Math.min(x2,y2)), String.valueOf(y1), String.valueOf(Math.max(x2,y2))));
            return ship.getArrayIndices();
        }
        if (shipOrientation.equalsIgnoreCase("vertical")) {
            ship.setOrientation("vertical");
            x1 = getRowLabelIndex(shipCoordinates.getX1());
            x2 = getColumnLabelIndex(shipCoordinates.getX2());
            y1 = getRowLabelIndex(shipCoordinates.getY1());
            y2 = getColumnLabelIndex(shipCoordinates.getY2());
            ship.setArrayIndices(new Coordinates(String.valueOf(Math.min(x1,y1)), String.valueOf(x2), String.valueOf(Math.max(x1,y1)), String.valueOf(y2)));
            return ship.getArrayIndices();
        }
        return ship.getArrayIndices();
    }

    public void placeShip(Coordinates coordinates) {
        String shipOrientation = getShipOrientation(coordinates);
        int x1, x2, y1, y2;
        if (shipOrientation.equalsIgnoreCase("horizontal")) {
            // Parsing Coordinates to Array indices
            x1 = getRowLabelIndex(coordinates.getX1());
            x2 = Integer.parseInt(coordinates.getX2());
            y2 = Integer.parseInt(coordinates.getY2());
            for (int i = Math.min(x2, y2); i <= Math.max(x2, y2); i++) {
                this.field[x1][i - 1] = "O";
            }
            /*if (x2 > y2) {
                for (int i = y2; i <= x2; i++) {
                    this.field[x1][i - 1] = "O";
                }
            }*/
        }
        if (shipOrientation.equalsIgnoreCase("vertical")) {
            x1 = getRowLabelIndex(coordinates.getX1());
            x2 = getColumnLabelIndex(coordinates.getX2());
            y1 = getRowLabelIndex(coordinates.getY1());
            y2 = getColumnLabelIndex(coordinates.getY2());
            for (int i = x1; i <= y1; i++) {
                this.field[i][x2] = "O";
            }
        }

    }

    public boolean isShipTooCloseToOtherShip(Coordinates coordinates) {
        String shipOrientation = getShipOrientation(coordinates);
        int x1, x2, y1, y2;
        boolean checkEast;
        boolean checkWest;
        boolean checkNorth;
        boolean checkSouth;
        if (shipOrientation.equalsIgnoreCase("horizontal")) {
            x1 = getRowLabelIndex(coordinates.getX1());
            x2 = Integer.parseInt(coordinates.getX2());
            y2 = Integer.parseInt(coordinates.getY2());
            if (x2 < y2) {
                try {
                    checkWest = this.field[x1][x2 - 2].equalsIgnoreCase("O");
                    if (checkWest) return true;
                } catch (Exception ignored) {
                }
                try {
                    checkEast = this.field[x1][y2 + 1].equalsIgnoreCase("O");
                    if (checkEast) return true;
                } catch (Exception ignored) {
                }
                for (int i = x2; i <= y2; i++) {
                    try {
                        checkNorth = this.field[x1 - 1][i - 1].equalsIgnoreCase("O");
                        if (checkNorth) return true;
                    } catch (Exception ignored) {
                    }
                    try {
                        checkSouth = this.field[x1 + 1][i - 1].equalsIgnoreCase("O");
                        if (checkSouth) return true;
                    } catch (Exception ignored) {
                    }
                }

            }

        }
        if (shipOrientation.equalsIgnoreCase("vertical")) {
            x1 = getRowLabelIndex(coordinates.getX1());
            x2 = getColumnLabelIndex(coordinates.getX2());
            y1 = getRowLabelIndex(coordinates.getY1());
            y2 = getColumnLabelIndex(coordinates.getY2());
            if (x1 < y1) {
                try {
                    checkNorth = this.field[x1 - 1][x2].equalsIgnoreCase("O");
                    if (checkNorth) return true;
                } catch (Exception ignored) {
                }
                try {
                    checkSouth = this.field[y1 + 1][y2 + 1].equalsIgnoreCase("O");
                    if (checkSouth) return true;
                } catch (Exception ignored) {
                }
                for (int i = x2; i <= y2; i++) {
                    try {
                        checkEast = this.field[x1 - 1][i - 1].equalsIgnoreCase("O");
                        if (checkEast) return true;
                    } catch (Exception ignored) {
                    }
                    try {
                        checkWest = this.field[x1 + 1][i - 1].equalsIgnoreCase("O");
                        if (checkWest) return true;
                    } catch (Exception ignored) {
                    }
                }

            }
            if (x1 > y1) {
                try {
                    checkNorth = this.field[x1 - 1][x2].equalsIgnoreCase("O");
                    if (checkNorth) return true;
                } catch (Exception ignored) {
                }
                try {
                    checkSouth = this.field[x1 + 1][x2].equalsIgnoreCase("O");
                    if (checkSouth) return true;
                } catch (Exception ignored) {
                }
                for (int i = y1; i <= x1; i++) {
                    try {
                        checkEast = this.field[x1 - 1][i - 1].equalsIgnoreCase("O");
                        if (checkEast) return true;
                    } catch (Exception ignored) {
                    }
                    try {
                        checkWest = this.field[x1 + 1][i - 1].equalsIgnoreCase("O");
                        if (checkWest) return true;
                    } catch (Exception ignored) {
                    }
                }

            }
        }
        return false;
    }

    public static int getRowLabelIndex(String rowLabel) {
        return switch (rowLabel) {
            case "A" -> 0;
            case "B" -> 1;
            case "C" -> 2;
            case "D" -> 3;
            case "E" -> 4;
            case "F" -> 5;
            case "G" -> 6;
            case "H" -> 7;
            case "I" -> 8;
            case "J" -> 9;
            default -> throw new IllegalStateException("Unexpected value: " + rowLabel);
        };
    }

    public static int getColumnLabelIndex(String colLabel) {
        return Integer.parseInt(colLabel) - 1;
    }
}


class Coordinates {
    private String x1;
    private String x2;
    private String y1;
    private String y2;

    public Coordinates(String x1, String x2) {
        this.x1 = x1;
        this.x2 = x2;
    }

    public Coordinates(String x1, String x2, String y1, String y2) {
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
    }


    public String getX1() {
        return x1;
    }

    public void setX1(String x1) {
        this.x1 = x1;
    }

    public String getX2() {
        return x2;
    }

    public void setX2(String x2) {
        this.x2 = x2;
    }

    public String getY1() {
        return y1;
    }

    public void setY1(String y1) {
        this.y1 = y1;
    }

    public String getY2() {
        return y2;
    }

    public void setY2(String y2) {
        this.y2 = y2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinates that = (Coordinates) o;
        return Objects.equals(x1, that.x1) && Objects.equals(x2, that.x2) && Objects.equals(y1, that.y1) && Objects.equals(y2, that.y2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x1, x2, y1, y2);
    }

    @Override
    public String toString() {
        return "Coordinates{" +
                "x1='" + x1 + '\'' +
                ", x2='" + x2 + '\'' +
                ", y1='" + y1 + '\'' +
                ", y2='" + y2 + '\'' +
                '}';
    }
}

class Ship {
    private String name;
    private int size;
    private int health;

    private String orientation;
    private String status;
    private Coordinates userInputCoordinates;

    private Coordinates arrayIndices;

    public Ship(ShipType shipType, int size, int health, Coordinates coordinates) {
        this.name = shipType.getName();
        this.size = size;
        this.health = health;
        this.userInputCoordinates = coordinates;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Coordinates getArrayIndices() {
        return arrayIndices;
    }

    public void setArrayIndices(Coordinates arrayIndices) {
        this.arrayIndices = arrayIndices;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public Coordinates getUserInputCoordinates() {
        return userInputCoordinates;
    }

    public void setUserInputCoordinates(Coordinates userInputCoordinates) {
        this.userInputCoordinates = userInputCoordinates;
    }

    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ship ship = (Ship) o;
        return size == ship.size && health == ship.health && Objects.equals(name, ship.name) && Objects.equals(userInputCoordinates, ship.userInputCoordinates);
    }

    @Override
    public String toString() {
        return "Ship{" +
                "name='" + name + '\'' +
                ", size=" + size +
                ", health=" + health +
                ", orientation='" + orientation + '\'' +
                ", status='" + status + '\'' +
                ", userInputCoordinates=" + userInputCoordinates +
                ", arrayIndices=" + arrayIndices +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, size, health, userInputCoordinates);
    }
}

enum ShipType {
    AIRCRAFT_CARRIER("Aircraft Carrier"),
    BATTLESHIP("Battleship"),
    SUBMARINE("Submarine"),
    CRUISER("Cruiser"),
    DESTROYER("Destroyer");

    private final String name;


    ShipType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


}





