package EncodingMagic;

public class EncoderPreset {

    /**
     * Create a FFMPEG command line string from the preset
     * @param startTime Start time of the video in seconds
     * @param endTime End time of the video in seconds
     * @param inputFile The file FFMPEG will read from
     * @param outputFile The file FFMPEG will output to
     * @return A FFMPEG command line string to be executed
     */
    public static String getCommand(float startTime, float endTime, String inputFile, String outputFile, int width, int height, String encoder){
        return null;
    }


    /**
     * Get if this preset supports hardware acceleration
     */
    public String hardwareAcceleration(){
        return null;
    }

}
