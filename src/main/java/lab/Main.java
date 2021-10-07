package lab;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);

        primaryStage.getIcons().add(
                new Image(getClass().getResourceAsStream("main-icon.png"))
        );
        primaryStage.setScene(scene);
        primaryStage.setTitle("Лабораторная работа по Методам Оптимизации");
        primaryStage.show();
    }
}
