package Interfaces;

import EncodingMagic.EncoderPreset;

import java.io.IOException;
import java.util.ArrayList;

public interface PegGenerator {

    /**
     * Returns all the supported presets.
     * @returns ArrayList of EncoderPreset objects
     */
    ArrayList<Integer> getSupportedResolutions(String inputURI) throws IOException;

    /**
     * Returns an iterable list of presets that are supported by the encoder.
     * @returns Iterable list of EncoderPreset objects
     */

    ArrayList<String> getSupportedModes();

}
