package com.adamkali.simpleide.editor.lang;

import com.adamkali.simpleide.editor.lang.tokens.*;
import com.adamkali.simpleide.editor.lang.tokens.character.NewLineToken;
import com.adamkali.simpleide.editor.lang.tokens.character.WhitespaceToken;
import com.adamkali.simpleide.editor.lang.tokens.literal.StringToken;
import com.adamkali.simpleide.editor.lang.tokens.operator.AssignmentToken;
import com.adamkali.simpleide.editor.lang.tokens.operator.DotToken;
import com.adamkali.simpleide.editor.lang.tokens.operator.OperatorToken;
import com.adamkali.simpleide.editor.lang.tokens.operator.SemicolonToken;
import com.adamkali.simpleide.editor.lang.tokens.operator.logic.bit.*;
import com.adamkali.simpleide.editor.lang.tokens.operator.logic.bit.assignment.AndEqualsToken;
import com.adamkali.simpleide.editor.lang.tokens.operator.logic.bit.assignment.OrEqualsToken;
import com.adamkali.simpleide.editor.lang.tokens.operator.logic.comparison.*;
import com.adamkali.simpleide.editor.lang.tokens.operator.math.*;
import com.adamkali.simpleide.editor.lang.tokens.operator.math.assignment.*;

import java.util.ArrayList;
import java.util.HashMap;
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
    private static final HashMap<String, Class<? extends OperatorToken>> OPERATOR_TOKENS = new HashMap<>() {{
        put(".", DotToken.class);
        put(";", SemicolonToken.class);
        put("=", AssignmentToken.class);
        put("+", AdditionToken.class);
        put("-", SubtractionToken.class);
        put("*", MultiplicationToken.class);
        put("/", DivisionToken.class);
        put("%", ModuloToken.class);
        put("++", IncrementToken.class);
        put("--", DecrementToken.class);
        put("==", EqualityToken.class);
        put("!=", NEqualityToken.class);
        put(">", GTToken.class);
        put("<", LTToken.class);
        put(">=", GTEqToken.class);
        put("<=", LTEqToken.class);
        put("&&", AndToken.class);
        put("||", OrToken.class);
        put("!", NotToken.class);
        put("&", BitAndToken.class);
        put("|", BitOrToken.class);
        put("^", BitXorToken.class);
        put("~", BitNotToken.class);
        put("<<", BitShiftLeftToken.class);
        put(">>", BitShiftRightToken.class);
        put(">>>", BitURightShiftToken.class);
        put("+=", PlusEqualsToken.class);
        put("-=", MinusEqualsToken.class);
        put("*=", StarEqualsToken.class);
        put("/=", DivideEqualsToken.class);
        put("%=", ModEqualsToken.class);
        put("&=", AndEqualsToken.class);
        put("|=", OrEqualsToken.class);
    }};

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
     *
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
                        i++;
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
                        tokens.add(new DivisionToken());
                    }
                    break;
                case '"':
                    // Get the contents of the string
                    boolean isValid = true;
                    while (i + 1 < input.length() && input.charAt(i + 1) != '"') {
                        i++;
                        atom += input.charAt(i);
                    }
                    if (i + 1 < input.length()) {
                        i++;
                        atom += input.charAt(i);
                    } else {
                        // If the string is not closed, then it isn't valid
                        isValid = false;
                    }
                    StringToken stringToken = new StringToken(atom);
                    stringToken.setValid(isValid);
                    tokens.add(stringToken);
                    break;
                default:
                    if (OPERATOR_TOKENS.containsKey(atom)) {
                        // Operators can be up to 3 characters long, so we need to check for that
                        if (i + 1 < input.length()) {
                            String nextAtom = atom + input.charAt(i + 1);
                            if (OPERATOR_TOKENS.containsKey(nextAtom)) {
                                atom = nextAtom;
                                i++;
                            }
                            if (i + 1 < input.length()) {
                                nextAtom = atom + input.charAt(i + 1);
                                if (OPERATOR_TOKENS.containsKey(nextAtom)) {
                                    atom = nextAtom;
                                    i++;
                                }
                            }
                        }
                        try {
                            tokens.add(OPERATOR_TOKENS.get(atom).getDeclaredConstructor().newInstance());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                    if (isOperator(atom)) {
                        tokens.add(new OperatorToken(atom));
                        break;
                    }

                    atom = getAtomicWord(input, i);
                    i += atom.length() - 1;
                    if (isKeyword(atom)) {
                        tokens.add(new KeywordToken(atom));
                    } else {
                        tokens.add(new UndefinedToken(atom));
                    }
                    break;
            }
        }

        System.out.println(tokens);
        return tokens;
    }

}
