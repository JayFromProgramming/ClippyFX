package HelperMethods;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class StreamedCommand {

    /**
     *
     * @param command The command to execute
     * @return The process object
     * @throws IOException If the command has an error
     */
    public static Process runCommand(String command) throws IOException {
        ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
        builder.redirectErrorStream(true);
        return builder.start();
    }

    /**
     * @param command The command to execute as a string
     * @return The process object as a string
     * @throws IOException If the command has an error
     */
    public static String getCommandOutput(String command) throws IOException {
        Process process = runCommand(command);
        int exitCode = waitForExit(process);
        if(exitCode != 0) {
            throw new IOException("Command " + command + " exited with code " + exitCode);
        }
        StringBuilder output = new StringBuilder();
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
        }
        return output.toString();
    }


    /**
     * Blocks until the command has finished executing, and returns the output
     * @param process The process to wait for
     * @return The exit code of the process.
     * @throws IOException If the process has an error
     */
    public static int waitForExit(Process process) throws IOException {
        while (process.isAlive()) {}
        return process.exitValue();
    }

    public static int throwForExit(Process process, int timeout) throws IOException, TimeoutException {
        long start = System.currentTimeMillis();
        long end = start + timeout * 1000L;
        while(process.isAlive()){
            if(System.currentTimeMillis() > end) {
                throw new TimeoutException(process.toString() + " wait for exit timed out");
            }
        }
        return process.exitValue();
    }
    public static int waitForExit(Process process, int timeout) throws IOException {
        long start = System.currentTimeMillis();
        long end = start + timeout * 1000L;
        while(process.isAlive()){
            if(System.currentTimeMillis() > end) {
                return Integer.MIN_VALUE;
            }
        }
        return process.exitValue();
    }
}
