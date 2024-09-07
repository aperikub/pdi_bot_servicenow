package fr.aperikub.botSnow;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Scanner;

public class PropertiesEncrypterMain {
    static EncryptedProperties props;
    static PasswordEncryption encryption;
    public static void main(String[] args) throws UnsupportedLookAndFeelException, InterruptedException {
        initializeProperties();
        FileHandler fileHandler = new FileHandler();

        try {
            encryption = new PasswordEncryption(FileHandler.encryptionKey, FileHandler.encryptionSalt, FileHandler.encryptionIterations, FileHandler.encryptionKeyLength);
            System.out.println("Encrypting Passwords... Press 'q' to quit.");
            fileHandler.encryptData(props, encryption, false);
        } catch (GeneralSecurityException | RuntimeException e) {
            System.out.println("Error while encrypting data :" + e.getMessage());
            throw new RuntimeException(e);
        }
        Scanner scanner = new Scanner(System.in);
        char input = ' ';


        // Maintenir la console ouverte jusqu'à ce que l'utilisateur appuie sur 'q'
        while (input != 'q') {
            System.out.print("Enter 'q' to quit): ");
            input = scanner.next().charAt(0); // Lire le premier caractère de l'entrée utilisateur
        }

        System.out.println("Exiting... Bye!");
        scanner.close();

    }

    private static void initializeProperties() {
        props = new EncryptedProperties(encryption);
        try (InputStream is = new FileInputStream("config/login.properties")) {
            props.load(is);
        } catch (IOException e) {
            Log.getInstance().addErrorLog("Error while loading properties: " + e.getMessage());
        }
    }


}
