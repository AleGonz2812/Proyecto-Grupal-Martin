package com.eventos.services;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;

/**
 * Servicio de procesamiento de pagos simulado.
 * Valida tarjetas y simula transacciones sin procesar pagos reales.
 */
public class PagoService {
    
    private static final Random random = new Random();
    
    /**
     * Valida un número de tarjeta usando el algoritmo de Luhn.
     */
    public static boolean validarNumeroTarjeta(String numero) {
        if (numero == null || numero.isEmpty()) {
            return false;
        }
        
        // Remover espacios y guiones
        numero = numero.replaceAll("[\\s-]", "");
        
        // Verificar que solo tenga dígitos
        if (!numero.matches("\\d+")) {
            return false;
        }
        
        // Verificar longitud (13-19 dígitos)
        if (numero.length() < 13 || numero.length() > 19) {
            return false;
        }
        
        // Algoritmo de Luhn
        int sum = 0;
        boolean alternate = false;
        
        for (int i = numero.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(numero.charAt(i));
            
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }
            
            sum += digit;
            alternate = !alternate;
        }
        
        return (sum % 10 == 0);
    }
    
    /**
     * Detecta el tipo de tarjeta basándose en el número.
     */
    public static String detectarTipoTarjeta(String numero) {
        if (numero == null || numero.isEmpty()) {
            return "Desconocida";
        }
        
        numero = numero.replaceAll("[\\s-]", "");
        
        if (numero.startsWith("4")) {
            return "Visa";
        } else if (numero.matches("^5[1-5].*")) {
            return "Mastercard";
        } else if (numero.matches("^3[47].*")) {
            return "American Express";
        } else if (numero.matches("^6(?:011|5).*")) {
            return "Discover";
        } else if (numero.matches("^35.*")) {
            return "JCB";
        }
        
        return "Desconocida";
    }
    
    /**
     * Valida la fecha de expiración de la tarjeta.
     */
    public static boolean validarFechaExpiracion(String fecha) {
        if (fecha == null || fecha.isEmpty()) {
            return false;
        }
        
        try {
            // Formato esperado: MM/AA o MM/YYYY
            String[] partes = fecha.split("/");
            if (partes.length != 2) {
                return false;
            }
            
            int mes = Integer.parseInt(partes[0]);
            int anio = Integer.parseInt(partes[1]);
            
            // Si el año es de 2 dígitos, convertirlo a 4
            if (anio < 100) {
                anio += 2000;
            }
            
            // Validar mes
            if (mes < 1 || mes > 12) {
                return false;
            }
            
            // Verificar que no haya expirado
            YearMonth fechaExpiracion = YearMonth.of(anio, mes);
            YearMonth fechaActual = YearMonth.now();
            
            return !fechaExpiracion.isBefore(fechaActual);
            
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Valida el código CVV.
     */
    public static boolean validarCVV(String cvv, String tipoTarjeta) {
        if (cvv == null || cvv.isEmpty()) {
            return false;
        }
        
        // CVV solo debe contener dígitos
        if (!cvv.matches("\\d+")) {
            return false;
        }
        
        // American Express usa 4 dígitos, el resto 3
        int longitudEsperada = "American Express".equals(tipoTarjeta) ? 4 : 3;
        
        return cvv.length() == longitudEsperada;
    }
    
    /**
     * Procesa un pago simulado.
     * Retorna un resultado con información de la transacción.
     */
    public static ResultadoPago procesarPago(
            String numeroTarjeta, 
            String fechaExpiracion, 
            String cvv, 
            String titular,
            double monto) {
        
        // Simular tiempo de procesamiento
        try {
            Thread.sleep(1500 + random.nextInt(1000)); // 1.5-2.5 segundos
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Validaciones
        if (!validarNumeroTarjeta(numeroTarjeta)) {
            return new ResultadoPago(false, "Número de tarjeta inválido", null, null);
        }
        
        if (!validarFechaExpiracion(fechaExpiracion)) {
            return new ResultadoPago(false, "Fecha de expiración inválida o tarjeta vencida", null, null);
        }
        
        String tipoTarjeta = detectarTipoTarjeta(numeroTarjeta);
        if (!validarCVV(cvv, tipoTarjeta)) {
            return new ResultadoPago(false, "Código CVV inválido", null, null);
        }
        
        if (titular == null || titular.trim().isEmpty()) {
            return new ResultadoPago(false, "Nombre del titular requerido", null, null);
        }
        
        if (monto <= 0) {
            return new ResultadoPago(false, "Monto inválido", null, null);
        }
        
        // Simular aprobación (95% de éxito)
        boolean aprobado = random.nextInt(100) < 95;
        
        if (aprobado) {
            String numeroAutorizacion = generarNumeroAutorizacion();
            String numeroTransaccion = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            
            return new ResultadoPago(
                true, 
                "Pago aprobado", 
                numeroAutorizacion, 
                numeroTransaccion
            );
        } else {
            return new ResultadoPago(false, "Pago rechazado por el banco", null, null);
        }
    }
    
    /**
     * Genera un número de autorización simulado.
     */
    private static String generarNumeroAutorizacion() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
    
    /**
     * Obtiene números de tarjeta de prueba válidos.
     */
    public static String[] obtenerTarjetasPrueba() {
        return new String[] {
            "4532015112830366",  // Visa
            "5425233430109903",  // Mastercard
            "374245455400126",   // American Express
            "6011000991001201",  // Discover
        };
    }
    
    /**
     * Clase para representar el resultado de un pago.
     */
    public static class ResultadoPago {
        private final boolean exitoso;
        private final String mensaje;
        private final String numeroAutorizacion;
        private final String numeroTransaccion;
        
        public ResultadoPago(boolean exitoso, String mensaje, String numeroAutorizacion, String numeroTransaccion) {
            this.exitoso = exitoso;
            this.mensaje = mensaje;
            this.numeroAutorizacion = numeroAutorizacion;
            this.numeroTransaccion = numeroTransaccion;
        }
        
        public boolean isExitoso() {
            return exitoso;
        }
        
        public String getMensaje() {
            return mensaje;
        }
        
        public String getNumeroAutorizacion() {
            return numeroAutorizacion;
        }
        
        public String getNumeroTransaccion() {
            return numeroTransaccion;
        }
    }
}
