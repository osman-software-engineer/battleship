package battleship;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        Player player1 = new Player();
        Player player2 = new Player();
        Player currentPlayer = player1; // Start with player 1
        Player opponent;

        System.out.println("Player 1, place your ships on the game field");
        System.out.println();
        player1.placeYourShips();
        System.out.print("Press Enter and pass the move to another player");
        scanner.nextLine();
        System.out.println("...");
        System.out.println("Player 2, place your ships on the game field");
        System.out.println();
        player2.placeYourShips();
        System.out.println("Press Enter and pass the move to another player");
        System.out.println("...");
        scanner.nextLine();


        while (true){
            opponent = (currentPlayer == player1) ? player2 : player1; // Determine the opponent
            opponent.getFogBoard().printFogOfWar();
            System.out.println("---------------------");
            currentPlayer.getGameBoard().printFogOfWar();
            System.out.println();
            System.out.println("Player " + (currentPlayer == player1 ? "1" : "2") + ", it's your turn:");
            System.out.println();
            currentPlayer.gameStarts(opponent,currentPlayer.getScanner(),opponent.getShips());

            // Check for a win condition here; if a player has won, break out of the loop
            if (hasWon(currentPlayer)) {
                System.out.println("Player " + (currentPlayer == player1 ? "1" : "2") + " wins!");
                break;
            }

            // Switch players
            currentPlayer = opponent;
            System.out.println();
            System.out.println("Press Enter and pass the move to another player");
            System.out.println("...");
            scanner.nextLine();

        }
    }

    // This is just a placeholder method; you'll need to implement the actual win check
    public static boolean hasWon(Player player) {
        return player.getShips()[0].getHealth() == 0 && player.getShips()[1].getHealth() == 0 && player.getShips()[2].getHealth() == 0 && player.getShips()[3].getHealth() == 0 && player.getShips()[4].getHealth() == 0;
    }
}