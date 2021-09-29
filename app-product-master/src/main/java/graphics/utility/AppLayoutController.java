package graphics.utility;

import graphics.gallery.BigPicture;
import graphics.gallery.ImageThumbnail;
import graphics.gallery.Orientation;
import graphics.gallery.GalleryPane;
import graphics.launcher.MainStage;
import hibernate.model.ImageData;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import map.Map;
import org.apache.log4j.Logger;
import utility.Device;

public class AppLayoutController implements Initializable, ChangeListener<ImageData> {

    /**
     * @author Karl Labrador, Eivind Berger-Nilsen
     * The controller-class for most static content in the program.
     */

    //FXML loaded
    @FXML private StackPane contentLayer;
    @FXML private SplitPane mainSplitPane;
    @FXML private Text identityText;
    @FXML private Button fullScreenToggleBtn;
    @FXML private Button zoomInBtn;
    @FXML private Button zoomOutBtn;
    @FXML private Button toggleSideBarBtn;
    @FXML private Button imagesBtnRotate;
    @FXML private Button albumsBtn;
    @FXML private Button mapBtn;
    @FXML private MenuButton addBtn;
    @FXML private MenuItem addImageBtn;
    @FXML private MenuItem addFolderBtn;
    @FXML private TextField searchField;
    @FXML private Button searchFieldClearBtn;

    //Other
    private static final Logger LOGGER = Logger.getLogger(AppLayoutController.class);
    private final WebView MAP_VIEW = Map.initializeMap();
    private final ImageManager IMAGE_MANAGER = new ImageManager();
    private final GalleryPane GALLERY_VIEW = new GalleryPane(250,250,5, Orientation.HORIZONTAL,null);
    private static final BooleanProperty REQUEST_FULLSCREEN = new SimpleBooleanProperty(false);

    //Context Menu
    private final MenuItem OPEN_FILE = new MenuItem("Open selected files");
    private final MenuItem ADD_FILES = new MenuItem("Add files");
    private final MenuItem ADD_FOLDER = new Menu("Add folder");
    private final MenuItem REMOVE_FILES = new MenuItem("Remove selected files");
    private final MenuItem PRINT_TO_PDF = new MenuItem("Print selected files");
    private final MenuItem SET_TAGS = new MenuItem("Tag pictures");
    private final MenuItem REMOVE_TAGS = new MenuItem("Remove tag");
    private final MenuItem INFO = new MenuItem("Properties");
    private final MenuItem TOGGLE_CONVERGENT_SCROLL = new MenuItem("Toggle scroll convergence");
    private final ContextMenu FILE_CONTEXT_MENU = new ContextMenu();
    private final ContextMenu ALBUM_CONTEXT_MENU = new ContextMenu();


    /**
     * Invoked on program initialization.
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        /*

        Initialization to load content.

         */

        IMAGE_MANAGER.pullFromHibernate();
        identityText.setText(String.format("Logged in as %s with UUID %s", Device.getUsername(), Device.getUUID()));
        mainSplitPane.getItems().setAll(IMAGE_MANAGER.getFileMenu(), GALLERY_VIEW);
        mainSplitPane.setDividerPositions(0.25);
        mainSplitPane.getDividers().addListener((ListChangeListener<? super SplitPane.Divider>) c -> {
            c.next();
            double[] doubles = c.getRemoved().stream().mapToDouble(SplitPane.Divider::getPosition).toArray();
            if(!mainSplitPane.isHover()) mainSplitPane.setDividerPositions(doubles);
        });
        GALLERY_VIEW.setConvergentScrolling(false);
        GALLERY_VIEW.getContent().setAll(IMAGE_MANAGER.getImageThumbnails(""));
        IMAGE_MANAGER.getTagsMenu().setContextMenu(ALBUM_CONTEXT_MENU);
        IMAGE_MANAGER.getFileMenu().setContextMenu(FILE_CONTEXT_MENU);
        Map.getFullImageRequest().addListener(this);
        IMAGE_MANAGER.thumbnailSelectedProperty().addListener(this);
        IMAGE_MANAGER.getFileMenu().setShowRoot(false);
        IMAGE_MANAGER.getTagsMenu().setShowRoot(false);


        /*

        Just a bunch of eventhandlers and listener to being initilized.

         */

        //MenuItems
        IMAGE_MANAGER.getFileMenu()
            .getSelectionModel().getSelectedItems().addListener((ListChangeListener<? super TreeItem<String>>) c -> {
                c.next();
                if(c.getList().size() > 0){
                    FILE_CONTEXT_MENU.getItems().setAll(OPEN_FILE, ADD_FILES, ADD_FOLDER,
                        REMOVE_FILES, PRINT_TO_PDF, SET_TAGS, TOGGLE_CONVERGENT_SCROLL);
                }else if(c.getList().stream().allMatch(TreeItem::isLeaf) && c.getList().size() == 1) {
                    FILE_CONTEXT_MENU.getItems().setAll(OPEN_FILE, ADD_FILES, ADD_FOLDER,
                        REMOVE_FILES, PRINT_TO_PDF, SET_TAGS, TOGGLE_CONVERGENT_SCROLL, INFO);
                }else{
                    FILE_CONTEXT_MENU.getItems().setAll(TOGGLE_CONVERGENT_SCROLL);
                }
        });
        IMAGE_MANAGER.getTagsMenu()
            .getSelectionModel().getSelectedItems().addListener((ListChangeListener<? super TreeItem<String>>)( c -> {
                ALBUM_CONTEXT_MENU.getItems().setAll(REMOVE_TAGS);
            }));
        REMOVE_TAGS.setOnAction(event -> {
            IMAGE_MANAGER.deleteFromTags(IMAGE_MANAGER.getTagsMenu().getSelectionModel().getSelectedItems());
        });
        OPEN_FILE.setOnAction(event -> {
            Set<TreeItem<String>> leafs = IMAGE_MANAGER.reduceTreeItems(IMAGE_MANAGER.getFileMenu().getSelectionModel().getSelectedItems());

            List<ImageData> imageData = leafs
                .stream()
                .map(treeItem -> IMAGE_MANAGER.getImageData(IMAGE_MANAGER.getFile(treeItem)))
                .collect(Collectors.toList());

            Set<ImageThumbnail> imageThumbnails = leafs
                .stream()
                .map(treeItem -> IMAGE_MANAGER.procureImageThumbnail(IMAGE_MANAGER.getFile(treeItem)))
                .collect(Collectors.toSet());

            GALLERY_VIEW.getContent().setAll(imageThumbnails);
            Map.addMarkers(imageData);

            IMAGE_MANAGER.getFileMenu().getRoot().getChildren()
            .stream()
            .peek(treeItem -> treeItem.setGraphic(null))
            .flatMap(treeItem -> treeItem.getChildren().stream())
            .forEach(treeItem -> treeItem.setGraphic(null));
            IMAGE_MANAGER.reduceTreeItems(IMAGE_MANAGER.getFileMenu().getSelectionModel().getSelectedItems())
                .forEach(treeItem -> {
                    ImageView selectedImage = new ImageView("/views/img/show_selected.png");
                    selectedImage.setFitHeight(17);
                    selectedImage.setFitWidth(17);
                    treeItem.setGraphic(selectedImage);
                });
        });
        TOGGLE_CONVERGENT_SCROLL.setOnAction(event -> {
            GALLERY_VIEW.setConvergentScrolling(!GALLERY_VIEW.getConvergentScrolling());
        });
        ADD_FILES.setOnAction(event -> {
            Set<File> selectedFiles = MainStage.UserPrompter.queryFiles();
            if(selectedFiles != null){
                IMAGE_MANAGER.addImages(selectedFiles);
                GALLERY_VIEW.getContent().setAll(IMAGE_MANAGER.getImageThumbnails(""));
                Map.addMarkers(new ArrayList<>(IMAGE_MANAGER.getImageDataSet()));
            }
            else event.consume();
        });
        ADD_FOLDER.setOnAction(event -> {
            Set<File> selectedFiles = MainStage.UserPrompter.queryDirectory();
            if(selectedFiles != null){
                IMAGE_MANAGER.addImages(selectedFiles);
                GALLERY_VIEW.getContent().setAll(IMAGE_MANAGER.getImageThumbnails(""));
                Map.addMarkers(new ArrayList<>(IMAGE_MANAGER.getImageDataSet()));
            }
            else event.consume();
        });
        REMOVE_FILES.setOnAction(event -> {
            if(IMAGE_MANAGER.getFileMenu().getSelectionModel().getSelectedItems() != null){
                IMAGE_MANAGER.removeImages(IMAGE_MANAGER.getFiles(IMAGE_MANAGER.getFileMenu().getSelectionModel().getSelectedItems()));
                GALLERY_VIEW.getContent().setAll(IMAGE_MANAGER.getImageThumbnails(""));
                Map.addMarkers(new ArrayList<>(IMAGE_MANAGER.getImageDataSet()));
            }
            else event.consume();
        });
        PRINT_TO_PDF.setOnAction(event -> {
            if(IMAGE_MANAGER.getFileMenu().getSelectionModel().getSelectedItems() != null)
                MainStage.UserPrompter.printPDF(IMAGE_MANAGER.getFiles(IMAGE_MANAGER.getFileMenu().getSelectionModel().getSelectedItems()));
        });
        SET_TAGS.setOnAction(event -> {
            IMAGE_MANAGER.addToTags(IMAGE_MANAGER.getFileMenu().getSelectionModel().getSelectedItems());
        });
        INFO.setOnAction(event -> {
            List<TreeItem<String>> selected = IMAGE_MANAGER.getFileMenu().getSelectionModel().getSelectedItems();
            if(selected.size() == 1){
                ImageData correspondingImageData = IMAGE_MANAGER.getImageData(IMAGE_MANAGER.getFile(selected.get(0)));
                MainStage.UserPrompter.showMessage("Property", correspondingImageData.getPath(), BigPicture.getReadable(correspondingImageData));
            }
        });

        // Buttons
        fullScreenToggleBtn.setOnAction(event -> REQUEST_FULLSCREEN.setValue(!REQUEST_FULLSCREEN.get()));
        searchFieldClearBtn.setOnAction(a -> searchField.setText(""));
        toggleSideBarBtn.setOnAction(event -> {
            if(mainSplitPane.getItems().get(0) instanceof TreeView) mainSplitPane.getItems().remove(0);
            else mainSplitPane.getItems().add(0,albumsBtn.getId().equals("albumsBtnFile") ? IMAGE_MANAGER.getTagsMenu() : IMAGE_MANAGER.getFileMenu());
        });
        MAP_VIEW.getEngine().setOnStatusChanged(engineEvent -> {
            mapBtn.setOnAction(buttonEvent -> {
                mainSplitPane.getItems().replaceAll(node -> node instanceof GalleryPane ? MAP_VIEW : node);
                Map.addMarkers(new ArrayList<>(IMAGE_MANAGER.getImageDataSet()));
                imagesBtnRotate.setId("imagesBtn");
                imagesBtnRotate.setText("Gallery");
            });
        });
        imagesBtnRotate.setOnAction(event -> {
            if(imagesBtnRotate.getId().equals("imagesBtnRotate")){
                GALLERY_VIEW.setOrientation(GALLERY_VIEW.getOrientation().equals(Orientation.HORIZONTAL) ? Orientation.VERTICAL : Orientation.HORIZONTAL);
                GALLERY_VIEW.setMinNodeTargetLength(GALLERY_VIEW.getOrientation().equals(Orientation.HORIZONTAL) ? 250 : 150);
            }
            else {
                mainSplitPane.getItems().replaceAll(node -> node instanceof WebView ? GALLERY_VIEW : node);
                imagesBtnRotate.setId("imagesBtnRotate");
                imagesBtnRotate.setText("Rotate");
            }
        });
        albumsBtn.setOnAction(event -> {
            if(albumsBtn.getId().equals("albumsBtnFile")){
                mainSplitPane.getItems().replaceAll(node -> node instanceof TreeView ? IMAGE_MANAGER.getFileMenu() : node);
                albumsBtn.setId("albumsBtn");
                albumsBtn.setText("Tags");
            } else {
                mainSplitPane.getItems().replaceAll(node -> node instanceof TreeView ? IMAGE_MANAGER.getTagsMenu() : node);
                albumsBtn.setId("albumsBtnFile");
                albumsBtn.setText("Files");
            }
        });
        addImageBtn.setOnAction(event -> {
            Set<File> images = MainStage.UserPrompter.queryFiles();
            if(images == null) event.consume();
            else {
                IMAGE_MANAGER.addImages(images);
                GALLERY_VIEW.getContent().setAll(IMAGE_MANAGER.getImageThumbnails(""));
                Map.addMarkers(new ArrayList<>(IMAGE_MANAGER.getImageDataSet()));
            }
        });
        addFolderBtn.setOnAction(event -> {
            Set<File> images = MainStage.UserPrompter.queryDirectory();
            if(images == null) event.consume();
            else {
                IMAGE_MANAGER.addImages(images);
                GALLERY_VIEW.getContent().setAll(IMAGE_MANAGER.getImageThumbnails(""));
                Map.addMarkers(new ArrayList<>(IMAGE_MANAGER.getImageDataSet()));
            }
        });
        zoomInBtn.setOnAction(event -> GALLERY_VIEW.zoomIn());
        zoomOutBtn.setOnAction(event -> GALLERY_VIEW.zoomOut());
        searchField.textProperty().addListener((observable, oldValue, newValue) ->
            GALLERY_VIEW.getContent().setAll(IMAGE_MANAGER.getImageThumbnails(newValue)));
    }


    /**
     * Property for requesting full-screen to the stage-class.
     * @return The listener object.
     */

    public static BooleanProperty getRequestFullScreenProperty(){
        return REQUEST_FULLSCREEN;
    }

    /**
     * Triggered when either {@link Map} or {@link GalleryPane} wants to display a full-screen image
     * {@link BigPicture}.
     * @param observable The listener object itself.
     * @param oldValue The prior value.
     * @param newValue The new value.
     */

    @Override
    public void changed(ObservableValue<? extends ImageData> observable, ImageData oldValue, ImageData newValue) {
        if(newValue != null){
            contentLayer.getChildren().removeIf(node -> node instanceof ImageView);
            BigPicture bigPicture = new BigPicture(newValue);

            contentLayer.getChildren().add(bigPicture);
            bigPicture.prefWidthProperty().bind(contentLayer.widthProperty());
            bigPicture.prefHeightProperty().bind(contentLayer.heightProperty());
            contentLayer.getChildren().forEach(node -> {
                if(!(node instanceof BigPicture)){
                    ColorAdjust effect = new ColorAdjust(0,0,-0.7, 0);
                    effect.setInput(new GaussianBlur());
                    node.setEffect(effect);
                }
            });
            bigPicture.setOnMouseClicked(event -> {
                contentLayer.getChildren().removeIf(node -> node instanceof BigPicture);
                contentLayer.getChildren().forEach(node -> {
                    if(!(node instanceof BigPicture)) node.setEffect(null);
                });
            });
        }
    }
}
