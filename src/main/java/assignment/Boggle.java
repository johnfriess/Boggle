package assignment;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Boggle {
    public static void main(String[] args) {

        // open user input
        try (Scanner in = new Scanner(System.in)) {

            // base initaliziation
            boolean playAgain = true;
            BoggleDictionary dict = new GameDictionary();
            BoggleGame game = new GameManager();

            String wordsPath = "", cubesPath = "";
            boolean invalidWords = true, invalidCubes = true;
            while(invalidWords) {
                wordsPath = "words.txt";
                dict.loadDictionary(wordsPath);
                invalidWords = false;
                File f = new File(wordsPath);
                if(!f.exists() || f.isDirectory())
                    invalidWords = true;
            }

            while(invalidCubes) {
                cubesPath = "cubes.txt";
                game.newGame(4, 1, cubesPath, dict);
                invalidCubes = false;
                File f = new File(cubesPath);
                if(!f.exists() || f.isDirectory())
                    invalidCubes = true;
            }

            while(playAgain) {
                // game setup queries
                int size = 1, numPlayers = 1;
                boolean invalidSize = true;
                boolean invalidPlayers = true;
                
                // game setup + ensuring conditions are met
                while(invalidSize) {
                    size = getInput(in, "\nSize: ");
                    game.newGame(size, numPlayers, cubesPath, dict);
                    invalidSize = size <= 0 || game.getBoard() == null;
                }
                while(invalidPlayers) {
                    numPlayers = getInput(in, "\nNumber of players: ");
                    game.newGame(size, numPlayers, cubesPath, dict);
                    invalidPlayers = numPlayers <= 0;
                }

                HashMap<Integer, Boolean> guessing = new HashMap<Integer, Boolean>(); // for each player, if they are done guessing
                for(int i = 0; i < numPlayers; i++)
                    guessing.put(i, true);
                
                char[][] board = game.getBoard();

                displayGrid(board);
                // start game
                while(guessing.containsValue(true)) {

                    for(int i = 0; i < numPlayers; i++) {
                        // if player still guessing
                        if(guessing.get(i)) {

                            // get and runs player's guess until valid
                            System.out.print("Player " + (i+1) + ", enter your guess or enter '!' to stop guessing: ");
                            String guess = in.nextLine();
                            int score = game.addWord(guess, i);

                            while(!guess.equals("!") && score <= 0) {
                                if(score == 0)
                                    System.out.print("Invalid word, enter another guess or enter '!' to stop guessing: ");
                                if(score == -1)
                                    System.out.print("Word guessed already, enter another guess or enter '!' to stop guessing: ");
                                if(score == -2) // should never happen, means script is wrong
                                    System.out.print("Invalid player, enter another guess or enter '!' to stop guessing: ");
                                guess = in.nextLine();
                                score = game.addWord(guess, i);
                            }

                            // end game if ! or display the word found
                            if(guess.equals("!"))
                                guessing.put(i, false);
                            else {
                                List<Point> lastAddedWord = game.getLastAddedWord();

                                // convert added word to uppercase and display
                                for(int j = 0; j < lastAddedWord.size(); j++) {
                                    int x = (int)lastAddedWord.get(j).getX();
                                    int y = (int)lastAddedWord.get(j).getY();
                                    board[x][y] = Character.toUpperCase(board[x][y]);
                                }
                                
                                displayGrid(board);
                                System.out.println("Player "+(i+1)+" got a score of " + score + ", making total score: " + game.getScores()[i]);
                                int[] scores = game.getScores();
                                int maxScore = 0;
                                for(int p = 0; p < numPlayers; p++) {
                                    if(scores[maxScore] < scores[p])
                                        maxScore = p;
                                }
                                System.out.println("Player "+(maxScore+1)+" is winning with a score of "+scores[maxScore]);

                                // reset board to all lowercase for next query
                                for(int j = 0; j < lastAddedWord.size(); j++) {
                                    int x = (int)lastAddedWord.get(j).getX();
                                    int y = (int)lastAddedWord.get(j).getY();
                                    board[x][y] = Character.toLowerCase(board[x][y]);
                                }
                                displayGrid(board);
                            }
                        }
                    }
                }

                // display player scores
                int[] scores = game.getScores();
                System.out.println("\nThe scores are as follows: ");
                for(int i = 0; i < numPlayers; i++) {
                    System.out.println("Player " + (i+1) + ": " + scores[i]);
                }

                // display all words not chosen by any players
                Collection<String> allWords = game.getAllWords();
                System.out.println("\nAll possible words: ");
                int computerScore = 0;
                for(String s : allWords) {
                    System.out.print(s + " ");
                    computerScore+=s.length()-3;
                }
                System.out.println("\n\nComputer score: "+computerScore);
            
                // query to play again
                String decision = "";
                while(!decision.equals("1") && !decision.equals("2")) {
                    System.out.println("\nGame ended:" +
                                       "\n    1. Play again" +
                                       "\n    2. Exit");
                    decision = in.nextLine();
                    if(!decision.equals("1") && !decision.equals("2"))
                        System.out.println("\nPlease enter a valid input");
                }
                System.out.println();
                playAgain = decision.equals("1") ? true : false;
                game = new GameManager();
            }
        }
        catch(IOException e){
            System.out.println("Threw IO exception " + e);
        }
    }

    /**
     * Displays input board on text UI (prints).
     * Adds surrounding borders for clarity
     */
    public static void displayGrid(char[][] board) {
        if(board == null) return;
        System.out.println(); // for spacing

        for(int i = 0; i < board.length; i++) {
            if(i == 0) {
                System.out.print(" ");
                for(int k = 1; k < board[0].length*2+2; k++) {
                    System.out.print("-");
                }
                System.out.println();
            } 

            for(int j = 0; j < board[0].length; j++) {
                if(j == 0) System.out.print("| ");
                System.out.print(board[i][j] + " ");
                if(j == board[0].length - 1) System.out.print("|");
            }
            System.out.println();

            if((i == board[0].length - 1)) {
                System.out.print(" ");
                for(int k = 1; k < board[0].length*2+2; k++) {
                    System.out.print("-");
                }
                System.out.println();
            } 
        }
    }

    public static int getInput(Scanner in, String prompt) {
        while(true) {
            System.out.print(prompt);
            String s = in.nextLine();
            try{
                int n = Integer.parseInt(s);
                return n;
            }
            catch(Exception e) {
                System.err.println("Please input an integer");
            }
        }
    }
}
