package com.google.gwt.benchmark.framework.server;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;


/**
 * BenchmarkCompiler compiles a given benchmark and outputs a HTML file to run it.
 */
public class BenchmarkCompiler {

  private static final String HTML_TEMPLATE =
      "<!doctype html>\n" +
      "<html>\n" +
      "  <head>\n" +
      "    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n" +
      "    <script type=\"text/javascript\" src=\"{module_nocache}\"></script>\n" +
      "  </head>\n" +
      "  <body>\n" +
      "  </body>\n" +
      "</html>\n";

  public static void main(String[] args) {
    if (args.length == 0) {
      printUsageAndExit();
    }

    // last parameter is the (short) module name
    String modulename = args[args.length - 1];
    if (!modulename.startsWith("com.google.gwt.benchmark.benchmarks.")) {
      modulename = "com.google.gwt.benchmark.benchmarks." + modulename;
    }

    // cut of first argument
    args = Arrays.copyOfRange(args, 0, args.length - 1);

    String classPath = System.getProperty("java.class.path");

    // This is assumed to be the directory of gwt-benchmark-benchmarks
    String currentDir = System.getProperty("user.dir");
    File outputDir = new File(new File(currentDir), "war/");
    try {
      compile(modulename, classPath, args, outputDir);
      File htmlFile = writeHostPage(HTML_TEMPLATE, outputDir, modulename);
      System.out.println("Successfully compiled " + modulename);
      System.out.println("You can now open the benchmark in your browser:");
      System.out.println(htmlFile.toURI().toURL());
    } catch (CompileException | IOException e) {
      System.err.println("Failed to compile benchmark");
      e.printStackTrace();
      System.exit(-1);
    }
  }

  private static void printUsageAndExit() {
    System.err.println("Usage: BenchmarkCompiler <Any GWT compiler options> <modulename>");
    System.exit(-1);
  }

  public static void compile(String moduleName, String classpath, String[] args, File outputDir)
      throws CompileException {

    StringBuilder builder = new StringBuilder();

    builder.append("java ");
    builder.append("-Dgwt.persistentunitcache=false ");
    builder.append("-cp ");
    builder.append(classpath);
    builder.append(" ");
    builder.append("com.google.gwt.dev.Compiler ");

    builder.append("-war ");
    builder.append(outputDir.getAbsolutePath() + " ");

    // pipe all parameter to the GWT compiler
    for (String arg : args) {
      builder.append(arg);
      builder.append(" ");
    }

    builder.append(moduleName);
    compile(builder.toString());
  }

  private static void compile(String command) throws CompileException {
    InputStream stream = null;
    try {
      Process process = Runtime.getRuntime().exec(command);
      int exitValue = process.waitFor();

      if (exitValue != 0) {
        stream = process.getInputStream();
        String error =
            "Command returned with " + exitValue + " " + IOUtils.toString(stream, "UTF-8");
        System.err.println(error);
        throw new CompileException(error);
      }
    } catch (IOException | InterruptedException e) {
      throw new CompileException("Can not run command");
    } finally {
      IOUtils.closeQuietly(stream);
    }
  }

  private static File writeHostPage(String moduleTemplate, File outputDir, String moduleName)
      throws IOException {
    String tpl =
        moduleTemplate.replace("{module_nocache}", moduleName + "/" + moduleName + ".nocache.js");
    FileOutputStream stream = null;
    try {
      File htmlFile = new File(outputDir, moduleName + ".html");
      stream = new FileOutputStream(htmlFile);
      IOUtils.write(tpl.getBytes("UTF-8"), stream);
      return htmlFile;
    } finally {
      IOUtils.closeQuietly(stream);
    }
  }

  private static class CompileException extends Exception {
    public CompileException(String error) {
      super(error);
    }
  }
}
