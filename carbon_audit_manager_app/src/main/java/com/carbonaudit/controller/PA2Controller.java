package com.carbonaudit.controller;

import com.carbonaudit.dao.*;
import com.carbonaudit.model.*;
import com.carbonaudit.service.ServicioCalculoHuella;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.carbonaudit.model.Direccion;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controlador de PA2 · Vista empresa.
 *
 * Realiza:
 *   - Mostrar la cabecera de la empresa (nombre, CIF, ciudad).
 *   - Gestionar el selector de período (mes/año) compartido entre tabs.
 *   - Tab Departamentos: listar departamentos, nuevo dpto., navegar a PA3.
 *   - Tab Consumos: mostrar consumos agregados de toda la empresa para el período.
 *   - Editar y eliminar la empresa activa.
 *   - Exportar consumos a CSV.
 */
public class PA2Controller {

    // ── Cabecera ─────────────────────────────────────────────────────────

    @FXML private Label lblBreadcrumb;
    @FXML private Label lblNombreEmpresa;
    @FXML private Label lblInfoEmpresa;

    // ── Selector de período ───────────────────────────────────────────────
    /*
     * combMes se deshabilita cuando el usuario marca "Año completo".
     * combAnio siempre permanece activo.
     */
    @FXML private ComboBox<String>  combMes;
    @FXML private ComboBox<Integer> combAnio;
    @FXML private CheckBox chkAnioCompleto;
    @FXML private CheckBox chkIncluirCommuting;

    // ── TabPane ───────────────────────────────────────────────────────────

    @FXML private TabPane tabPane;
    @FXML private Tab     tabDepartamentos;
    @FXML private Tab     tabConsumos;

    // ========= Tab Departamentos ==========================

    @FXML private TableView<Departamento>        tablaDepartamentos;
    @FXML private TableColumn<Departamento, String> colDeptNombre;
    @FXML private TableColumn<Departamento, String> colDeptLocalidad;
    @FXML private TableColumn<Departamento, String> colDeptEmpleados;
    @FXML private TableColumn<Departamento, String> colDeptResponsable;

    @FXML private VBox  panelDpto;
    @FXML private Label panelDptoTitulo;
    @FXML private TextField campDptoNombre;
    @FXML private TextField campDptoCalle;
    @FXML private TextField campDptoNumero;
    @FXML private TextField campDptoCiudad;
    @FXML private TextField campDptoCp;
    @FXML private TextField campDptoProvincia;

    // ================== Tab Consumos ======================

    @FXML private TableView<ConsumoMensual>        tablaConsumos;
    @FXML private TableColumn<ConsumoMensual, String> colConsDept;
    @FXML private TableColumn<ConsumoMensual, String> colConsRecurso;
    @FXML private TableColumn<ConsumoMensual, String> colConsCantidad;
    @FXML private TableColumn<ConsumoMensual, String> colConsUnidad;
    @FXML private TableColumn<ConsumoMensual, String> colConsAlcance;
    @FXML private TableColumn<ConsumoMensual, String> colConsEmision;
    @FXML private Label lblTotalEmpresa;

    // ========= Panel editar empresa =========

    @FXML private VBox      panelEditEmpresa;
    @FXML private TextField editNombre;
    @FXML private TextField editCif;
    @FXML private TextField editTelefono;
    @FXML private TextField editEmail;
    @FXML private TextField editSector;
    @FXML private TextField editCalle;
    @FXML private TextField editNumero;
    @FXML private TextField editCiudad;
    @FXML private TextField editCp;
    @FXML private TextField editProvincia;

    // ========= DAOs ==========

    private final EmpresaDAO        empresaDAO      = new EmpresaDAO();
    private final DepartamentoDAO   departamentoDAO = new DepartamentoDAO();
    private final ResponsableDAO    responsableDAO  = new ResponsableDAO();
    private final EmpleadoDAO       empleadoDAO     = new EmpleadoDAO();
    private final ConsumoMensualDAO consumoDAO      = new ConsumoMensualDAO();

    /*
     * Se pasa null como servicio geográfico porque en PA2 solo usamos los métodos
     * de cálculo de huella, que no necesitan llamar a la API de geolocalización.
     * El geo service solo se usa en AsignarDistanciaTrabajo(), que no se invoca aquí.
     */
    private final ServicioCalculoHuella servicioHuella = new ServicioCalculoHuella(null);

    // ── Estado ────────────────────────────────────────────────────────────

    private Empresa empresa;

    /*
     * Nombres de los meses en español. 0 = Enero
     * Para el ComboBox y para convertir índice ↔ número de mes.
     */
    private static final String[] NOMBRES_MESES = {
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    };

    private final ObservableList<Departamento>   listaDepartamentos = FXCollections.observableArrayList();
    private final ObservableList<ConsumoMensual> listaConsumos      = FXCollections.observableArrayList();

    // ── Inicialización ─────────────────────────────────────────────────────

    /**
     * Se ejecuta automáticamente cuando JavaFX termina de cargar el FXML.
     * En este punto 'empresa' aún es null — se asigna después en setEmpresa().
     * Solo se configuran las partes estáticas que no dependen de la empresa.
     */
    @FXML
    public void initialize() {

        configurarSelectorPeriodo();
        configurarColumnasDepartamentos();
        configurarColumnasConsumos();
        configurarClicEnFila();
        configurarCambioTab();
    }

    /**
     * Recibe la empresa seleccionada desde PA1 y actualiza toda la pantalla.
     *
     * El controlador origen llama a este método
     * DESPUÉS de cargar el FXML (loader.getController()) y ANTES de mostrar
     * la escena, para pasar datos al controlador destino.
     */
    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
        actualizarCabecera();
        cargarDepartamentos();
    }

    // ============== Configuración inicial ================

    /**
     * Rellena los ComboBox de mes y año con los valores posibles y selecciona el período actual.
     * Registra los listeners que recargan la pestaña activa al cambiar el período.
     */
    private void configurarSelectorPeriodo() {
        combMes.getItems().addAll(NOMBRES_MESES);
        // Seleccionar el mes actual (getMonthValue() devuelve 1-12; el índice emieza en 0)
        combMes.getSelectionModel().select(LocalDate.now().getMonthValue() - 1);

        // Se muestran solo los 4 ultimos años
        int anioActual = LocalDate.now().getYear();
        for (int a = anioActual; a >= anioActual - 4; a--) {
            combAnio.getItems().add(a);
        }
        combAnio.getSelectionModel().selectFirst();

        // Al cambiar el período, recargar la pestaña activa
        combMes.setOnAction(e -> refrescarTabActiva());
        combAnio.setOnAction(e -> refrescarTabActiva());
    }

    /**
     * Asocia cada columna de la tabla de departamentos con el campo del objeto que debe mostrar.
     * La columna de empleados y la de responsable activo lanzan consultas a BD por cada fila.
     */
    private void configurarColumnasDepartamentos() {
        tablaDepartamentos.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        colDeptNombre.setCellValueFactory(data -> //recibe el dept de la fila
                new SimpleStringProperty(data.getValue().getNombre())); // devuelve su nombre

        colDeptLocalidad.setCellValueFactory(data -> {
            Direccion dir = data.getValue().getDireccion();
            return new SimpleStringProperty(dir != null ? dir.getCiudad() : "—");
        });

        // countByDepartamento() lanza una consulta COUNT(*) por cada fila
        colDeptEmpleados.setCellValueFactory(data -> {
            int count = empleadoDAO.countByDepartamento(
                    data.getValue().getIdDepartamento());
            return new SimpleStringProperty(String.valueOf(count)); // Total de empleados en el Departamento
        });

        colDeptResponsable.setCellValueFactory(data -> {
            Optional<Responsable> resp = responsableDAO.findActivoByDepartamento(
                    data.getValue().getIdDepartamento());
            // Si no hay responsable activo, mostramos un guion
            return new SimpleStringProperty(
                    resp.map(r -> r.getEncargado().getNombre()).orElse("—"));
        });
    }

    /**
     * Asocia cada columna de la tabla de consumos con el campo del objeto que debe mostrar.
     * La emisión se calcula en el momento con calcularEmision() (cantidad × factor).
     */
    private void configurarColumnasConsumos() {
        tablaConsumos.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        colConsDept.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDepartamento().getNombre()));

        colConsRecurso.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getFactorEmision().getNombre()));

        colConsCantidad.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getCantidad().toPlainString()));

        colConsUnidad.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getFactorEmision().getUnidad()));

        colConsAlcance.setCellValueFactory(data ->
                new SimpleStringProperty("Alcance " + data.getValue().getFactorEmision().getAlcance()));

        colConsEmision.setCellValueFactory(data -> {
            BigDecimal emision = data.getValue().calcularEmision();
            return new SimpleStringProperty(String.format("%.2f", emision));
        });
    }

    /**
     * Configura la navegación a PA3 con un clic en la fila del departamento.
     */
    private void configurarClicEnFila() {
        tablaDepartamentos.setRowFactory(tv -> {
            TableRow<Departamento> fila = new TableRow<>();
            fila.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && !fila.isEmpty()) {
                    navegarAPA3(fila.getItem());
                }
            });
            return fila;
        });
    }

    /**
     * Carga el tab de Consumos al seleccionarlo, evitando consultas innecesarias
     * si el usuario no visita ese tab durante la sesión.
     */
    private void configurarCambioTab() {
        tabPane.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, nuevo) -> {
                    if (empresa == null || nuevo == null) return;
                    if (nuevo == tabConsumos) cargarConsumos();
                });
    }

    // ── Carga de datos ─────────────────────────────────────────────────────

    /** Actualiza los labels de cabecera con los datos de la empresa activa. */
    private void actualizarCabecera() {
        lblBreadcrumb.setText(empresa.getNombreSocial());
        lblNombreEmpresa.setText(empresa.getNombreSocial());
        String ciudad = (empresa.getDireccion() != null)
                ? empresa.getDireccion().getCiudad() : "";
        lblInfoEmpresa.setText("CIF: " + empresa.getCif() + "  ·  " + ciudad);
    }

    /** Consulta todos los departamentos de la empresa activa y los muestra en la tabla. */
    private void cargarDepartamentos() {
        listaDepartamentos.setAll(
                departamentoDAO.findAllByEmpresa(empresa.getIdEmpresa()));
        tablaDepartamentos.setItems(listaDepartamentos);
    }

    /**
     * Carga los consumos según el período y las opciones activas.
     * Si "Año completo" está marcado, consulta los 12 meses; si no, solo el mes seleccionado.
     * Si "Incluir commuting" está marcado, el total incluye Alcance 3 vía ServicioCalculoHuella.
     */
    private void cargarConsumos() {
        if (empresa == null) return;

        int anio = combAnio.getSelectionModel().getSelectedItem();
        List<ConsumoMensual> consumos;
        BigDecimal total;

        // Calculo anualizado
        if (chkAnioCompleto.isSelected()) {
            consumos = consumoDAO.getConsumosByEmpresaAnio(empresa.getIdEmpresa(), anio);

            if (chkIncluirCommuting.isSelected()) {
                // getHuellaAnualEmpresa suma Scope 1+2+3 (12 meses) para toda la empresa
                total = servicioHuella.getHuellaAnualEmpresa(empresa, anio);
            } else { // Sin commuting
                total = consumos.stream()
                        .map(ConsumoMensual::calcularEmision)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }
            // Calculo mensual
        } else {
            int mes = combMes.getSelectionModel().getSelectedIndex() + 1;
            consumos = consumoDAO.getConsumosByEmpresaMes(empresa.getIdEmpresa(), mes, anio);

            if (chkIncluirCommuting.isSelected()) {
                // getHuellaTotalEmpresaMes suma Scope 1+2+3 para el mes seleccionado
                total = servicioHuella.getHuellaTotalEmpresaMes(empresa, mes, anio);
            } else {
                total = consumos.stream()
                        .map(ConsumoMensual::calcularEmision)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }
        }

        listaConsumos.setAll(consumos);
        tablaConsumos.setItems(listaConsumos);
        lblTotalEmpresa.setText(String.format("%.2f kg CO₂e", total));
    }

    /** Activa o desactiva el modo año completo y deshabilita el combo de mes en consecuencia. */
    @FXML
    private void onToggleAnioCompleto() {
        combMes.setDisable(chkAnioCompleto.isSelected());
        if (tabPane.getSelectionModel().getSelectedItem() == tabConsumos) {
            cargarConsumos();
        }
    }

    /** Recalcula el total de la pestaña Consumos al marcar o desmarcar "Incluir commuting". */
    @FXML
    private void onToggleCommuting() {
        if (tabPane.getSelectionModel().getSelectedItem() == tabConsumos) {
            cargarConsumos();
        }
    }

    /** Recarga los datos de la pestaña activa al cambiar el selector de período. */
    private void refrescarTabActiva() {
        if (empresa == null) return;
        Tab activa = tabPane.getSelectionModel().getSelectedItem();
        if (activa == tabConsumos) cargarConsumos();
        // Gráficos: pendiente de implementar en una fase posterior
    }

    // =========== Panel lateral: NUEVO DEPARTAMENTO =======================

    /**
     * Abre el panel lateral de nuevo departamento con los campos de dirección
     * pre-rellenos con los datos de la empresa activa como punto de partida.
     */
    @FXML
    private void onNuevoDepartamento() {
        // Pre-rellenar con la dirección de la empresa como punto de partida
        campDptoNombre.clear();
        if (empresa.getDireccion() != null) {
            Direccion dir = empresa.getDireccion();
            campDptoCalle.setText(dir.getCalle());
            campDptoNumero.setText(String.valueOf(dir.getNumero()));
            campDptoCiudad.setText(dir.getCiudad());
            campDptoCp.setText(dir.getCodigoPostal());
            campDptoProvincia.setText(dir.getProvincia() != null ? dir.getProvincia() : "");
        }
        cerrarPanelEditEmpresa();
        panelDpto.setVisible(true);
        panelDpto.setManaged(true);
    }

    /**
     * Crea el departamento con una Dirección propia.
     * Se crea siempre una Dirección nueva (idDireccion = 0) para que el departamento
     * sea independiente de la empresa — si luego se edita una, la otra no cambia.
     */
    @FXML
    private void onGuardarDpto() {
        String nombre = campDptoNombre.getText().trim();
        if (nombre.isEmpty()
                || campDptoCalle.getText().trim().isEmpty()
                || campDptoNumero.getText().trim().isEmpty()
                || campDptoCiudad.getText().trim().isEmpty()
                || campDptoCp.getText().trim().isEmpty()) {
            mostrarError("Los campos marcados con * son obligatorios.");
            return;
        }
        try {
            // Dirección nueva con idDireccion = 0 → el DAO la inserta como registro propio
            Direccion dir = new Direccion();
            dir.setCalle(campDptoCalle.getText().trim());
            dir.setNumero(Integer.parseInt(campDptoNumero.getText().trim()));
            dir.setCiudad(campDptoCiudad.getText().trim());
            dir.setCodigoPostal(campDptoCp.getText().trim());
            dir.setProvincia(campDptoProvincia.getText().trim());

            Departamento dpto = new Departamento();
            dpto.setNombre(nombre);
            dpto.setEmpresa(empresa);
            dpto.setDireccion(dir);
            departamentoDAO.create(dpto);
            cargarDepartamentos();
            cerrarPanelDpto();
        } catch (NumberFormatException e) {
            mostrarError("El número de calle debe ser un valor numérico.");
        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());
        }
    }

    @FXML
    private void onCancelarDpto() {
        cerrarPanelDpto();
    }

    // ── Panel lateral: Editar empresa ──────────────────────────────────────

    @FXML
    private void onEditarEmpresa() {
        // Pre-rellenar el formulario con los datos actuales de la empresa
        editNombre.setText(empresa.getNombreSocial());
        editCif.setText(empresa.getCif());
        editTelefono.setText(empresa.getTelefono() != null ? empresa.getTelefono() : "");
        editEmail.setText(empresa.getEmail()    != null ? empresa.getEmail()    : "");
        editSector.setText(empresa.getSector()  != null ? empresa.getSector()   : "");
        if (empresa.getDireccion() != null) {
            Direccion dir = empresa.getDireccion();
            editCalle.setText(dir.getCalle());
            editNumero.setText(String.valueOf(dir.getNumero()));
            editCiudad.setText(dir.getCiudad());
            editCp.setText(dir.getCodigoPostal());
            editProvincia.setText(dir.getProvincia());
        }
        cerrarPanelDpto();
        panelEditEmpresa.setVisible(true);
        panelEditEmpresa.setManaged(true);
    }

    @FXML
    private void onGuardarEditEmpresa() {
        if (editNombre.getText().trim().isEmpty()  || editCif.getText().trim().isEmpty()
                || editCalle.getText().trim().isEmpty()  || editNumero.getText().trim().isEmpty()
                || editCiudad.getText().trim().isEmpty() || editCp.getText().trim().isEmpty()
                || editProvincia.getText().trim().isEmpty()) {
            mostrarError("Los campos marcados con * son obligatorios.");
            return;
        }
        try {
            empresa.setNombreSocial(editNombre.getText().trim());
            empresa.setCif(editCif.getText().trim());
            empresa.setTelefono(editTelefono.getText().trim());
            empresa.setEmail(editEmail.getText().trim());
            empresa.setSector(editSector.getText().trim());

            Direccion dir = empresa.getDireccion();
            dir.setCalle(editCalle.getText().trim());
            dir.setNumero(Integer.parseInt(editNumero.getText().trim()));
            dir.setCiudad(editCiudad.getText().trim());
            dir.setCodigoPostal(editCp.getText().trim());
            dir.setProvincia(editProvincia.getText().trim());

            // EmpresaDAO.update() llama a DireccionDAO.update() si la dirección tiene id > 0
            empresaDAO.update(empresa);
            actualizarCabecera();
            cerrarPanelEditEmpresa();
        } catch (NumberFormatException e) {
            mostrarError("El número de calle debe ser un valor numérico.");
        }
    }

    @FXML
    private void onCancelarEditEmpresa() {
        cerrarPanelEditEmpresa();
    }

    // ── Eliminar empresa ───────────────────────────────────────────────────

    @FXML
    private void onEliminarEmpresa() {
        List<Departamento> departamentos =
                departamentoDAO.findAllByEmpresa(empresa.getIdEmpresa());
        if (!departamentos.isEmpty()) {
            mostrarError("No se puede eliminar la empresa porque tiene "
                    + departamentos.size() + " departamento(s) asociado(s).\n"
                    + "Elimina primero todos los departamentos.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Eliminar \"" + empresa.getNombreSocial() + "\"?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");
        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                empresaDAO.delete(empresa.getIdEmpresa());
                navegarAPA1();
            }
        });
    }

    // ── Exportar CSV ───────────────────────────────────────────────────────

    @FXML
    private void onExportarCSV() {
        if (listaConsumos.isEmpty()) {
            mostrarError("No hay datos que exportar para el período seleccionado.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar CSV");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivo CSV (*.csv)", "*.csv"));
        fileChooser.setInitialFileName(
                "consumos_" + empresa.getNombreSocial().replaceAll("\\s+", "_") + ".csv");

        Stage stage = (Stage) tablaConsumos.getScene().getWindow();
        java.io.File archivo = fileChooser.showSaveDialog(stage);
        if (archivo == null) return;

        try (FileWriter fw = new FileWriter(archivo)) {
            fw.write("Departamento;Recurso;Cantidad;Unidad;Alcance;Emision_kgCO2e\n");
            for (ConsumoMensual c : listaConsumos) {
                fw.write(c.getDepartamento().getNombre() + ";"
                        + c.getFactorEmision().getNombre()  + ";"
                        + c.getCantidad()                   + ";"
                        + c.getFactorEmision().getUnidad()  + ";"
                        + c.getFactorEmision().getAlcance() + ";"
                        + c.calcularEmision()               + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            mostrarError("Error al exportar el archivo: " + e.getMessage());
        }
    }

    // ── Navegación ─────────────────────────────────────────────────────────

    /** Vuelve a PA1 al hacer clic en "Empresas" del breadcrumb. */
    @FXML
    private void onVolverAPA1() {
        navegarAPA1();
    }

    private void navegarAPA1() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/carbonaudit/view/pa1-empresas-view.fxml"));
            Scene escena = new Scene(loader.load());
            Stage stage = (Stage) lblNombreEmpresa.getScene().getWindow();
            stage.setScene(escena);
            stage.sizeToScene();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Navega a PA3 pasando el departamento seleccionado al controlador destino.
     * Mismo patrón que PA1 → PA2: cargar FXML, obtener controlador, pasar objeto, mostrar.
     */
    private void navegarAPA3(Departamento departamento) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/carbonaudit/view/pa3-departamento-view.fxml"));
            Scene escena = new Scene(loader.load());
            PA3Controller pa3 = loader.getController();
            pa3.setDepartamento(departamento);
            Stage stage = (Stage) tablaDepartamentos.getScene().getWindow();
            stage.setScene(escena);
            stage.sizeToScene();
        } catch (Exception e) {
            System.err.println("PA3 no implementada aún.");
            e.printStackTrace();
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private void cerrarPanelDpto() {
        panelDpto.setVisible(false);
        panelDpto.setManaged(false);
        campDptoNombre.clear();
        campDptoCalle.clear();    campDptoNumero.clear();
        campDptoCiudad.clear();   campDptoCp.clear();
        campDptoProvincia.clear();
    }

    private void cerrarPanelEditEmpresa() {
        panelEditEmpresa.setVisible(false);
        panelEditEmpresa.setManaged(false);
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Aviso");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
