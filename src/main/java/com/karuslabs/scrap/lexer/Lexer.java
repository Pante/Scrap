/*
 * The MIT License
 *
 * Copyright 2019 Karus Labs.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.karuslabs.scrap.lexer;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import org.checkerframework.checker.nullness.qual.Nullable;

import static com.karuslabs.scrap.lexer.Type.*;


public class Lexer {
    
    private static final String DIGITS = "(\\p{Digit}+)";
    private static final String HEX = "(\\p{XDigit}+)";
    private static final String EXP = "[eE][+-]?" + DIGITS;
    public static final Pattern DOUBLE = Pattern.compile("[\\x00-\\x20]*[+-]?(NaN|Infinity|"
            + "(((" + DIGITS + "(\\.)?(" + DIGITS + "?)(" + EXP + ")?)|"
            + "(\\.(" + DIGITS + ")(" + EXP + ")?)|(((0[xX]" + HEX + "(\\.)?)|"
            + "(0[xX]" + HEX + "?(\\.)" + HEX + "))[pP][+-]?" + DIGITS + "))"
            + "[fFdD]?))[\\x00-\\x20]*");
    
    private Source source;
    private List<String> errors;
    
    
    public Lexer(Source source) {
        this.source = source;
        this.errors = new ArrayList<>();
    }
    
    
    public @Nullable Token next() {
        try {
            source.skip();
            return lex();
            
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    protected @Nullable Token lex() throws IOException {
        Type type = null;
        switch (source.peek()) {
            case -1:
                return null;
                
            case '+':
                return op(PLUS);
            case '-':
                return op(MINUS);
            case '*':
                return op(MULTIPLY);
            case '/':
                return op(DIVIDE);
            case '=':
                source.next();
                if (source.peek() == '=') {
                    return op(EQUAL);
                }
                return op(ASSIGNMENT);
                
            default:
                return variable();
        }
    }
    
    
    protected Token op(Type type) throws IOException {
        source.next();
        return new Token(type, null, source.line(), source.column(), source.column());
    }
    
    protected Token variable() throws IOException {
        if (source.peek() == '"' || source.peek() == '\'') {
            return string();
        }
        
        var start = source.column();
        
        if (Character.isAlphabetic(source.peek())) {
            return new Token(IDENTIFIER, source.read(), source.line(), start, source.column());
        }
        
        var value = source.read();
        if (DOUBLE.matcher(value).matches()) {
            return new Token(LITERAL_DOUBLE, Double.parseDouble(value), source.line(), start, source.column());
        } else {
            return error(value);
        }
    }
            
    
    protected Token string() throws IOException {
        var builder = new StringBuilder();
        var escaped = false;
        char enclosing = (char) source.next();
        var start = source.column();
        
        while (true) {
            int c = source.peek();
            switch (c) {
                case '"':
                    if (!escaped && enclosing == '"') {
                        return new Token(LITERAL_STRING, builder.toString(), source.line(), start, source.column());
                    }
                    
                case '\'':
                    if (!escaped && enclosing == '\'') {
                        return new Token(LITERAL_STRING, builder.toString(), source.line(), start, source.column());
                    }
                
                case '\n':
                case -1:
                    return error("Unclosed string: " + enclosing, builder.toString());
                    
                case '\\':
                    escaped = !escaped;
                    
                default:
                    source.next();
                    builder.append((char) c);
            }
        }
    }

    
    public Token error(String starting) throws IOException {
        return error("Unknown token: ", starting);
    }
    
    public Token error(String type, String starting) throws IOException {
        var builder = new StringBuilder(type).append(starting);
        var start = source.column();
        while (true) {
            switch (source.peek()) {
                case ' ':
                case '\n':
                case ';':
                case -1:
                    var message = builder.append(" << at line ").append(source.line()).append(" column ").append(start).toString();
                    return new Token(Type.LEX_ERROR, message, source.line(), start, source.column());
                
                default:
                    builder.append((char) source.next());
            }
        }
    }
    
}
