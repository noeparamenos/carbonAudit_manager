package com.carbonaudit.controller;

import com.carbonaudit.model.Empresa;
import javafx.fxml.FXML;

/**
 * Controlador de PA2 · Vista empresa.
 * Stub temporal — implementación pendiente.
 */
public class PA2Controller {

    private Empresa empresa;

    /**
     * Recibe la empresa seleccionada desde PA1.
     * PA1Controller llama a este método antes de mostrar la escena.
     */
    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    @FXML
    public void initialize() {
        // TODO: implementar cuando se desarrolle PA2
    }
}
