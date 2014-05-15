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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link AuthController}.
 */
public class AuthControllerTest {

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  @Before
  public void setUp() {
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void testValidValue() throws ControllerException {
    // empty datastore should always return false
    Assert.assertFalse(new AuthController().validateAuth("someauth1"));

    // prepare datastore
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity entity = new Entity(KeyFactory.createKey("Auth", "auth"));
    entity.setProperty("value", "someauth2");
    datastore.put(entity);

    // now see if auth validation works
    Assert.assertTrue(new AuthController().validateAuth("someauth2"));
    Assert.assertFalse(new AuthController().validateAuth("invalidAuth"));

    // change the auth
    new AuthController().updateAuth("changed auth");

    // does the change propagate?
    Assert.assertTrue(new AuthController().validateAuth("changed auth"));
    Assert.assertFalse(new AuthController().validateAuth("invalidAuth1"));

    // test for two short auths
    try {
      new AuthController().updateAuth("12345");
      Assert.fail("exception did not occur (auth too short)");
    } catch (ControllerException ignored) {
    }

    // verify that it did not change
    Assert.assertTrue(new AuthController().validateAuth("changed auth"));
  }

}
