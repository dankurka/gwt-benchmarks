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
package com.google.gwt.benchmark.dashboard.server.controller;

import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;

import java.util.ConcurrentModificationException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AuthContoller stores and verifies an auth token.
 */
public class AuthController {

  private static final Logger logger = Logger.getLogger(AuthController.class.getName());

  public void updateAuth(String auth) throws ControllerException {

    if (auth == null || auth.length() < 6) {
      throw new ControllerException("Auth empty or too short (min. 6 chars)");
    }

    try {
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      Entity entity = new Entity(KeyFactory.createKey("Auth", "auth"));
      entity.setProperty("value", auth);
      datastore.put(entity);
    } catch (ConcurrentModificationException | DatastoreFailureException e) {
      logger.log(Level.WARNING, "Can not persist new auth", e);
      throw new ControllerException("Can not persist new auth", e);
    }
  }

  public boolean validateAuth(String auth) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    try {
      Entity entity = datastore.get(KeyFactory.createKey("Auth", "auth"));
      String authFromStore = (String)entity.getProperty("value");
      if(authFromStore.equals(auth)) {
        return true;
      }
      logger.severe("Auth failed validation");
      return false;
    } catch (EntityNotFoundException e) {
      logger.log(Level.SEVERE, "No auth entry in datastore", e);
      return false;
    }
  }
}
