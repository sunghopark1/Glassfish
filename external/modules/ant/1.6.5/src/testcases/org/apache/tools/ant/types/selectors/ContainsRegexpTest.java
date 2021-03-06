/*
 * Copyright  2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.tools.ant.types.selectors;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildFileTest;

import junit.framework.TestCase;
import junit.framework.AssertionFailedError;

import java.io.File;


public class ContainsRegexpTest extends TestCase {

    private Project project;

    public ContainsRegexpTest(String name) {
        super(name);
    }

    public void setUp() {
        project = new Project();
        project.setBasedir(".");
    }

    public void testContainsRegexp() {
        TaskdefForRegexpTest MyTask =
            new TaskdefForRegexpTest("containsregexp");
        try {
            MyTask.setUp();
            MyTask.test();
        } finally {
            MyTask.tearDown();
        }
    }

    private class TaskdefForRegexpTest extends BuildFileTest {
        TaskdefForRegexpTest(String name) {
            super(name);
        }

        public void setUp() {
            configureProject("src/etc/testcases/types/selectors.xml");
        }

        public void tearDown() {
            executeTarget("cleanupregexp");
        }

        public void test() {
            File dir = null;
            File[] files = null;
            int filecount;

            executeTarget("containsregexp");
	
            dir = new File(getProjectDir() + "/regexpseltestdest/");
            files = dir.listFiles();
            filecount = files.length;
	
            if (filecount != 1)
                assertEquals("ContainsRegexp test should have copied 1 file",
                             1, files.length);
	
        }
    }
}

