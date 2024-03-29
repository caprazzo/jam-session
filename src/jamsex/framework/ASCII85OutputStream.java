package jamsex.framework;

/*
 * Copyright (c) 2009-2010, i Data Connect!
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the copyright holder nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.spaceroots.jarmor.ASCII85Encoder;

/**
 * This class represents an ASCII85 output stream.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.7 $
 */
public class ASCII85OutputStream extends FilterOutputStream
{

    private int lineBreak;
    private int count;

    private byte[] indata;
    private byte[] outdata;

    /**
     * Function produces five ASCII printing characters from
     * four bytes of binary data.
     */
    private int maxline;
    private boolean flushed;
    private char terminator;

    /**
     * Constructor.
     *
     * @param out The output stream to write to.
     */
    public ASCII85OutputStream( OutputStream out )
    {
        super( out );
        lineBreak = 36*2;
        maxline = 36*2;
        count=0;
        indata=new byte[4];
        outdata=new byte[5];
        flushed=true;
        terminator='~';
    }

    /**
     * This will set the terminating character.
     *
     * @param term The terminating character.
     */
    public void setTerminator(char term)
    {
        if(term<118 || term>126 || term=='z')
        {
            throw new IllegalArgumentException("Terminator must be 118-126 excluding z");
        }
        terminator=term;
    }

    /**
     * This will get the terminating character.
     *
     * @return The terminating character.
     */
    public char getTerminator()
    {
        return terminator;
    }

    /**
     * This will set the line length that will be used.
     *
     * @param l The length of the line to use.
     */
    public void setLineLength(int l)
    {
        if( lineBreak > l )
        {
            lineBreak = l;
        }
        maxline=l;
    }

    /**
     * This will get the length of the line.
     *
     * @return The line length attribute.
     */
    public int getLineLength()
    {
        return maxline;
    }

    /**
     * This will transform the next four ascii bytes.
     */
    private final void  transformASCII85()
    {
        long word;
        word=( (((indata[0] << 8) | (indata[1] &0xFF)) << 16) |
        ( (indata[2] & 0xFF) << 8) | (indata[3] & 0xFF)
        )    & 0xFFFFFFFFL;
        // System.out.println("word=0x"+Long.toString(word,16)+" "+word);

        if (word == 0 )
        {
            outdata[0]=(byte)'z';
            outdata[1]=0;
            return;
        }
        long x;
        x=word/(85L*85L*85L*85L);
        // System.out.println("x0="+x);
        outdata[0]=(byte)(x+'!');
        word-=x*85L*85L*85L*85L;

        x=word/(85L*85L*85L);
        // System.out.println("x1="+x);
        outdata[1]=(byte)(x+'!');
        word-=x*85L*85L*85L;

        x=word/(85L*85L);
        // System.out.println("x2="+x);
        outdata[2]=(byte)(x+'!');
        word-=x*85L*85L;

        x=word/85L;
        // System.out.println("x3="+x);
        outdata[3]=(byte)(x+'!');

        // word-=x*85L;

        // System.out.println("x4="+(word % 85L));
        outdata[4]=(byte)((word%85L)+'!');
    }

    /**
     * This will write a single byte.
     *
     * @param b The byte to write.
     *
     * @throws IOException If there is an error writing to the stream.
     */
    public final void write(int b) throws IOException
    {
        flushed=false;
        indata[count++]=(byte)b;
        if(count < 4 )
        {
            return;
        }
        transformASCII85();
        for(int i=0;i<5;i++)
        {
            if(outdata[i]==0)
            {
                break;
            }
            out.write(outdata[i]);
            if(--lineBreak==0)
            {
                out.write('\n');
                lineBreak=maxline;
            }
        }
        count = 0;
    }

    /**
     * This will write a chunk of data to the stream.
     *
     * @param b The byte buffer to read from.
     * @param off The offset into the buffer.
     * @param sz The number of bytes to read from the buffer.
     *
     * @throws IOException If there is an error writing to the underlying stream.
     */
    public final void write(byte[] b,int off, int sz) throws IOException
    {
        for(int i=0;i<sz;i++)
        {
            if(count < 3)
            {
                indata[count++]=b[off+i];
            }
            else
            {
                write(b[off+i]);
            }
        }
    }

    /**
     * This will flush the data to the stream.
     *
     * @throws IOException If there is an error writing the data to the stream.
     */
    public final void flush() throws IOException
    {
        if(flushed)
        {
            return;
        }
        if(count > 0 )
        {
            for( int i=count; i<4; i++ )
            {
                indata[i]=0;
            }
            transformASCII85();
            if(outdata[0]=='z')
            {
                for(int i=0;i<5;i++) // expand 'z',
                {
                    outdata[i]=(byte)'!';
                }
            }
            for(int i=0;i<count+1;i++)
            {
                out.write(outdata[i]);
                if(--lineBreak==0)
                {
                    out.write('\n');
                    lineBreak=maxline;
                }
            }
        }
        if(--lineBreak==0)
        {
            out.write('\n');
        }
        out.write(terminator);
        out.write('\n');
        count = 0;
        lineBreak=maxline;
        flushed=true;
        super.flush();
    }

    /**
     * This will close the stream.
     *
     * @throws IOException If there is an error closing the wrapped stream.
     */
    public void close() throws IOException
    {
        try
        {
            super.close();
        }
        finally
        {
            indata=outdata=null;
        }
    }

    /**
     * This will flush the stream.
     *
     * @throws Throwable If there is an error.
     */
    protected void finalize() throws Throwable
    {
        try
        {
            flush();
        }
        finally
        {
            super.finalize();
        }
    }

    
    public static String encodeBytesToAscii85(byte[] bytes) {
        ByteArrayOutputStream out_byte = new ByteArrayOutputStream();
        ASCII85OutputStream  out_ascii = new ASCII85OutputStream(out_byte);

        try {
            out_ascii.write(bytes);
            out_ascii.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String res = "";
        try {
            res = out_byte.toString("ascii");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return res;
    }
    
    public static void main(String[] args) throws IOException {
		long val = 2L;
		while (val > 0) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ASCII85Encoder enc = new ASCII85Encoder(baos);
			enc.write(toByte(val));
			enc.close();
			String code = Long.toString(val,  36); //new String(baos.toByteArray(), "UTF-8");
			System.out.println(val + " --> " + code + " (" + code.length() + ")");
			val = val * 2;			
		}
	}
    
    private static byte[] toByte(long l) {
	    byte [] b = new byte[8];  
	    for(int i= 0; i < 8; i++){  
	       b[7 - i] = (byte)(l >>> (i * 8));  
	    } 
	    return b;
    }
}
