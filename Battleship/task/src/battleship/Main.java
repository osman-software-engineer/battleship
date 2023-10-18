package battleship;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
        Board gameBoard = new Board(10, 10);
        gameBoard.createFogOfWar();
        gameBoard.printFogOfWar();
        Ship[] ships = new Ship[5];
        initializeShips(ships);
        Scanner scanner = new Scanner(System.in);
        //Main Game Loop
        for (Ship ship : ships) {
            System.out.printf("Enter the coordinates of the %s (%s cells):\n", ship.getName(), ship.getSize());
            String userInput = scanner.nextLine();
            // Validate Ship Location is within the dimensions of game board.
            while (gameBoard.isShipLocationCorrect(userInput)) {
                System.out.println("Error! Wrong ship location! Try again:");
                System.out.printf("Enter the coordinates of the %s (%s cells):\n", ship.getName(), ship.getSize());
                userInput = scanner.nextLine();
                continue;
            }
            // Validate Ship Length
            Location shipLocation = getCoordinates(userInput);

            while (!gameBoard.isShipLengthCorrect(shipLocation, ship)) {
                System.out.printf("Error! Wrong length of the %s! Try again:\n", ship.getName());
                userInput = scanner.nextLine();
                shipLocation = getCoordinates(userInput);
                //Validate Ship Location
                while (gameBoard.isShipLocationCorrect(userInput)) {
                    System.out.println("Error! Wrong ship location! Try again:");
                    userInput = scanner.nextLine();
                    shipLocation = getCoordinates(userInput);
                }
                continue;
            }
            // Validate Ship Closeness to Other Ship
            while (gameBoard.isShipTooCloseToOtherShip(shipLocation)) {
                System.out.println("Error! You placed it too close to another one. Try again:");
                userInput = scanner.nextLine();
                shipLocation = getCoordinates(userInput);
                while (gameBoard.isShipLocationCorrect(userInput)) {
                    System.out.println("Error! Wrong ship location! Try again:");
                    userInput = scanner.nextLine();
                    shipLocation = getCoordinates(userInput);
                }
                continue;
            }

            gameBoard.placeShip(shipLocation);
            gameBoard.printFogOfWar();
        }

    }

    private static Location getCoordinates(String userInput) {
        String[] splitUserInput = userInput.split(" ");
        String x1 = splitUserInput[0].substring(0, 1);
        String x2 = splitUserInput[0].substring(1);
        String y1 = splitUserInput[1].substring(0, 1);
        String y2 = splitUserInput[1].substring(1);
        return new Location(x1, x2, y1, y2);
    }

    private static void initializeShips(Ship[] ships) {
        Ship aircraftCarrier = new Ship(ShipType.AIRCRAFT_CARRIER, 5, 5, null);
        Ship battleship = new Ship(ShipType.BATTLESHIP, 4, 4, null);
        Ship submarine = new Ship(ShipType.SUBMARINE, 3, 3, null);
        Ship cruiser = new Ship(ShipType.CRUISER, 3, 2, null);
        Ship destroyer = new Ship(ShipType.DESTROYER, 2, 2, null);
        ships[0] = aircraftCarrier;
        ships[1] = battleship;
        ships[2] = submarine;
        ships[3] = cruiser;
        ships[4] = destroyer;
    }
}


class Board {
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
                this.field[row][col] = "~";
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
        return !matcher.matches();
    }

    public boolean isShipLengthCorrect(Location location, Ship ship) {
        int x1;
        int x2;
        int y1;
        int y2;
        String shipOrientation = getShipOrientation(location);
        if (shipOrientation.equalsIgnoreCase("horizontal")) {
            x2 = Integer.parseInt(location.getX2());
            y2 = Integer.parseInt(location.getY2());
            if (x2 < y2) {
                x2 = Integer.parseInt(location.getX2()) - 1;
                y2 = Integer.parseInt(location.getY2());
            } else {
                x2 = Integer.parseInt(location.getX2());
                y2 = Integer.parseInt(location.getY2()) - 1;
            }
            return (Math.abs(Math.abs(x2 - y2) - ship.getSize()) == 0);
        } else if (shipOrientation.equalsIgnoreCase("vertical")) {
            x1 = getRowLabelIndex(location.getX1());
            y1 = getRowLabelIndex(location.getY1());
            if (x1 < y1) {
                x1 = getRowLabelIndex(location.getX1()) - 1;
                y1 = getRowLabelIndex(location.getY1());
            } else {
                x1 = getRowLabelIndex(location.getX1());
                y1 = getRowLabelIndex(location.getY1()) - 1;
            }


            return (Math.abs(Math.abs(x1 - y1) - ship.getSize()) == 0);
        } else {
            return false;
        }
    }

    private String getShipOrientation(Location location) {
        if (!location.getX1().equalsIgnoreCase(location.getY1()) && !location.getX2().equalsIgnoreCase(location.getY2())) {
            return "diagonal";
        } else if (location.getX1().equals(location.getY1())) {
            return "horizontal";
        } else {
            return "vertical";
        }
    }

    public void placeShip(Location location) {
        String shipOrientation = getShipOrientation(location);
        int x1;
        int x2;
        int y1;
        int y2;
        if (shipOrientation.equalsIgnoreCase("horizontal")) {
            x1 = getRowLabelIndex(location.getX1());
            x2 = Integer.parseInt(location.getX2());
            y2 = Integer.parseInt(location.getY2());
            if (x2 < y2) {
                for (int i = x2; i <= y2; i++) {
                    this.field[x1][i - 1] = "O";
                }
            }
            if (x2 > y2) {
                for (int i = y2; i <= x2; i++) {
                    this.field[x1][i - 1] = "O";
                }
            }


        }
        if (shipOrientation.equalsIgnoreCase("vertical")) {
            x1 = getRowLabelIndex(location.getX1());
            x2 = getColumnLabelIndex(location.getX2());
            y1 = getRowLabelIndex(location.getY1());
            y2 = getColumnLabelIndex(location.getY2());
            for (int i = x1; i <= y1; i++) {
                this.field[i][x2] = "O";
            }
        }

    }

    public boolean isShipTooCloseToOtherShip(Location location) {
        String shipOrientation = getShipOrientation(location);
        int x1;
        int x2;
        int y2;
        boolean checkEast;
        boolean checkWest;
        boolean checkNorth;
        boolean checkSouth;
        if (shipOrientation.equalsIgnoreCase("horizontal")) {
            x1 = getRowLabelIndex(location.getX1());
            x2 = Integer.parseInt(location.getX2());
            y2 = Integer.parseInt(location.getY2());
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
            x1 = getRowLabelIndex(location.getX1());
            x2 = getColumnLabelIndex(location.getX2());
            y2 = getColumnLabelIndex(location.getY2());
            if (x2 < y2) {
                try {
                    checkNorth = this.field[x1][x2].equalsIgnoreCase("O");
                    if (checkNorth) return true;
                } catch (Exception ignored) {
                }
                try {
                    checkSouth = this.field[x1][y2 + 1].equalsIgnoreCase("O");
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

        }
        return false;

    }

    private static int getRowLabelIndex(String rowLabel) {
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

    private static int getColumnLabelIndex(String colLabel) {
        return Integer.parseInt(colLabel) - 1;
    }
}


class Location {
    private String x1;
    private String x2;
    private String y1;
    private String y2;

    public Location(String x1, String x2, String y1, String y2) {
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
}

class Ship {
    private String name;
    private int size;
    private int health;
    private Location location;

    public Ship(ShipType shipType, int size, int health, Location location) {
        this.name = shipType.getName();
        this.size = size;
        this.health = health;
        this.location = location;
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

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
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





