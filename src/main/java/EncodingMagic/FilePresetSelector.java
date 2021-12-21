package EncodingMagic;

import Interfaces.EncoderPreset;
import Interfaces.PresetSelector;

import java.util.ArrayList;

public class FilePresetSelector implements PresetSelector {


    @Override
    public ArrayList<EncoderPreset> getSupportedPresets() {
        return null;
    }

    @Override
    public EncoderPreset getOptimalPreset() {
        return null;
    }

    @Override
    public EncoderPreset getPreset(String name) {
        return null;
    }

    @Override
    public Iterable<EncoderPreset> supportedPresets() {
        return null;
    }
}
