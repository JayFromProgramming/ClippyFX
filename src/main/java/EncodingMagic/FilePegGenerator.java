package EncodingMagic;

import HelperMethods.EncoderCheck;
import HelperMethods.StreamedCommand;
import Interfaces.PegGenerator;

import java.io.IOException;
import java.util.ArrayList;

public class FilePegGenerator implements PegGenerator {

    public ArrayList<Integer> getSupportedResolutions(String inputFile) throws IOException {
        int videoHeight = Integer.parseInt(StreamedCommand.getCommandOutput("ffprobe -v error -select_streams v -of default=noprint_wrappers=1:nokey=1 -show_entries stream=height \"" + inputFile + "\""));
        int[] heights = {videoHeight, 2160, 1440, 1080, 720, 480, 360, 240, 144};
        ArrayList<Integer> heightOptions = new ArrayList<>();
        for (int height : heights){
            if (height <= videoHeight){
                heightOptions.add(height);
            }
        }
        return heightOptions;

    }

    public ArrayList<String> getSupportedModes(){
        ArrayList<String> modes = new ArrayList<>();
        for (EncoderCheck.Encoders encoders : EncoderCheck.getEncoders()){
            modes.add(encoders.toString());
        }
        return modes;
    }

}
