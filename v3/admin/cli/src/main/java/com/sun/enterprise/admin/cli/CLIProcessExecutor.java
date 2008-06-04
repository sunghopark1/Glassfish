/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.enterprise.admin.cli;

import com.sun.enterprise.universal.process.ProcessStreamDrainer;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;


/**
 *  CLIProcessExecutor
 *  A simple process executor class that is used by CLI.
 *  @author  jane.young@sun.com
 * bnevins -- there is no reason for this to be public
 */
public class CLIProcessExecutor
{

    public void CLIProcessExecutor() {
        process = null;
    }
    

    /**
     * This method invokes the runtime exec
     * @param cmd the command to execute
     * @param wait if true, wait for process to end.
     * @exception Exception
     */
    public void execute(String name, String[] cmd, boolean wait) throws Exception
    {
        process=Runtime.getRuntime().exec(cmd);
        ProcessStreamDrainer.redirect(name, process);

        try {
            if (wait) {
                process.waitFor();
            }
        }
        catch (InterruptedException ie) {
        }
    }

   /**
      return the exit value of this process.
      if process is null, then there is no process running
      therefore the return value is 0.
    */
    public int exitValue() {
        if (process == null) return -1;
        return process.exitValue();
    }

    private Process process;
}
