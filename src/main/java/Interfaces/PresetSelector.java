package Interfaces;

import java.util.ArrayList;

public interface PresetSelector {


    /**
     * Returns all the supported presets.
     * @returns ArrayList of EncoderPreset objects
     */
    public ArrayList<EncoderPreset> getSupportedPresets();

    /**
     * Returns the most optimal preset.
     * @returns EncoderPreset object
     */
    public EncoderPreset getOptimalPreset();

    /**
     * Returns the preset with the given name.
     * @param name Name of the preset
     *              (case-sensitive)
     * @returns EncoderPreset object
     */
    public EncoderPreset getPreset(String name);

    /**
     * Returns an iterable list of presets that are supported by the encoder.
     * @returns Iterable list of EncoderPreset objects
     */
    public Iterable<EncoderPreset> supportedPresets();

}
