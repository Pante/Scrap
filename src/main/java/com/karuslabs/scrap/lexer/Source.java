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

import java.io.IOException;
import java.io.Reader;


public class Source {
    
    private static final int INITIAL = -2;
    
    private Reader reader;
    private int line;
    private int column;
    private int next;
    
    
    public Source(Reader reader) {
        this.reader = reader;
        line = 0;
        column = 0;
        next = INITIAL;
    }
    
    
    public int peek() throws IOException {
        if (next == INITIAL) {
            next = reader.read();
        }
        
        return next;
    }
    
    
    public int next() throws IOException {
        int current = next;
        next = reader.read();
        
        if (current == '\n' || current == INITIAL) {
            line++;
            column = 0;
            
        } else if (current != -1) {
            column++;
        }
        
        return current;
    }
    
    
    public int line() {
        return line;
    }
    
    public int column() {
        return column;
    }
    
}
