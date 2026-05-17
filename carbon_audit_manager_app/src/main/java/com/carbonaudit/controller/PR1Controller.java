package com.carbonaudit.controller;

import com.carbonaudit.dao.*;
import com.carbonaudit.model.*;
import com.carbonaudit.service.ServicioCalculoHuella;
import com.carbonaudit.service.ServicioGestionEmpleado;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controlador de PR1 · Vista responsable.
 *
 * Pantalla principal del responsable de sostenibilidad. Agrupa en cuatro tabs
 * todas las funciones que puede realizar sobre su departamento:
 *
 *   - Tab Consumos mensuales : alta, edición y duplicado de consumos del período.
 *   - Tab Trabajadores       : consulta (solo lectura) de los empleados del departamento.
 *   - Tab Huella mensual     : KPIs de CO₂e, desglose por Scope y sección commuting.
 *   - Tab Gráficos           : placeholder para la evolución histórica.
 *
 * El selector de mes/año es compartido por todos los tabs; al cambiar el período
 * se recargan únicamente los tabs de Consumos y Huella, ya que Trabajadores
 * no depende del período.
 */
public class PR1Controller {

    // Breadcrumb de navegación ( Inicio > responsable > nombre )
    @FXML private Label lblBreadcrumb;

    // Cabecera
    @FXML private Label lblNombreResponsable;
    @FXML private Label lblInfoResponsable;

    // Selector de período
    @FXML private ComboBox<String>  combMes;
    @FXML private ComboBox<Integer> combAnio;

    // TabPane
    @FXML private TabPane tabPane;
    @FXML private Tab     tabConsumos;
    @FXML private Tab     tabTrabajadores;
    @FXML private Tab     tabHuella;

    // Tab Consumos mensuales
    @FXML private TableView<ConsumoMensual>           tablaConsumos;
    @FXML private TableColumn<ConsumoMensual, String> colConsRecurso;
    @FXML private TableColumn<ConsumoMensual, String> colConsUnidad;
    @FXML private TableColumn<ConsumoMensual, String> colConsCantidad;
    @FXML private TableColumn<ConsumoMensual, String> colConsAlcance;
    @FXML private TableColumn<ConsumoMensual, String> colConsEmision;
    @FXML private Label lblTotalDpto;

    // Tab Trabajadores
    @FXML private TableView<Empleado>           tablaEmpleados;
    @FXML private TableColumn<Empleado, String> colEmpNombre;
    @FXML private TableColumn<Empleado, String> colEmpCiudad;
    @FXML private TableColumn<Empleado, String> colEmpDias;
    @FXML private TableColumn<Empleado, String> colEmpVehiculo;
    @FXML private TableColumn<Empleado, String> colEmpDistancia;

    // Tab Huella mensual
    @FXML private Label kpiTotal;
    @FXML private Label kpiScope1;
    @FXML private Label kpiScope2;
    @FXML private Label kpiScope3;
    @FXML private TableView<ConsumoMensual>           tablaHuella; //Componente visual de la tabla
    @FXML private TableColumn<ConsumoMensual, String> colHuellaRecurso;
    @FXML private TableColumn<ConsumoMensual, String> colHuellaUnidad;
    @FXML private TableColumn<ConsumoMensual, String> colHuellaCantidad;
    @FXML private TableColumn<ConsumoMensual, String> colHuellaAlcance;
    @FXML private TableColumn<ConsumoMensual, String> colHuellaEmision;
    @FXML private Label lblCommuting;

    // Panel lateral: consumo
    @FXML private VBox      panelConsumo;
    @FXML private Label     panelConsumoTitulo;
    @FXML private ComboBox<FactorEmision> combFactor;
    @FXML private Label     lblUnidad;
    @FXML private Label     lblAlcance;
    @FXML private TextField campCantidad;
    @FXML private Button    btnEliminarConsumo;

    // DAOs y servicios

    private final FactorEmisionDAO factorDAO = new FactorEmisionDAO();

    private final ServicioCalculoHuella  servicioHuella  = new ServicioCalculoHuella();
    private final ServicioGestionEmpleado servicioGestion = new ServicioGestionEmpleado();

    //  Estado
    private Responsable  responsable;
    private Departamento departamento;

    /*
     * Consumo que se está editando en el panel lateral (`null` seria uno nuevo)
     */
    private ConsumoMensual consumoEditando;

    /*
     * Lista compartida entre tablaConsumos (Tab Consumos) y tablaHuella (Tab Huella).
     * Ambas tablas observan esta lista, por lo que se actualizan a la vez
     * con una sola llamada a listaConsumos.setAll(...).
     */
    private final ObservableList<ConsumoMensual> listaConsumos  = FXCollections.observableArrayList();
    private final ObservableList<Empleado>        listaEmpleados = FXCollections.observableArrayList();

    /*
     * Nombres de los meses en español. Índice 0 = Enero.
     * para el ComboBox y para convertir índice - número de mes.
     */
    private static final String[] NOMBRES_MESES = {
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    };

    // ====================== METODOS ==========================================

    /**
     * Se ejecuta automáticamente cuando JavaFX termina de cargar el FXML.
     * En este punto 'responsable' aún es null (se asigna después en setResponsable())
     * Solo se configuran las partes estáticas que no dependen del responsable.
     */
    @FXML
    public void initialize() {
        configurarSelectorPeriodo();
        // Configuración de como se rellenaran las columans
        configurarColumnasConsumos();
        configurarColumnasEmpleados();
        configurarColumnasHuella();
        // Configuración del panel lateral
        configurarClicEnFilaConsumo();
        configurarCambioTab();
        cargarFactores();
    }

    /**
     * Recibe el responsable seleccionado desde PR0 y actualiza toda la pantalla.
     * El controlador origen llama a este método DESPUÉS de cargar el FXML y
     * ANTES de mostrar la escena.
     *
     * @param responsable el responsable activo elegido en PR0
     */
    public void setResponsable(Responsable responsable) {
        this.responsable   = responsable;
        this.departamento  = responsable.getDepartamento();
        actualizarCabecera();
        cargarConsumos();
        cargarEmpleados();
    }

    // ========== Configuración inicial

    /**
     * Rellena los ComboBox de mes y año con los valores posibles y selecciona el período actual.
     * Registra los listeners que recargan la pestaña activa al cambiar el período.
     */
    private void configurarSelectorPeriodo() {
        combMes.getItems().addAll(NOMBRES_MESES);
        combMes.getSelectionModel().select(LocalDate.now().getMonthValue() - 1); // Carga el més actual

        // Muestra los 4 últimos años
        int anioActual = LocalDate.now().getYear();
        for (int a = anioActual; a >= anioActual - 4; a--) {
            combAnio.getItems().add(a);
        }
        combAnio.getSelectionModel().selectFirst(); // Selecciona el año actual
        // Refrescar la tabla cada vez qeu se cambie el mes/anio
        combMes.setOnAction(e  -> refrescarTabActiva());
        combAnio.setOnAction(e -> refrescarTabActiva());
    }

    /**
     * Asocia cada columna de la tabla de consumos (Tab Consumos) con el campo correspondiente.
     * La emisión se calcula en el momento: cantidad × factor_emisión.
     * Solo indica como se han de rellenar los datos en la tabla cuando se tenga (cuando ser realice setItems)
     */
    private void configurarColumnasConsumos() {
        // La tabla ocupa todo el ancho (la última columna se adapta
        tablaConsumos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        // Configuración de cada columna.
        // - Para cada fila usa la función anonima que recibe la fila actual `data` (java la pasa automáticamente)
        // - Extrae el objeto de data y devuelve el campo correspondiente
        colConsRecurso.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getFactorEmision().getNombre()));

        colConsUnidad.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getFactorEmision().getUnidad()));

        colConsCantidad.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getCantidad().toPlainString()));

        colConsAlcance.setCellValueFactory(data ->
                new SimpleStringProperty("Alcance " + data.getValue().getFactorEmision().getAlcance()));

        colConsEmision.setCellValueFactory(data -> {
            BigDecimal emision = data.getValue().calcularEmision();
            return new SimpleStringProperty(String.format("%.2f", emision));
        });
    }

    /**
     * Asocia cada columna de la tabla de empleados (Tab Trabajadores) con su campo.
     * No se configura clic en fila porque este tab es de solo lectura.
     * Solo indica como se han de rellenar los datos en la tabla cuando se tenga (cuando ser realice setItems)
     */
    private void configurarColumnasEmpleados() {
        tablaEmpleados.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        colEmpNombre.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getNombre()));

        colEmpCiudad.setCellValueFactory(data -> {
            Direccion dir = data.getValue().getDireccion();
            return new SimpleStringProperty(dir != null ? dir.getCiudad() : "—");
        });

        colEmpDias.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getDiasPresenciales())));

        colEmpVehiculo.setCellValueFactory(data -> {
            FactorEmision f = data.getValue().getMedioTransporte();
            return new SimpleStringProperty(f != null ? f.getNombre() : "—");
        });

        colEmpDistancia.setCellValueFactory(data -> {
            BigDecimal dist = data.getValue().getDistanciaTrabajo();
            return new SimpleStringProperty(dist != null ? dist.toPlainString() : "—");
        });
    }

    /**
     * Asocia cada columna de la tabla de huella (Tab Huella mensual) con su campo.
     * Las columnas son idénticas a las de tablaConsumos; ambas comparten la misma lista.
     */
    private void configurarColumnasHuella() {
        tablaHuella.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        colHuellaRecurso.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getFactorEmision().getNombre()));

        colHuellaUnidad.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getFactorEmision().getUnidad()));

        colHuellaCantidad.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getCantidad().toPlainString()));

        colHuellaAlcance.setCellValueFactory(data ->
                new SimpleStringProperty("Alcance " + data.getValue().getFactorEmision().getAlcance()));

        colHuellaEmision.setCellValueFactory(data -> {
            BigDecimal emision = data.getValue().calcularEmision();
            return new SimpleStringProperty(String.format("%.2f", emision));
        });
    }

    /**
     * Configura la apertura del panel lateral al hacer clic en una fila de consumos.
     * Un clic en una fila existente abre el panel en modo edición con los datos pre-rellenados.
     */
    private void configurarClicEnFilaConsumo() {
        tablaConsumos.setRowFactory(tv -> {
            TableRow<ConsumoMensual> fila = new TableRow<>(); // Crea una fila
            fila.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && !fila.isEmpty()) { //con un solo clic sobre una fila con datos
                    abrirPanelEditarConsumo(fila.getItem()); //Abre el panel y le pasa objeto de la fila
                }
            });
            return fila;
        });
    }

    /**
     * Carga los datos de los tabs Consumos y Huella al seleccionarlos,
     * evitando consultas innecesarias si el usuario no visita ese tab.
     * Trabajadores se carga una sola vez en setResponsable() y no cambia con el período.
     */
    private void configurarCambioTab() {
        // Obtiene una propiedad observalbe que representa al tab seleccionado. Cada vez que cambie java llamará al lambda
        tabPane.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, nuevo) -> { //De que tab venias y a cual has clicado
                    if (departamento == null || nuevo == null) return;
                    if (nuevo == tabConsumos)    cargarConsumos(); //navega a consumos
                    else if (nuevo == tabHuella) cargarHuella(); //navega a huella
                });
    }

    /**
     * Rellena el ComboBox del panel lateral con todos los factores de emisión disponibles.
     * Se muestra nombre + unidad para facilitar la identificación del recurso.
     */
    private void cargarFactores() {
        List<FactorEmision> factores = factorDAO.findAll();
        combFactor.setItems(FXCollections.observableArrayList(factores)); //exige observable (aunque aqui no es necesario)
        // extraer la información para rellenar el combobox. (sin llamar al toString original)
        combFactor.setConverter(new StringConverter<>() {
            //Usa esta función en lugar del toString original (sin modificarlo)
            @Override public String toString(FactorEmision f) {
                return f != null ? f.getNombre() + "  (" + f.getUnidad() + ")" : "";
            }
            @Override public FactorEmision fromString(String s) { return null; } //no se usa
        });
    }

    // Carga de datos

    /** Actualiza los labels de cabecera y breadcrumb con los datos del responsable activo. */
    private void actualizarCabecera() {
        String nombre    = responsable.getEncargado().getNombre();
        String dpto      = departamento.getNombre();
        String empresa   = departamento.getEmpresa() != null
                ? departamento.getEmpresa().getNombreSocial() : "";

        lblBreadcrumb.setText(nombre);
        lblNombreResponsable.setText(nombre);
        lblInfoResponsable.setText("Departamento: " + dpto + "  ·  Empresa: " + empresa);
    }

    /**
     * Consulta los consumos del departamento para el período seleccionado y actualiza
     * la tabla y el total. Ambas tablas (Consumos y Huella) comparten la misma lista
     * observable, por lo que se refrescan simultáneamente.
     */
    private void cargarConsumos() {
        if (departamento == null) return;

        int mes  = combMes.getSelectionModel().getSelectedIndex() + 1;
        int anio = combAnio.getSelectionModel().getSelectedItem();

        listaConsumos.setAll(servicioHuella.getConsumosMensuales(
                departamento.getIdDepartamento(), mes, anio));

        tablaConsumos.setItems(listaConsumos);
        tablaHuella.setItems(listaConsumos);

        lblTotalDpto.setText(String.format("%.2f kg CO₂e",
                servicioHuella.getTotalConsumos(listaConsumos)));
    }

    /**
     * Consulta los empleados del departamento y los muestra en la tabla de Trabajadores.
     * Se llama una sola vez en setResponsable(); no depende del período seleccionado.
     */
    private void cargarEmpleados() {
        listaEmpleados.setAll(servicioGestion.getEmpleadosDepartamento(departamento.getIdDepartamento()));
        tablaEmpleados.setItems(listaEmpleados);
    }

    /**
     * Calcula la huella del período usando ServicioCalculoHuella y actualiza los KPIs.
     * También refresca listaConsumos para que tablaHuella muestre los datos más recientes.
     * El campo de commuting indica si el Alcance 3 está habilitado para el departamento.
     */
    private void cargarHuella() {
        if (departamento == null) return;

        int mes  = combMes.getSelectionModel().getSelectedIndex() + 1;
        int anio = combAnio.getSelectionModel().getSelectedItem();

        // Refrescar consumos a través del Servicio (no del DAO directamente)
        listaConsumos.setAll(servicioHuella.getConsumosMensuales(
                departamento.getIdDepartamento(), mes, anio));

        //Calculos de la huella
        BigDecimal total          = servicioHuella.getHuellaTotalDepartamentoMes(departamento, mes, anio);
        Map<Integer, BigDecimal> porScope = servicioHuella.getHuellaPorScope(departamento, mes, anio);

        //Rellenar la UI
        kpiTotal.setText(String.format("%.2f kg CO₂e", total));
        kpiScope1.setText(String.format("%.2f kg CO₂e", porScope.getOrDefault(1, BigDecimal.ZERO)));
        kpiScope2.setText(String.format("%.2f kg CO₂e", porScope.getOrDefault(2, BigDecimal.ZERO)));
        kpiScope3.setText(String.format("%.2f kg CO₂e", porScope.getOrDefault(3, BigDecimal.ZERO)));

        if (departamento.isIncluirAlcance3()) {
            lblCommuting.setText(String.format("Huella de commuting incluida en el total: %.2f kg CO₂e",
                    porScope.getOrDefault(3, BigDecimal.ZERO)));
        } else {
            lblCommuting.setText("Commuting (Alcance 3) no habilitado para este departamento. "
                    + "El administrador puede activarlo en la configuración del departamento.");
        }
    }

    /** Recarga los datos del tab activo al cambiar el selector de período. */
    private void refrescarTabActiva() {
        if (departamento == null) return;
        Tab activa = tabPane.getSelectionModel().getSelectedItem();
        if (activa == tabConsumos)    cargarConsumos();
        else if (activa == tabHuella) cargarHuella();
    }

    // =========== Panel lateral: Nuevo consumo =========

    /**
     * Abre el panel lateral en modo "nuevo consumo" con todos los campos limpios.
     * El botón Eliminar se oculta porque no hay registro existente que borrar.
     */
    @FXML
    private void onNuevoConsumo() {
        consumoEditando = null;
        panelConsumoTitulo.setText("Nuevo consumo");
        combFactor.getSelectionModel().clearSelection();
        campCantidad.clear();
        lblUnidad.setText("—");
        lblAlcance.setText("—");
        btnEliminarConsumo.setVisible(false);
        btnEliminarConsumo.setManaged(false);
        panelConsumo.setVisible(true);
        panelConsumo.setManaged(true);
    }

    // ── Panel lateral: Editar consumo ─────────────────────────────────────

    /**
     * Abre el panel lateral en modo edición pre-rellenando los campos con el registro
     * seleccionado. El botón Eliminar se muestra para poder borrar el registro.
     *
     * @param consumo el consumo seleccionado en la tabla
     */
    private void abrirPanelEditarConsumo(ConsumoMensual consumo) {
        consumoEditando = consumo;
        panelConsumoTitulo.setText("Editar consumo");
        combFactor.setValue(consumo.getFactorEmision());
        campCantidad.setText(consumo.getCantidad().toPlainString()); //Pasa el BigDecimal a String legible
        lblUnidad.setText(consumo.getFactorEmision().getUnidad());
        lblAlcance.setText("Alcance " + consumo.getFactorEmision().getAlcance());
        btnEliminarConsumo.setVisible(true);
        btnEliminarConsumo.setManaged(true);
        panelConsumo.setVisible(true);
        panelConsumo.setManaged(true);
    }

    /**
     * Actualiza los labels de unidad y alcance al seleccionar un factor en el ComboBox.
     * Permite al responsable ver las unidades antes de introducir la cantidad.
     * Se llama desde la vista cada vez que se selecciona un factor
     */
    @FXML
    private void onFactorSeleccionado() {
        FactorEmision f = combFactor.getValue();
        if (f != null) { //Rellenar los valores configurados para el factor
            lblUnidad.setText(f.getUnidad());
            lblAlcance.setText("Alcance " + f.getAlcance());
        } else { //No hay tal factor
            lblUnidad.setText("—");
            lblAlcance.setText("—");
        }
    }

    /**
     * Crea un consumo nuevo o actualiza el existente según el modo del panel.
     * Si ya existe un consumo para el mismo factor en el mismo mes/departamento,
     * el DAO lanzará una excepción por la restricción UNIQUE — se muestra un aviso.
     */
    @FXML
    private void onGuardarConsumo() {
        if (combFactor.getValue() == null || campCantidad.getText().trim().isEmpty()) {
            mostrarError("El recurso y la cantidad son obligatorios.");
            return;
        }

        try {
            // Extraer datos de la UI
            BigDecimal cantidad = new BigDecimal(campCantidad.getText().trim().replace(',', '.'));
            int mes  = combMes.getSelectionModel().getSelectedIndex() + 1;
            int anio = combAnio.getSelectionModel().getSelectedItem();

            if (consumoEditando == null) {
                servicioHuella.crearConsumo(new ConsumoMensual(
                        cantidad, mes, anio, departamento, combFactor.getValue()));
            } else {
                consumoEditando.setCantidad(cantidad);
                consumoEditando.setFactorEmision(combFactor.getValue());
                servicioHuella.actualizarConsumo(consumoEditando);
            }

            cargarConsumos();
            cerrarPanelConsumo();
        } catch (NumberFormatException e) {
            mostrarError("La cantidad debe ser un valor numérico (ej: 125.50).");
        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage()); // Si ya existe un consumo para ese mes y factor saltara error
        }
    }

    /**
     * Elimina el consumo que se está editando tras confirmar con el usuario.
     * No se permite borrar si consumoEditando es null (no debería ocurrir, el botón está oculto).
     */
    @FXML
    private void onEliminarConsumo() {
        if (consumoEditando == null) return;

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Eliminar el registro de \""
                + consumoEditando.getFactorEmision().getNombre() + "\"?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");
        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                // BORRAR de la BD
                servicioHuella.eliminarConsumo(consumoEditando.getIdConsumo());
                cargarConsumos();
                cerrarPanelConsumo();
            }
        });
    }

    @FXML
    private void onCancelarConsumo() {
        cerrarPanelConsumo();
    }

    // Duplicar mes anterior
    /**
     * Copia todos los consumos del mes anterior al mes seleccionado actualmente.
     * Si ya existe un registro para el mismo factor en el mes actual, se omite
     * (no se sobreescribe) para preservar los datos que el responsable ya introdujo.
     */
    @FXML
    private void onDuplicarMes() {
        int mesActual  = combMes.getSelectionModel().getSelectedIndex() + 1;
        int anioActual = combAnio.getSelectionModel().getSelectedItem();

        int mesAnterior  = mesActual == 1 ? 12 : mesActual - 1;
        int anioAnterior = mesActual == 1 ? anioActual - 1 : anioActual;

        List<ConsumoMensual> anteriores = servicioHuella.getConsumosMensuales(
                departamento.getIdDepartamento(), mesAnterior, anioAnterior);

        if (anteriores.isEmpty()) {
            mostrarError("No hay consumos registrados en el mes anterior para duplicar.");
            return;
        }

        int creados = 0;
        for (ConsumoMensual anterior : anteriores) {
            boolean yaExiste = false;
            for (ConsumoMensual c : listaConsumos) { // No duplicar (ya se ha ingresado un valor para ese factor)
                if (c.getFactorEmision().getIdFactor() == anterior.getFactorEmision().getIdFactor()) {
                    yaExiste = true;
                    break;
                }
            }
            if (!yaExiste) { //Duplicar
                ConsumoMensual nuevo = new ConsumoMensual(
                        anterior.getCantidad(), mesActual, anioActual,
                        departamento, anterior.getFactorEmision());
                servicioHuella.crearConsumo(nuevo);
                creados++;
            }
        }

        cargarConsumos();

        if (creados == 0) {
            mostrarError("Todos los recursos del mes anterior ya tienen registro en el mes actual.");
        }
    }

    // ========== Exportación de datos (placeholders) =====================================

    /** Placeholder: TODO: exportación a PDF pendiente de implementar. */
    @FXML
    private void onExportarPDF() {
        mostrarError("La exportación a PDF estará disponible próximamente.");
    }

    /** Placeholder: TODO: exportación a CSV pendiente de implementar. */
    @FXML
    private void onExportarCSV() {
        mostrarError("La exportación a CSV estará disponible próximamente.");
    }

    // ============ Navegación entre pantallas ============

    /**
     * Vuelve a P0 (selección de rol) al hacer clic en "Inicio" del breadcrumb.
     * Se llama desde FXML
     */
    @FXML
    private void onVolverAP0() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/carbonaudit/view/p0-seleccion-rol-view.fxml"));
            Scene escena = new Scene(loader.load());
            Stage stage = (Stage) lblNombreResponsable.getScene().getWindow();
            stage.setScene(escena);
            stage.sizeToScene();
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Vuelve a PR0 (selección de responsable) al hacer clic en el breadcrumb.
     */
    @FXML
    private void onVolverAPR0() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/carbonaudit/view/pr0-seleccion-responsable-view.fxml"));
            Scene escena = new Scene(loader.load());
            Stage stage = (Stage) lblNombreResponsable.getScene().getWindow();
            stage.setScene(escena);
            stage.sizeToScene();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==== Métodos Auxiliares =========================

    private void cerrarPanelConsumo() {
        panelConsumo.setVisible(false);
        panelConsumo.setManaged(false);
        consumoEditando = null;
        combFactor.getSelectionModel().clearSelection();
        campCantidad.clear();
        lblUnidad.setText("—");
        lblAlcance.setText("—");
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Aviso");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}