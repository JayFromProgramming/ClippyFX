package Interfaces;

public interface EncoderPreset {

    /**
     * Get display name of the preset
     */
    public String getDisplayName();

    /**
     * Create a FFMPEG command line string from the preset
     * @param startTime Start time of the video in seconds
     * @param endTime End time of the video in seconds
     * @param inputFile The file FFMPEG will read from
     * @param outputFile The file FFMPEG will output to
     * @return A FFMPEG command line string to be executed
     */
    public String getCommand(float startTime, float endTime, String inputFile, String outputFile);


    /**
     * Get if this preset supports hardware acceleration
     */
    public boolean supportsHardwareAcceleration();

}
