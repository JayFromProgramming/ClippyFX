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
     * Blocks until the command has finished executing, and returns the output
     * @param process The process to wait for
     * @return The exit code of the process.
     * @throws IOException If the process has an error
     */
    public static int waitForExit(Process process) throws IOException {
        while (process.isAlive()) {}
        return process.exitValue();
    }

    public static int waitForExit(Process process, int timeout) throws IOException, TimeoutException {
        long start = System.currentTimeMillis();
        long end = start + timeout * 1000L;
        while(process.isAlive()){
            if(System.currentTimeMillis() > end) {
                throw new TimeoutException(process.toString() + " wait for exit timed out");
            }
        }
        return process.exitValue();
    }
}
