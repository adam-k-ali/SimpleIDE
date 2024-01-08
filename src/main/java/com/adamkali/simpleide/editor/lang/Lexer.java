package com.adamkali.simpleide.editor.lang;

import com.adamkali.simpleide.editor.lang.tokens.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Lexes a string into tokens
 */
public class Lexer {
    private static final String[] KEYWORDS = {
            "assert", "break", "case", "catch", "class",
            "continue", "default", "do", "else", "enum", "extends", "finally",
            "for", "goto", "if", "implements", "import", "instanceof", "interface", "native",
            "new", "package", "return", "strictfp",
            "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try",
            "volatile", "while",
            "boolean", "byte", "char", "double", "float", "int", "long", "short", "void",
            "public", "private", "protected",
            "const", "static", "final", "abstract"
    };

    private static final String[] OPERATORS = {
            "+", "-", "*", "/", "%", "++", "--", "==", "!=", ">", "<", ">=", "<=", "&&", "||", "!",
            "&", "|", "^", "~", "<<", ">>", ">>>", "+=", "-=", "*=", "/=", "%=", "&=", "|=", "^=",
            "<<=", ">>=", ">>>="
    };

    private static boolean isKeyword(String word) {
        for (String keyword : KEYWORDS) {
            if (keyword.equals(word)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isOperator(String word) {
        for (String operator : OPERATORS) {
            if (operator.equals(word)) {
                return true;
            }
        }
        return false;
    }


    private static String getAtomicWord(String input, int startIndex) {
        int endIndex = startIndex;
        while (endIndex < input.length() && Character.isLetterOrDigit(input.charAt(endIndex))) {
            endIndex++;
        }

        if (endIndex == startIndex) {
            return String.valueOf(input.charAt(startIndex));
        }

        return input.substring(startIndex, endIndex);
    }

    /**
     * Lexes a string into tokens
     * @param input the string to lex
     * @return the tokens
     */
    public static List<Token> lex(String input) {
        List<Token> tokens = new ArrayList<>();
        String atom = "";
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            atom = String.valueOf(c);
            switch (c) {
                case '\n':
                    tokens.add(new NewLineToken());
                    break;
                case '\t':
                    tokens.add(new TabToken());
                    break;
                case ' ':
                    tokens.add(new WhitespaceToken());
                    break;
                case '/':
                    if (i + 1 < input.length() && input.charAt(i + 1) == '/') {
                        // If the next character is a /, then it is a single line comment
                        i ++;
                        atom += '/';

                        // Get the contents of the comment
                        while (i + 1 < input.length() && input.charAt(i + 1) != '\n') {
                            i++;
                            atom += input.charAt(i);
                        }
                        tokens.add(new SLCommentToken(atom));

//                    } else if (i + 1 < input.length() && input.charAt(i + 1) == '*') {
//                        i ++;
//                        atom += '*';
//
//                        // Get the contents of the comment
//                        while (i + 2 < input.length() && (input.charAt(i + 1) != '*' && input.charAt(i + 2) != '/'))) {
//                            i++;
//                            atom += input.charAt(i);
//                        }
//                        tokens.add(new MLCommentToken(atom));
                    } else {
                        // Not a single line comment, just a plain text token
                        tokens.add(new PlainTextToken(atom));
                    }
                    break;
                default:
                    if (isOperator(atom)) {
                        tokens.add(new OperatorToken(atom));
                        break;
                    }

                    atom = getAtomicWord(input, i);
                    i += atom.length() - 1;
                    if (isKeyword(atom)) {
                        tokens.add(new KeywordToken(atom));
                    } else {
                        tokens.add(new PlainTextToken(atom));
                    }
                    break;
            }
        }

        System.out.println(tokens);
        return tokens;
    }

}
