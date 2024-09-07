package fr.aperikub.botSnow;

import java.io.*;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import static fr.aperikub.botSnow.Main.props;

public class FileHandler {

    //constantes de réglage du cryptage des mots de passe
    public static final char[] encryptionKey = "rootEncryptKey".toCharArray();
    public static final byte[] encryptionSalt = {-42, 94, -104, -16, -123, 42, 121, 6, -72, 30};
    public static final int encryptionIterations = 4000;
    public static final int encryptionKeyLength = 128;

    public void encryptData(EncryptedProperties props, PasswordEncryption encryption, Boolean useLogger) throws RuntimeException {
        // Création de deux maps pour séparer les informations de connexion et de configuration
        Map<String, String> loginInfo = new LinkedHashMap<>();
        Map<String, String> configInfo = new LinkedHashMap<>();

        // Parcourir les propriétés à la recherche des couples property_key/property_key_encrypted
        Enumeration<?> propertyNames = props.propertyNames();

        while (propertyNames.hasMoreElements()) {
            String propertyKey = (String) propertyNames.nextElement();

            // Si la propriété est une clé normale et qu'elle a une version _encrypted
            if (!propertyKey.endsWith("_encrypted") && props.getPropertyNoDecrypt(propertyKey + "_encrypted") != null) {
                String encryptedPropertyKey = propertyKey + "_encrypted";

                // Si la propriété n'est pas encore chiffrée
                if ("false".equals(props.getPropertyNoDecrypt(encryptedPropertyKey))) {
                    try {
                        // Chiffrer la propriété
                        String encryptedValue = encryption.encrypt(props.getPropertyNoDecrypt(propertyKey));
                        props.setProperty(propertyKey, encryptedValue);
                        props.setProperty(encryptedPropertyKey, "true");

                        if (useLogger) {
                            Log.getInstance().addInfoLog(propertyKey + " has been encrypted successfully.");
                        } else {
                            System.out.println(propertyKey + " has been encrypted successfully.");
                        }
                    } catch (GeneralSecurityException | UnsupportedEncodingException e) {
                        if (useLogger) {
                            Log.getInstance().addErrorLog("Encryption of " + propertyKey + " failed: " + e.getMessage());
                        } else {
                            System.out.println("Encryption of " + propertyKey + " failed: " + e.getMessage());
                        }
                        throw new RuntimeException(e);
                    }
                }

                // Ajouter la propriété et sa version "_encrypted" à la map des login informations
                loginInfo.put(propertyKey, props.getPropertyNoDecrypt(propertyKey));
                loginInfo.put(encryptedPropertyKey, props.getPropertyNoDecrypt(encryptedPropertyKey));
            } else if (!propertyKey.endsWith("_encrypted")) {
                // Ajouter les propriétés de configuration (sans version _encrypted)
                configInfo.put(propertyKey, props.getPropertyNoDecrypt(propertyKey));
            }
        }

        // Sauvegarder les propriétés chiffrées et configurées dans le fichier login.properties
        try (FileOutputStream outputStream = new FileOutputStream("config/login.properties")) {
            // Écrire les informations de connexion
            StringBuilder fileContent = new StringBuilder();

            fileContent.append("#login informations (if you want a property to be encrypted create a new one like : <property name>_encrypted=false)\n\n");

            // Vérifier s'il y a des login informations à écrire
            if (!loginInfo.isEmpty()) {
                for (Map.Entry<String, String> entry : loginInfo.entrySet()) {
                    fileContent.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
                }
            } else {
                fileContent.append("# No login information to encrypt\n");
            }

            fileContent.append("\n# configuration\n\n");

            // Écrire les propriétés de configuration
            for (Map.Entry<String, String> entry : configInfo.entrySet()) {
                fileContent.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
            }

            // Sauvegarder dans le fichier properties
            outputStream.write(fileContent.toString().getBytes());

            if (useLogger) {
                Log.getInstance().addInfoLog("Properties saved to file successfully.");
            } else {
                System.out.println("Properties saved to file successfully.");
            }
        } catch (IOException e) {
            if (useLogger) {
                Log.getInstance().addErrorLog("Failed to save properties to file: " + e.getMessage());
            } else {
                System.out.println("Failed to save properties to file: " + e.getMessage());
            }
            throw new RuntimeException(e);
        }

        // Log général indiquant que le processus de chiffrement est terminé
        if (useLogger) {
            Log.getInstance().addInfoLog("Encryption process completed.");
        } else {
            System.out.println("Encryption process completed.");
        }
    }

    public void encryptDatav2(EncryptedProperties props, PasswordEncryption encryption, Boolean useLogger) throws RuntimeException {
        // Création de deux maps pour séparer les informations de connexion et de configuration
        Map<String, String> loginInfo = new LinkedHashMap<>();
        Map<String, String> configInfo = new LinkedHashMap<>();

        // Parcourir les propriétés à la recherche des couples property_key/property_key_encrypted
        Enumeration<?> propertyNames = props.propertyNames();

        while (propertyNames.hasMoreElements()) {
            String propertyKey = (String) propertyNames.nextElement();

            // Vérifier si la version "_encrypted" existe et qu'elle est "false"
            if (!propertyKey.endsWith("_encrypted") && "false".equals(props.getPropertyNoDecrypt(propertyKey + "_encrypted"))) {
                String encryptedPropertyKey = propertyKey + "_encrypted";



                    try {
                        // Chiffrer la propriété si elle n'est pas encore chiffrée
                        String encryptedValue = encryption.encrypt(props.getPropertyNoDecrypt(propertyKey));
                        props.setProperty(propertyKey, encryptedValue);
                        props.setProperty(encryptedPropertyKey, "true");
                        if (useLogger) {
                            Log.getInstance().addInfoLog(propertyKey + " has been encrypted successfully.");
                        } else {
                            System.out.println(propertyKey + " has been encrypted successfully.");
                        }
                    } catch (GeneralSecurityException | UnsupportedEncodingException e) {
                        if (useLogger) {
                            Log.getInstance().addErrorLog("Encryption of " + propertyKey + " failed: " + e.getMessage());
                        } else {
                            System.out.println("Encryption of " + propertyKey + " failed: " + e.getMessage());
                        }
                        throw new RuntimeException(e);
                    }


                // Ajouter la propriété et sa version encryptée à la map des login informations
                loginInfo.put(propertyKey, props.getPropertyNoDecrypt(propertyKey));
                loginInfo.put(encryptedPropertyKey, props.getPropertyNoDecrypt(encryptedPropertyKey));
            } else {
                // Ajouter les propriétés de configuration (celles sans "_encrypted")
                if (!loginInfo.containsKey(propertyKey) && !propertyKey.endsWith("_encrypted")) {
                    configInfo.put(propertyKey, props.getPropertyNoDecrypt(propertyKey));
                }
            }
        }

        // Sauvegarder les propriétés chiffrées et configurées dans le fichier login.properties
        try (FileOutputStream outputStream = new FileOutputStream("config/login.properties")) {
            // Écrire les informations de connexion
            StringBuilder fileContent = new StringBuilder();

            fileContent.append("#login informations (if you want a property to be encrypted create a new one like : <property name>_encrypted=false)\n\n");

            // Écrire les propriétés de connexion (login informations)
            for (Map.Entry<String, String> entry : loginInfo.entrySet()) {
                fileContent.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
            }

            fileContent.append("\n# configuration\n\n");

            // Écrire les propriétés de configuration
            for (Map.Entry<String, String> entry : configInfo.entrySet()) {
                fileContent.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
            }

            // Sauvegarder dans le fichier properties
            outputStream.write(fileContent.toString().getBytes());

            if (useLogger) {
                Log.getInstance().addInfoLog("Properties saved to file successfully.");
            } else {
                System.out.println("Properties saved to file successfully.");
            }
        } catch (IOException e) {
            if (useLogger) {
                Log.getInstance().addErrorLog("Failed to save properties to file: " + e.getMessage());
            } else {
                System.out.println("Failed to save properties to file: " + e.getMessage());
            }
            throw new RuntimeException(e);
        }

        // Log général indiquant que le processus de chiffrement est terminé
        if (useLogger) {
            Log.getInstance().addInfoLog("Encryption process completed.");
        } else {
            System.out.println("Encryption process completed.");
        }
    }
    public void encryptDatav1(Properties props, PasswordEncryption encryption, Boolean useLogger) throws RuntimeException {
        // Parcourir les propriétés à la recherche des couples property_key/property_key_encrypted
        Enumeration<?> propertyNames = props.propertyNames();

        while (propertyNames.hasMoreElements()) {
            String propertyKey = (String) propertyNames.nextElement();

            // Vérifier si la propriété actuelle correspond à une clé sans "_encrypted"
            if (!propertyKey.endsWith("_encrypted")) {
                String encryptedPropertyKey = propertyKey + "_encrypted";

                // Vérifier si la version "_encrypted" existe et qu'elle est "false"
                if ("false".equals(props.getProperty(encryptedPropertyKey))) {
                    try {
                        // Chiffrer la propriété si elle n'est pas encore chiffrée
                        String encryptedValue = encryption.encrypt(props.getProperty(propertyKey));
                        props.setProperty(propertyKey, encryptedValue);
                        props.setProperty(encryptedPropertyKey, "true");
                        if (useLogger) {
                            Log.getInstance().addInfoLog(propertyKey + " has been encrypted successfully.");
                        }else{
                            System.out.println(propertyKey + " has been encrypted successfully.");
                        }

                    } catch (GeneralSecurityException | UnsupportedEncodingException e) {
                        if (useLogger) {
                            Log.getInstance().addErrorLog("Encryption of " + propertyKey + " failed: " + e.getMessage());
                        }else{
                            System.out.println("Encryption of " + propertyKey + " failed:" + e.getMessage());
                        }
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        // Sauvegarder les propriétés chiffrées dans le fichier login.properties
        try (FileOutputStream outputStream = new FileOutputStream("config/login.properties")) {
            props.store(outputStream, null);
            if (useLogger) {
                Log.getInstance().addInfoLog("Properties saved to file successfully.");
            } else {
                System.out.println("Properties saved to file successfully.");
            }
        } catch (IOException e) {
            if (useLogger) {
                Log.getInstance().addErrorLog("Failed to save properties to file: " + e.getMessage());
            } else {
                System.out.println("Failed to save properties to file: " + e.getMessage());
            }
            throw new RuntimeException(e);
        }

        // Log général indiquant que le processus de chiffrement est terminé
        if (useLogger) {
            Log.getInstance().addErrorLog("Encryption process completed.");
        }else{
            System.out.println("Encryption process completed.");
        }



    }

    public void saveDataInFile() {
        String filename = "log file" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyyyyHHmm")) + ".txt";
        File file = new File(filename);
        try {
            if (!file.exists()) {

                file.createNewFile();

            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));


            Log.getInstance().getMessages(Log.INFO_MESSAGE_TYPE).forEach((msg) -> {
                try {
                    writer.write(msg + "\n");

                } catch (IOException e) {
                    Log.getInstance().addErrorLog("Error while saving info logs");

                }
            });
            Log.getInstance().clearMessages(Log.INFO_MESSAGE_TYPE);

            Log.getInstance().getMessages(Log.WARNING_MESSAGE_TYPE).forEach((msg) -> {
                try {
                    writer.write(msg + "\n");
                } catch (IOException e) {
                    Log.getInstance().addErrorLog("Error while saving warn logs");

                }
            });

            Log.getInstance().clearMessages(Log.WARNING_MESSAGE_TYPE);

            Log.getInstance().getMessages(Log.ERROR_MESSAGE_TYPE).forEach((msg) -> {
                try {
                    writer.write(msg + "\n");
                } catch (IOException e) {
                    Log.getInstance().addErrorLog("Error while saving error logs");

                }
            });

            Log.getInstance().clearMessages(Log.ERROR_MESSAGE_TYPE);

            writer.flush();
        } catch (IOException e) {
            Log.getInstance().addErrorLog("Error while saving logs : " + e.getMessage());
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        System.out.println("finalize called");
        if(props.getProperty("generate_log_files").equals("true")){
            saveDataInFile();
        }


    }
}
