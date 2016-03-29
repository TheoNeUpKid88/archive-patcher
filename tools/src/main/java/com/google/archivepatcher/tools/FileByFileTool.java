// Copyright 2016 Google Inc. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.archivepatcher.tools;

import com.google.archivepatcher.applier.FileByFileV1DeltaApplier;
import com.google.archivepatcher.generator.FileByFileV1DeltaGenerator;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Simple command-line tool for generating and applying patches.
 */
public class FileByFileTool {

  /**
   * Usage instructions for the command line.
   */
  private static final String USAGE =
      "java -cp <classpath> com.google.archivepatcher.tools.FileByFileTool <options>\n"
          + "\nOptions:\n"
          + "  --generate      generate a patch\n"
          + "  --apply         apply a patch\n"
          + "  --old           the old file\n"
          + "  --new           the new file\n"
          + "  --patch         the patch file\n"
          + "\nExamples:\n"
          + "  To generate a patch from OLD to NEW, saving the patch in PATCH:\n"
          + "    java -cp <classpath> com.google.archivepatcher.tools.FileByFileTool --generate \\\n"
          + "      --old OLD --new NEW --patch PATCH\n"
          + "  To apply a patch PATCH to OLD, saving the result in NEW:\n"
          + "    java -cp <classpath> com.google.archivepatcher.tools.FileByFileTool --apply \\\n"
          + "      --old OLD --patch PATCH --new NEW";

  /**
   * Modes of operation.
   */
  private static enum Mode {
    /**
     * Generate a patch.
     */
    GENERATE,

    /**
     * Apply a patch.
     */
    APPLY;
  }

  /**
   * Runs the tool. See usage instructions for more information.
   */
  public static void main(String... args) throws IOException {
    String oldPath = null;
    String newPath = null;
    String patchPath = null;
    Mode mode = null;
    Iterator<String> argIterator = new LinkedList<String>(Arrays.asList(args)).iterator();
    while (argIterator.hasNext()) {
      String arg = argIterator.next();
      if ("--old".equals(arg)) {
        oldPath = popOrDie(argIterator, "--old");
      } else if ("--new".equals(arg)) {
        newPath = popOrDie(argIterator, "--new");
      } else if ("--patch".equals(arg)) {
        patchPath = popOrDie(argIterator, "--patch");
      } else if ("--generate".equals(arg)) {
        mode = Mode.GENERATE;
      } else if ("--apply".equals(arg)) {
        mode = Mode.APPLY;
      } else {
        exitWithUsage("unknown argument: " + arg);
      }
    }
    if (oldPath == null || newPath == null || patchPath == null || mode == null) {
      exitWithUsage("missing required argument(s)");
    }
    File oldFile = getRequiredFileOrDie(oldPath, "old file");
    if (mode == Mode.GENERATE) {
      File newFile = getRequiredFileOrDie(newPath, "new file");
      generatePatch(oldFile, newFile, new File(patchPath));
    } else { // mode == Mode.APPLY
      File patchFile = getRequiredFileOrDie(patchPath, "patch file");
      applyPatch(oldFile, patchFile, new File(newPath));
    }
  }

  /**
   * Pop an argument from the argument iterator or exit with a usage message about the expected
   * type of argument that was supposed to be found.
   * @param iterator the iterator to take an element from if available
   * @param expectedType description for the thing that was supposed to be in the iterator, for
   * error messages
   * @return the element retrieved from the iterator
   */
  private static String popOrDie(Iterator<String> iterator, String expectedType) {
    if (!iterator.hasNext()) {
      exitWithUsage("missing argument for " + expectedType);
    }
    return iterator.next();
  }

  /**
   * Find and return a readable file if it exists, exit with a usage message if it does not.
   * @param path the path to check and get a {@link File} for
   * @param description what the file represents, for error messages
   * @return a {@link File} representing the path, which exists and is readable
   */
  private static File getRequiredFileOrDie(String path, String description) {
    File result = new File(path);
    if (!result.exists() || !result.canRead()) {
      exitWithUsage(description + " does not exist or cannot be read: " + path);
    }
    return result;
  }

  /**
   * Terminate the program with an error message and usage instructions.
   * @param message the error message to give to the user prior to the usage instructions
   */
  private static void exitWithUsage(String message) {
    System.err.println("Error: " + message);
    System.err.println(USAGE);
    System.exit(1);
  }

  /**
   * Generate a specified patch to transform the specified old file to the specified new file.
   * @param oldFile the old file (will be read)
   * @param newFile the new file (will be read)
   * @param patchFile the patch file (will be written)
   * @throws IOException if anything goes wrong
   */
  public static void generatePatch(File oldFile, File newFile, File patchFile) throws IOException {
    FileByFileV1DeltaGenerator generator = new FileByFileV1DeltaGenerator();
    try (FileOutputStream patchOut = new FileOutputStream(patchFile);
        BufferedOutputStream bufferedPatchOut = new BufferedOutputStream(patchOut)) {
      generator.generateDelta(oldFile, newFile, bufferedPatchOut);
      bufferedPatchOut.flush();
    }
  }

  /**
   * Apply a specified patch to the specified old file, creating the specified new file.
   * @param oldFile the old file (will be read)
   * @param patchFile the patch file (will be read)
   * @param newFile the new file (will be written)
   * @throws IOException if anything goes wrong
   */
  public static void applyPatch(File oldFile, File patchFile, File newFile) throws IOException {
    // Figure out temp directory
    File tempFile = File.createTempFile("fbftool", "tmp");
    File tempDir = tempFile.getParentFile();
    tempFile.delete();
    FileByFileV1DeltaApplier applier = new FileByFileV1DeltaApplier(tempDir);
    try (FileInputStream patchIn = new FileInputStream(patchFile);
        BufferedInputStream bufferedPatchIn = new BufferedInputStream(patchIn);
        FileOutputStream newOut = new FileOutputStream(newFile);
        BufferedOutputStream bufferedNewOut = new BufferedOutputStream(newOut)) {
      applier.applyDelta(oldFile, bufferedPatchIn, bufferedNewOut);
      bufferedNewOut.flush();
    }
  }
}
