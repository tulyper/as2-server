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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.as2.cmd.ICommand;
import com.helger.as2.cmd.ICommandRegistry;
import com.helger.as2lib.IDynamicComponent;
import com.helger.as2lib.ISession;
import com.helger.as2lib.exception.OpenAS2Exception;
import com.helger.as2lib.util.IStringMap;
import com.helger.as2lib.util.StringMap;
import com.phloc.commons.annotations.ReturnsMutableCopy;
import com.phloc.commons.annotations.UnsupportedOperation;
import com.phloc.commons.collections.ContainerHelper;

public abstract class AbstractCommandProcessor extends StringMap implements ICommandProcessor, IDynamicComponent, Runnable
{
  private final List <ICommand> m_aCommands = new ArrayList <ICommand> ();
  private boolean m_bTerminated = false;

  public AbstractCommandProcessor ()
  {}

  @Nullable
  public String getName ()
  {
    return null;
  }

  @Nullable
  public ISession getSession ()
  {
    // TODO Auto-generated method stub
    return null;
  }

  public void initDynamicComponent (@Nonnull final ISession session, @Nullable final IStringMap parameters) throws OpenAS2Exception
  {}

  public void init () throws OpenAS2Exception
  {}

  @Nonnull
  @ReturnsMutableCopy
  public List <ICommand> getAllCommands ()
  {
    return ContainerHelper.newList (m_aCommands);
  }

  @Nullable
  public ICommand getCommand (final String name)
  {
    for (final ICommand currentCmd : m_aCommands)
      if (currentCmd.getName ().equals (name))
        return currentCmd;
    return null;
  }

  public boolean isTerminated ()
  {
    return m_bTerminated;
  }

  @UnsupportedOperation
  public void processCommand () throws OpenAS2Exception
  {
    throw new OpenAS2Exception ("super class method call, not initialized correctly");
  }

  public void addCommands (@Nonnull final ICommandRegistry reg)
  {
    m_aCommands.addAll (reg.getAllCommands ());
  }

  public void terminate ()
  {
    m_bTerminated = true;
  }
}
