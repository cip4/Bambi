/**
 * 
 */
package org.cip4.bambi.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.jdflib.util.ByteArrayIOStream;

/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2008 The International Cooperation for the Integration of 
 * Processes in  Prepress, Press and Postpress (CIP4).  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:  
 *       "This product includes software developed by the
 *        The International Cooperation for the Integration of 
 *        Processes in  Prepress, Press and Postpress (www.cip4.org)"
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "CIP4" and "The International Cooperation for the Integration of 
 *    Processes in  Prepress, Press and Postpress" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written 
 *    permission, please contact info@cip4.org.
 *
 * 5. Products derived from this software may not be called "CIP4",
 *    nor may "CIP4" appear in their name, without prior written
 *    permission of the CIP4 organization
 *
 * Usage of this software in commercial products is subject to restrictions. For
 * details please consult info@cip4.org.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE INTERNATIONAL COOPERATION FOR
 * THE INTEGRATION OF PROCESSES IN PREPRESS, PRESS AND POSTPRESS OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the The International Cooperation for the Integration 
 * of Processes in Prepress, Press and Postpress and was
 * originally based on software 
 * copyright (c) 1999-2001, Heidelberger Druckmaschinen AG 
 * copyright (c) 1999-2001, Agfa-Gevaert N.V. 
 *  
 * For more information on The International Cooperation for the 
 * Integration of Processes in  Prepress, Press and Postpress , please see
 * <http://www.cip4.org/>.
 *  
 * 
 */
/**
 * buffered servlet request class
 * @author prosirai
 *
 */
public class BambiServletResponse implements HttpServletResponse
{
    private HttpServletResponse parent;
    private ByteArrayIOStream buffer;
    private static Log log = LogFactory.getLog(BambiServletResponse.class.getName());

     public BambiServletResponse(HttpServletResponse _parent, boolean bBuffer)
    {
        parent=_parent;
        if(bBuffer)
        {
            buffer = new ByteArrayIOStream();
        }
        else
            buffer=null;
    }

    public void addCookie(Cookie arg0)
    {
        parent.addCookie(arg0);
    }

    public void addDateHeader(String arg0, long arg1)
    {
        parent.addDateHeader(arg0, arg1);
    }

    public void addHeader(String arg0, String arg1)
    {
        parent.addHeader(arg0, arg1);
    }

    public void addIntHeader(String arg0, int arg1)
    {
        parent.addIntHeader(arg0, arg1);
    }

    public boolean containsHeader(String arg0)
    {
        return parent.containsHeader(arg0);
    }

    public String encodeRedirectUrl(String arg0)
    {
        return parent.encodeRedirectUrl(arg0);
    }

    public String encodeRedirectURL(String arg0)
    {
        return parent.encodeRedirectURL(arg0);
    }

    public String encodeUrl(String arg0)
    {
        return parent.encodeUrl(arg0);
    }

    public String encodeURL(String arg0)
    {
        return parent.encodeURL(arg0);
    }

    public void flushBuffer() throws IOException
    {
        parent.flushBuffer();
    }

    public int getBufferSize()
    {
        return parent.getBufferSize();
    }

    public String getCharacterEncoding()
    {
        return parent.getCharacterEncoding();
    }

    public String getContentType()
    {
        return parent.getContentType();
    }

    public Locale getLocale()
    {
        return parent.getLocale();
    }

    /**
     * never acces this directly - always use flush finally
     */
    public ServletOutputStream getOutputStream() throws NotImplementedException
    {
        throw new NotImplementedException("Use getBufferedOutputStream");
    }
    public OutputStream getBufferedOutputStream() throws IOException
    {
        return buffer==null ? parent.getOutputStream() : buffer;
    }
    public InputStream getBufferedInputStream() 
    {
        return buffer==null ? null : buffer.getInputStream();
    }

    public PrintWriter getWriter() throws IOException
    {
        return parent.getWriter();
    }

    public boolean isCommitted()
    {
        return parent.isCommitted();
    }

    public void reset()
    {
        parent.reset();
    }

    public void resetBuffer()
    {
        parent.resetBuffer();
    }

    public void sendError(int arg0, String arg1) throws IOException
    {
        parent.sendError(arg0, arg1);
    }

    public void sendError(int arg0) throws IOException
    {
        parent.sendError(arg0);
    }

    public void sendRedirect(String arg0) throws IOException
    {
        parent.sendRedirect(arg0);
    }

    public void setBufferSize(int arg0)
    {
        parent.setBufferSize(arg0);
    }

    public void setCharacterEncoding(String arg0)
    {
        parent.setCharacterEncoding(arg0);
    }

    public void setContentLength(int arg0)
    {
        parent.setContentLength(arg0);
    }

    public void setContentType(String arg0)
    {
        parent.setContentType(arg0);
    }

    public void setDateHeader(String arg0, long arg1)
    {
        parent.setDateHeader(arg0, arg1);
    }

    public void setHeader(String arg0, String arg1)
    {
        parent.setHeader(arg0, arg1);
    }

    public void setIntHeader(String arg0, int arg1)
    {
        parent.setIntHeader(arg0, arg1);
    }

    public void setLocale(Locale arg0)
    {
        parent.setLocale(arg0);
    }

    public void setStatus(int arg0, String arg1)
    {
        parent.setStatus(arg0, arg1);
    }

    public void setStatus(int arg0)
    {
        parent.setStatus(arg0);
    }

    /**
     * 
     */
    public void flush()
    {

        if(buffer!=null)
        {
            try{
                ServletOutputStream outputStream = parent.getOutputStream();
                IOUtils.copy(buffer.getInputStream(), outputStream);
                outputStream.flush();
                outputStream.close();
                buffer=null;
            }
            catch (IOException e) {
                log.error("Error while flushing response stream",e);
            }
        }

    }

}
