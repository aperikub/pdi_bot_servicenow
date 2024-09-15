package fr.aperikub.botSnow;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.Properties;
import java.util.Set;

public class Main {
    static EncryptedProperties props;
    static FileHandler fileHandler;
    static PasswordEncryption passwordEncryption;
    public static void main(String[] args) throws InterruptedException {
        initializeProperties();

        try {
            passwordEncryption = new PasswordEncryption(FileHandler.encryptionKey,FileHandler.encryptionSalt,
                    FileHandler.encryptionIterations, FileHandler.encryptionKeyLength);
        } catch (GeneralSecurityException e) {
            Log.getInstance().addErrorLog("Error while encrypting data :" + e.getMessage());
            throw new RuntimeException(e);
        }
        ChromeOptions options = configureChromeOptions();
        WebDriver driver = new ChromeDriver(options);
        fileHandler = new FileHandler();
//        driver.get("https://google.com/");
        fileHandler.encryptData(props,passwordEncryption,true);
//        testLog(driver);
        if (logIntoDev(driver)) {
            if(props.getProperty("generate_log_files").equals("true")){
                fileHandler.saveDataInFile();
            }
            logIntoInstance(driver);
        }
        Thread.sleep(5000);
        driver.quit();
    }

    private static void testLog(WebDriver driver) {
        driver.get("https://www.google.com");
        WebElement truc = waitForElement(driver, By.id("JeSuisPasLa"), 300);

    }
    private static void initializeProperties() {
        props = new EncryptedProperties(passwordEncryption);
        try (InputStream is = new FileInputStream("config/login.properties")) {
            props.load(is);
        } catch (IOException e) {
            Log.getInstance().addErrorLog("Error while loading properties: " + e.getMessage());
        }
    }

    private static void openNewTab(WebDriver driver) {
        // Obtenir l'onglet actuel
        String originalTab = driver.getWindowHandle();
        // Ouvrir un nouvel onglet
        ((JavascriptExecutor) driver).executeScript("window.open();");
        // Obtenir toutes les fenêtres ouvertes
        Set<String> allTabs = driver.getWindowHandles();
        // Passer au nouvel onglet (le dernier ouvert)
        for (String tab : allTabs) {
            if (!tab.equals(originalTab)) {
                driver.switchTo().window(tab);
                break;
            }
        }
    }

    private static ChromeOptions configureChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        // Configure Chrome options as needed
        options.addArguments("--disable-search-engine-choice-screen");
        // Uncomment and adjust the following lines if needed
        // options.addArguments("disable-default-apps");
        // options.addArguments("disable-popup-blocking");

        if(props.getProperty("no_interface").equals("true")){
            options.addArguments("--headless");
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
        }
        if(props.getProperty("use_profile").equals("true")){
            options.addArguments("no-default-browser-check");
            options.addArguments("no-first-run");
            options.addArguments("user-data-dir="+System.getProperty("chrome_user_dir"));
            options.addArguments("profile-directory="+System.getProperty("chrome_profile_folder"));
        }
        return options;
    }
    private static boolean isInstanceHibernating(WebDriver driver) {

        try {
            // Vérifier si la page indique que l'instance est en hibernation
            WebElement hibernateMessage = waitForElement(driver, By.xpath("//*[contains(text(), 'Your instance is hibernating')]"), 10);
            if (hibernateMessage != null) {

                Log.getInstance().addInfoLog("Instance is hibernating.");
                return true;
            }
        } catch (TimeoutException e) {
            Log.getInstance().addInfoLog("Instance is not hibernating.");
        }
        return false;
    }


    private static Boolean logIntoInstance(WebDriver driver) throws InterruptedException {
        var url = props.getProperty("instance_url") + "/login.do";
        openNewTab(driver);
        Thread.sleep(5000);
        driver.get(url);
        if(isInstanceHibernating(driver)){
            ((JavascriptExecutor) driver).executeScript("alert('instance is hibernating, don't close the window, the bot will login when the instance will wake up');");
        }



        WebElement instanceUsernameField;
        try {
            instanceUsernameField = waitForElement(driver, By.id("user_name"),1500);
        } catch (org.openqa.selenium.TimeoutException e) {
            ((JavascriptExecutor) driver).executeScript("alert('TimeOut : "+e.getMessage()+"');");
            Log.getInstance().addErrorLog(e.getMessage());
            throw new RuntimeException(e);
        }

        WebElement instancePasswordField = driver.findElement(By.id("user_password"));
        WebElement instanceLoginButton = driver.findElement(By.id("sysverb_login"));

        instanceUsernameField.sendKeys(props.getProperty("instance_login"));
        instancePasswordField.sendKeys(props.getProperty("instance_password"));
        instanceLoginButton.click();

        return true;
    }

    private static Boolean logIntoDev(WebDriver driver) throws InterruptedException {
        driver.get("https://signon.service-now.com/x_snc_sso_auth.do?pageId=username");


        WebElement emailField = waitForElement(driver, By.id("email"), 20);
        Thread.sleep(2000);

        fillOrCheckUserId(driver, emailField);

        WebElement nextButton = waitForElement(driver, By.id("username_submit_button"), 20);
        nextButton.click();

        WebElement passwordField = waitForElement(driver, By.id("password"), 10);
        passwordField.sendKeys(props.getProperty("developer_password"));

        WebElement loginButton = driver.findElement(By.id("password_submit_button"));
        loginButton.click();

        Log.getInstance().addInfoLog("Logging into developer");

        handleTwoFactorAuthentication(driver);

        return verifyLoginSuccess(driver, "https://signon.service-now.com/sso");
    }

    private static void fillOrCheckUserId(WebDriver driver, WebElement emailField) {
        String userId = emailField.getText();
        if (!userId.equals(props.getProperty("developer_login"))) {
            emailField.sendKeys(props.getProperty("developer_login"));
        }
    }

    private static void handleTwoFactorAuthentication(WebDriver driver) {
        WebElement authChangeLink = waitForElement(driver, By.id("factor_verify_form_change_mfa_link"), 20);

        boolean isMfaEmailMode = isElementPresent(driver, "factor_verify_resend_factor_span");
        if (isMfaEmailMode) {
            authChangeLink.click();
            WebElement totpButton = waitForElement(driver, By.id("factor_challenge_form_token_software_totp_factor_button"), 20);
            totpButton.click();
        } else {
            Log.getInstance().addInfoLog("Authentication already in TOTP mode.");
        }

        WebElement authCodeField = waitForElement(driver, By.id("passCode"), 20);
        String totpCode = String.format("%06d", new GoogleAuthProvider(props.getProperty("totp_key")).getCode());
        authCodeField.sendKeys(totpCode);

        WebElement authSubmitButton = driver.findElement(By.id("factor_verify_form_submit_button"));
        authSubmitButton.click();
    }

    private static Boolean verifyLoginSuccess(WebDriver driver, String expectedUrl) {
        WebDriverWait waitPass = new WebDriverWait(driver, Duration.ofSeconds(10));
        boolean loginSuccessful = waitPass.until(ExpectedConditions.urlToBe(expectedUrl));

        if (loginSuccessful) {
            Log.getInstance().addInfoLog("Login developer success");
            return true;
        } else {
            Log.getInstance().addErrorLog("Login developer failed: trying again");
            return false;
        }
    }

    private static boolean isElementPresent(WebDriver driver, String elementId) {
        try {
            driver.findElement(By.id(elementId));
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    private static WebElement waitForElement(WebDriver driver, By locator, int timeoutSeconds) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
}
