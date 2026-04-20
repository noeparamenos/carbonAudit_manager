package com.carbonaudit;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
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
			getClass().getResource("/com/carbonaudit/view/main-view.fxml")
		);


		// Crear una Scene a partir del FXML
		Scene scene = new Scene(loader.load());

		// Configurar la ventana principal
		primaryStage.setTitle("CarbonAudit Manager");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	// Punto de entrada de la aplicación (main tradicional de Java)
	public static void main(String[] args) {
		// inicia la aplicación JavaFX y llama a start()
		launch(args);
	}
}
