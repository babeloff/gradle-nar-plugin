//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.components.io.attributes;

import org.gradle.internal.impldep.org.codehaus.plexus.interpolation.os.Os;
import org.gradle.internal.impldep.org.codehaus.plexus.logging.Logger;
import org.gradle.internal.impldep.org.codehaus.plexus.logging.console.ConsoleLogger;
import org.gradle.internal.impldep.org.codehaus.plexus.util.FileUtils;
import org.gradle.internal.impldep.org.codehaus.plexus.util.cli.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.regex.Pattern;

public final class PlexusIoResourceAttributeUtils {
    static Pattern totalLinePattern = Pattern.compile("\\w*\\s\\d*");

    private PlexusIoResourceAttributeUtils() {
    }

    public static PlexusIoResourceAttributes mergeAttributes(PlexusIoResourceAttributes override, PlexusIoResourceAttributes base, PlexusIoResourceAttributes def) {
        if (override == null) {
            return base;
        } else {
            SimpleResourceAttributes result;
            if (base == null) {
                result = new SimpleResourceAttributes();
            } else {
                result = new SimpleResourceAttributes(base.getUserId(), base.getUserName(), base.getGroupId(), base.getGroupName(), base.getOctalMode());
            }

            if (override.getGroupId() != null && override.getGroupId() != -1) {
                result.setGroupId(override.getGroupId());
            }

            if (def != null && def.getGroupId() >= 0 && (result.getGroupId() == null || result.getGroupId() < 0)) {
                result.setGroupId(def.getGroupId());
            }

            if (override.getGroupName() != null) {
                result.setGroupName(override.getGroupName());
            }

            if (def != null && result.getGroupName() == null) {
                result.setGroupName(def.getGroupName());
            }

            if (override.getUserId() != null && override.getUserId() != -1) {
                result.setUserId(override.getUserId());
            }

            if (def != null && def.getUserId() >= 0 && (result.getUserId() == null || result.getUserId() < 0)) {
                result.setUserId(def.getUserId());
            }

            if (override.getUserName() != null) {
                result.setUserName(override.getUserName());
            }

            if (def != null && result.getUserName() == null) {
                result.setUserName(def.getUserName());
            }

            if (override.getOctalMode() > 0) {
                result.setOctalMode(override.getOctalMode());
            }

            if (def != null && result.getOctalMode() < 0) {
                result.setOctalMode(def.getOctalMode());
            }

            return result;
        }
    }

    public static boolean isGroupExecutableInOctal(int mode) {
        return isOctalModeEnabled(mode, 8);
    }

    public static boolean isGroupReadableInOctal(int mode) {
        return isOctalModeEnabled(mode, 32);
    }

    public static boolean isGroupWritableInOctal(int mode) {
        return isOctalModeEnabled(mode, 16);
    }

    public static boolean isOwnerExecutableInOctal(int mode) {
        return isOctalModeEnabled(mode, 64);
    }

    public static boolean isOwnerReadableInOctal(int mode) {
        return isOctalModeEnabled(mode, 256);
    }

    public static boolean isOwnerWritableInOctal(int mode) {
        return isOctalModeEnabled(mode, 128);
    }

    public static boolean isWorldExecutableInOctal(int mode) {
        return isOctalModeEnabled(mode, 1);
    }

    public static boolean isWorldReadableInOctal(int mode) {
        return isOctalModeEnabled(mode, 4);
    }

    public static boolean isWorldWritableInOctal(int mode) {
        return isOctalModeEnabled(mode, 2);
    }

    public static boolean isOctalModeEnabled(int mode, int targetMode) {
        return (mode & targetMode) != 0;
    }

    public static PlexusIoResourceAttributes getFileAttributes(File file) throws IOException {
        Map byPath = getFileAttributesByPath(file, (Logger)null, 0, false, true);
        return (PlexusIoResourceAttributes)byPath.get(file.getAbsolutePath());
    }

    public static PlexusIoResourceAttributes getFileAttributes(File file, Logger logger) throws IOException {
        Map byPath = getFileAttributesByPath(file, logger, 0, false, true);
        return (PlexusIoResourceAttributes)byPath.get(file.getAbsolutePath());
    }

    public static PlexusIoResourceAttributes getFileAttributes(File file, Logger logger, int logLevel) throws IOException {
        Map byPath = getFileAttributesByPath(file, logger, logLevel, false, true);
        return (PlexusIoResourceAttributes)byPath.get(file.getAbsolutePath());
    }

    public static Map<String, PlexusIoResourceAttributes> getFileAttributesByPath(File dir) throws IOException {
        return getFileAttributesByPath(dir, (Logger)null, 0, true, true);
    }

    public static Map<String, PlexusIoResourceAttributes> getFileAttributesByPath(File dir, Logger logger) throws IOException {
        return getFileAttributesByPath(dir, logger, 0, true, true);
    }

    public static Map<String, PlexusIoResourceAttributes> getFileAttributesByPath(File dir, Logger logger, int logLevel) throws IOException {
        return getFileAttributesByPath(dir, logger, 0, true, true);
    }

    public static Map<String, PlexusIoResourceAttributes> getFileAttributesByPath(File dir, Logger logger, int logLevel, boolean recursive, boolean includeNumericUserId) throws IOException {
        if (!enabledOnCurrentOperatingSystem()) {
            return Collections.emptyMap();
        } else if (Java7Reflector.isJava7()) {
            return getFileAttributesByPathJava7(dir);
        } else {
            if (logger == null) {
                logger = new ConsoleLogger(1, "Internal");
            }

            PlexusIoResourceAttributeUtils.LoggerStreamConsumer loggerConsumer = new PlexusIoResourceAttributeUtils.LoggerStreamConsumer((Logger)logger, logLevel);
            NumericUserIDAttributeParser numericIdParser = null;
            FutureTask<Integer> integerFutureTask = null;
            Commandline numericCli = null;
            if (includeNumericUserId) {
                numericIdParser = new NumericUserIDAttributeParser(loggerConsumer, (Logger)logger);
                String lsOptions1 = "-1nla" + (recursive ? "R" : "d");

                try {
                    numericCli = setupCommandLine(dir, lsOptions1, (Logger)logger);
                    CommandLineCallable commandLineCallable = CommandLineUtils.executeCommandLineAsCallable(numericCli, (InputStream)null, (StreamConsumer) numericIdParser, loggerConsumer, 0);
                    integerFutureTask = new FutureTask(commandLineCallable);
                    (new Thread(integerFutureTask)).start();
                } catch (CommandLineException var14) {
                    IOException error = new IOException("Failed to quote directory: '" + dir + "'");
                    error.initCause(var14);
                    throw error;
                }
            }

            SymbolicUserIDAttributeParser userId = getNameBasedParser(dir, (Logger)logger, recursive, loggerConsumer);
            if (includeNumericUserId) {
                Integer result;
                try {
                    result = (Integer)integerFutureTask.get();
                } catch (InterruptedException var12) {
                    throw new RuntimeException(var12);
                } catch (ExecutionException var13) {
                    throw new RuntimeException(var13);
                }

                if (result != 0) {
                    throw new IOException("Failed to retrieve numeric file attributes using: '" + numericCli.toString() + "'");
                }
            }

            return userId.merge(numericIdParser);
        }
    }

    private static SymbolicUserIDAttributeParser getNameBasedParser(File dir, Logger logger, boolean recursive, PlexusIoResourceAttributeUtils.LoggerStreamConsumer loggerConsumer) throws IOException {
        SymbolicUserIDAttributeParser userId = new SymbolicUserIDAttributeParser(loggerConsumer, logger);
        String lsOptions2 = "-1la" + (recursive ? "R" : "d");

        try {
            executeLs(dir, lsOptions2, loggerConsumer, (StreamConsumer) userId, logger);
            return userId;
        } catch (CommandLineException var8) {
            IOException error = new IOException("Failed to quote directory: '" + dir + "'");
            error.initCause(var8);
            throw error;
        }
    }

    private static Map<String, PlexusIoResourceAttributes> getFileAttributesByPathJava7(File dir) throws IOException {
        Map<Integer, String> userCache = new HashMap();
        Map<Integer, String> groupCache = new HashMap();
        List fileAndDirectoryNames;
        if (dir.isDirectory()) {
            fileAndDirectoryNames = FileUtils.getFileAndDirectoryNames(dir, (String)null, (String)null, true, true, true, true);
        } else {
            fileAndDirectoryNames = Collections.singletonList(dir.getAbsolutePath());
        }

        Map<String, PlexusIoResourceAttributes> attributesByPath = new LinkedHashMap();
        Iterator i$ = fileAndDirectoryNames.iterator();

        while(i$.hasNext()) {
            Object fileAndDirectoryName = i$.next();
            String fileName = (String)fileAndDirectoryName;
            attributesByPath.put(fileName, new Java7FileAttributes(new File(fileName), userCache, groupCache));
        }

        return attributesByPath;
    }

    private static boolean enabledOnCurrentOperatingSystem() {
        return !Os.isFamily("windows") && !Os.isFamily("win9x");
    }

    private static void executeLs(File dir, String options, PlexusIoResourceAttributeUtils.LoggerStreamConsumer loggerConsumer, StreamConsumer parser, Logger logger) throws IOException, CommandLineException {
        Commandline numericCli = setupCommandLine(dir, options, logger);

        try {
            int result = CommandLineUtils.executeCommandLine(numericCli, parser, loggerConsumer);
            if (result != 0) {
                throw new IOException("Failed to retrieve numeric file attributes using: '" + numericCli.toString() + "'");
            }
        } catch (CommandLineException var8) {
            IOException error = new IOException("Failed to retrieve numeric file attributes using: '" + numericCli.toString() + "'");
            error.initCause(var8);
            throw error;
        }
    }

    private static Commandline setupCommandLine(File dir, String options, Logger logger) {
        Commandline numericCli = new Commandline();
        numericCli.getShell().setQuotedArgumentsEnabled(true);
        numericCli.getShell().setQuotedExecutableEnabled(false);
        numericCli.setExecutable("ls");
        numericCli.createArg().setLine(options);
        numericCli.createArg().setValue(dir.getAbsolutePath());
        if (logger.isDebugEnabled()) {
            logger.debug("Executing:\n\n" + numericCli.toString() + "\n");
        }

        return numericCli;
    }

    static final class LoggerStreamConsumer implements StreamConsumer {
        private final Logger logger;
        private final int level;

        public LoggerStreamConsumer(Logger logger, int level) {
            this.logger = logger;
            this.level = level;
        }

        public void consumeLine(String line) {
            switch(this.level) {
                case 0:
                    this.logger.debug(line);
                    break;
                case 1:
                default:
                    this.logger.info(line);
                    break;
                case 2:
                    this.logger.warn(line);
                    break;
                case 3:
                    this.logger.error(line);
                    break;
                case 4:
                    this.logger.fatalError(line);
            }

        }
    }
}
