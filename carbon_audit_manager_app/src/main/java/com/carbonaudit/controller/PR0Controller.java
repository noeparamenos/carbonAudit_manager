package com.carbonaudit.controller;

import com.carbonaudit.dao.ResponsableDAO;
import com.carbonaudit.model.Responsable;
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

    // DAOs
    private final ResponsableDAO responsableDAO = new ResponsableDAO();

    // Estado
    private final ObservableList<Responsable> listaResponsables = FXCollections.observableArrayList();


    /**
     * Se ejecuta automáticamente al cargar el FXML.
     * Configura las columnas, el comportamiento de clic y carga los datos.
     */
    @FXML
    public void initialize() {
        configurarColumnas();
        configurarClicEnFila();
        cargarResponsables();
    }


    /**
     * Asocia cada columna con el campo del objeto Responsable que debe mostrar.
     * La empresa se obtiene por composición a través del departamento.
     */
    private void configurarColumnas() {
        tablaResponsables.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        colNombre.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getEncargado().getNombre()));

        colDepartamento.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDepartamento().getNombre()));

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
        tablaResponsables.setRowFactory(tv -> {
            TableRow<Responsable> fila = new TableRow<>();
            fila.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && !fila.isEmpty()) {
                    navegarAPR1(fila.getItem());
                }
            });
            return fila;
        });
    }

    // ── Carga de datos ────────────────────────────────────────────────────

    /**
     * Consulta todos los mandatos con fecha_fin IS NULL y los muestra en la tabla.
     */
    private void cargarResponsables() {
        listaResponsables.setAll(responsableDAO.findAllActivos());
        tablaResponsables.setItems(listaResponsables);
    }

    // ── Navegación ────────────────────────────────────────────────────────

    /**
     * Vuelve a P0 (selección de rol) al hacer clic en "Inicio" del breadcrumb.
     */
    @FXML
    private void onVolverAP0() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/carbonaudit/view/p0-seleccion-rol-view.fxml"));
            Scene escena = new Scene(loader.load());
            Stage stage = (Stage) tablaResponsables.getScene().getWindow();
            stage.setScene(escena);
            stage.sizeToScene();
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
            PR1Controller pr1 = loader.getController();
            pr1.setResponsable(responsable);
            Stage stage = (Stage) tablaResponsables.getScene().getWindow();
            stage.setScene(escena);
            stage.sizeToScene();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}