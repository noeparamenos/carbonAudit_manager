package com.carbonaudit;

/**
 * Punto de entrada real de la aplicación (Launcher pattern).
 * Generada para
 * Delega en {@link Main} para arrancar JavaFX sin disparar la comprobación
 * estricta de módulos del JVM.
 *
 * Es el patrón "Launcher pattern"
 */
public class Launcher {

    public static void main(String[] args) {
        // Delegamos en Main.main() para que JavaFX se inicialice correctamente
        Main.main(args);
    }
}
