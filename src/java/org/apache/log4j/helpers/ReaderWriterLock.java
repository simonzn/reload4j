/*
 * ============================================================================
 *                   The Apache Software License, Version 1.1
 * ============================================================================
 *
 *    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of  source code must  retain the above copyright  notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include  the following  acknowledgment:  "This product includes  software
 *    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
 *    Alternately, this  acknowledgment may  appear in the software itself,  if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "log4j" and  "Apache Software Foundation"  must not be used to
 *    endorse  or promote  products derived  from this  software without  prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products  derived from this software may not  be called "Apache", nor may
 *    "Apache" appear  in their name,  without prior written permission  of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 * APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 * ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 * (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software  consists of voluntary contributions made  by many individuals
 * on  behalf of the Apache Software  Foundation.  For more  information on the
 * Apache Software Foundation, please see <http://www.apache.org/>.
 *
 */

package org.apache.log4j.helpers;

import java.io.PrintWriter;


/**
 *
 * A RederWriterLock allows multiple readers to obtain the lock at the same time
 * but allows only one writer at a time.
 *
 * When both readers and writers wait to obtain the lock, priority is given to 
 * waiting writers.
 *
 * This lock is not reentrant. It is possible for a writer in possession of a writer
 * lock to fail to obtain a reader lock. The same goes for reader in possession of a 
 * reader lock. It can fail to obtain a writer lock.
 * 
 * THIS LOCK IS NOT RENTRANT.
 * 
 * It is the developer's responsability to retstrict the use of this lock to small segments
 * of code where reentrancy can be avoided.
 * 
 * Note that the RederWriterLock is only useful in cases where a resource:
 * 
 * 1) Has many frequent read operations performed on it
 * 2) Only rarely is the resource modified (written)
 * 3) Read operations are invoked by many different threads
 * 
 * If any of the above conditions are not met, it is better to avoid this fancy lock.
 * 
 * @author Ceki G&uuml;lc&uuml;
 *
 */
public class ReaderWriterLock {
  int readers = 0;
  int writers = 0;
  int waitingWriters = 0;
  PrintWriter printWriter;

  public ReaderWriterLock() {
  }

  public ReaderWriterLock(PrintWriter pw) {
    printWriter = pw;
  }

  public synchronized void getReadLock() {
    if (printWriter != null) {
      printMessage("Asking for read lock.");
    }

    while ((writers > 0) || (waitingWriters > 0)) {
      try {
        wait();
      } catch (InterruptedException ie) {
      }
    }

    if (printWriter != null) {
      printMessage("Got read lock.");
    }

    readers++;
  }

  public synchronized void releaseReadLock() {
    if (printWriter != null) {
      printMessage("About to release read lock.");
    }

    readers--;

    if (waitingWriters > 0) {
      notifyAll();
    }
  }

  public synchronized void getWriteLock() {
    if (printWriter != null) {
      printMessage("Asking for write lock.");
    }

    waitingWriters++;

    while ((readers > 0) || (writers > 0)) {
      try {
        wait();
      } catch (InterruptedException ie) {
      }
    }

    if (printWriter != null) {
      printMessage("Got write lock.");
    }

    waitingWriters--;
    writers++;
  }

  public synchronized void releaseWriteLock() {
    if (printWriter != null) {
      printMessage("About to release write lock.");
    }

    writers--;
    notifyAll();
  }

  void printMessage(String msg) {
    //printWriter.print("[");      
    printWriter.println(Thread.currentThread().getName() + " " + msg);
  }
}