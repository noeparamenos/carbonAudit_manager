package com.carbonaudit.controller;

import com.carbonaudit.model.Responsable;
import com.carbonaudit.service.ServicioGestionResponsable;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

/**
 * Controlador de PR0 · Selección de responsable activo.
 *
 * Muestra la lista de mandatos con fecha_fin IS NULL para que cada responsable
 * se identifique eligiendo su nombre.
 *
 * Al seleccionar un responsable navega a PR1, pasando el objeto Responsable
 * al controlador destino para filtrar todos los datos al departamento correspondiente.
 */
public class PR0Controller {

    // Tabla de responsables activos
    @FXML private TableView<Responsable>           tablaResponsables;
    @FXML private TableColumn<Responsable, String> colNombre;
    @FXML private TableColumn<Responsable, String> colDepartamento;
    @FXML private TableColumn<Responsable, String> colEmpresa;

    private final ServicioGestionResponsable servicioResponsable = new ServicioGestionResponsable();

    // Estado (lista Observable a la que se suscribe la tabla)
    private final ObservableList<Responsable> listaResponsables = FXCollections.observableArrayList();

    //======================== Métodos ==================================

    /**
     * Se ejecuta automáticamente al cargar el FXML.
     * Configura las columnas, el comportamiento de clic y carga los datos.
     */
    @FXML
    public void initialize() {
        configurarColumnas();       // define qué campo muestra cada columna
        configurarClicEnFila();     // define qué ocurre al hacer clic en una fila
        cargarResponsables();       // conecta la lista Observable a la tabla y carga los datos
    }


    /**
     * Asocia cada columna con el campo del objeto Responsable que debe mostrar.
     * La empresa se obtiene por composición a través del departamento.
     */
    private void configurarColumnas() {
        tablaResponsables.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // Para cada fila, la lambda recibe el objeto Responsable (data) y extrae el campo a mostrar
        colNombre.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getEncargado().getNombre()));

        colDepartamento.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDepartamento().getNombre()));

        // La empresa se obtiene por composición: Responsable → Departamento → Empresa
        colEmpresa.setCellValueFactory(data -> {
            var empresa = data.getValue().getDepartamento().getEmpresa();
            return new SimpleStringProperty(empresa != null ? empresa.getNombreSocial() : "—");
        });
    }

    /**
     * Configura la navegación a PR1 con un clic en la fila del responsable.
     * Mismo patrón que PA1 → PA2: un clic selecciona e inmediatamente navega.
     */
    private void configurarClicEnFila() {
        // setRowFactory crea una fila personalizada por cada elemento de la tabla
        tablaResponsables.setRowFactory(tv -> {
            TableRow<Responsable> fila = new TableRow<>();
            // Al hacer clic en una fila con datos, navega a PR1 con ese responsable
            fila.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && !fila.isEmpty()) {
                    navegarAPR1(fila.getItem()); // fila.getItem() devuelve el Responsable de esa fila
                }
            });
            return fila;
        });
    }

    // ===================== Carga de datos ======================

    /**
     * Consulta todos los mandatos con fecha_fin IS NULL y los muestra en la tabla.
     */
    private void cargarResponsables() {
        listaResponsables.setAll(servicioResponsable.getAllActivos());
        tablaResponsables.setItems(listaResponsables);             // suscribe la tabla a la lista
    }

    // ===================== Navegación ======================

    /**
     * Vuelve a P0 (selección de rol) al hacer clic en "Inicio" del breadcrumb.
     */
    @FXML
    private void onVolverAP0() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/carbonaudit/view/p0-seleccion-rol-view.fxml"));
            Scene escena = new Scene(loader.load());
            // Obtiene la ventana actual a través de cualquier nodo visible y cambia la escena
            Stage stage = (Stage) tablaResponsables.getScene().getWindow();
            stage.setScene(escena);    // reemplaza el contenido de la ventana
            stage.sizeToScene();       // ajusta el tamaño de la ventana al de la nueva escena
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Navega a PR1 pasando el responsable seleccionado al controlador destino.
     * Mismo patrón que PA1 → PA2: cargar FXML, obtener controlador, pasar objeto, mostrar.
     *
     * @param responsable el responsable activo seleccionado en la tabla
     */
    private void navegarAPR1(Responsable responsable) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/carbonaudit/view/pr1-responsable-view.fxml"));
            Scene escena = new Scene(loader.load());
            PR1Controller pr1 = loader.getController(); // obtiene el controlador del FXML cargado
            pr1.setResponsable(responsable);             // pasa el responsable seleccionado a PR1
            // Obtiene la ventana actual y cambia la escena
            Stage stage = (Stage) tablaResponsables.getScene().getWindow();
            stage.setScene(escena);    // reemplaza el contenido de la ventana
            stage.sizeToScene();       // ajusta el tamaño de la ventana al de la nueva escena
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}