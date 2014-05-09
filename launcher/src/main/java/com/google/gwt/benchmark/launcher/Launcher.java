/*
 * Copyright 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.benchmark.launcher;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;


/**
 * Launcher starts a jetty web server which runs the war of the compile server.
 */
public class Launcher {
  public static void main(String[] args) throws Exception {
    // Create an embedded Jetty server on port 8080
    Server server = new Server(8080);

    // Use a context handler that allows for an exploded war
    WebAppContext altHandler = new WebAppContext();
    altHandler.setResourceBase("./target/war");
    altHandler.setDescriptor("./target/war/WEB-INF/web.xml");
    altHandler.setContextPath("/");
    altHandler.setParentLoaderPriority(true);

    // Add it to the server
    server.setHandler(altHandler);

    // And start it up
    server.start();
    server.join();
  }
}
