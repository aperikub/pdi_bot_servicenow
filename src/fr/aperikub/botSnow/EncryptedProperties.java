package fr.aperikub.botSnow;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Properties;



public class EncryptedProperties extends Properties {
    PasswordEncryption passwordEncryption;
    public EncryptedProperties(PasswordEncryption _passwordEncryption) {
        super();
        passwordEncryption = _passwordEncryption;
    }

    @Override
    public synchronized Object setProperty(String key, String value) {
        return super.setProperty(key, value);
    }

    @Override
    public String getProperty(String key) {
        String encryptedPropertyKey = key + "_encrypted";

        // Vérifier si la propriété est chiffrée
        if ("true".equals(super.getProperty(encryptedPropertyKey))) {
            try {
                // Si elle est chiffrée, la déchiffrer avant de la renvoyer
                return passwordEncryption.decrypt(super.getProperty(key));
            } catch (GeneralSecurityException | IOException e) {
                Log.getInstance().addErrorLog("Decryption of " + key + " failed: " + e.getMessage());
            }
        }

        // Si la propriété n'est pas chiffrée, renvoyer la valeur telle quelle
        return super.getProperty(key);
    }
    public String getPropertyNoDecrypt(String key) {
        return super.getProperty(key);
    }
}
