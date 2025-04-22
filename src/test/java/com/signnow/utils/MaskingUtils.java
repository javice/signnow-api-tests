package com.signnow.utils;

/**
 * Utilidad para enmascarar información sensible en reportes y logs
 */
public class MaskingUtils {

	/**
	 * Enmascara un valor sensible mostrando solo los primeros caracteres
	 * @param value El valor a enmascarar
	 * @param visibleChars Número de caracteres visibles al inicio
	 * @param maskChar Carácter a usar para enmascarar
	 * @return Valor enmascarado (ej: "abc***")
	 */
	public static String maskSensitiveValue(String value, int visibleChars, char maskChar) {
		if (value == null || value.isEmpty()) {
			return "";
		}

		// Si el valor es más corto que los caracteres visibles, mostramos al menos un carácter
		int charsToShow = Math.min(visibleChars, Math.max(1, value.length() - 1));

		StringBuilder masked = new StringBuilder();
		masked.append(value.substring(0, charsToShow));

		// Añade los caracteres de enmascaramiento
		for (int i = 0; i < 3; i++) {
			masked.append(maskChar);
		}

		return masked.toString();
	}

	/**
	 * Enmascara un valor sensible usando configuración predeterminada (3 primeros caracteres, *)
	 * @param value El valor a enmascarar
	 * @return Valor enmascarado (ej: "abc***")
	 */
	public static String maskSensitiveValue(String value) {
		return maskSensitiveValue(value, 3, '*');
	}
}
