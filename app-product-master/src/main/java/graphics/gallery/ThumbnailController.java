package graphics.gallery;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

/**
 * @author Eivind Berger-Nilsen
 * The controller class for {@link ImageThumbnail} loads the necessary controls
 * overlapping the background picture.
 */

public class ThumbnailController extends StackPane {

  /**
   * privatly invoked constructor invoked only by the factory method "load()".
   * @param imageName Name of the parent thumbnails image.
   */

  private ThumbnailController(String imageName){
    getChildren().setAll(getHeader(imageName));
  }

  /**
   * privatly invoked method that produces a formatted header.
   * @param imageName Name of the image to yield the header text.
   * @return The text in a container which may wrap the text itself.
   */

  private TextFlow getHeader(String imageName){
    Text imageHeader = new Text(imageName != null ? imageName : "");
    imageHeader.setId("ThumbnailTitle");
    TextFlow textFlow = new TextFlow(imageHeader);
    textFlow.setMaxWidth(getMaxWidth());
    textFlow.setMaxHeight(getMaxHeight());
    textFlow.setPrefWidth(widthProperty().doubleValue() - 10);
    textFlow.setTextAlignment(TextAlignment.CENTER);
    StackPane.setAlignment(textFlow, Pos.CENTER);
    StackPane.setMargin(textFlow,new Insets(5,5,5,5));
    return textFlow;
  }

  /**
   * Factory method for the controller.
   * @param imageName Name of the image.
   * @return The controller.
   */

  public static ThumbnailController load(String imageName){
    return new ThumbnailController(imageName);
  }
}
