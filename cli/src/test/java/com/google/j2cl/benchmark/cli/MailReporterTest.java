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
package com.google.j2cl.benchmark.cli;


import com.google.j2cl.benchmark.cli.MailReporter.MailHelper;
import com.google.j2cl.benchmark.cli.MailReporter.PasswordAuthenticator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Properties;

import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * Test for {@link MailReporter}.
 */
public class MailReporterTest {


  private MailSettings mailSettings;
  private MailHelper mailHelper;
  private MailReporter errorReporter;

  @Before
  public void setup() {
    mailSettings = new MailSettings("from1", "to1", "host1", "username1", "password1");
    mailHelper = Mockito.mock(MailHelper.class);
    errorReporter = new MailReporter(mailHelper, mailSettings);
  }

  @Test
  public void testSendEmail() throws MessagingException, IOException {

    errorReporter.sendEmail("my cool message");

    ArgumentCaptor<Properties> captor = ArgumentCaptor.forClass(Properties.class);
    ArgumentCaptor<PasswordAuthenticator> authenticatorCaptor =
        ArgumentCaptor.forClass(PasswordAuthenticator.class);

    Mockito.verify(mailHelper).create(captor.capture(), authenticatorCaptor.capture());
    Properties properties = captor.getValue();

    Assert.assertEquals(mailSettings.getHost(), properties.get("mail.smtp.host"));
    Assert.assertEquals("465", properties.get("mail.smtp.port"));
    Assert.assertEquals("465", properties.get("mail.smtp.socketFactory.port"));
    Assert.assertEquals("javax.net.ssl.SSLSocketFactory",
        properties.get("mail.smtp.socketFactory.class"));
    Assert.assertEquals("false", properties.get("mail.smtp.socketFactory.fallback"));
    Assert.assertEquals("true", properties.get("mail.smtp.auth"));

    PasswordAuthenticator authenticator = authenticatorCaptor.getValue();
    Assert.assertEquals(mailSettings.getUsername(),
        authenticator.getPasswordAuthentication().getUserName());
    Assert.assertEquals(mailSettings.getPassword(),
        authenticator.getPasswordAuthentication().getPassword());

    ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);

    Mockito.verify(mailHelper).send(messageCaptor.capture());
    MimeMessage message = messageCaptor.getValue();

    Assert.assertEquals(mailSettings.getFrom(), message.getFrom()[0].toString());
    Assert.assertEquals(mailSettings.getTo(),
        message.getRecipients(RecipientType.TO)[0].toString());
    Assert.assertEquals("Error in the benchmarking system that needs attention",
        message.getSubject());
    Assert.assertEquals("my cool message", message.getContent());
  }

  @Test
  public void testFailedToSendMail() throws MessagingException {
    Mockito.doThrow(new MessagingException()).when(mailHelper)
        .send(Mockito.<MimeMessage>anyObject());

    errorReporter.sendEmail("my cool message");
  }
}
