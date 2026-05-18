package com.carbonaudit.controller;

import com.carbonaudit.model.Direccion;
import com.carbonaudit.model.Empresa;
import com.carbonaudit.model.FactorEmision;
import com.carbonaudit.service.ServicioGestionEmpresa;
import com.carbonaudit.service.ServicioGestionFactores;
import com.carbonaudit.service.external.ServicioGeograficoORS;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controlador de PA0 · Lista de empresas.
 *
 * Realiza:
 *   - Tab Empresas: listar empresas, crear nueva, navegar a PA1.
 *   - Tab Factores de emisión: listar, crear, editar y eliminar factores.
 */
public class PA0Controller {

    // ── Tab Empresas ──────────────────────────────────────────────────────

    @FXML private TableView<Empresa>        tablaEmpresas;
    @FXML private TableColumn<Empresa, String> colNombre;
    @FXML private TableColumn<Empresa, String> colCif;
    @FXML private TableColumn<Empresa, String> colCiudad;
    @FXML private TableColumn<Empresa, String> colSector;

    // ── Tab Factores de emisión ───────────────────────────────────────────

    @FXML private TableView<FactorEmision>           tablaFactores;
    @FXML private TableColumn<FactorEmision, String> colFactNombre;
    @FXML private TableColumn<FactorEmision, String> colFactUnidad;
    @FXML private TableColumn<FactorEmision, String> colFactValor;
    @FXML private TableColumn<FactorEmision, String> colFactAlcance;

    // ── Panel lateral: empresa ────────────────────────────────────────────

    @FXML private VBox      panelEmpresa;
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

    // ── Panel lateral: factor ─────────────────────────────────────────────

    @FXML private VBox      panelFactor;
    @FXML private Label     panelFactorTitulo;
    @FXML private TextField campFactNombre;
    @FXML private TextField campFactUnidad;
    @FXML private TextField campFactValor;
    @FXML private ComboBox<String> combFactAlcance;
    @FXML private Button    btnEliminarFactor;

    // ── Servicios ─────────────────────────────────────────────────────────

    private final ServicioGestionEmpresa  servicioEmpresa  = new ServicioGestionEmpresa();
    private final ServicioGestionFactores servicioFactores = new ServicioGestionFactores();
    private final ServicioGeograficoORS   geoService       = new ServicioGeograficoORS();

    // ── Estado ────────────────────────────────────────────────────────────

    /*
     * Factor que se está editando. null indica modo "nuevo factor".
     */
    private FactorEmision factorEditando;

    private final ObservableList<Empresa>       listaEmpresas = FXCollections.observableArrayList();
    private final ObservableList<FactorEmision> listaFactores = FXCollections.observableArrayList();

    private static final List<String> ALCANCES = List.of(
            "1 · Combustión directa",
            "2 · Energía (electricidad)",
            "3 · Transporte y movilidad"
    );

    // ── Inicialización ─────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        configurarColumnasEmpresas();
        configurarColumnasFactores();
        configurarClicEnFilaEmpresas();
        configurarClicEnFilaFactores();
        combFactAlcance.setItems(FXCollections.observableArrayList(ALCANCES));
        cargarEmpresas();
        cargarFactores();
    }

    // ── Configuración de columnas ──────────────────────────────────────────

    private void configurarColumnasEmpresas() {
        tablaEmpresas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

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
            return new SimpleStringProperty(sector != null ? sector : "—");
        });
    }

    private void configurarColumnasFactores() {
        tablaFactores.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        colFactNombre.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getNombre()));

        colFactUnidad.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getUnidad()));

        colFactValor.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getValorFactor().toPlainString()));

        colFactAlcance.setCellValueFactory(data -> {
            int alcance = data.getValue().getAlcance();
            String texto = switch (alcance) {
                case 1 -> "Alcance 1 · Directo";
                case 2 -> "Alcance 2 · Energía";
                case 3 -> "Alcance 3 · Transporte";
                default -> "—";
            };
            return new SimpleStringProperty(texto);
        });
    }

    private void configurarClicEnFilaEmpresas() {
        tablaEmpresas.setRowFactory(tv -> {
            TableRow<Empresa> fila = new TableRow<>();
            fila.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && !fila.isEmpty()) {
                    navegarAPA1(fila.getItem());
                }
            });
            return fila;
        });
    }

    /** Clic en una fila de factores abre el panel en modo edición. */
    private void configurarClicEnFilaFactores() {
        tablaFactores.setRowFactory(tv -> {
            TableRow<FactorEmision> fila = new TableRow<>();
            fila.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && !fila.isEmpty()) {
                    abrirPanelEditarFactor(fila.getItem());
                }
            });
            return fila;
        });
    }

    // ── Carga de datos ────────────────────────────────────────────────────

    private void cargarEmpresas() {
        listaEmpresas.setAll(servicioEmpresa.getEmpresas());
        tablaEmpresas.setItems(listaEmpresas);
    }

    private void cargarFactores() {
        listaFactores.setAll(servicioFactores.getFactores());
        tablaFactores.setItems(listaFactores);
    }

    // ── Panel lateral: Nueva empresa ──────────────────────────────────────

    @FXML
    private void onNuevaEmpresa() {
        limpiarFormEmpresa();
        cerrarPanelFactor();
        panelEmpresa.setVisible(true);
        panelEmpresa.setManaged(true);
    }

    @FXML
    private void onGuardarEmpresa() {
        if (campNombre.getText().trim().isEmpty()   || campCif.getText().trim().isEmpty()
                || campCalle.getText().trim().isEmpty()   || campNumero.getText().trim().isEmpty()
                || campCiudad.getText().trim().isEmpty()  || campCp.getText().trim().isEmpty()
                || campProvincia.getText().trim().isEmpty()) {
            mostrarError("Los campos marcados con * son obligatorios.");
            return;
        }
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

            servicioEmpresa.crearEmpresa(empresa);
            cargarEmpresas();
            cerrarPanelEmpresa();
            geocodificarDireccionEnSegundoPlano(dir);
        } catch (NumberFormatException e) {
            mostrarError("El número de calle debe ser un valor numérico.");
        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());
        }
    }

    @FXML
    private void onCancelarEmpresa() {
        cerrarPanelEmpresa();
    }

    // ── Panel lateral: Nuevo / Editar factor ──────────────────────────────

    @FXML
    private void onNuevoFactor() {
        factorEditando = null;
        panelFactorTitulo.setText("Nuevo factor");
        limpiarFormFactor();
        btnEliminarFactor.setVisible(false);
        btnEliminarFactor.setManaged(false);
        cerrarPanelEmpresa();
        panelFactor.setVisible(true);
        panelFactor.setManaged(true);
    }

    private void abrirPanelEditarFactor(FactorEmision factor) {
        factorEditando = factor;
        panelFactorTitulo.setText("Editar factor");
        campFactNombre.setText(factor.getNombre());
        campFactUnidad.setText(factor.getUnidad());
        campFactValor.setText(factor.getValorFactor().toPlainString());
        combFactAlcance.getSelectionModel().select(factor.getAlcance() - 1);
        btnEliminarFactor.setVisible(true);
        btnEliminarFactor.setManaged(true);
        cerrarPanelEmpresa();
        panelFactor.setVisible(true);
        panelFactor.setManaged(true);
    }

    @FXML
    private void onGuardarFactor() {
        String nombre = campFactNombre.getText().trim();
        String unidad = campFactUnidad.getText().trim();
        if (nombre.isEmpty() || unidad.isEmpty()
                || campFactValor.getText().trim().isEmpty()
                || combFactAlcance.getValue() == null) {
            mostrarError("Todos los campos son obligatorios.");
            return;
        }
        try {
            BigDecimal valor = new BigDecimal(campFactValor.getText().trim().replace(",", "."));
            if (valor.compareTo(BigDecimal.ZERO) < 0) {
                mostrarError("El factor de emisión no puede ser negativo.");
                return;
            }
            int alcance = combFactAlcance.getSelectionModel().getSelectedIndex() + 1;

            if (factorEditando == null) {
                FactorEmision nuevo = new FactorEmision();
                nuevo.setNombre(nombre);
                nuevo.setUnidad(unidad);
                nuevo.setValorFactor(valor);
                nuevo.setAlcance(alcance);
                servicioFactores.crearFactor(nuevo);
            } else {
                factorEditando.setNombre(nombre);
                factorEditando.setUnidad(unidad);
                factorEditando.setValorFactor(valor);
                factorEditando.setAlcance(alcance);
                servicioFactores.actualizarFactor(factorEditando);
            }

            cargarFactores();
            cerrarPanelFactor();
        } catch (NumberFormatException e) {
            mostrarError("El valor del factor debe ser un número decimal (usa punto como separador).");
        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());
        }
    }

    /**
     * Elimina el factor si no está en uso. La BD rechazará el DELETE con una
     * excepción de FK si algún empleado, consumo o commuting lo referencia;
     * el DAO imprime el error y el controlador informa al usuario.
     */
    @FXML
    private void onEliminarFactor() {
        if (factorEditando == null) return;

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Eliminar \"" + factorEditando.getNombre() + "\"?");
        confirmacion.setContentText(
                "Solo es posible si ningún empleado ni consumo lo está usando.");
        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                servicioFactores.eliminarFactor(factorEditando.getIdFactor());
                cargarFactores();
                cerrarPanelFactor();
            }
        });
    }

    @FXML
    private void onCancelarFactor() {
        cerrarPanelFactor();
    }

    // ── Navegación ─────────────────────────────────────────────────────────

    private void navegarAPA1(Empresa empresa) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/carbonaudit/view/pa1-empresa-view.fxml"));
            Scene escena = new Scene(loader.load());
            PA1Controller pa1 = loader.getController();
            pa1.setEmpresa(empresa);
            Stage stage = (Stage) tablaEmpresas.getScene().getWindow();
            stage.setScene(escena);
            stage.sizeToScene();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private void cerrarPanelEmpresa() {
        panelEmpresa.setVisible(false);
        panelEmpresa.setManaged(false);
        limpiarFormEmpresa();
    }

    private void cerrarPanelFactor() {
        panelFactor.setVisible(false);
        panelFactor.setManaged(false);
        limpiarFormFactor();
        factorEditando = null;
    }

    private void limpiarFormEmpresa() {
        campNombre.clear();   campCif.clear();
        campTelefono.clear(); campEmail.clear();  campSector.clear();
        campCalle.clear();    campNumero.clear(); campCiudad.clear();
        campCp.clear();       campProvincia.clear();
    }

    private void limpiarFormFactor() {
        campFactNombre.clear();
        campFactUnidad.clear();
        campFactValor.clear();
        combFactAlcance.getSelectionModel().clearSelection();
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Aviso");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void geocodificarDireccionEnSegundoPlano(Direccion dir) {
        Task<Void> tarea = new Task<>() {
            @Override
            protected Void call() throws Exception {
                geoService.completarCoordenadas(dir);
                return null;
            }
        };
        tarea.setOnSucceeded(e -> servicioEmpresa.persistirCoordenadas(dir));
        tarea.setOnFailed(e -> Platform.runLater(() ->
                mostrarError("No se pudo geocodificar la dirección de la empresa.")));
        Thread hilo = new Thread(tarea);
        hilo.setDaemon(true);
        hilo.start();
    }
}