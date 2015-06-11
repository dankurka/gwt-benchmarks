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

import com.google.inject.Inject;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * MailReporter can send emails.
 */
public class MailReporter {

  public static class PasswordAuthenticator extends Authenticator {

    private final String username;
    private final String password;

    public PasswordAuthenticator(String username, String password) {
      this.username = username;
      this.password = password;
    }

    @Override
    public PasswordAuthentication getPasswordAuthentication() {
      return new PasswordAuthentication(username, password);
    }
  }

  public interface MailHelper {
    Session create(Properties properties, PasswordAuthenticator authenticator);

    void send(MimeMessage message) throws MessagingException;
  }

  public static class MailHelperProdImpl implements MailHelper {

    @Override
    public Session create(Properties properties, PasswordAuthenticator authenticator) {
      return Session.getInstance(properties, authenticator);
    }

    @Override
    public void send(MimeMessage message) throws MessagingException {
      Transport.send(message);
    }
  }

  private static Logger logger = Logger.getLogger(MailReporter.class.getName());
  private MailSettings settings;
  private MailHelper mailHelper;

  @Inject
  public MailReporter(MailHelper mailHelper, MailSettings settings) {
    this.mailHelper = mailHelper;
    this.settings = settings;
  }

  public void sendEmail(String messageToSend) {

    // Get system properties
    Properties properties = new Properties();

    // Setup mail server
    properties.setProperty("mail.smtp.host", settings.getHost());
    properties.put("mail.smtp.port", "465");
    properties.put("mail.smtp.socketFactory.port", "465");
    properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
    properties.put("mail.smtp.socketFactory.fallback", "false");
    properties.put("mail.smtp.auth", "true");

    Session session = mailHelper.create(properties,
        new PasswordAuthenticator(settings.getUsername(), settings.getPassword()));

    try {
      MimeMessage message = new MimeMessage(session);
      message.setFrom(new InternetAddress(settings.getFrom()));
      message.addRecipient(Message.RecipientType.TO, new InternetAddress(settings.getTo()));
      message.setSubject("Error in the benchmarking system that needs attention");
      message.setContent(messageToSend, "text/html");
      mailHelper.send(message);
    } catch (MessagingException e) {
      logger.log(Level.SEVERE, "Can not send email to report an error", e);
      logger.severe(messageToSend);
    }
  }
}
