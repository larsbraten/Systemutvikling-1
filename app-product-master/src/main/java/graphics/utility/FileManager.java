package graphics.utility;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import utility.Device;

/**
 * @author Eivind Berger-Nilsen
 * Objects like {@link TreeItem} may prove useful for navigating, but sometimes you may need to
 * cross-reference it to some other object, say a file, image, string, etc. This class attempts to bridge
 * this gap to some extent. By utlilizing {@link BiMap}, the value can be obtained by key, and vice versa.
 * In short, this class primarly manages files, and maintaines order in the treeview.
 *
 */

public class FileManager {

  private final Logger LOGGER = LogManager.getLogger(getClass());
  private final BiMap<File, TreeItem<String>> FILES = HashBiMap.create(1000);
  private final BiMap<File, TreeItem<String>> FOLDERS = HashBiMap.create(30);
  private final TreeItem<String> ROOT = new TreeItem<>(Device.getUsername());
  private final TreeView<String> TREE_VIEW = new TreeView<>();

  /**
   * Default settings.
   */
  public FileManager(){
    TREE_VIEW.setRoot(ROOT);
    TREE_VIEW.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    ROOT.setExpanded(true);
  }

  /**
   * Default setting.
   * @param files files to include.
   */
  public FileManager(Set<File> files) {
    this();
    addFiles(files);
  }

  /**
   * Returns the {@link TreeView} for further implementation.
   * @return The file menu.
   */

  protected TreeView<String> getFileMenu() {
    return TREE_VIEW;
  }

  /**
   * User method to add files.
   * @param files files to add.
   */

  protected void addFiles(Set<File> files){
    LOGGER.info("Added: " +
        files
            .stream()
            .peek(this::addFile)
            .count() + " files.");
  }

  /**
   *
   * @param file User method to add a file.
   * @return true if success, false if not added.
   */

  protected boolean addFile(File file){
    try{
      if(file.isFile()) addFileTreeItem(file);
      else LOGGER.info("Added: " +
          Arrays.stream(file.listFiles())
              .filter(this::addFileTreeItem)
              .count() + " files");
      return true;
    }catch (NullPointerException e){
      LOGGER.error(e);
      return false;
    }
  }

  /**
   * User method for removing files.
   * @param files files to remove.
   */

  protected void removeFiles(Set<File> files) {
    LOGGER.info("Removed: " +
        files
            .stream()
            .flatMap(file -> file.isFile() ? Stream.of(file) : Stream.of(file.listFiles()))
            .filter(this::removeFileTreeItem)
            .count() + " files"
    );
  }

  /**
   * User method which accepts both directories/files, and removes them.
   * @param file file to remove.
   */

  protected void removeFile(File file){
    if(file.isFile() && removeFileTreeItem(file)) LOGGER.info("Removing file: " + file.getAbsolutePath());
    else LOGGER.info("Removed: " +
        Arrays.stream(file.listFiles())
            .filter(this::removeFileTreeItem)
            .count() + " files");
  }

  /**
   * User method to remove files based on the provided {@link TreeItem}.
   * @param treeItems treeItems from the file menu.
   */

  protected void removeFiles(Collection<TreeItem<String>> treeItems){
    LOGGER.info("Removed: " +
        reduceTreeItems(treeItems)
            .stream()
            .filter(treeItem -> removeFileTreeItem(FILES.inverse().get(treeItem)))
            .count() + " files"
    );
  }

  /**
   * User method to load file objects.
   * @param treeItems {@link TreeView} from the file menu.
   * @return The present files.
   */

  protected Set<File> getFiles(Collection<TreeItem<String>> treeItems){
    return reduceTreeItems(treeItems)
        .stream()
        .map(this::getFile)
        .peek(file -> LOGGER.info("Collecting file: " + file.getAbsolutePath()))
        .collect(Collectors.toSet());
  }

  /**
   * Retrieve a file given a {@link TreeView} from the filemenu.
   * @param treeItem treeItems from the file menu.
   * @return the files.
   */

  protected File getFile(TreeItem<String> treeItem){
    File collectedFile = FILES.inverse().get(treeItem);
    LOGGER.info("Collecting file: " + collectedFile.getAbsolutePath());
    return collectedFile;
  }

  /**
   * Retrieves all present files.
   * @return all files.
   */

  protected Set<File> getFiles(){
    return FILES.inverse().values()
        .stream()
        .peek(file -> LOGGER.info("Collecting file" + file.getAbsolutePath()))
        .collect(Collectors.toSet());
  }

  /**
   * Recursive method to yield all {@link TreeItem} leafs provided a collection given.
   * A parent treeItem yields all underlying children. No duplicated returned.
   * @param treeItems Collection of various treeItems.
   * @return Extracted leaf treeItems.
   */

  protected Set<TreeItem<String>> reduceTreeItems(Collection<TreeItem<String>> treeItems){
    LOGGER.info("Recursive MenuItem breakdown in progress...");
    return treeItems
        .stream()
        .flatMap(treeItem -> treeItem.isLeaf() ? Stream.of(treeItem) : reduceTreeItems(treeItem.getChildren()).stream())
        .peek(treeItem -> LOGGER.info("Collected treeItem: " + treeItem.getValue()))
        .collect(Collectors.toSet());
  }

  /**
   * Recursive method to yield all {@link TreeItem} leafs provided a collection given.
   * A parent treeItem yields all underlying children. No duplicated returned.
   * @param treeItems Any treeItem.
   * @return Extracted leaf treeItems.
   */

  protected Set<TreeItem<String>> reduceTreeItems(TreeItem<String> treeItems){
    LOGGER.info("Recursive MenuItem breakdown in progress...");
    return treeItems.isLeaf() ? Collections.singleton(treeItems) : treeItems.getChildren()
        .stream()
        .flatMap(treeItem -> treeItem.isLeaf() ? Stream.of(treeItem) : reduceTreeItems(treeItem.getChildren()).stream())
        .peek(treeItem -> LOGGER.info("Collected treeItem: " + treeItem.getValue()))
        .collect(Collectors.toSet());
  }

  /**
   * non-user method to add treeItems to the context menu.
   * @param file the file to add.
   * @return true if success.
   */

  private boolean addFileTreeItem(File file){
    return FILES.computeIfAbsent(file, fileKey -> {
      LOGGER.info("Adding file: " + fileKey.getAbsolutePath());
      FOLDERS.computeIfAbsent(fileKey.getParentFile(), directoryKey -> {
        LOGGER.info("New folder detected: " + directoryKey.getAbsolutePath());
        TreeItem<String> newDirectory = new TreeItem<>(directoryKey.getAbsolutePath());
        ROOT.getChildren().add(newDirectory);
        return newDirectory;
      });
      TreeItem<String> newFile = new TreeItem<>(fileKey.getName());
      FOLDERS.get(fileKey.getParentFile()).getChildren().add(newFile);
      return newFile;
    }).equals(FILES.get(file));
  }

  /**
   * non-user method to remove treeItems from the context-menu.
   * @param file the file to remove.
   * @return true of success
   */

  private boolean removeFileTreeItem(File file){
    LOGGER.info("Removing if present: " + file.getAbsolutePath());
    boolean removedFlag = false;
    TreeItem<String> directory = FOLDERS.get(file.getParentFile());

    if(FILES.containsKey(file)){
      removedFlag = directory.getChildren().remove(FILES.get(file)) &&
          FILES.remove(file) != null;
    }
    if(directory.isLeaf()) ROOT.getChildren().remove(FOLDERS.remove(file.getParentFile()));
    return removedFlag;
  }
}
