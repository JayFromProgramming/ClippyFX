package HelperMethods;

public class PegArgument {

    public VideoChecks.Encoders encoder;
    public VideoChecks.Sizes dimensions;
    public int cropX1;
    public int cropX2;
    public int cropY1;
    public int cropY2;
    public boolean allow100MB;
    public double fps;


    public PegArgument (VideoChecks.Encoders encoder, VideoChecks.Sizes dimensions, boolean allow100MB, double fps) {
        this.encoder = encoder;
        this.dimensions = dimensions;
        this.allow100MB = allow100MB;
        this.fps = fps;
    }

    public PegArgument(){

    }

}
