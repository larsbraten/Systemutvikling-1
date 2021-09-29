package graphics.gallery;


import graphics.launcher.MainStage;
import hibernate.model.ImageData;
import hibernate.model.Metadata;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * @author Eivind Berger-Nilsen
 * Convenience class for displaying pictures in fullscreen format. This implementation supports
 * basic scaling and picture centering, and provides whitespace for necessary controls.
 */


public class BigPicture extends StackPane {

  private final Logger LOGGER = LogManager.getLogger(getClass());
  private final ImageView BIG_IMAGE;
  private final ImageView NO_HOVER_ICON = new ImageView("/views/img/info.png");
  private final ImageView HOVER_ICON = new ImageView("/views/img/info_hover.png");
  private final Button INFO_BUTTON = new Button();
  private final ImageData IMAGE_DATA;

  /**
   * This implementation provides no extended features beyond what's defined by it's
   * super-class. As such, most initialization happens in the constructor.
   * @param imageData Class providing metadata extracted from it's corresponding picture.
   *
   */

  public BigPicture(ImageData imageData){
    this.IMAGE_DATA = imageData;
    this.BIG_IMAGE = procureImage(new File(imageData.getPath()));
    INFO_BUTTON.setBackground(null);
    INFO_BUTTON.setGraphic(NO_HOVER_ICON);
    HOVER_ICON.setFitHeight(50);
    HOVER_ICON.setFitWidth(50);
    NO_HOVER_ICON.setFitHeight(50);
    NO_HOVER_ICON.setFitWidth(50);
    StackPane.setAlignment(INFO_BUTTON, Pos.TOP_RIGHT);
    StackPane.setMargin(INFO_BUTTON,new Insets(5,5,0,0));
    INFO_BUTTON.hoverProperty().addListener((observable, oldValue, newValue) -> {
      INFO_BUTTON.setGraphic(newValue ? HOVER_ICON : NO_HOVER_ICON);
    });
    INFO_BUTTON.setOnAction(event -> {
      MainStage.UserPrompter.showMessage("Properties", IMAGE_DATA.getPath(), getReadable(imageData));
    });
    BIG_IMAGE.fitHeightProperty().bind(heightProperty().subtract(125));
    BIG_IMAGE.fitWidthProperty().bind(widthProperty().subtract(125));
    getChildren().setAll(BIG_IMAGE, INFO_BUTTON);
  }

  /**
   * Compiles imageData into readable format for dialog windws.
   * @param imageData The imageData.
   * @return The compiled message.
   */

  public static String getReadable(ImageData imageData){
    int maxTagsPrint = 5;
    int amountTags = imageData.getTags().size();
    String unKnownMarker = "unknown";

    Metadata metadata = imageData.getMetadata();
    StringBuilder stringBuilder = new StringBuilder("");
    if(imageData.getTags() != null && !imageData.getTags().isEmpty()){
      for(int i = 0 ; i < amountTags && i < maxTagsPrint ; i++){
        stringBuilder.append(imageData.getTags().get(0) + ", ");
      }
      if(amountTags > maxTagsPrint) stringBuilder.append("...more tags.");
    }else stringBuilder.append("no tags found.");
    return "Name:\t\t\t\t" + new File(imageData.getPath()).getName() + "\n" +
        "Picture captured:\t\t" + ((metadata.getCaptured() == null) ? unKnownMarker : metadata.getCaptured()) + "\n" +
        "Camera manufacturer:\t" + ((metadata.getMake() == null || metadata.getMake().isBlank()) ? unKnownMarker : metadata.getMake()) + "\n" +
        "Camera model:\t\t" + ((metadata.getModel() == null || metadata.getModel().isBlank()) ? unKnownMarker : metadata.getModel()) + "\n" +
        "Picture latitude:\t\t" + metadata.getLatitude() + "\n" +
        "Picture longitude:\t\t" + metadata.getLongitude() + "\n" +
        "Picture resolution\t\t" + metadata.getWidth() + " x " + metadata.getHeight() + "\n" +
        "Tags: \t\t\t\t" + stringBuilder.toString();
  }


  /**
   * This methods loads a image from a specified file. As this methods hogs system resources
   * during loading, it's beneficial to store the instance in an accessible place, if reusability is desired.
   * @param file The file which the image reside in, i.e not a directory.
   * @return The viewable image.
   */

  public ImageView procureImage(File file) {
    return procureImage(file,0,0);
  }

  /**
   * This methods loads a image from a specified file. As this methods hogs system resources
   * during loading, it's beneficial to store the instance in an accessible place, if reusability is desired.
   * The initial width and height determines the resolution of the loaded image.
   * If performance i desired, choose a small picture, and vice versa.
   * @param file The file which the image reside in, i.e not a directory.
   * @param width Width of the image. This number
   * @param height Height of the image.
   * @return The viewable image.
   */

  public ImageView procureImage(File file, int width, int height) {
    try {
      ImageView imageView = width > 0 && height > 0 ?
          new ImageView(new Image(new FileInputStream(file), width, height, true, true)) :
          new ImageView(new Image(new FileInputStream(file)));
      imageView.setId(file.getName());
      imageView.setPreserveRatio(true);
      return imageView;
    } catch (FileNotFoundException e) {
      LOGGER.debug(e.fillInStackTrace().getMessage());
    }
    return null;
  }
}
