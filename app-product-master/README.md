# app-product

This is Flagship's photo management application for the course IDATT1002 Software Engineering. Flagship is Team 1.

## Releases

Latest releases (JAR files) are published on our Google Drive folder: https://drive.google.com/drive/folders/1If3hBeItZfv3G3pevXEwgVbkD4bHV7UW?usp=sharing

## For Developers

Run the application by running the `Launcher` class in `src/java/main`. Verify that Maven configuration and dependencies are loaded.

### Requirements

* Java 11.0.6
* JavaFX 11.0.2

All dependencies including JavaFX are managed with Maven.

### Running MainStage directly

To run MainStage directly, you need a local copy of JavaFX 11.0.2:

* Download: https://gluonhq.com/download/javafx-11.0.2-sdk-windows/
* Unzip to a location of your choice, example: C:\Java

Then:

* Attempt to run/build your project once, and then;
* In your top menu: Run -> Edit Configurations
* Under VM options, add:  **--module-path "C:\Java\javafx-sdk-11.0.2\lib" --add-modules javafx.controls,javafx.fxml,javafx.web**

![Run Configuration](https://i.imgur.com/WqEOzIB.png)
