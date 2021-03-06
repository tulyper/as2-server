/**
 * The FreeBSD Copyright
 * Copyright 1994-2008 The FreeBSD Project. All rights reserved.
 * Copyright (C) 2014 Philip Helger ph[at]phloc[dot]com
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *    1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE FREEBSD PROJECT ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE FREEBSD PROJECT OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation
 * are those of the authors and should not be interpreted as representing
 * official policies, either expressed or implied, of the FreeBSD Project.
 */
package com.helger.as2.cmd.processor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import org.xml.sax.SAXException;

import com.helger.as2.cmd.CommandResult;
import com.helger.as2.cmd.ICommand;
import com.helger.as2.util.CommandTokenizer;
import com.helger.as2lib.ISession;
import com.helger.as2lib.exception.OpenAS2Exception;
import com.helger.as2lib.util.IStringMap;
import com.helger.as2lib.util.StringMap;
import com.phloc.commons.io.streams.StreamUtils;
import com.phloc.commons.string.StringHelper;

/**
 * actual socket command processor takes commands from socket/port and passes
 * them to the OpenAS2Server message format <command userid="abc" pasword="xyz">
 * the actual command </command> when inited the valid userid and password is
 * passed, then as each command is processed the processCommand method verifies
 * the two fields correctness
 *
 * @author joseph mcverry
 */
public class SocketCommandProcessor extends AbstractCommandProcessor
{
  private BufferedReader m_aReader;
  private BufferedWriter m_aWriter;
  private SSLServerSocket m_aSSLServerSocket;

  private String m_sUserID;
  private String m_sPassword;
  private SocketCommandParser m_aParser;

  public SocketCommandProcessor ()
  {}

  @Override
  public void initDynamicComponent (@Nonnull final ISession aSession, @Nullable final IStringMap aParams) throws OpenAS2Exception
  {
    final StringMap aParameters = aParams == null ? new StringMap () : new StringMap (aParams);
    final String p = aParameters.getAttributeAsString ("portid");
    try
    {
      final int nPort = Integer.parseInt (p);

      final SSLServerSocketFactory aSSLServerSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault ();
      m_aSSLServerSocket = (SSLServerSocket) aSSLServerSocketFactory.createServerSocket (nPort);
      final String [] enabledCipherSuites = { "SSL_DH_anon_WITH_RC4_128_MD5" };
      m_aSSLServerSocket.setEnabledCipherSuites (enabledCipherSuites);
    }
    catch (final IOException e)
    {
      e.printStackTrace ();
      throw new OpenAS2Exception (e);
    }
    catch (final NumberFormatException e)
    {
      e.printStackTrace ();
      throw new OpenAS2Exception ("error converting portid parameter '" + p + "': " + e);
    }
    m_sUserID = aParameters.getAttributeAsString ("userid");
    if (StringHelper.hasNoText (m_sUserID))
      throw new OpenAS2Exception ("missing userid parameter");

    m_sPassword = aParameters.getAttributeAsString ("password");
    if (StringHelper.hasNoText (m_sPassword))
      throw new OpenAS2Exception ("missing password parameter");

    try
    {
      m_aParser = new SocketCommandParser ();
    }
    catch (final Exception e)
    {
      throw new OpenAS2Exception (e);
    }
  }

  @Override
  public void processCommand () throws OpenAS2Exception
  {
    SSLSocket socket = null;
    try
    {
      socket = (SSLSocket) m_aSSLServerSocket.accept ();
      socket.setSoTimeout (2000);
      m_aReader = new BufferedReader (new InputStreamReader (socket.getInputStream ()));
      m_aWriter = new BufferedWriter (new OutputStreamWriter (socket.getOutputStream ()));

      final String line = m_aReader.readLine ();

      m_aParser.parse (line);

      if (!m_aParser.getUserid ().equals (m_sUserID))
      {
        m_aWriter.write ("Bad userid/password");
        throw new OpenAS2Exception ("Bad userid");
      }

      if (!m_aParser.getPassword ().equals (m_sPassword))
      {
        m_aWriter.write ("Bad userid/password");
        throw new OpenAS2Exception ("Bad password");
      }

      final String str = m_aParser.getCommandText ();
      if (str != null && str.length () > 0)
      {
        final CommandTokenizer cmdTkn = new CommandTokenizer (str);

        if (cmdTkn.hasMoreTokens ())
        {
          final String commandName = cmdTkn.nextToken ().toLowerCase ();

          if (commandName.equals (StreamCommandProcessor.EXIT_COMMAND))
          {
            terminate ();
          }
          else
          {
            final List <String> params = new ArrayList <String> ();

            while (cmdTkn.hasMoreTokens ())
            {
              params.add (cmdTkn.nextToken ());
            }

            final ICommand cmd = getCommand (commandName);

            if (cmd != null)
            {
              final CommandResult result = cmd.execute (params.toArray ());

              if (result.getType () == CommandResult.TYPE_OK)
              {
                m_aWriter.write (result.toXML ());
              }
              else
              {
                m_aWriter.write ("\r\n" + StreamCommandProcessor.COMMAND_ERROR + "\r\n");
                m_aWriter.write (result.getResult ());
              }
            }
            else
            {
              m_aWriter.write (StreamCommandProcessor.COMMAND_NOT_FOUND + "> " + commandName + "\r\n");
              m_aWriter.write ("List of commands:" + "\r\n");
              for (final ICommand aCurCmd : getAllCommands ())
                m_aWriter.write (aCurCmd.getName () + "\r\n");
            }
          }
        }

      }
      m_aWriter.flush ();
    }
    catch (final IOException ioe)
    {
      ioe.printStackTrace ();
    }
    catch (final SAXException e)
    {
      throw new OpenAS2Exception (e);
    }
    catch (final Exception e)
    {
      throw new OpenAS2Exception (e);
    }
    finally
    {
      StreamUtils.close (socket);
    }
  }

  @Override
  public void run ()
  {
    try
    {
      while (true)
      {
        processCommand ();
      }
    }
    catch (final OpenAS2Exception e)
    {
      e.printStackTrace ();
    }
  }
}
