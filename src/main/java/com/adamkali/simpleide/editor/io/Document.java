package com.adamkali.simpleide.editor.io;

import com.adamkali.simpleide.editor.lang.Lexer;
import com.adamkali.simpleide.editor.lang.tokens.Token;
import com.adamkali.simpleide.editor.lang.tokens.character.NewLineToken;

import java.util.ArrayList;
import java.util.List;

public class Document {
    private List<Line> lines;

    public Document() {
        lines = new ArrayList<>() {{
            add(new Line());
        }};
//        lines = new ArrayList<>() {{
//            add(new Line() {{ addToken(new NewLineToken()); }});
//            add(new Line() {{ addToken(new SLCommentToken("// This is a comment")); addToken(new NewLineToken());  }});
//            add(new Line() {{ addToken(new KeywordToken("for")); addToken(new WhitespaceToken()); addToken(new UndefinedToken("test")); }});
//        }};
    }

    public List<Line> getLines() {
        return lines;
    }

    /**
     * Inserts a new line after the given line.
     *
     * @param lineBefore The line to insert the new line after.
     */
    public void newLine(int lineBefore) {
        if (lineBefore < 0 || lineBefore >= numberOfLines()) {
            throw new IndexOutOfBoundsException("Line index out of bounds");
        }

        lines.add(lineBefore + 1, new Line());
    }

    public Line getLine(int line) {
        if (line < 0 || line >= numberOfLines()) {
            throw new IndexOutOfBoundsException("Line index out of bounds");
        }
        return lines.get(line);
    }

    public void removeToken(int line, int index) {
        if (line < 0 || line >= numberOfLines()) {
            throw new IndexOutOfBoundsException("Line index out of bounds");
        }
        lines.get(line).removeToken(index);
    }

    /**
     * Removes a line from the document.
     *
     * @param line Line number
     */
    public void removeLine(int line) {
        if (line < 0 || line >= numberOfLines()) {
            throw new IndexOutOfBoundsException("Line index out of bounds");
        }
        lines.remove(line);
    }

    public int numberOfLines() {
        return lines.size();
    }

    public static class Line {
        private List<Token> tokens;

        public Line() {
            tokens = new ArrayList<>();
        }

        public List<Token> getTokens() {
            return tokens;
        }

        public void rewrite(String newText) {
            List<Token> newTokens = Lexer.lex(newText);
            // Check there's no newline in the middle of the line
            for (int index = 0; index < newTokens.size(); index++) {
                if (newTokens.get(index) instanceof NewLineToken && index != newTokens.size() - 1) {
                    throw new IllegalArgumentException("Newline in the middle of the line");
                }
            }
            tokens = newTokens;
        }

        public void append(String textToAppend) {
            rewrite(this + textToAppend);
        }

        public void insert(int index, String textToInsert) {
            rewrite(toString().substring(0, index) + textToInsert + toString().substring(index));
        }

        public void addToken(Token token) {
            tokens.add(token);
        }

        public void removeToken(int index) {
            tokens.remove(index);
        }

        /**
         * Returns the string length of the line.
         *
         * @return
         */
        public int length() {
            int length = 0;
            for (Token token : tokens) {
                length += token.getText().length();
            }
            return length;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (Token token : tokens) {
                sb.append(token.getText());
            }
            return sb.toString();
        }

        public String substring(int start, int end) {
            if (start < 0 || end < 0 || start > end || end > length()) {
                throw new IndexOutOfBoundsException("Index out of bounds");
            }
            return toString().substring(start, end);
        }
    }
}
