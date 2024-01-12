package com.adamkali.simpleide.editor.lang;

import com.adamkali.simpleide.editor.lang.tokens.IdentifierToken;
import com.adamkali.simpleide.editor.lang.tokens.SLCommentToken;
import com.adamkali.simpleide.editor.lang.tokens.TabToken;
import com.adamkali.simpleide.editor.lang.tokens.Token;
import com.adamkali.simpleide.editor.lang.tokens.character.*;
import com.adamkali.simpleide.editor.lang.tokens.keyword.KeywordToken;
import com.adamkali.simpleide.editor.lang.tokens.keyword.exception.*;
import com.adamkali.simpleide.editor.lang.tokens.keyword.flow.*;
import com.adamkali.simpleide.editor.lang.tokens.keyword.literal.FalseKeywordToken;
import com.adamkali.simpleide.editor.lang.tokens.keyword.literal.NullKeywordToken;
import com.adamkali.simpleide.editor.lang.tokens.keyword.literal.TrueKeywordToken;
import com.adamkali.simpleide.editor.lang.tokens.keyword.modifier.*;
import com.adamkali.simpleide.editor.lang.tokens.keyword.pckg.ImportKeywordToken;
import com.adamkali.simpleide.editor.lang.tokens.keyword.pckg.PackageKeywordToken;
import com.adamkali.simpleide.editor.lang.tokens.keyword.reference.SuperKeywordToken;
import com.adamkali.simpleide.editor.lang.tokens.keyword.reference.ThisKeywordToken;
import com.adamkali.simpleide.editor.lang.tokens.keyword.scope.PrivateKeywordToken;
import com.adamkali.simpleide.editor.lang.tokens.keyword.scope.ProtectedKeywordToken;
import com.adamkali.simpleide.editor.lang.tokens.keyword.scope.PublicKeywordToken;
import com.adamkali.simpleide.editor.lang.tokens.keyword.type.*;
import com.adamkali.simpleide.editor.lang.tokens.literal.DoubleToken;
import com.adamkali.simpleide.editor.lang.tokens.literal.FloatToken;
import com.adamkali.simpleide.editor.lang.tokens.literal.StringToken;
import com.adamkali.simpleide.editor.lang.tokens.operator.AssignmentToken;
import com.adamkali.simpleide.editor.lang.tokens.operator.DotToken;
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
    private static final HashMap<String, Class<? extends KeywordToken>> KEYWORD_TOKENS = new HashMap<>() {{
        put("boolean", BooleanKeywordToken.class);
        put("byte", ByteKeywordToken.class);
        put("char", CharKeywordToken.class);
        put("double", DoubleKeywordToken.class);
        put("float", FloatKeywordToken.class);
        put("int", IntKeywordToken.class);
        put("long", LongKeywordToken.class);
        put("short", ShortKeywordToken.class);
        put("void", VoidKeywordToken.class);

        put("public", PublicKeywordToken.class);
        put("private", PrivateKeywordToken.class);
        put("protected", ProtectedKeywordToken.class);

        put("const", ConstKeywordToken.class);
        put("static", StaticKeywordToken.class);
        put("final", FinalKeywordToken.class);
        put("abstract", AbstractKeywordToken.class);
        put("volatile", VolatileKeywordToken.class);
        put("synchronized", SynchronizedKeywordToken.class);
        put("transient", TransientKeywordToken.class);
        put("native", NativeKeywordToken.class);
        put("strictfp", StrictFPKeywordToken.class);
        put("class", ClassKeywordToken.class);
        put("interface", InterfaceKeywordToken.class);
        put("enum", EnumKeywordToken.class);
        put("extends", ExtendsKeywordToken.class);
        put("implements", ImplementsKeywordToken.class);
        put("new", NewKeywordToken.class);

        put("package", PackageKeywordToken.class);
        put("import", ImportKeywordToken.class);

        put("true", TrueKeywordToken.class);
        put("false", FalseKeywordToken.class);
        put("null", NullKeywordToken.class);

        put("assert", AssertKeywordToken.class);
        put("catch", CatchKeywordToken.class);
        put("finally", FinallyKeywordToken.class);
        put("throw", ThrowKeywordToken.class);
        put("throws", ThrowsKeywordToken.class);
        put("try", TryKeywordToken.class);

        put("break", BreakKeywordToken.class);
        put("case", CaseKeywordToken.class);
        put("continue", ContinueKeywordToken.class);
        put("default", DefaultKeywordToken.class);
        put("do", DoKeywordToken.class);
        put("else", ElseKeywordToken.class);
        put("for", ForKeywordToken.class);
        put("goto", GoToKeywordToken.class);
        put("if", IfKeywordToken.class);
        put("instanceof", InstanceofKeywordToken.class);
        put("return", ReturnKeywordToken.class);
        put("switch", SwitchKeywordToken.class);
        put("while", WhileKeywordToken.class);

        put("super", SuperKeywordToken.class);
        put("this", ThisKeywordToken.class);
    }};

    private static final HashMap<String, Class<? extends Token>> OPERATOR_TOKENS = new HashMap<>() {{
        put("(", LParenToken.class);
        put(")", RParenToken.class);
        put("{", LBraceToken.class);
        put("}", RBraceToken.class);
        put("[", LBracketToken.class);
        put("]", RBracketToken.class);
        put(",", CommaToken.class);
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

    private static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
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

                    atom = getAtomicWord(input, i);
                    i += atom.length() - 1;
                    if (KEYWORD_TOKENS.containsKey(atom)) {
                        try {
                            tokens.add(KEYWORD_TOKENS.get(atom).getDeclaredConstructor().newInstance());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        // If the word is followed by a ., then it might be a decimal number
                        if (i + 1 < input.length() && input.charAt(i + 1) == '.' && Character.isDigit(atom.charAt(0))) {
                            i++;
                            atom += '.';
                            while (i + 1 < input.length() && Character.isDigit(input.charAt(i + 1))) {
                                i++;
                                atom += input.charAt(i);
                            }
                            // If the number ends with F, then it is a float, otherwise it is a double
                            if (i + 1 < input.length() && input.charAt(i + 1) == 'F') {
                                i++;
                                atom += 'F';
                                tokens.add(new FloatToken(atom));
                            } else {
                                tokens.add(new DoubleToken(atom));
                            }

                        } else {
                            tokens.add(new IdentifierToken(atom));
                        }
                    }
                    break;
            }
        }

        return tokens;
    }

}
