package com.carbonaudit.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controlador de P0 · Selección de rol.
 *
 * En el patrón MVC de JavaFX:
 *   - La Vista  es el archivo FXML (p0-seleccion-rol-view.fxml).
 *   - El Modelo son las clases del paquete model/ y dao/.
 *   - El Controlador (esta clase) conecta la vista con el modelo
 *     y gestiona los eventos del usuario.
 *
 * JavaFX instancia este controlador automáticamente al cargar el FXML
 * gracias al atributo fx:controller del nodo raíz.
 */
public class SeleccionRolController {

    /*
     * @FXML inyecta automáticamente el nodo cuyo fx:id en el FXML
     * coincide con el nombre del campo. Si el nombre no coincide,
     * el campo quedará null y lanzará NullPointerException al usarlo.
     */
    @FXML
    private VBox tarjetaAdmin;

    @FXML
    private VBox tarjetaResponsable;

    /**
     * initialize() es llamado por JavaFX justo después de cargar el FXML
     * y de inyectar todos los @FXML. Es el lugar correcto para configurar
     * el estado inicial de la pantalla.
     */
    @FXML
    public void initialize() {
        // En esta pantalla no hay estado inicial que preparar.
        // El efecto hover de las tarjetas se gestiona directamente en CSS (:hover),
        // por lo que no necesitamos código Java para ello.
    }

    /**
     * Se ejecuta al hacer clic en la tarjeta "Administrador".
     * El parámetro MouseEvent contiene información sobre el clic
     * (posición, botón pulsado, nodo origen...).
     */
    @FXML
    private void onAdministradorClick(MouseEvent event) {
        navegarA("/com/carbonaudit/view/pa1-empresas-view.fxml", event);
    }

    /**
     * Se ejecuta al hacer clic en la tarjeta "Responsable".
     */
    @FXML
    private void onResponsableClick(MouseEvent event) {
        navegarA("/com/carbonaudit/view/pr0-seleccion-responsable-view.fxml", event);
    }

    /**
     * Cambia la escena activa cargando un nuevo FXML.
     *
     * Flujo de navegación en JavaFX:
     *   1. FXMLLoader lee el archivo .fxml y construye el árbol de nodos.
     *   2. Se crea una nueva Scene con ese árbol.
     *   3. Se obtiene el Stage (ventana) actual desde el nodo que disparó el evento.
     *   4. Se asigna la nueva Scene al Stage → la ventana muestra la nueva pantalla.
     *
     * @param rutaFxml ruta al archivo FXML dentro de resources (debe empezar por /)
     * @param event    evento del ratón, necesario para obtener la referencia al Stage
     */
    private void navegarA(String rutaFxml, MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFxml));
            Scene nuevaEscena = new Scene(loader.load());

            // Subimos por el árbol: nodo origen → escena → ventana (Stage)
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

            // Las pantallas interiores son redimensionables (P0 era fija)
            stage.setResizable(true);
            stage.setScene(nuevaEscena);
            stage.sizeToScene();
            stage.centerOnScreen();

        } catch (Exception e) {
            System.err.println("Error cargando: " + rutaFxml);
            e.printStackTrace();
        }
    }
}
