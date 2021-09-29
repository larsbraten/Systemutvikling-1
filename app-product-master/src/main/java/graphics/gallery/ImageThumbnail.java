package graphics.gallery;

import java.io.IOException;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class ImageThumbnail extends StackPane {

  /**
   * This class provides a template for reoccurring thumbnails that also may need to display
   * information a.nd  user-controls.
   */

  private final Logger LOGGER = LogManager.getLogger(getClass());
  private final ImageView IMAGE_VIEW;
  private static final ColorAdjust COLOR_ADJUST = new ColorAdjust(0, 0, -0.5, 0);

  public ImageThumbnail(ImageView imageView){
    this.IMAGE_VIEW = imageView == null ? new ImageView() : imageView;
    getImageView().setPreserveRatio(true);
    getChildren().setAll(imageView);

    hoverProperty().addListener((observable, oldValue, newValue) -> {
      setHoverAttributes(newValue);
    });
  }

  /**
   * Called on hovering events. Displays the images name with a color adjusted background.
   * @param hovering hovering status.
   */

  private void setHoverAttributes(boolean hovering){
    if(hovering){
      getImageView().setEffect(COLOR_ADJUST);
      ThumbnailController controller = ThumbnailController.load(getImageView().getId());
      StackPane.setAlignment(controller, Pos.CENTER);
      getChildren().setAll(getImageView(), controller);
    } else {
      getImageView().setEffect(null);
      getChildren().setAll(getImageView());
    }
  }

  public ImageView getImageView() {
    return IMAGE_VIEW;
  }
}
