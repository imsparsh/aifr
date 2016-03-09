package application;

import java.io.File;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Scene;
import javafx.scene.image.Image;

public class App extends Application {

	@SuppressWarnings("deprecation")
	@Override
	public void start(Stage primaryStage) {
		primaryStage.setMinHeight(540);
		primaryStage.setMinWidth(960);
		primaryStage.setMaxHeight(560);
		primaryStage.setMaxWidth(960);
		primaryStage.setResizable(false);
		primaryStage.setTitle("inTuiT | Age Invariant Face Recognition System");
		primaryStage.getIcons().add(new Image("file:resources/ui/icon.png"));
		FXMLLoader loader = new FXMLLoader();
		try {
			loader.setLocation(new File("src/main/java/application/App.fxml").toURL());
			Parent root = loader.load();
			Scene scene = new Scene(root, 960, 540);
			scene.getStylesheets().add("file:src/main/java/application/application.css");
			primaryStage.setScene(scene);
		} catch (Exception e) {
			e.printStackTrace();
		}
		final AppController controller = (AppController) loader.getController();
		primaryStage.addEventHandler(WindowEvent.WINDOW_SHOWN, new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent window) {
				/*
				 * Platform.runLater(() -> { });
				 */
				new Thread() {
					public void run() {
						controller.initClassifier();
					}
				}.start();
			}
		});
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
