package assignment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import assignment.BoggleGame.SearchTactic;

import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

public class BoggleTesting {
    @Test
    public void dictionaryContainsTest() {
        boolean containsAllWords = true;
        BoggleDictionary d = new GameDictionary();
        Scanner in = null;
        try {
            d.loadDictionary("/Users/jackfriess/Documents/UT Austin/CS 314H/prog5/CS-314H/prog5/words.txt");
            in = new Scanner(new File("/Users/jackfriess/Documents/UT Austin/CS 314H/prog5/CS-314H/prog5/words.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //compare dictionary contains with scanner traversal
        while(in.hasNextLine())
            if(!d.contains(in.nextLine()))
                containsAllWords = false;

        assertTrue(containsAllWords);
    }

    @Test
    public void containsAllPrefixesTest() {
        boolean containsAllPrefixes = true;
        BoggleDictionary d = new GameDictionary();
        try {
            d.loadDictionary("/Users/jackfriess/Documents/UT Austin/CS 314H/prog5/CS-314H/prog5/words.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }

        //check if each substring for every word in the dictionary is a valid prefix
        for(String word : d) {
            for(int i = 0; i < word.length(); i++) {
                if(!d.isPrefix(word.substring(0, i+1)))
                containsAllPrefixes = false;
            }
        }

        assertTrue(containsAllPrefixes);
    }

    @Test
    public void boardEqualityTest() {
        boolean boardEquals = true;
        BoggleDictionary d = new GameDictionary();
        BoggleGame g = new GameManager();
        try {
            d.loadDictionary("/Users/jackfriess/Documents/UT Austin/CS 314H/prog5/CS-314H/prog5/words.txt");
            g.newGame(4, 2, "/Users/jackfriess/Documents/UT Austin/CS 314H/prog5/CS-314H/prog5/cubes.txt", d);
        } catch (IOException e) {
            e.printStackTrace();
        }
        char[][] board = {
            {'p', 'a', 'a', 'a'},
            {'a', 'l', 'a', 'a'},
            {'a', 'a', 'a', 'a'},
            {'a', 'a', 'a', 'n'}
        };
        g.setGame(board);
        char[][] result = g.getBoard();

        //comapre if each element in the generated board is equal to each element in the resulting board
        for(int i = 0; i < result.length; i++) {
            for(int j = 0; j < result[0].length; j++) {
                if(board[i][j] != result[i][j])
                    boardEquals= false;
            }
        }

        assertTrue(boardEquals);
    }

    @Test
    public void addWordTest() {
        boolean allPointsValid = true;
        BoggleDictionary d = new GameDictionary();
        BoggleGame g = new GameManager();
        try {
            d.loadDictionary("/Users/jackfriess/Documents/UT Austin/CS 314H/prog5/CS-314H/prog5/words.txt");
            g.newGame(4, 2, "/Users/jackfriess/Documents/UT Austin/CS 314H/prog5/CS-314H/prog5/cubes.txt", d);
        } catch (IOException e) {
            e.printStackTrace();
        }
        char[][] board = {
            {'p', 'a', 'a', 'a'},
            {'a', 'l', 'a', 'a'},
            {'a', 'a', 'a', 'a'},
            {'a', 'a', 'a', 'n'}
        };
        g.setGame(board);
        g.addWord("plan", 1);
        List<Point> points = new ArrayList<Point>();
        points.add(new Point(3, 3));
        points.add(new Point(1,1));
        points.add(new Point(2, 2));
        points.add(new Point(0, 0));

        List<Point> theirPoints = g.getLastAddedWord();
        
        //check if each hardcoded point has an equal point in getLastAddedWord()
        for(Point p : points) {
            boolean samePointExists = false;
            for(Point t : theirPoints) {
                if(p.equals(t))
                    samePointExists = true;
            }
            if(!samePointExists) {
                allPointsValid = false;
            }
        }
        assertTrue(allPointsValid);
    }

    @Test
    public void iteratorTest() {
        BoggleDictionary d = new GameDictionary();
        BoggleGame g = new GameManager();
        try {
            d.loadDictionary("/Users/jackfriess/Documents/UT Austin/CS 314H/prog5/CS-314H/prog5/words.txt");
            g.newGame(4, 2, "/Users/jackfriess/Documents/UT Austin/CS 314H/prog5/CS-314H/prog5/cubes.txt", d);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //check if the iterator reaches every line in words.txt (113809 lines)
        int count = 0;
        for(String s : d) {
            count++;
        }
        assertEquals(count, 113809);
    }

    @Test
    public void searchDictTest() {
        BoggleDictionary d = new GameDictionary();
        BoggleGame g = new GameManager();
        try {
            d.loadDictionary("/Users/jackfriess/Documents/UT Austin/CS 314H/prog5/CS-314H/prog5/words.txt");
            g.newGame(4, 2, "/Users/jackfriess/Documents/UT Austin/CS 314H/prog5/CS-314H/prog5/cubes.txt", d);
        } catch (IOException e) {
            e.printStackTrace();
        }

        g.setSearchTactic(SearchTactic.SEARCH_DICT);

        char[][] board = {
            {'p', 'a', 'a', 'a'},
            {'a', 'l', 'a', 'a'},
            {'a', 'a', 'a', 'e'},
            {'a', 'a', 'a', 'k'}
        };
        g.setGame(board);

        Collection<String> allWords = g.getAllWords();

        //check if all the possible words that we expect on this board are actually met
        int count = 0;
        for(String s : allWords) {
            if(s.equals("alae") || s.equals("kalpa") || s.equals("lake"))
                count++;
        }
        assertEquals(count, 3);
    }

    @Test
    public void searchBoardTest() {
        BoggleDictionary d = new GameDictionary();
        BoggleGame g = new GameManager();
        try {
            d.loadDictionary("/Users/jackfriess/Documents/UT Austin/CS 314H/prog5/CS-314H/prog5/words.txt");
            g.newGame(4, 2, "/Users/jackfriess/Documents/UT Austin/CS 314H/prog5/CS-314H/prog5/cubes.txt", d);
        } catch (IOException e) {
            e.printStackTrace();
        }

        g.setSearchTactic(SearchTactic.SEARCH_BOARD);

        char[][] board = {
            {'p', 'a', 'a', 'a'},
            {'a', 'l', 'a', 'a'},
            {'a', 'a', 'a', 'e'},
            {'a', 'a', 'a', 'k'}
        };
        g.setGame(board);

        Collection<String> allWords = g.getAllWords();

        //check if all the possible words that we expect on this board are actually met
        int count = 0;
        for(String s : allWords) {
            if(s.equals("alae") || s.equals("kalpa") || s.equals("lake"))
                count++;
        }
        assertEquals(count, 3);
    }

    @Test
    public void doubleCharNearby() {
        boolean containsDuplicateCharacters = false;
        BoggleDictionary d = new GameDictionary();
        BoggleGame g = new GameManager();
        try {
            d.loadDictionary("/Users/jackfriess/Documents/UT Austin/CS 314H/prog5/CS-314H/prog5/words.txt");
            g.newGame(4, 2, "/Users/jackfriess/Documents/UT Austin/CS 314H/prog5/CS-314H/prog5/cubes.txt", d);
        } catch (IOException e) {
            e.printStackTrace();
        }
        char[][] board = {
            {'p', 'a', 'a', 'a'},
            {'a', 'l', 'a', 'a'},
            {'a', 'a', 'a', 'a'},
            {'a', 'a', 'a', 'n'}
        };
        g.setGame(board);
        g.addWord("plan", 1);
        List<Point> points = new ArrayList<Point>();
        points.add(new Point(3, 3));
        points.add(new Point(1,1));
        points.add(new Point(2, 2));
        points.add(new Point(0, 0));

        List<Point> theirPoints = g.getLastAddedWord();
        
        //check if each character at a hardcoded point corresponds to the same character at different points from getLastAddedWord() multiple times
        for(Point p : points) {
            int count = 0;
            for(Point t : theirPoints) {
                if(board[(int)p.getX()][(int)p.getY()] == board[(int)t.getX()][(int)t.getY()])
                    count++;
            }
            if(count > 1) {
                containsDuplicateCharacters = true;
            }
        }
        assertTrue(!containsDuplicateCharacters);
    }
}
