/*
 * Nom de classe : PasswordEncryption
 *
 * Description   : gère le cryptage des mots de passes dans le fichier des comptes
 *                 (informations trouvées ici :https://docs.oracle.com/javase/10/security/java-cryptography-architecture-jca-reference-guide.htm
 *                 et ici : https://www.codejava.net/coding/file-encryption-and-decryption-simple-example)
 *
 * Auteurs       : Steven Besnard, Agnes Laurencon, Olivier Baylac, Benjamin Launay
 *
 * Version       : 1.0
 *
 * Date          : 09/01/2022
 *
 * Copyright     : CC-BY-SA
 */

package fr.aperikub.botSnow;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.util.Base64;

public class PasswordEncryption {


    private final SecretKeySpec keySpec;

    public PasswordEncryption(char[] _password, byte[] _salt, int _iterations, int _keyLength) throws GeneralSecurityException {

        keySpec = createSecretKey(_password,_salt,_iterations,_keyLength);


    }

    /* crée la clé de décryptage */

    public SecretKeySpec createSecretKey(char[] password, byte[] salt, int iterationCount, int keyLength) throws GeneralSecurityException {
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        PBEKeySpec keySpec = new PBEKeySpec(password, salt, iterationCount, keyLength);
        SecretKey secretKey = keyFactory.generateSecret(keySpec);
        return new SecretKeySpec(secretKey.getEncoded(), "AES");
    }

    /*
        renvoie le String passé en paramètre sous forme cryptée
     */

    public String encrypt(String uncryptedPassword) throws GeneralSecurityException, UnsupportedEncodingException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        AlgorithmParameters parameters = cipher.getParameters();
        IvParameterSpec parameterSpec = parameters.getParameterSpec(IvParameterSpec.class);
        byte[] textBytes = cipher.doFinal(uncryptedPassword.getBytes(StandardCharsets.UTF_8));
        byte[] iv = parameterSpec.getIV();
        return EncodeBytes(iv) + ":" + EncodeBytes(textBytes);
    }

    /*
        renvoie le String passé en paramètre sous forme décryptée
     */

    public String decrypt(String encryptedPassword) throws GeneralSecurityException, IOException {
        String iv = encryptedPassword.split(":")[0];
        String property = encryptedPassword.split(":")[1];
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(DecodeBytes(iv)));
        return new String(cipher.doFinal(DecodeBytes(property)), StandardCharsets.UTF_8);
    }

    /*
        renvoie le tableau d'octets passé en paramètre encodé en Base64
     */

    private String EncodeBytes(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    /*
        décode et renvoie le String passé en paramètre
     */

    private byte[] DecodeBytes(String base64EncodedString) {
        return Base64.getDecoder().decode(base64EncodedString);
    }
}

