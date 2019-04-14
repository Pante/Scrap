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

import org.checkerframework.checker.nullness.qual.Nullable;


public class Lexer {
    
    private Source source;
    private List<String> errors;
    
    
    public Lexer(Source source) {
        this.source = source;
        this.errors = new ArrayList<>();
    }
    
    
    public @Nullable Token next() {
        try {
            skip();
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
                return op(Type.PLUS);
            case '-':
                return op(Type.MINUS);
            case '*':
                return op(Type.MULTIPLY);
            case '/':
                return op(Type.DIVIDE);
                
            default:
                return error();
        }
    }
    
    protected Token op(Type type) {
        return new Token(type, null, source.line(), source.column(), source.column());
    }
    
    
    public void skip() throws IOException {
        while (true) {
            switch (source.peek()) {
                case ' ':
                case '\n':
                    source.next();
                    break;
                    
                default:
                    return;
            }
        }
    }
        
    
    public Token error() throws IOException {
        var builder = new StringBuilder("Unknown lexeme: ");
        var start = source.column();
        while (true) {
            switch (source.peek()) {
                case ' ':
                case '\n':
                case ';':
                case -1:
                    var message = builder.append("<< at line ").append(source.line()).append(" column ").append(start).toString();
                    return new Token(Type.ERROR, message, source.line(), start, source.column());
                
                default:
                    builder.append((char) source.next());
            }
        }
    }
    
}
