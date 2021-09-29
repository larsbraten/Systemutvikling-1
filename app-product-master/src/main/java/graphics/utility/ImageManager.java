package graphics.utility;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import graphics.gallery.ImageThumbnail;
import graphics.launcher.MainStage;
import hibernate.api.DataAPI;
import hibernate.model.Album;
import hibernate.model.ImageData;
import hibernate.model.Metadata;
import hibernate.model.User;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class ImageManager extends FileManager {

  /**
   * @author Eivind Berger-Nilsen
   * Extends the {@link FileManager} functionality to also manage images.
   * Also invokes calls to hibernate for synchronization.
   * This class provides a additional {@link TreeView} to overview image tags.
   */

  private static final Logger LOGGER = LogManager.getLogger(ImageManager.class);
  private final BiMap<File, ImageThumbnail> IMAGES = HashBiMap.create(1000);
  private final BiMap<File, ImageData> IMAGE_DATA = HashBiMap.create(1000);
  private final BiMap<File, TreeItem<String>> TREE_ITEMS = HashBiMap.create(1000);
  private final ObjectProperty<ImageData> THUMBNAIL_SELECTED_EVENT = new SimpleObjectProperty<>();
  private final User USER;
  private final DataAPI DATA_API = new DataAPI();
  private final TreeView<String> TREE_VIEW = new TreeView<>();
  private final TreeItem<String> ROOT = new TreeItem<>("Albums");

  /**
   * Initiation with login.
   */

  public ImageManager(){
    this.USER = DATA_API.login();
    ROOT.setExpanded(true);
    getTagsMenu().setRoot(ROOT);
    getTagsMenu().getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
  }

  /**
   * Triggers when the user mouse-clicks on the contained thumbnails.
   * @return Data of the corresponding image.
   */

  public ObjectProperty<ImageData> thumbnailSelectedProperty(){
    return THUMBNAIL_SELECTED_EVENT;
  }

  /**
   * Getter for the tag-menu allowing caller to add functionality.
   * @return The tag menu.
   */

  public TreeView<String> getTagsMenu(){
    return TREE_VIEW;
  }

  /**
   * Returns all present {@link ImageData} objects.
   * @return All imageData.
   */

  public Set<ImageData> getImageDataSet(){
    return IMAGE_DATA.values();
  }

  /**
   * Returns the ImageData belonging to provided file if present.
   * @param file imagefile.
   * @return The imageData.
   */

  public ImageData getImageData(File file){
    return IMAGE_DATA.get(file);
  }

  /**
   * User-method for adding images to the {@link TreeView}.
   * @param files imagefiles to add.
   */

  public void addImages(Set<File> files){
    files
        .stream()
        .filter(this::addFile)
        .forEach(file -> {
              ImageData newImageData = new ImageData();
              newImageData.setPath(file.getAbsolutePath());
              newImageData.setUser(USER);
              USER.addImage(newImageData);
              newImageData.setMetadata(Metadata.generate(file.getAbsolutePath()));
              IMAGE_DATA.forcePut(file,newImageData);
            });
    pushToHibernate();
  }

  /**
   * User-method for removing images to the {@link TreeView}
   * @param files imagefils to remove.
   */

  public void removeImages(Set<File> files){
    for (File file : files) {
      USER.deleteImage(IMAGE_DATA.remove(file));
      IMAGES.remove(file);
      TREE_ITEMS.remove(file);
      removeFile(file);
    }
    pushToHibernate();
  }

  /**
   * User-method for retrieving images given a search-constraint.
   * This search constraint will also place markers upon the file and tag-menu.
   * @param searchConstraints search constraint.
   * @return matching image thumbnails.
   */

  public List<ImageThumbnail> getImageThumbnails(String searchConstraints){
    //Places markers on treeItems.
    getFileMenu().getRoot().getChildren()
        .stream()
        .peek(treeItem -> treeItem.setGraphic(null))
        .flatMap(treeItem -> {
          if(treeItem.getValue().contains(searchConstraints) && !searchConstraints.isEmpty()) {
            ImageView fileHourGlass = new ImageView("/views/img/search.png");
            fileHourGlass.setPreserveRatio(true);
            fileHourGlass.setFitWidth(17);
            fileHourGlass.setFitHeight(17);
            treeItem.setGraphic(fileHourGlass);
          }
          return treeItem.getChildren().stream();
        }).peek(treeItem -> treeItem.setGraphic(null))
        .forEach(treeItem -> {
          if(treeItem.getValue().contains(searchConstraints) && !searchConstraints.isEmpty()){
            ImageView fileHourGlass = new ImageView("/views/img/search.png");
            fileHourGlass.setPreserveRatio(true);
            fileHourGlass.setFitWidth(17);
            fileHourGlass.setFitHeight(17);
            treeItem.setGraphic(fileHourGlass);
            if(treeItem.getParent().getGraphic() == null) {
              ImageView folderHourGlass = new ImageView("/views/img/search.png");
              folderHourGlass.setPreserveRatio(true);
              folderHourGlass.setFitWidth(17);
              folderHourGlass.setFitHeight(17);
              treeItem.getParent().setGraphic(folderHourGlass);
            }
          }
        });
    //Places markers on treeItems.
    getTagsMenu().getRoot().getChildren()
        .stream()
        .peek(treeItem -> treeItem.setGraphic(null))
        .flatMap(treeItem -> {
          if(treeItem.getValue().contains(searchConstraints) && !searchConstraints.isEmpty()) {
            ImageView fileHourGlass = new ImageView("/views/img/search.png");
            fileHourGlass.setPreserveRatio(true);
            fileHourGlass.setFitWidth(17);
            fileHourGlass.setFitHeight(17);
            treeItem.setGraphic(fileHourGlass);
          }
          return treeItem.getChildren().stream();
        }).peek(treeItem -> treeItem.setGraphic(null))
        .forEach(treeItem -> {
          if(treeItem.getValue().contains(searchConstraints) && !searchConstraints.isEmpty()){
            ImageView fileHourGlass = new ImageView("/views/img/search.png");
            fileHourGlass.setPreserveRatio(true);
            fileHourGlass.setFitWidth(17);
            fileHourGlass.setFitHeight(17);
            treeItem.setGraphic(fileHourGlass);
            if(treeItem.getParent().getGraphic() == null) {
              ImageView folderHourGlass = new ImageView("/views/img/search.png");
              folderHourGlass.setPreserveRatio(true);
              folderHourGlass.setFitWidth(17);
              folderHourGlass.setFitHeight(17);
              treeItem.getParent().setGraphic(folderHourGlass);
            }
          }
        });

    //Filters images based on search constraint
    return getFiles()
        .stream()
        .map(this::procureImageThumbnail)
        .filter(imageThumbnail -> imageThumbnail.getImageView().getId().contains(searchConstraints) ||
            IMAGE_DATA.get(IMAGES.inverse().get(imageThumbnail)).getTags().toString().contains(searchConstraints)
        || IMAGE_DATA.get(IMAGES.inverse().get(imageThumbnail)).getMetadata().toString().contains(searchConstraints))
        .collect(Collectors.toList());
  }

  /**
   * User-method to procure a viewable image given it's file.
   * This method retrieves a pre-configured 800x600 image.
   * @param file Image-file.
   * @return The thumbnail.
   */

  public ImageThumbnail procureImageThumbnail(File file){
    return procureImageThumbnail(file,800,600);
  }

  /**
   * User-method to procure a viewable image given it's file.
   * This method retrieves a pre-configured 800x600 image.
   * @param file Image-file.
   * @param width Desired width.
   * @param height Desired height.
   * @return A imageThumbnail.
   */

  public ImageThumbnail procureImageThumbnail(File file, int width, int height) {
    return IMAGES.computeIfAbsent(file, fileKey -> {
      try {
        ImageThumbnail newImageThumbnail = new ImageThumbnail(new ImageView(new Image(new FileInputStream(fileKey), width, height, true, true)));
        newImageThumbnail.getImageView().setId(fileKey.getName());
        newImageThumbnail.setOnMouseClicked(event -> {
          THUMBNAIL_SELECTED_EVENT.setValue(IMAGE_DATA.get(fileKey));
          THUMBNAIL_SELECTED_EVENT.setValue(null);
        });
        return newImageThumbnail;
      } catch (FileNotFoundException fileNotFoundException) {
        LOGGER.error(fileNotFoundException.getMessage());
      }
      return null;
    });
  }

  /**
   * User-method for adding tags to selected pictures from the {@link TreeView}.
   * @param selectedFiles TreeItems representing the images.
   * @return true if registered.
   */

  public boolean addToTags(Collection<TreeItem<String>> selectedFiles){
    Set<ImageData> imageDataList = reduceTreeItems(selectedFiles)
        .stream()
        .map(treeItem -> getImageData(getFile(treeItem)))
        .collect(Collectors.toSet());

    String tag = MainStage.UserPrompter.queryName("Please choose a tag.");
    if(!tag.isBlank()){
      imageDataList.forEach(imageData -> {
        if(!imageData.getTags().contains(tag)){
          imageData.getTags().add(tag);
        }
      });
      pushToHibernate();
      refreshTagsTree();
      return true;
    }
    return false;
  }

  /**
   * User-method for removing tags from selected pictures from the {@link TreeView}.
   * @param treeItems TreeItems representing the images.
   *
   */

  public void deleteFromTags(List<TreeItem<String>> treeItems){
    treeItems.stream().forEach(treeItem -> {
      for (ImageData current : USER.getImages()) {
        if (current.getPath().contains(treeItem.getValue())) {
          current.getTags().remove(treeItem.getParent().getValue());
        }
      }
    });

    pushToHibernate();
    refreshTagsTree();
  }

  /**
   * Method invoked to refresh tags treeView.
   */

  private void refreshTagsTree(){
    List<TreeItem<String>> treeItemsList = new ArrayList<TreeItem<String>>();
    USER.getImagesByTags().forEach((key, value) -> {
      TreeItem<String> tagTreeItem = new TreeItem<>(key);
      tagTreeItem.getChildren().setAll(
              value.stream()
                      .map(imageData -> new TreeItem<>(IMAGE_DATA.inverse().get(imageData).getName()))
                      .collect(Collectors.toList())
      );
      treeItemsList.add(tagTreeItem);
    });
    ROOT.getChildren().setAll(treeItemsList);
  }

  /**
   * Privately invoked for push changes to hibernate.
   */

  private void pushToHibernate(){
    DATA_API.saveUser(USER);
  }

  /**
   * User-method for pulling data.
   */

  public void pullFromHibernate(){
    List<ImageData> deleteList = new ArrayList<>();

    USER.getImages()
        .stream()
        .filter(imageData -> {
          File newFile = new File(imageData.getPath());
          if(addFile(newFile)){
            IMAGE_DATA.inverse().forcePut(imageData, newFile);
            return false;
          }else return true;
        }).forEach(deleteList::add);

        deleteList.forEach(imageData -> {
      LOGGER.info("File: " + imageData.getPath() + " not found locally.\n" +
          "Deleting reference from remote SQL.");
      USER.deleteImage(imageData);
    });
    refreshTagsTree();
  }
}
