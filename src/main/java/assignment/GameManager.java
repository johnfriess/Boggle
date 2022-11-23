package assignment;

import java.io.*;
import java.util.*;
import java.awt.Point;

public class GameManager implements BoggleGame{
    
    private int size;
    private int numPlayers;
    private char[][] board;
    private int[][] visited;
    private BoggleDictionary dict;
    private Set<String> guessedWords;
    private int[] playerScores;
    private List<Point> lastAddedWord;
    private SearchTactic searchTactic;

    public GameManager() {
        size = 4;
        numPlayers = 1;
        board = null;
        visited = null;
        dict = null;
        guessedWords = null;
        playerScores = null;
        lastAddedWord = null;
        searchTactic = SEARCH_DEFAULT;
    }
    
    public void newGame(int size, int numPlayers, String cubeFile, BoggleDictionary dict) throws IOException {
        this.size = size;
        if(size <= 0) {
            System.err.println("Must be greater than a 0x0 board");
            this.size = 4;
        }
        this.numPlayers = numPlayers;
        if(numPlayers <= 0) {
            System.err.println("Must have at least 1 player");
            this.numPlayers = 1;
        }
        this.dict = dict;
        initializePlayers();
        createBoard(cubeFile);
    }

    private void initializePlayers() {
        guessedWords = new HashSet<>();
        playerScores = numPlayers < 0 ? new int[0] : new int[numPlayers];
        lastAddedWord = null;
    }

    private void createBoard(String cubeFile) {
        board = null;
        visited = null;

        if(cubeFile == null) {
            System.err.println("Cubes file can't be null");
            return;
        }

        List<String> loadedWords = new ArrayList<>(size*size);

        try (Scanner scanner = new Scanner(new File(cubeFile))) {
            while(scanner.hasNextLine()) {
                String cube = scanner.nextLine().trim().toLowerCase();
                if(cube.length() > 0) // no empty cubes allowed
                    loadedWords.add(cube);
            }
        } catch (FileNotFoundException e) {
            System.err.println("Cube file: "+cubeFile+" not found");
            return;
        }

        if(loadedWords.size() < size*size) {
            System.err.println("Not enough cubes in file for size: "+size+"x"+size);
            size = (int)Math.sqrt(loadedWords.size());
            return;
        }

        // randomize cubes and get random side of cube for board
        Collections.shuffle(loadedWords);
        board = new char[size][size];
        visited = new int[size][size];

        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
                int numSides = loadedWords.get(i*size + j).length(); // should be 6 if its a cube but if not for flexibility
                int randomSide = (int) (Math.random() * numSides);
                board[i][j] = loadedWords.get(i*size + j).charAt(randomSide);
            }
        }
    }

    public char[][] getBoard() {
       return board;
    }

    public int addWord(String word, int player) {
        // if no dictionary, not an english word, word is not min 4 letters, word is not on board
        if(dict == null || !dict.contains(word) || word.length() <= 3 || !checkWordOnBoard(word))
            return 0;

        word = word.toLowerCase();

        // a player has already said word
        if(guessedWords.contains(word))
            return -1;

        // invalid player
        if(player < 0 || player >= numPlayers)
            return -2;
        
        int score = word.length() - 3;
        playerScores[player]+=score;
        guessedWords.add(word);

        // values of visited signify where the word is
        lastAddedWord = new ArrayList<>(word.length());
        for(int i = 0; i < word.length(); i++)
            lastAddedWord.add(null);

        for(int i = 0; i < size; i++)
            for(int j = 0; j < size; j++)
                if(visited[i][j] > 0)
                    lastAddedWord.set(visited[i][j]-1, new Point(i,j));
        
        return score;
    }

    private boolean checkWordOnBoard(String word) {
        if(board == null)
            return false;

        word = word.toLowerCase();
        char firstLetter = word.charAt(0);
        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
                if(board[i][j] == firstLetter) {
                    clearVisited();
                    if(boardDfs(word, 0, i, j)) // if word found on board
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * Runs a dfs on the board to find a given word.
     * visited[][] is used to make sure it doesn't revisit nodes but also for finding the outputted path 
     */
    private boolean boardDfs(String word, int idx, int i, int j) {
        // reached end of word meaning word has been found
        if(idx >= word.length())
            return true;

        // if out of bounds, visited or doesn't match word
        if(i < 0 || i >= size || j < 0 || j >= size || visited[i][j] > 0 || board[i][j] != word.charAt(idx))
            return false;

        // update visited and recurse
        visited[i][j] = idx+1;
        for(int ix = -1; ix <= 1; ix++) {
            for(int jx = -1; jx <= 1; jx++) {
                if(ix == 0 && jx == 0)
                    continue;
                if(boardDfs(word, idx+1, i+ix, j+jx))
                    return true;
            }
        }
        // resetting visited[i][j] to false since solution was not found on this path
        // when solution is found, visited would remain true on the solution path
        visited[i][j] = 0;
        return false;
    }

    private void clearVisited() {
        for(int i = 0; i < size; i++)
            for(int j = 0; j < size; j++)
                visited[i][j] = 0;
    }
    
    public List<Point> getLastAddedWord() {
        return lastAddedWord;
    }

    public void setGame(char[][] board) {
        if(board == null || board.length != size || board[0].length != size) {
            System.out.println("Invalid size input board (null or not same as preset size)");
            return;
        }
        for(int i = 0; i < size; i++)
            for(int j = 0; j < size; j++)
                this.board[i][j] = Character.toLowerCase(board[i][j]);
        initializePlayers();
    }

    public Collection<String> getAllWords() {
        Set<String> allWords = new HashSet<>();
        if(board == null)
            return allWords;
        
        if(searchTactic == SearchTactic.SEARCH_BOARD) {
            clearVisited(); // clear visited once, every run auto resets itself

            for(int i = 0; i < size; i++)
                for(int j = 0; j < size; j++) 
                    searchAllWordsDfs(i,j,"",allWords);
        }
        else if(searchTactic == SearchTactic.SEARCH_DICT) {
            for(String word : dict)
                if(word.length() >= 4 && checkWordOnBoard(word))
                    allWords.add(word);
        }
        else
            System.err.println("Invalid search type: "+searchTactic);

        return allWords;
    }

    /**
     * Searches for valid words on board by running a recursive dfs on the board.
     */
    private void searchAllWordsDfs(int i, int j, String word, Set<String> allWords) {
        // if out of bounds end or visited already
        if(i < 0 || i >= size || j < 0 || j >= size || visited[i][j] > 0)
            return;

        // update word
        word+=board[i][j];

        // if word doesn't exist as a prefix in dictionary then stop
        if(!dict.isPrefix(word))
            return;

        // add to set if valid word
        if(word.length() >= 4 && dict.contains(word))
            allWords.add(word);
        
        // update visited, then recurse
        visited[i][j] = 1;
        for(int ix = -1; ix <= 1; ix++)
            for(int jx = -1; jx <= 1; jx++)
                searchAllWordsDfs(i+ix, j+jx, word, allWords);

        visited[i][j] = 0;
    }

    public void setSearchTactic(SearchTactic tactic) {
        searchTactic = tactic;
    }

    public int[] getScores() {
        return playerScores;
    }

}
