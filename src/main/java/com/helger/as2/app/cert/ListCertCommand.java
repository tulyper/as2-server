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
package com.helger.as2.app.cert;

import java.security.cert.Certificate;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.helger.as2.cmd.CommandResult;
import com.helger.as2lib.cert.IAliasedCertificateFactory;
import com.helger.as2lib.exception.OpenAS2Exception;

public class ListCertCommand extends AbstractAliasedCertCommand
{
  @Override
  public String getDefaultDescription ()
  {
    return "List all certificate aliases in the current certificate store";
  }

  @Override
  public String getDefaultName ()
  {
    return "list";
  }

  @Override
  public String getDefaultUsage ()
  {
    return "list";
  }

  @Override
  public CommandResult execute (final IAliasedCertificateFactory certFx, final Object [] params) throws OpenAS2Exception
  {
    synchronized (certFx)
    {
      final Map <String, Certificate> certs = certFx.getCertificates ();
      final Iterator <Entry <String, Certificate>> certIt = certs.entrySet ().iterator ();
      Map.Entry <String, Certificate> currentCert;
      final CommandResult cmdRes = new CommandResult (CommandResult.TYPE_OK);

      while (certIt.hasNext ())
      {
        currentCert = certIt.next ();
        cmdRes.getResults ().add (currentCert.getKey ());
      }

      if (cmdRes.getResults ().size () == 0)
      {
        cmdRes.getResults ().add ("No certificates available");
      }

      return cmdRes;

    }
  }
}
