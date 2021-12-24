package Interfaces;

import javafx.scene.layout.AnchorPane;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

public interface PopOut {

    boolean isAlive = true;

    enum popOutType {
        ClippingView,
        ConverterView,
        YoutubeFinderView
    }

    /**
     *  Get the window object of the popout
     *  @return Window The window object of the popout
     */
    Window getWindow();

    /**
     *  Get the type of the popout
     *  @return popOutType The type of the popout
     */
    popOutType getType();

    /**
     *  Close the popout
     */
    boolean close();

    /**
     *  Gets if the current popout is alive
     */
    boolean isAlive();

}
