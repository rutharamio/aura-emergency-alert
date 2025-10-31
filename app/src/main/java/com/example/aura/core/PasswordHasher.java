package com.example.aura.core;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Clase de utilidad para hashear y verificar contraseñas de forma segura.
 * Usa el algoritmo PBKDF2 con un "salt" aleatorio.
 */
public class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA1";
    private static final int ITERATION_COUNT = 65536;
    private static final int KEY_LENGTH = 128;
    private static final int SALT_SIZE = 16; // en bytes

    /**
     * Hashea una contraseña dada y devuelve el hash como una cadena.
     * El formato de salida es: salt_en_hex:hash_en_hex
     */
    public static String hashPassword(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_SIZE];
        random.nextBytes(salt);

        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
        byte[] hash = factory.generateSecret(spec).getEncoded();

        return toHexString(salt) + ":" + toHexString(hash);
    }

    /**
     * Verifica si una contraseña en texto plano coincide con un hash almacenado.
     * @param plainPassword La contraseña que el usuario ingresó.
     * @param storedPasswordHash El hash que está guardado en la base de datos (formato: salt:hash).
     * @return true si la contraseña es correcta, false en caso contrario.
     */
    public static boolean verifyPassword(String plainPassword, String storedPasswordHash) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String[] parts = storedPasswordHash.split(":");
        if (parts.length != 2) {
            // El formato del hash es incorrecto
            return false;
        }

        byte[] salt = fromHexString(parts[0]);
        byte[] originalHash = fromHexString(parts[1]);

        KeySpec spec = new PBEKeySpec(plainPassword.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
        byte[] testHash = factory.generateSecret(spec).getEncoded();

        return Arrays.equals(originalHash, testHash);
    }

    // --- Métodos de utilidad para convertir entre byte[] y String hexadecimal ---

    private static String toHexString(byte[] array) {
        StringBuilder sb = new StringBuilder();
        for (byte b : array) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static byte[] fromHexString(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }
}
