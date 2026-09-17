package test;

/**
 * Highlighter kitchen: comments, strings, operators, and tabs.
 */
public class SyntaxKitchen {
    // single-line comment for lexer QA
    public static final String GREETING = "hello, kitchen";

    public static int mix(int x, int y) {
	int tabIndented = x + y * 2;
        boolean ok = x >= 0 && y != 1 || !false;
        int bits = x & y | ~x ^ (y << 1);
        if (ok) {
            System.out.println(GREETING + "\t" + tabIndented + bits);
        }
        return tabIndented;
    }
}
