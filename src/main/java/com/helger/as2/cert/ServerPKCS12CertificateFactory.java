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
package com.helger.as2.cert;

import java.io.File;
import java.io.InputStream;

import javax.annotation.WillClose;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.as2.util.FileMonitor;
import com.helger.as2.util.IFileMonitorListener;
import com.helger.as2lib.cert.PKCS12CertificateFactory;
import com.helger.as2lib.exception.InvalidParameterException;
import com.helger.as2lib.exception.OpenAS2Exception;

public class ServerPKCS12CertificateFactory extends PKCS12CertificateFactory implements IFileMonitorListener
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (ServerPKCS12CertificateFactory.class);

  private FileMonitor m_aFileMonitor;

  @Override
  public void load (@WillClose final InputStream in, final char [] password) throws OpenAS2Exception
  {
    super.load (in, password);
    getFileMonitor ();
  }

  public void setFileMonitor (final FileMonitor fileMonitor)
  {
    m_aFileMonitor = fileMonitor;
  }

  public FileMonitor getFileMonitor () throws InvalidParameterException
  {
    boolean createMonitor = m_aFileMonitor == null && getAttributeAsString (PARAM_INTERVAL) != null;
    if (!createMonitor && m_aFileMonitor != null)
    {
      final String filename = m_aFileMonitor.getFilename ();
      createMonitor = filename != null && !filename.equals (getFilename ());
    }

    if (createMonitor)
    {
      if (m_aFileMonitor != null)
        m_aFileMonitor.stop ();

      final int nInterval = getParameterIntRequired (PARAM_INTERVAL);
      final File file = new File (getFilename ());
      m_aFileMonitor = new FileMonitor (file, nInterval);
      m_aFileMonitor.addListener (this);
    }

    return m_aFileMonitor;
  }

  public void handle (final FileMonitor monitor, final File file, final int eventID)
  {
    switch (eventID)
    {
      case IFileMonitorListener.EVENT_MODIFIED:
        try
        {
          load ();
          s_aLogger.info ("- Certificates Reloaded -");
        }
        catch (final OpenAS2Exception oae)
        {
          oae.terminate ();
        }
        break;
    }
  }
}
