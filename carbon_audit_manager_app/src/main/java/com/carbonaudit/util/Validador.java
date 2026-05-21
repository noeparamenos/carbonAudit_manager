package com.carbonaudit.util;

import com.carbonaudit.model.Direccion;

import java.math.BigDecimal;

/**
 * Validaciones de formato y reglas de negocio reutilizables.
 * Cada método lanza IllegalArgumentException con mensaje legible si el dato no es válido.
 * Los servicios de gestión llaman a estos métodos antes de invocar al DAO.
 */
public final class Validador {

    private Validador() {}

    // ── CIF ───────────────────────────────────────────────────────────────────

    /**
     * Valida el formato y el dígito de control de un CIF español.
     * Formato: letra inicial válida + 7 dígitos + carácter de control (dígito o letra A-J).
     */
    public static void validarCIF(String cif) {
        if (cif == null || cif.isBlank())
            throw new IllegalArgumentException("El CIF no puede estar vacío.");

        String c = cif.trim().toUpperCase();

        if (!c.matches("[A-HJ-NP-SUVW]\\d{7}[0-9A-J]"))
            throw new IllegalArgumentException(
                    "El CIF «" + cif + "» no tiene formato válido. " +
                    "Debe tener 9 caracteres: letra inicial (A-H, J-N, P-S, U-W), " +
                    "7 dígitos y un carácter de control (dígito o letra A-J). Ejemplo: B12345674.");

        // Suma de posiciones impares (×2, si >9 restar 9) y pares del número de 7 dígitos
        int suma = 0;
        for (int i = 0; i < 7; i++) {
            int d = c.charAt(i + 1) - '0';
            if (i % 2 == 0) {
                d *= 2;
                if (d > 9) d -= 9;
            }
            suma += d;
        }
        int control = (10 - suma % 10) % 10;

        char ultimo   = c.charAt(8);
        char tipo     = c.charAt(0);
        // Mapeo control → letra: 0→J, 1→A, 2→B, …, 9→I
        char letraEsp = control == 0 ? 'J' : (char) ('A' + control - 1);
        char digitoEsp = (char) ('0' + control);

        boolean valido;
        if ("ABEH".indexOf(tipo) >= 0)         valido = (ultimo == digitoEsp);
        else if ("KPQRSW".indexOf(tipo) >= 0)  valido = (ultimo == letraEsp);
        else                                    valido = (ultimo == digitoEsp || ultimo == letraEsp);

        if (!valido) {
            String esperado = "ABEH".indexOf(tipo) >= 0  ? String.valueOf(digitoEsp)
                            : "KPQRSW".indexOf(tipo) >= 0 ? String.valueOf(letraEsp)
                            : digitoEsp + " o " + letraEsp;
            throw new IllegalArgumentException(
                    "El dígito de control del CIF «" + cif + "» no es válido " +
                    "(se esperaba «" + esperado + "»).");
        }
    }

    // Email

    /** Valida el formato básico de un email. Campo opcional: si está vacío o es null, no lanza excepción. */
    public static void validarEmail(String email) {
        if (email == null || email.isBlank()) return;

        String e = email.trim();
        int at = e.indexOf('@');
        if (at < 1 || at == e.length() - 1 || e.indexOf('.', at) < 0)
            throw new IllegalArgumentException(
                    "El email «" + email + "» no tiene un formato válido.");
    }

    // ── Teléfono ──────────────────────────────────────────────────────────────

    /** Valida el formato básico de un teléfono. Campo opcional: si está vacío o es null, no lanza excepción. */
    public static void validarTelefono(String telefono) {
        if (telefono == null || telefono.isBlank()) return;

        if (!telefono.trim().matches("[+\\d][\\d\\s\\-]{6,14}"))
            throw new IllegalArgumentException(
                    "El teléfono «" + telefono + "» no tiene un formato válido.");
    }


    /** Valida los campos obligatorios y el formato de una dirección. */
    public static void validarDireccion(Direccion dir) {
        if (dir == null)
            throw new IllegalArgumentException("La dirección es obligatoria.");

        if (dir.getCalle() == null || dir.getCalle().isBlank())
            throw new IllegalArgumentException("La calle es obligatoria.");

        if (dir.getNumero() <= 0)
            throw new IllegalArgumentException("El número de la dirección debe ser un valor positivo.");

        if (dir.getCiudad() == null || dir.getCiudad().isBlank())
            throw new IllegalArgumentException("La ciudad es obligatoria.");

        if (dir.getCodigoPostal() == null || dir.getCodigoPostal().isBlank())
            throw new IllegalArgumentException("El código postal es obligatorio.");

        if (!dir.getCodigoPostal().trim().matches("\\d{5}"))
            throw new IllegalArgumentException(
                    "El código postal «" + dir.getCodigoPostal() + "» debe tener exactamente 5 dígitos.");
    }

    // Empleado

    /** Valida que los días presenciales estén entre 1 y 31. */
    public static void validarDiasPresenciales(int dias) {
        if (dias < 1 || dias > 31)
            throw new IllegalArgumentException(
                    "Los días presenciales deben estar entre 1 y 31 (valor recibido: " + dias + ").");
    }

    // Factor de emisión

    /** Valida que el valor del factor sea cero o positivo (ej: bicicleta = 0 kg CO₂e/km). */
    public static void validarValorFactor(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException(
                    "El factor de emisión no puede ser nulo ni negativo.");
    }

    /** Valida que el alcance sea 1, 2 ó 3 (GHG Protocol). */
    public static void validarAlcance(int alcance) {
        if (alcance < 1 || alcance > 3)
            throw new IllegalArgumentException(
                    "El alcance debe ser 1, 2 ó 3 (valor recibido: " + alcance + ").");
    }
}