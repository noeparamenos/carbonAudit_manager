package com.carbonaudit.controller;

import com.carbonaudit.model.*;
import com.carbonaudit.service.ServicioGestionDepartamento;
import com.carbonaudit.service.ServicioGestionEmpleado;
import com.carbonaudit.service.ServicioGestionFactores;
import com.carbonaudit.service.ServicioGestionResponsable;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controlador de PA2 · Gestión de departamento.
 *
 * Realiza:
 *   - Mostrar la cabecera del departamento (nombre, empresa, ciudad).
 *   - Tab Empleados: listar, crear, editar y dar de baja empleados.
 *   - Tab Responsable: ver responsable activo, asignar nuevo, finalizar mandato e historial.
 *   - Editar y eliminar el departamento activo.
 *   - Navegación de vuelta a PA1 (empresa) o PA0 (lista de empresas).
 */
public class PA2Controller {

    // ── Breadcrumb ────────────────────────────────────────────────────────

    @FXML private Label lblBreadcrumbEmpresa;
    @FXML private Label lblBreadcrumbDpto;

    // ── Cabecera ──────────────────────────────────────────────────────────

    @FXML private Label lblNombreDpto;
    @FXML private Label lblInfoDpto;

    // ── Tab Empleados ─────────────────────────────────────────────────────

    @FXML private TableView<Empleado>           tablaEmpleados;
    @FXML private TableColumn<Empleado, String> colEmpNombre;
    @FXML private TableColumn<Empleado, String> colEmpCiudad;
    @FXML private TableColumn<Empleado, String> colEmpDias;
    @FXML private TableColumn<Empleado, String> colEmpVehiculo;
    @FXML private TableColumn<Empleado, String> colEmpDistancia;

    // ── Tab Responsable ───────────────────────────────────────────────────

    @FXML private Label  lblResponsableActivo;
    @FXML private Button btnFinalizarMandato;

    @FXML private TableView<Responsable>           tablaHistorial;
    @FXML private TableColumn<Responsable, String> colRespNombre;
    @FXML private TableColumn<Responsable, String> colRespDesde;
    @FXML private TableColumn<Responsable, String> colRespHasta;

    // ── Panel lateral: Empleado ───────────────────────────────────────────

    @FXML private VBox      panelEmpleado;
    @FXML private Label     panelEmpleadoTitulo;
    @FXML private TextField campEmpNombre;
    @FXML private TextField campEmpDias;
    @FXML private ComboBox<FactorEmision> combEmpVehiculo;
    @FXML private TextField campEmpCalle;
    @FXML private TextField campEmpNumero;
    @FXML private TextField campEmpCiudad;
    @FXML private TextField campEmpCp;
    @FXML private TextField campEmpProvincia;
    @FXML private Button    btnDarDeBaja;

    // ── Panel lateral: Asignar responsable ───────────────────────────────

    @FXML private VBox panelResponsable;
    @FXML private ComboBox<Empleado> combRespEmpleado;
    @FXML private DatePicker dateRespInicio;

    // ── Panel lateral: Editar departamento ───────────────────────────────

    @FXML private VBox      panelEditDpto;
    @FXML private TextField editDptoNombre;
    @FXML private TextField editDptoDesc;
    @FXML private CheckBox  chkAlcance3;
    @FXML private TextField editDptoCalle;
    @FXML private TextField editDptoNumero;
    @FXML private TextField editDptoCiudad;
    @FXML private TextField editDptoCp;
    @FXML private TextField editDptoProvincia;

    // ── Servicios ─────────────────────────────────────────────────────────

    private final ServicioGeograficoORS      geoService           = new ServicioGeograficoORS();
    private final ServicioGestionEmpleado    servicioGestion      = new ServicioGestionEmpleado(geoService);
    private final ServicioGestionResponsable servicioResponsable  = new ServicioGestionResponsable();
    private final ServicioGestionDepartamento servicioDepartamento = new ServicioGestionDepartamento();
    private final ServicioGestionFactores    servicioFactores     = new ServicioGestionFactores();

    // ── Estado ────────────────────────────────────────────────────────────

    private Departamento departamento;

    /*
     * Empleado que se está editando en el panel lateral.
     * null indica modo "nuevo empleado".
     */
    private Empleado empleadoEditando;

    /*
     * listaEmpleados: observada por tablaEmpleados.
     * listaHistorial: observada por tablaHistorial (historial de responsables del departamento).
     * Un setAll() en cualquiera de ellas refresca su tabla automáticamente.
     */
    private final ObservableList<Empleado>    listaEmpleados = FXCollections.observableArrayList();
    private final ObservableList<Responsable> listaHistorial = FXCollections.observableArrayList();

    // ── Inicialización ─────────────────────────────────────────────────────

    /**
     * Se ejecuta automáticamente al cargar el FXML.
     * Solo configura las partes estáticas; los datos se cargan en setDepartamento().
     */
    @FXML
    public void initialize() {
        configurarColumnasEmpleados();
        configurarColumnasHistorial();
        configurarClicEnFilaEmpleado();
        cargarVehiculos();
    }

    /**
     * Recibe el departamento seleccionado desde PA1 y actualiza toda la pantalla.
     * Llamado por PA1Controller después de cargar el FXML y antes de mostrar la escena.
     */
    public void setDepartamento(Departamento departamento) {
        this.departamento = departamento;
        actualizarCabecera();
        tablaEmpleados.setItems(listaEmpleados); // suscripción única
        tablaHistorial.setItems(listaHistorial);
        cargarEmpleados();
        cargarResponsable();
    }

    // ── Configuración inicial ──────────────────────────────────────────────

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
            var dist = data.getValue().getDistanciaTrabajo();
            return new SimpleStringProperty(dist != null ? dist.toPlainString() : "—");
        });
    }

    private void configurarColumnasHistorial() {
        tablaHistorial.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        colRespNombre.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getEncargado().getNombre()));

        colRespDesde.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getFechaInicio().toString()));

        colRespHasta.setCellValueFactory(data -> {
            LocalDate fin = data.getValue().getFechaFin();
            return new SimpleStringProperty(fin != null ? fin.toString() : "Activo");
        });
    }

    /** Un clic en una fila de empleados abre el panel en modo edición. */
    private void configurarClicEnFilaEmpleado() {
        tablaEmpleados.setRowFactory(tv -> {
            TableRow<Empleado> fila = new TableRow<>();
            fila.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && !fila.isEmpty()) {
                    abrirPanelEditarEmpleado(fila.getItem());
                }
            });
            return fila;
        });
    }

    /**
     * Carga los factores de emisión de alcance 3 (medios de transporte) en el ComboBox.
     * Usa un StringConverter para mostrar nombre + unidad en lugar del toString() por defecto.
     */
    private void cargarVehiculos() {
        List<FactorEmision> vehiculos = servicioFactores.getFactoresPorAlcance(3);
        combEmpVehiculo.setItems(FXCollections.observableArrayList(vehiculos));
        combEmpVehiculo.setConverter(new StringConverter<>() {
            @Override public String toString(FactorEmision f) {
                return f != null ? f.getNombre() + " (" + f.getUnidad() + ")" : "";
            }
            @Override public FactorEmision fromString(String s) { return null; }
        });
    }

    // ── Carga de datos ────────────────────────────────────────────────────

    /** Actualiza los labels de breadcrumb y cabecera con los datos del departamento activo. */
    private void actualizarCabecera() {
        String empresa = departamento.getEmpresa() != null
                ? departamento.getEmpresa().getNombreSocial() : "";
        String ciudad = departamento.getDireccion() != null
                ? departamento.getDireccion().getCiudad() : "";

        lblBreadcrumbEmpresa.setText(empresa);
        lblBreadcrumbDpto.setText(departamento.getNombre());
        lblNombreDpto.setText(departamento.getNombre());
        lblInfoDpto.setText("Empresa: " + empresa + "  ·  " + ciudad);
    }

    /** Consulta los empleados activos del departamento y refresca la tabla. */
    private void cargarEmpleados() {
        listaEmpleados.setAll(servicioGestion.getEmpleadosDepartamento(departamento.getIdDepartamento()));
    }

    /**
     * Actualiza la sección de responsable activo y recarga el historial.
     * Si hay responsable activo muestra su nombre y habilita "Finalizar mandato".
     */
    private void cargarResponsable() {
        Optional<Responsable> activo = servicioResponsable.getActivoByDepartamento(
                departamento.getIdDepartamento());

        if (activo.isPresent()) {
            Responsable r = activo.get();
            lblResponsableActivo.setText(
                    r.getEncargado().getNombre() + "  (desde " + r.getFechaInicio() + ")");
            btnFinalizarMandato.setVisible(true);
            btnFinalizarMandato.setManaged(true);
        } else {
            lblResponsableActivo.setText("Sin responsable activo");
            btnFinalizarMandato.setVisible(false);
            btnFinalizarMandato.setManaged(false);
        }

        listaHistorial.setAll(
                servicioResponsable.getAllByDepartamento(departamento.getIdDepartamento()));
    }

    // ── Panel lateral: Nuevo empleado ─────────────────────────────────────

    /** Abre el panel lateral en modo "nuevo empleado" con todos los campos limpios. */
    @FXML
    private void onNuevoEmpleado() {
        empleadoEditando = null;
        panelEmpleadoTitulo.setText("Nuevo empleado");
        limpiarFormEmpleado();
        campEmpDias.setText("20");
        btnDarDeBaja.setVisible(false);
        btnDarDeBaja.setManaged(false);
        cerrarPanelResponsable();
        cerrarPanelEditDpto();
        panelEmpleado.setVisible(true);
        panelEmpleado.setManaged(true);
    }

    // ── Panel lateral: Editar empleado ────────────────────────────────────

    /**
     * Abre el panel lateral en modo edición pre-rellenando los campos con los datos del empleado.
     * Muestra el botón "Dar de baja" en este modo.
     */
    private void abrirPanelEditarEmpleado(Empleado emp) {
        empleadoEditando = emp;
        panelEmpleadoTitulo.setText("Editar empleado");

        campEmpNombre.setText(emp.getNombre());
        campEmpDias.setText(String.valueOf(emp.getDiasPresenciales()));
        combEmpVehiculo.setValue(emp.getMedioTransporte());

        if (emp.getDireccion() != null) {
            Direccion dir = emp.getDireccion();
            campEmpCalle.setText(dir.getCalle());
            campEmpNumero.setText(String.valueOf(dir.getNumero()));
            campEmpCiudad.setText(dir.getCiudad());
            campEmpCp.setText(dir.getCodigoPostal());
            campEmpProvincia.setText(dir.getProvincia() != null ? dir.getProvincia() : "");
        }

        btnDarDeBaja.setVisible(true);
        btnDarDeBaja.setManaged(true);
        cerrarPanelResponsable();
        cerrarPanelEditDpto();
        panelEmpleado.setVisible(true);
        panelEmpleado.setManaged(true);
    }

    /** Crea un empleado nuevo o actualiza el que se está editando. */
    @FXML
    private void onGuardarEmpleado() {
        String nombre = campEmpNombre.getText().trim();
        if (nombre.isEmpty()
                || campEmpDias.getText().trim().isEmpty()
                || combEmpVehiculo.getValue() == null
                || campEmpCalle.getText().trim().isEmpty()
                || campEmpNumero.getText().trim().isEmpty()
                || campEmpCiudad.getText().trim().isEmpty()
                || campEmpCp.getText().trim().isEmpty()) {
            mostrarError("Los campos marcados con * son obligatorios.");
            return;
        }

        try {
            int dias = Integer.parseInt(campEmpDias.getText().trim());
            if (dias < 1 || dias > 31) {
                mostrarError("Los días presenciales deben estar entre 1 y 31.");
                return;
            }
            int numCalle = Integer.parseInt(campEmpNumero.getText().trim());

            Empleado empleadoParaApi;

            if (empleadoEditando == null) {
                Direccion dir = new Direccion();
                dir.setCalle(campEmpCalle.getText().trim());
                dir.setNumero(numCalle);
                dir.setCiudad(campEmpCiudad.getText().trim());
                dir.setCodigoPostal(campEmpCp.getText().trim());
                dir.setProvincia(campEmpProvincia.getText().trim());

                Empleado nuevo = new Empleado();
                nuevo.setNombre(nombre);
                nuevo.setDiasPresenciales(dias);
                nuevo.setMedioTransporte(combEmpVehiculo.getValue());
                nuevo.setDireccion(dir);
                nuevo.setDepartamento(departamento);
                servicioGestion.crearEmpleado(nuevo);
                empleadoParaApi = nuevo;
            } else {
                empleadoEditando.setNombre(nombre);
                empleadoEditando.setDiasPresenciales(dias);
                empleadoEditando.setMedioTransporte(combEmpVehiculo.getValue());

                Direccion dir = empleadoEditando.getDireccion();
                dir.setCalle(campEmpCalle.getText().trim());
                dir.setNumero(numCalle);
                dir.setCiudad(campEmpCiudad.getText().trim());
                dir.setCodigoPostal(campEmpCp.getText().trim());
                dir.setProvincia(campEmpProvincia.getText().trim());
                servicioGestion.actualizarEmpleado(empleadoEditando);
                empleadoParaApi = empleadoEditando;
            }

            cargarEmpleados();
            cerrarPanelEmpleado();
            calcularDistanciaEnSegundoPlano(empleadoParaApi);
        } catch (NumberFormatException e) {
            mostrarError("El número de calle y los días presenciales deben ser valores numéricos.");
        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());
        }
    }

    /**
     * Marca al empleado como dado de baja (soft delete).
     * Se conserva el historial de commuting y responsabilidad para auditoría.
     */
    @FXML
    private void onDarDeBaja() {
        if (empleadoEditando == null) return;

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar baja");
        confirmacion.setHeaderText("¿Dar de baja a \"" + empleadoEditando.getNombre() + "\"?");
        confirmacion.setContentText(
                "El empleado quedará inactivo. Sus registros históricos se conservarán para auditoría.");
        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                servicioGestion.darDeBajaEmpleado(empleadoEditando.getIdEmpleado(), LocalDate.now());
                cargarEmpleados();
                cerrarPanelEmpleado();
            }
        });
    }

    /** Cierra el panel lateral sin guardar cambios. */
    @FXML
    private void onCancelarEmpleado() {
        cerrarPanelEmpleado();
    }

    // ── Panel lateral: Asignar responsable ───────────────────────────────

    /**
     * Abre el panel de asignación de responsable con el ComboBox poblado con
     * los empleados activos del departamento y la fecha de hoy preseleccionada.
     */
    @FXML
    private void onAsignarResponsable() {
        List<Empleado> empleados = servicioGestion.getEmpleadosDepartamento(departamento.getIdDepartamento());
        combRespEmpleado.setItems(FXCollections.observableArrayList(empleados));
        combRespEmpleado.setConverter(new StringConverter<>() {
            @Override public String toString(Empleado e) { return e != null ? e.getNombre() : ""; }
            @Override public Empleado fromString(String s) { return null; }
        });
        combRespEmpleado.getSelectionModel().clearSelection();
        dateRespInicio.setValue(LocalDate.now());
        cerrarPanelEmpleado();
        cerrarPanelEditDpto();
        panelResponsable.setVisible(true);
        panelResponsable.setManaged(true);
    }

    /**
     * Crea el nuevo mandato de responsabilidad.
     * Si existe un responsable activo, cierra su mandato el día anterior a la nueva fecha de inicio.
     */
    @FXML
    private void onGuardarResponsable() {
        if (combRespEmpleado.getValue() == null || dateRespInicio.getValue() == null) {
            mostrarError("El empleado y la fecha de inicio son obligatorios.");
            return;
        }

        Optional<Responsable> activo = servicioResponsable.getActivoByDepartamento(
                departamento.getIdDepartamento());
        activo.ifPresent(r -> {
            r.setFechaFin(dateRespInicio.getValue().minusDays(1));
            servicioResponsable.actualizarResponsable(r);
        });

        Responsable nuevo = new Responsable(
                dateRespInicio.getValue(),
                departamento,
                combRespEmpleado.getValue());
        servicioResponsable.crearResponsable(nuevo);

        cargarResponsable();
        cerrarPanelResponsable();
    }

    /**
     * Pone fecha_fin = hoy en el responsable activo del departamento,
     * terminando su mandato.
     */
    @FXML
    private void onFinalizarMandato() {
        Optional<Responsable> activo = servicioResponsable.getActivoByDepartamento(
                departamento.getIdDepartamento());
        if (activo.isEmpty()) return;

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Finalizar mandato");
        confirmacion.setHeaderText(
                "¿Finalizar el mandato de \"" + activo.get().getEncargado().getNombre() + "\"?");
        confirmacion.setContentText("Se registrará la fecha de hoy como fin del mandato.");
        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                Responsable r = activo.get();
                r.setFechaFin(LocalDate.now());
                servicioResponsable.actualizarResponsable(r);
                cargarResponsable();
            }
        });
    }

    @FXML
    private void onCancelarResponsable() {
        cerrarPanelResponsable();
    }

    // ── Panel lateral: Editar departamento ───────────────────────────────

    @FXML
    private void onEditarDpto() {
        editDptoNombre.setText(departamento.getNombre());
        editDptoDesc.setText(departamento.getDescripcion() != null ? departamento.getDescripcion() : "");
        chkAlcance3.setSelected(departamento.isIncluirAlcance3());
        if (departamento.getDireccion() != null) {
            Direccion dir = departamento.getDireccion();
            editDptoCalle.setText(dir.getCalle());
            editDptoNumero.setText(String.valueOf(dir.getNumero()));
            editDptoCiudad.setText(dir.getCiudad());
            editDptoCp.setText(dir.getCodigoPostal());
            editDptoProvincia.setText(dir.getProvincia() != null ? dir.getProvincia() : "");
        }
        cerrarPanelEmpleado();
        cerrarPanelResponsable();
        panelEditDpto.setVisible(true);
        panelEditDpto.setManaged(true);
    }

    @FXML
    private void onGuardarEditDpto() {
        String nombre = editDptoNombre.getText().trim();
        if (nombre.isEmpty()
                || editDptoCalle.getText().trim().isEmpty()
                || editDptoNumero.getText().trim().isEmpty()
                || editDptoCiudad.getText().trim().isEmpty()
                || editDptoCp.getText().trim().isEmpty()) {
            mostrarError("Los campos marcados con * son obligatorios.");
            return;
        }
        try {
            departamento.setNombre(nombre);
            departamento.setDescripcion(editDptoDesc.getText().trim());
            departamento.setIncluirAlcance3(chkAlcance3.isSelected());

            Direccion dir = departamento.getDireccion();
            dir.setCalle(editDptoCalle.getText().trim());
            dir.setNumero(Integer.parseInt(editDptoNumero.getText().trim()));
            dir.setCiudad(editDptoCiudad.getText().trim());
            dir.setCodigoPostal(editDptoCp.getText().trim());
            dir.setProvincia(editDptoProvincia.getText().trim());
            servicioDepartamento.actualizarDepartamento(departamento);
            actualizarCabecera();
            cerrarPanelEditDpto();

            // Geocodificar la nueva dirección en segundo plano
            geocodificarDireccionEnSegundoPlano(dir);
        } catch (NumberFormatException e) {
            mostrarError("El número de calle debe ser un valor numérico.");
        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());
        } catch (RuntimeException e) {
            mostrarError("Error al guardar: " + e.getMessage());
        }
    }

    @FXML
    private void onCancelarEditDpto() {
        cerrarPanelEditDpto();
    }

    // ── Eliminar departamento ─────────────────────────────────────────────

    @FXML
    private void onEliminarDpto() {
        List<Empleado> empleados = servicioGestion.getEmpleadosDepartamento(departamento.getIdDepartamento());
        if (!empleados.isEmpty()) {
            mostrarError("No se puede eliminar el departamento porque tiene "
                    + empleados.size() + " empleado(s) activo(s).\n"
                    + "Da de baja a todos los empleados primero.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Eliminar \"" + departamento.getNombre() + "\"?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");
        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                servicioDepartamento.eliminarDepartamento(departamento.getIdDepartamento());
                navegarAPA1();
            }
        });
    }

    // ── Navegación ─────────────────────────────────────────────────────────

    @FXML
    private void onVolverAPA0() {
        navegarAPA0();
    }

    @FXML
    private void onVolverAPA1() {
        navegarAPA1();
    }

    /** Vuelve a PA0 (lista de empresas) al hacer clic en el breadcrumb de empresa. */
    private void navegarAPA0() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/carbonaudit/view/pa0-empresas-view.fxml"));
            Scene escena = new Scene(loader.load());
            Stage stage = (Stage) lblNombreDpto.getScene().getWindow();
            stage.setScene(escena);
            stage.sizeToScene();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Vuelve a PA1 pasando la empresa del departamento actual.
     * La empresa está disponible porque DepartamentoDAO la resuelve por composición al mapear.
     */
    private void navegarAPA1() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/carbonaudit/view/pa1-empresa-view.fxml"));
            Scene escena = new Scene(loader.load());
            PA1Controller pa1 = loader.getController();
            pa1.setEmpresa(departamento.getEmpresa());
            Stage stage = (Stage) lblNombreDpto.getScene().getWindow();
            stage.setScene(escena);
            stage.sizeToScene();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Geocodificación y cálculo de distancia ────────────────────────────────

    /**
     * Geocodifica una dirección en segundo plano y persiste las coordenadas.
     * Se llama al guardar el departamento para que sus coords estén listas
     * antes de que se calcule la distancia de cualquier empleado.
     */
    private void geocodificarDireccionEnSegundoPlano(Direccion dir) {
        Task<Void> tarea = new Task<>() {
            @Override
            protected Void call() throws Exception {
                geoService.completarCoordenadas(dir); // hilo secundario: llama a la API ORS
                return null;
            }
        };

        tarea.setOnSucceeded(e -> servicioGestion.persistirCoordenadas(dir)); // hilo JavaFX: persiste coords
        tarea.setOnFailed(e -> Platform.runLater(() ->
                mostrarError("No se pudo geocodificar la dirección del departamento "
                        + "(comprueba la conexión y la clave API ORS).")));

        Thread hilo = new Thread(tarea);
        hilo.setDaemon(true); // muere si la app se cierra
        hilo.start();
    }



    /**
     * Llama a la API de ORS en un hilo secundario para calcular la distancia
     * entre la residencia del empleado y la dirección del departamento.
     * Al terminar actualiza la BD y refresca la tabla sin bloquear la UI.
     * Si la API falla, el empleado queda guardado pero con distancia nula.
     */
    private void calcularDistanciaEnSegundoPlano(Empleado empleado) {
        //Runeable con la tarea a ejecutar (metodo call()). NO se toca la UI durante la tarea
        Task<Void> tarea = new Task<>() {
            @Override
            protected Void call() throws Exception {
                servicioGestion.calcularYPersistirDistancia(empleado);
                return null;
            }
        };
        // Se ejecuta cuando call() termina. Podemos modificar la UI (la tarea pesada ya acabó)
        tarea.setOnSucceeded(e -> cargarEmpleados()); //Refresca la tabla

        tarea.setOnFailed(e -> Platform.runLater(() -> { // Mensjaje de error
            mostrarError("El empleado se guardó, pero no se pudo calcular la distancia al trabajo "
                    + "(comprueba la conexión y la clave API ORS).");
            cargarEmpleados();
        }));

        //Crear y Lanzar el Hilo con el runeable
        Thread hilo = new Thread(tarea);
        hilo.setDaemon(true); // muere si la app se cierra
        hilo.start();
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private void cerrarPanelEmpleado() {
        panelEmpleado.setVisible(false);
        panelEmpleado.setManaged(false);
        limpiarFormEmpleado();
        empleadoEditando = null;
    }

    private void cerrarPanelResponsable() {
        panelResponsable.setVisible(false);
        panelResponsable.setManaged(false);
    }

    private void cerrarPanelEditDpto() {
        panelEditDpto.setVisible(false);
        panelEditDpto.setManaged(false);
    }

    private void limpiarFormEmpleado() {
        campEmpNombre.clear();
        campEmpDias.clear();
        combEmpVehiculo.getSelectionModel().clearSelection();
        campEmpCalle.clear();
        campEmpNumero.clear();
        campEmpCiudad.clear();
        campEmpCp.clear();
        campEmpProvincia.clear();
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Aviso");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}