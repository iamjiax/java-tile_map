package bearmaps.proj2c;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class MyTrieSet{
    private TrieNode root = new TrieNode('0', false);  // root of trie
    private int n;      // number of keys in trie

    // HashMap trie node
    private static class TrieNode {
        private char ch;
        private boolean isKey;
        private HashMap<Character, TrieNode> map;
        private TrieNode(char c, boolean b) {
            ch = c;
            isKey = b;
            map = new HashMap();
        }
    }

    /** Clears all items out of Trie */
    public void clear() {
        root = new TrieNode('0', false);
    }

    /** Returns true if the Trie contains KEY, false otherwise */
    public boolean contains(String key){
        if (key == null || key.length() < 1) {
            return false;
        }
        TrieNode curr = nodeOfLastChar(key);
        if (curr != null && curr.isKey) {
            return true;
        }
        return false;
    }

    /**
     *  Find the node of the last char of string.
     *  If the Trie doesn't contain string, return null;
     */
    private TrieNode nodeOfLastChar(String str) {
        TrieNode curr = root;
        for (int i = 0, n = str.length(); i < n; i++) {
            char c = str.charAt(i);
            if (!curr.map.containsKey(c)) {
                return null;
            }
            curr = curr.map.get(c);
        }
        return curr;
    }

    /** Inserts string KEY into Trie */
    public void add(String key) {
        if (key == null || key.length() < 1) {
            return;
        }
        TrieNode curr = root;
        for (int i = 0, n = key.length(); i < n; i++) {
            char c = key.charAt(i);
            if (!curr.map.containsKey(c)) {
                curr.map.put(c, new TrieNode(c, false));
            }
            curr = curr.map.get(c);
        }
        curr.isKey = true;
        n += 1;
    }

    /** Returns a list of all words that start with PREFIX */
    public List<String> keysWithPrefix(String prefix) {
        if (prefix == null || prefix.length() < 1) {
            throw new IllegalArgumentException("Call keysWithPrefix() with null argument.");
        }
        List<String> returnStrings = new LinkedList<>();
        TrieNode curr = nodeOfLastChar(prefix);
        if (curr != null) {
            kWPHelp(prefix, returnStrings, curr);
        }
        return returnStrings;
    }

    private void kWPHelp(String prefix, List<String> strings, TrieNode node) {
        if (node.isKey) {
            strings.add(prefix);
        }
        for (char c : node.map.keySet()) {
            TrieNode curr = node.map.get(c);
            kWPHelp(prefix + c, strings, curr);
        }
    }

    public static void main(String[] args) {
        MyTrieSet t = new MyTrieSet();
        t.add("hello");
        t.add("hi");
        t.add("help");
        t.add("zebra");
    }
}
