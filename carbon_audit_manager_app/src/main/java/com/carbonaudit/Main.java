package com.carbonaudit;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Clase principal de entrada en la aplicación.
 * Se basa en la implemantación de JavaFX
 */
public class Main extends Application {

	// Punto de entrada: Se llama automáticamente al invocar a `launch()`
	@Override
	public void start(Stage primaryStage) throws Exception {
		// Cargar el archivo FXML desde resources
		FXMLLoader loader = new FXMLLoader(
			getClass().getResource("/com/carbonaudit/view/p0-seleccion-rol-view.fxml")
		);


		// Crear una Scene a partir del FXML
		Scene scene = new Scene(loader.load());

		// Configurar la ventana principal
		primaryStage.setTitle("CarbonAudit Manager");
		primaryStage.setScene(scene);

		// Pantalla de inicio: tamaño fijo, no redimensionable
		primaryStage.setResizable(false);

		primaryStage.show();

		// Doble runLater: el WM de Linux reposiciona la ventana después del primer ciclo,
		// el segundo callback se ejecuta tras ese reposicionamiento y gana la carrera.
		Platform.runLater(() -> Platform.runLater(() -> {
			Rectangle2D pantalla = Screen.getPrimary().getVisualBounds();
			primaryStage.setX((pantalla.getWidth()  - primaryStage.getWidth())  / 2);
			primaryStage.setY((pantalla.getHeight() - primaryStage.getHeight()) / 2);
		}));
	}

	// Punto de entrada de la aplicación (main tradicional de Java)
	public static void main(String[] args) {
		// inicia la aplicación JavaFX y llama a start()
		launch(args);
	}
}
