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
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.as2.cmd.CommandResult;
import com.helger.as2.cmd.ICommand;
import com.helger.as2.util.CommandTokenizer;
import com.helger.as2lib.exception.OpenAS2Exception;
import com.helger.as2lib.exception.WrappedException;
import com.phloc.commons.concurrent.ThreadUtils;
import com.phloc.commons.string.StringHelper;

/**
 * original author unknown in this release made the process a thread so it can
 * be shared with other command processors like the SocketCommandProcessor
 * created innerclass CommandTokenizer so it could handle quotes and spaces
 * within quotes
 * 
 * @author joseph mcverry
 */
public class StreamCommandProcessor extends AbstractCommandProcessor
{
  public static final String COMMAND_NOT_FOUND = "Error: command not found";
  public static final String COMMAND_ERROR = "Error executing command";
  public static final String EXIT_COMMAND = "exit";
  public static final String PROMPT = "#>";
  private BufferedReader reader = null;
  private BufferedWriter writer = null;

  public StreamCommandProcessor ()
  {
    reader = new BufferedReader (new InputStreamReader (System.in));
    writer = new BufferedWriter (new OutputStreamWriter (System.out));
  }

  @Nonnull
  public BufferedReader getReader ()
  {
    return reader;
  }

  @Nonnull
  public BufferedWriter getWriter ()
  {
    return writer;
  }

  @Override
  public void run ()
  {
    try
    {
      while (true)
        processCommand ();
    }
    catch (final OpenAS2Exception e)
    {
      e.printStackTrace ();
    }
  }

  @Override
  public void processCommand () throws OpenAS2Exception
  {
    try
    {

      final String sLine = readLine ();
      if (sLine != null)
      {
        final CommandTokenizer aTokenizer = new CommandTokenizer (sLine);
        if (aTokenizer.hasMoreTokens ())
        {
          final String sCommandName = aTokenizer.nextToken ().toLowerCase (Locale.US);

          if (sCommandName.equals (EXIT_COMMAND))
          {
            terminate ();
          }
          else
          {
            final List <String> aParams = new ArrayList <String> ();
            while (aTokenizer.hasMoreTokens ())
            {
              aParams.add (aTokenizer.nextToken ());
            }

            final ICommand aCommand = getCommand (sCommandName);
            if (aCommand != null)
            {
              final CommandResult aResult = aCommand.execute (aParams.toArray ());
              if (aResult.getType () == CommandResult.TYPE_OK)
              {
                writeLine (aResult.toString ());
              }
              else
              {
                writeLine (COMMAND_ERROR);
                writeLine (aResult.getResult ());
              }
            }
            else
            {
              writeLine (COMMAND_NOT_FOUND + "> " + sCommandName);
              writeLine ("List of commands:");
              writeLine (EXIT_COMMAND);
              for (final ICommand aCurCmd : getAllCommands ())
                writeLine (aCurCmd.getName ());
            }
          }
        }

        write (PROMPT);
      }
      else
      {
        ThreadUtils.sleep (100);
      }
    }
    catch (final IOException ex)
    {
      throw new WrappedException (ex);
    }
  }

  @Nullable
  public String readLine () throws IOException
  {
    final BufferedReader aReader = getReader ();

    return StringHelper.trim (aReader.readLine ());
  }

  public void write (final String text) throws IOException
  {
    final BufferedWriter aWriter = getWriter ();
    aWriter.write (text);
    aWriter.flush ();
  }

  public void writeLine (final String line) throws IOException
  {
    final BufferedWriter aWriter = getWriter ();
    aWriter.write (line + "\r\n");
    aWriter.flush ();
  }
}
