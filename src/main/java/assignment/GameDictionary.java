package assignment;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.TreeMap;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Iterator;
import java.util.Stack;
import java.util.Collection;
import java.util.Collections;

/**
 * Stores dictionary as trie
 */
public class GameDictionary implements BoggleDictionary {
    private Node root;

    public GameDictionary() {
        root = new Node("");
    }

    public void loadDictionary(String filename) throws IOException {
        if(filename == null) {
            System.err.println("Words file can't be null");
            return;
        }

        try (Scanner scanner = new Scanner(new File(filename))) {
            while(scanner.hasNextLine()) {
                String word = scanner.nextLine().trim().toLowerCase();
                if(word.length() > 0) // no empty words allowed
                    addWord(root, word, 0);
            }
        } catch (FileNotFoundException e) {
            System.err.println("Dictionary file: "+filename+" not found");
        }
    }

    /**
     * Tries to find prefix in trie, if doesn't exist then it not a prefix
     */
    public boolean isPrefix(String prefix) {
        if(prefix == null)
            return false;
        prefix = prefix.toLowerCase();
        Node n = searchWord(root, prefix, 0);
        return n != null;
    }

    /**
     * Tries to find word in trie, if doesn't exist or word found is not a valid word (just a prefix) then it not in the dictionary
     */
    public boolean contains(String word) {
        if(word == null)
            return false;
        word = word.toLowerCase();
        Node n = searchWord(root, word, 0);
        return n != null && n.getValidWord();
    }

    public Iterator<String> iterator() {
        return new DictionaryIterator(root);
    }

    /**
     * Adds word to trie by recursively going through each letter from first to last
     * @param n current node
     * @param word word attempting to add
     * @param idx current index of word that we are processing
     */
    private void addWord(Node n, String word, int idx) {
        if(idx == word.length())
            n.setValidWord();
        else
            addWord(n.addChild(word.charAt(idx)), word, idx+1);
    }

    /**
     * Finds word in trie by recursively going through each letter from first to last
     * @param n current node
     * @param word word attempting to find
     * @param idx current index of word that we are processing
     * @return node found that is the word or null if not found
     */
    private Node searchWord(Node n, String word, int idx) {
        if(n == null || idx == word.length())
            return n;
        return searchWord(n.getChild(word.charAt(idx)), word, idx+1);
    }
    
}

/**
 * Node object that stores children, string and if its a valid word
 */
class Node {
    private TreeMap<Character, Node> children; // TreeMap since we want to iterate in order for the iterator (also size is small so log n is close to 1)
    private String value;
    private boolean validWord;

    public Node(String value) {
        this.value = value;
        validWord = false;
        children = new TreeMap<Character,Node>(Collections.reverseOrder());
    }

    /**
     * Adds the child that would follow this word with char c.
     * If that child already exists then just return pointer to that existing child
     */
    public Node addChild(char c) {
        if(!children.containsKey(c))
            children.put(c, new Node(value + c));
        return children.get(c);
    }

    /**
     * Gets the child that would follow this word with char c.
     * If that child doesn't exist then return null
     */
    public Node getChild(char c) {
        if(children.containsKey(c))
            return children.get(c);
        return null;
    }

    public String getValue() { return value; }

    public void setValidWord() { validWord = true; }

    public boolean getValidWord() { return validWord; }

    public boolean hasChild() { return !children.isEmpty(); }
    
    public Collection<Node> getChildren() { return children.values(); }
}

/**
 * Iterates through trie using a stack
 */
class DictionaryIterator implements Iterator<String> {

    Stack<Node> s;

    public DictionaryIterator(Node root) {
        s = new Stack<>();
        if(root.hasChild())
            s.push(root);
    }

    // since a node only exists if there is a word that the node is a prefix of (or equal to)
    // and the stack has all nodes that have yet to be processed
    // then as long as a node is in the stack, the trie will have a next value
    public boolean hasNext() {
        return !s.empty();
    }

    /**
     * Non-recursive dfs through a stack to get the next value (has to be a valid word).
     * Allows to keep track of how far you have iterated using state of stack
     */
    public String next() throws NoSuchElementException {
        Node curr;
        Collection<Node> children;

        while(!s.empty()) {
            curr = s.pop();

            children = curr.getChildren();
            for(Node n : children)
                s.push(n);

            if(curr.getValidWord())
                return curr.getValue();
        }
        throw new NoSuchElementException();
    }
}