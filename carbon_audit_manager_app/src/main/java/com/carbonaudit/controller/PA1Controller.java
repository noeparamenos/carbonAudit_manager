package com.carbonaudit.controller;

import com.carbonaudit.dao.EmpresaDAO;
import com.carbonaudit.model.Direccion;
import com.carbonaudit.model.Empresa;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controlador de PA1 · Lista de empresas.
 *
 * Responsabilidades:
 *   - Cargar y mostrar todas las empresas en la TableView.
 *   - Gestionar el panel lateral de alta de empresa.
 *   - Navegar a PA2 al seleccionar una empresa.
 */
public class PA1Controller {

    // ── Tabla ────────────────────────────────────────────────────────────

    /*
     * TableView<Empresa>: tabla tipada con objetos Empresa.
     * TableColumn<Empresa, String>: columna que extrae un String de cada Empresa.
     */
    @FXML private TableView<Empresa>        tablaEmpresas;
    @FXML private TableColumn<Empresa, String> colNombre;
    @FXML private TableColumn<Empresa, String> colCif;
    @FXML private TableColumn<Empresa, String> colCiudad;
    @FXML private TableColumn<Empresa, String> colSector;

    // ── Panel lateral ────────────────────────────────────────────────────

    @FXML private VBox panelLateral;

    // ── Campos del formulario ────────────────────────────────────────────

    @FXML private TextField campNombre;
    @FXML private TextField campCif;
    @FXML private TextField campTelefono;
    @FXML private TextField campEmail;
    @FXML private TextField campSector;
    @FXML private TextField campCalle;
    @FXML private TextField campNumero;
    @FXML private TextField campCiudad;
    @FXML private TextField campCp;
    @FXML private TextField campProvincia;

    // ── DAO y datos ──────────────────────────────────────────────────────

    private final EmpresaDAO empresaDAO = new EmpresaDAO();

    /*
     * ObservableList: lista especial de JavaFX que notifica a la TableView
     * automáticamente cuando se añaden o eliminan elementos. Es el "modelo"
     * de la tabla — no hace falta refrescarla manualmente.
     */
    private final ObservableList<Empresa> listaEmpresas = FXCollections.observableArrayList();

    // ── Inicialización ───────────────────────────────────────────────────

    @FXML
    public void initialize() {
        // CONSTRAINED_RESIZE_POLICY no se puede referenciar desde FXML en JavaFX 21,
        // por eso se asigna aquí. Reparte el ancho sobrante entre las columnas.
        tablaEmpresas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        configurarColumnas();
        configurarClicEnFila();
        cargarEmpresas();
    }

    /**
     * Asocia cada columna a la propiedad del objeto Empresa que debe mostrar.
     *
     * setCellValueFactory recibe una función lambda que, dado un objeto Empresa,
     * devuelve un ObservableValue<String> con el valor a mostrar en esa celda.
     * SimpleStringProperty envuelve un String en un ObservableValue.
     */
    private void configurarColumnas() {
        colNombre.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getNombreSocial()));

        colCif.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getCif()));

        colCiudad.setCellValueFactory(data -> {
            Empresa e = data.getValue();
            String ciudad = (e.getDireccion() != null) ? e.getDireccion().getCiudad() : "";
            return new SimpleStringProperty(ciudad);
        });

        colSector.setCellValueFactory(data -> {
            String sector = data.getValue().getSector();
            return new SimpleStringProperty(sector != null ? sector : "");
        });
    }

    /**
     * Configura la navegación a PA2 al hacer doble clic en una fila.
     *
     * setRowFactory permite personalizar cada fila de la tabla.
     * Aquí añadimos un listener de doble clic a cada fila generada.
     */
    private void configurarClicEnFila() {
        tablaEmpresas.setRowFactory(tv -> {
            TableRow<Empresa> fila = new TableRow<>();
            fila.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && !fila.isEmpty()) {
                    navegarAPA2(fila.getItem());
                }
            });
            return fila;
        });
    }

    /**
     * Carga todas las empresas desde la BD y las asigna a la tabla.
     * Al usar ObservableList, la TableView se actualiza sola.
     */
    private void cargarEmpresas() {
        listaEmpresas.setAll(empresaDAO.findAll());
        tablaEmpresas.setItems(listaEmpresas);
    }

    // ── Panel lateral ────────────────────────────────────────────────────

    /** Muestra el panel lateral y limpia los campos del formulario. */
    @FXML
    private void onNuevaEmpresa() {
        limpiarFormulario();
        panelLateral.setVisible(true);
        panelLateral.setManaged(true);
    }

    /**
     * Valida el formulario, crea la empresa en BD y refresca la tabla.
     *
     * Si la validación falla (campos vacíos o datos incorrectos), muestra
     * un Alert informativo y no cierra el panel.
     */
    @FXML
    private void onGuardar() {
        if (!formularioValido()) return;

        try {
            Direccion dir = new Direccion();
            dir.setCalle(campCalle.getText().trim());
            dir.setNumero(Integer.parseInt(campNumero.getText().trim()));
            dir.setCiudad(campCiudad.getText().trim());
            dir.setCodigoPostal(campCp.getText().trim());
            dir.setProvincia(campProvincia.getText().trim());

            Empresa empresa = new Empresa();
            empresa.setNombreSocial(campNombre.getText().trim());
            empresa.setCif(campCif.getText().trim());
            empresa.setTelefono(campTelefono.getText().trim());
            empresa.setEmail(campEmail.getText().trim());
            empresa.setSector(campSector.getText().trim());
            empresa.setDireccion(dir);

            empresaDAO.create(empresa);

            // Refrescar la tabla con los datos actualizados de BD
            cargarEmpresas();
            cerrarPanel();

        } catch (NumberFormatException e) {
            mostrarError("El número de calle debe ser un valor numérico.");
        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());
        }
    }

    /** Cierra el panel lateral sin guardar. */
    @FXML
    private void onCancelar() {
        cerrarPanel();
    }

    // ── Navegación ───────────────────────────────────────────────────────

    /**
     * Navega a PA2 pasando la empresa seleccionada al controlador destino.
     *
     * Patrón de paso de datos entre controladores en JavaFX:
     *   1. Cargar el FXML con FXMLLoader (no instanciar el controlador a mano).
     *   2. Obtener el controlador con loader.getController().
     *   3. Llamar a un método setter en el controlador destino ANTES de mostrar la escena.
     */
    private void navegarAPA2(Empresa empresa) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/carbonaudit/view/pa2-empresa-view.fxml"));
            Scene escena = new Scene(loader.load());

            // Pasamos la empresa seleccionada al controlador de PA2
            PA2Controller pa2 = loader.getController();
            pa2.setEmpresa(empresa);

            Stage stage = (Stage) tablaEmpresas.getScene().getWindow();
            stage.setScene(escena);
            stage.sizeToScene();

        } catch (Exception e) {
            // TODO: eliminar cuando PA2 esté implementada
            System.err.println("PA2 no implementada aún.");
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    /** Valida que los campos obligatorios no estén vacíos. */
    private boolean formularioValido() {
        if (campNombre.getText().trim().isEmpty() ||
            campCif.getText().trim().isEmpty()    ||
            campCalle.getText().trim().isEmpty()  ||
            campNumero.getText().trim().isEmpty() ||
            campCiudad.getText().trim().isEmpty() ||
            campCp.getText().trim().isEmpty()     ||
            campProvincia.getText().trim().isEmpty()) {

            mostrarError("Los campos marcados con * son obligatorios.");
            return false;
        }
        return true;
    }

    private void cerrarPanel() {
        panelLateral.setVisible(false);
        panelLateral.setManaged(false);
        limpiarFormulario();
    }

    private void limpiarFormulario() {
        campNombre.clear();   campCif.clear();
        campTelefono.clear(); campEmail.clear();  campSector.clear();
        campCalle.clear();    campNumero.clear(); campCiudad.clear();
        campCp.clear();       campProvincia.clear();
    }

    /**
     * Muestra un diálogo de error con el mensaje indicado.
     * Alert es el componente estándar de JavaFX para mensajes al usuario.
     */
    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Aviso");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
