package com.lakeside.core.io;

import java.io.*;

/**
 *
 * BoundedBufferedReader that limit the line length when read from a file.
 * @dejun
 */
public class BoundedBufferedReader extends BufferedReader {
    private static final int DEFAULT_MAX_LINE_LENGTH = Integer.MAX_VALUE;    //Max bytes per line
    private static final int DEFAULT_EXPECTED_LINE_LENGTH = 80;
    private int readerMaxLineLen;

    public BoundedBufferedReader(Reader reader, int maxLineLen) {
        super(reader);
        if ((maxLineLen <= 0))
            throw new IllegalArgumentException("BoundedBufferedReader - maxLines and maxLineLen must be greater than 0");
        readerMaxLineLen = maxLineLen;
    }

    public BoundedBufferedReader(Reader reader) {
        super(reader);
        readerMaxLineLen = DEFAULT_MAX_LINE_LENGTH;
    }

    public String readLine() throws IOException {
        StringBuffer s = null;
        long currentPos = 0;
        final int CR = 13;
        final int LF = 10;
        int currentCharVal = super.read();

        //Read characters and add them to the data buffer until we hit the end of a line or the end of the file.
        while ((currentCharVal != CR) && (currentCharVal != LF) && (currentCharVal >= 0)) {
            if (s == null)
                s = new StringBuffer(DEFAULT_EXPECTED_LINE_LENGTH);
            if (currentPos < readerMaxLineLen) {
                s.append((char) currentCharVal);
            }
            //Check readerMaxLineLen limit
            currentCharVal = super.read();
            currentPos++;
        }

        if (currentCharVal < 0) {
            //End of file
            if (s != null)
                //Return last line
                return (s.toString());
            else
                return null;
        } else {
            //Remove newline characters from the buffer
            if (currentCharVal == CR) {
                //Check for LF and remove from buffer
                super.mark(1);
                if (super.read() != LF)
                    super.reset();
            } else if (currentCharVal != LF) {
                //readerMaxLineLen has been hit, but we still need to remove newline characters.
                super.mark(1);
                int nextCharVal = super.read();
                if (nextCharVal == CR) {
                    super.mark(1);
                    if (super.read() != LF)
                        super.reset();
                } else if (nextCharVal != LF)
                    super.reset();
            }
            if (s != null)
                return (s.toString());
            else
                return "";
        }

    }
}