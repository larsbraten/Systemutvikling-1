package graphics.gallery;

import com.sun.istack.Nullable;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * @author Eivind Berger-Nilse
 * This implementation resembles FlowPane in the way it allignes it's children, but also
 * eliminates whitespace which may occur between nodes of different sizes. This container were
 * taylored for rectangular nodes in mind, i.e pictures and other smaller containers. This containers
 * embeds a scrollpane, and several HBox and VBoxes for the actual alligning.
 * This API also provde a few settings.
 */

public class GalleryPane extends StackPane{

  private final Logger LOGGER = LogManager.getLogger(getClass());

  boolean convergentScrolling = false;
  private final ScrollPane SCROLLPANE = new ScrollPane();
  private final ObservableList<ImageThumbnail> IMAGES;
  private double spacing;
  private int nodeTargetLength;
  private int minNodeTargetLength;
  private Orientation orientation;
  private final ChangeListener<Number> SCROLL_LISTENER = (observable, oldValue, newValue) -> setImageOffset();
  private final ChangeListener<Bounds> VIEWPORT_LISTENER = (observable, oldValue, newValue) -> reArrangeImages();
  private final ListChangeListener<ImageThumbnail> LIST_CHANGE_LISTENER = c -> reArrangeImages();

  /**
   * Initializes with default settings.
   * @param images Initial list of {@link ImageThumbnail} to display.
   */

  public GalleryPane(@Nullable List<ImageThumbnail> images){
    this(200, 150, 5, Orientation.HORIZONTAL, images);
  }

  /**
   * Initializes with default settings, except orientation.
   * @param orientation Initial scroll orientation.
   * @param images Initial list of {@link ImageThumbnail} to display.
   */

  public GalleryPane(Orientation orientation, @Nullable List<ImageThumbnail> images){
    this(200, 150, 5, orientation, images);
  }

  /**
   * Initializes with used defines settings.
   * @param nodeTargetLength The width/height (depends on the orientation) the container attempts to maintain the nodes at.
   * @param minNodeTargetLength Minimum zoom-out level.
   * @param spacing Desired whitespace between each node, including the parent's padding.
   * @param orientation Initial scroll {@link Orientation}.
   * @param images Initial list of {@link ImageThumbnail} to display.
   */

  public GalleryPane(int nodeTargetLength, int minNodeTargetLength, double spacing, Orientation orientation, @Nullable List<ImageThumbnail> images) {
    SCROLLPANE.setContent(new Pane());
    SCROLLPANE.setPrefWidth(nodeTargetLength * 3);
    SCROLLPANE.setPrefHeight(nodeTargetLength * 3);
    super.getChildren().setAll(SCROLLPANE);
    this.IMAGES = images == null ? FXCollections.observableArrayList() : FXCollections.observableList(images);

    setOrientation(orientation);
    setSpacing(spacing);
    setNodeTargetLength(nodeTargetLength);
    setMinNodeTargetLength(minNodeTargetLength);
    this.IMAGES.addListener(LIST_CHANGE_LISTENER);
    SCROLLPANE.viewportBoundsProperty().addListener(VIEWPORT_LISTENER);
  }

  /**
   * As nodes of different sizes inevitably causes misalignment at the endpoint of each row/column
   * it may prove beneficial to have them converge during scrolling. Minor difference in row/column
   * length will yield desirable results, while the opposite may prove uncomfortable.
   * @param convergentScrolling true is enable, false is disable.
   */

  public void setConvergentScrolling(boolean convergentScrolling){
    this.convergentScrolling = convergentScrolling;

    if(this.convergentScrolling){
      SCROLLPANE.vvalueProperty().addListener(SCROLL_LISTENER);
      SCROLLPANE.hvalueProperty().addListener(SCROLL_LISTENER);
    } else {
      SCROLLPANE.vvalueProperty().removeListener(SCROLL_LISTENER);
      SCROLLPANE.hvalueProperty().removeListener(SCROLL_LISTENER);
      ((Pane) SCROLLPANE.getContent()).getChildren().forEach(node -> {
        VBox.setMargin(node, null);
        HBox.setMargin(node,null);
      });
    }
    reArrangeImages();
  }

  /**
   * Getter for convergent scroll setting.
   * @return The setting.
   */

  public boolean getConvergentScrolling(){
    return convergentScrolling;
  }

  /**
   * Getter for the content. Use this method to add/remove pictures.
   * @return A observableList containing this containers {@link ImageThumbnail}.
   */

  public ObservableList<ImageThumbnail> getContent() {
    return IMAGES;
  }

  /**
   * Alters the low-limit target length. As this is only a target value,
   * all nodes will revolve around this value with some wiggle room.
   * @param minNodeTargetLength The desired target length.
   */

  public void setMinNodeTargetLength(int minNodeTargetLength) {
    this.minNodeTargetLength = Math.max(minNodeTargetLength, 1);
    if(nodeTargetLength < this.minNodeTargetLength){
      nodeTargetLength = this.minNodeTargetLength;
    }
    reArrangeImages();
  }

  /**
   * Getter for the low-limit target length. As this is only a target value,
     all nodes will revolve around this value with some wiggle room.
   * @return The current low-limit value.
   */

  public int getMinNodeTargetLength() {
    return minNodeTargetLength;
  }

  /**
   * Alters the current target length. As this is only a target value,
   * all nodes will revolve around this value with some wiggle room.
   * @param nodeTargetLength The desired target length.
   */

  public void setNodeTargetLength(int nodeTargetLength){
    this.nodeTargetLength = Math.abs(nodeTargetLength);
    reArrangeImages();
  }

  /**
   * Getter for the current target length. As this is only a target value,
   * all nodes will revolve around this value with some wiggle room.
   * @return The current target value.
   */

  public int getNodeTargetLength(){
    return nodeTargetLength;
  }

  /**
   * Alters the space between each node (parent inclusive).
   * @param spacing The spacing value.
   */

  public void setSpacing(double spacing) {
    this.spacing = Math.abs(spacing);
    SCROLLPANE.setPadding(new Insets(this.spacing));
    reArrangeImages();
  }

  /**
   * Getter for the spacing
   * @return The spacing value.
   */

  public double getSpacing() {
    return spacing;
  }

  /**
   * Increments the nodeTargetLengthValue such that on additional row/column will appear.
   */

  public void zoomIn(){
    int newTargetLength;
    switch (orientation){
      case HORIZONTAL:
        newTargetLength = (int) Math.round((nodeTargetLength * ((SCROLLPANE.widthProperty().get() / nodeTargetLength) + 1.0)) / computeImageBoxes());
        setNodeTargetLength(Math.min(newTargetLength, SCROLLPANE.widthProperty().intValue()));
        break;
      case VERTICAL:
        newTargetLength = (int) Math.round((nodeTargetLength * ((SCROLLPANE.heightProperty().get() / nodeTargetLength) + 1.0)) / computeImageBoxes());
        setNodeTargetLength(Math.min(newTargetLength, SCROLLPANE.heightProperty().intValue()));
        break;
      default:
        throw new IllegalArgumentException();
    }
    reArrangeImages();
  }

  /**
   * Decrements the nodeTargetLength value such that on row/column wil disappear.
   */

  public void zoomOut(){
    int newTargetLength;
    switch (orientation){
      case HORIZONTAL:
        newTargetLength = (int) Math.round((nodeTargetLength * ((SCROLLPANE.widthProperty().get() / nodeTargetLength) - 1.0)) / computeImageBoxes());
        setNodeTargetLength(Math.max(newTargetLength, minNodeTargetLength));
        break;
      case VERTICAL:
        newTargetLength = (int) Math.round((nodeTargetLength * ((SCROLLPANE.heightProperty().get() / nodeTargetLength) - 1.0)) / computeImageBoxes());
        setNodeTargetLength(Math.max(newTargetLength, minNodeTargetLength));
        break;
      default:
        throw new IllegalArgumentException();
    }
    reArrangeImages();
  }

  /**
   * Getter for the orientation.
   * @return An enum representing the orientation.
   */

  public Orientation getOrientation(){
    return orientation;
  }

  /**
   * Alters the scroll orientation. Updates to node alignment only occur after a scrollevent due
   * to limitation in the scrollpane itself.
   * @param orientation
   */

  public void setOrientation(Orientation orientation){
    this.orientation = orientation;
    switch (orientation) {
      case HORIZONTAL:
        SCROLLPANE.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        SCROLLPANE.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        SCROLLPANE.setVmin(0);
        SCROLLPANE.setHmin(5);
        SCROLLPANE.setVvalue(0);
        break;
      case VERTICAL:
        SCROLLPANE.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        SCROLLPANE.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        SCROLLPANE.setVmin(5);
        SCROLLPANE.setHmin(0);
        SCROLLPANE.setHvalue(0);
        break;
      default:
        throw new IllegalArgumentException();
    }
    reArrangeImages();
  }

  /**
   * Computes necessary columns/rows based on the current viewport.
   * @return the needed columns/rows.
   */

  private int computeImageBoxes() {
    switch (orientation){
      case HORIZONTAL:
        int columns = (int) Math.round(SCROLLPANE.widthProperty().doubleValue() / nodeTargetLength);
        if(columns <= IMAGES.size()){
          if(columns > 0){
            return columns;
          }
          return 1;
        }else {
          return IMAGES.size();
        }
      case VERTICAL:
        int rows = (int) Math.round(SCROLLPANE.heightProperty().doubleValue() / nodeTargetLength);
        if (rows <= IMAGES.size()){
          if (rows > 0){
            return rows;
          }
          return 1;
        }else {
          return IMAGES.size();
        }
      default:
        throw new IllegalArgumentException();
    }
  }

  /**
   * This method constructs each row/column in the viewport, mainly when scaling events occur.
   *
   */

  private void stackImages() {
    int requiredImageContainers = computeImageBoxes();

    switch (orientation) {
      case HORIZONTAL:
          SCROLLPANE.setContent(IMAGES
              .stream()
              .filter(imageThumbnail -> imageThumbnail.getImageView().isVisible())
              .map(imageView -> {
                VBox[] vBoxes = new VBox[requiredImageContainers];

                for (int i = 0, max = IMAGES.size(); i < max; i++) {
                  int accessor = i % requiredImageContainers;

                  if (vBoxes[accessor] == null) vBoxes[accessor] = new VBox();
                  vBoxes[accessor].getChildren().add(IMAGES.get(i));
                  vBoxes[accessor].setSpacing(spacing);
                }
                return vBoxes;
              })
              .map(HBox::new)
              .peek(hBox -> hBox.setSpacing(spacing))
              .findFirst().orElse(new HBox(Node.BASELINE_OFFSET_SAME_AS_HEIGHT)));
        break;
      case VERTICAL:
          SCROLLPANE.setContent(IMAGES
              .stream()
              .filter(imageThumbnail -> imageThumbnail.getImageView().isVisible())
              .map(imageView -> {
                HBox[] hBoxes = new HBox[requiredImageContainers];

                for (int i = 0, max = IMAGES.size(); i < max; i++) {
                  int accessor = i % requiredImageContainers;
                  if (hBoxes[accessor] == null) hBoxes[accessor] = new HBox();
                  hBoxes[accessor].getChildren().add(IMAGES.get(i));
                  hBoxes[accessor].setSpacing(spacing);
                }
                return hBoxes;
              }).map(VBox::new)
              .peek(vBox -> vBox.setSpacing(spacing))
              .findFirst().orElse(new VBox(Node.BASELINE_OFFSET_SAME_AS_HEIGHT)));
        break;
      default:
        throw new IllegalArgumentException();
    }
    setImageOffset();
  }

  /**
   * This method is invoked only if convergent scrolling is enables.
   * The convergence is achieved by setting a margin on the nodes dynamically.
   */

  private void setImageOffset(){
    if(convergentScrolling){
      switch (orientation){
        case HORIZONTAL:
          ((Pane) SCROLLPANE.getContent()).getChildren().forEach(node -> {
            double nodeToBoundGap = SCROLLPANE.getContent().boundsInLocalProperty().get().getHeight()
                - node.boundsInLocalProperty().get().getHeight();
            boolean exceedingViewport = SCROLLPANE.getViewportBounds().getHeight()
                - node.boundsInLocalProperty().get().getHeight() < 0;

            HBox.setMargin(node, new Insets(exceedingViewport ? SCROLLPANE.getVvalue() * nodeToBoundGap : 0, 0, 0, 0));
          });
          break;
        case VERTICAL:
          ((Pane) SCROLLPANE.getContent()).getChildren().forEach(node -> {
            double nodeToBoundGap = SCROLLPANE.getContent().boundsInLocalProperty().get().getWidth()
                - node.boundsInLocalProperty().get().getWidth();
            boolean exceedingViewport = SCROLLPANE.getViewportBounds().getWidth()
                - node.boundsInLocalProperty().get().getWidth() < 0;

            VBox.setMargin(node, new Insets(0, 0, 0, exceedingViewport ? SCROLLPANE.getHvalue() * nodeToBoundGap : 0));
          });
          break;
        default:
          throw new IllegalArgumentException();
      }
    }
  }

  /**
   * This method is responsible for the sizing of each image contained, only privately invoked when necessary.
   * @param width Current width of the viewport.
   * @param height Current height of the viewport.
   */

  private void reArrangeImages(double width, double height){
    int imageGroups = computeImageBoxes();

    switch (orientation){
      case HORIZONTAL:
        IMAGES.forEach(imageView -> {
          imageView.getImageView().setFitHeight(0);
          imageView.getImageView().setFitWidth(width - ((imageGroups - 1) * spacing) > (nodeTargetLength * IMAGES.size()) ||
              width < nodeTargetLength ?
              nodeTargetLength :
              width / imageGroups - spacing * (imageGroups - 1) / (imageGroups));
        });
        break;
      case VERTICAL:
        IMAGES.forEach(imageView -> {
          imageView.getImageView().setFitWidth(0);
          imageView.getImageView().setFitHeight(height - ((imageGroups - 1) * spacing) > (nodeTargetLength * IMAGES.size()) ||
              height < nodeTargetLength ?
              nodeTargetLength :
              height / imageGroups - spacing * (imageGroups - 1) / (imageGroups));
        });
        break;
      default:
        throw new IllegalArgumentException();
    }
    stackImages();
  }

  /**
   * This method is responsible for the sizing of each image contained, only privately invoked when necessary.
   */

  private void reArrangeImages(){
    reArrangeImages(SCROLLPANE.viewportBoundsProperty().get().getWidth(), SCROLLPANE.viewportBoundsProperty().get().getHeight());
  }
}

