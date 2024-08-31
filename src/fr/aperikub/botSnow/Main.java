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
import java.time.Duration;
import java.util.Properties;

public class Main {
    static Properties props;

    public static void main(String[] args) throws UnsupportedLookAndFeelException, InterruptedException {
        initializeProperties();

        ChromeOptions options = configureChromeOptions();
        WebDriver driver = new ChromeDriver(options);

        if (logIntoDev(driver)) {
            FileHandler fileHandler = new FileHandler();
            fileHandler.saveDataInFile();
            //TODO: trouver élément en hibernation
            //WebElement openInstanceButton = waitForElement(driver,By.cssSelector(".dps-button.-round.-md.has-action.-external-action.has-button-and-icon"),20);
//            if(openInstanceButton.isEnabled()){
//                //instance is awake
//                Log.getInstance().addInfoLog("instance is awake");
//            }
//            else{
//                Log.getInstance().addInfoLog("instance is hibernating");
//
//            }
            logIntoInstance(driver);

            // TODO: wake up instance
            // WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(300));
            // WebElement openInstanceButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".dps-button.-round.-md.has-action.-external-action.has-button-and-icon")));
            // Log.getInstance().addInfoLog("waiting instance wake up");
            // openInstanceButton.click();
        }
        Thread.sleep(5000);

        driver.quit();
    }

    private static void initializeProperties() {
        props = new Properties();
        try (InputStream is = new FileInputStream("config/login.properties")) {
            props.load(is);
        } catch (IOException e) {
            Log.getInstance().addErrorLog("Error while loading properties: " + e.getMessage());
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
            options.addArguments("user-data-dir="+System.getProperty("user_dir"));
            options.addArguments("profile-directory="+System.getProperty("profile_folder"));
        }
        return options;
    }

    private static Boolean logIntoInstance(WebDriver driver) {
        driver.get(props.getProperty("instance_url") + "/login.do");

        WebElement instanceUsernameField = driver.findElement(By.id("user_name"));
        WebElement instancePasswordField = driver.findElement(By.id("user_password"));
        WebElement instanceLoginButton = driver.findElement(By.id("sysverb_login"));

        instanceUsernameField.sendKeys(props.getProperty("instance_login"));
        instancePasswordField.sendKeys(props.getProperty("instance_password"));
        instanceLoginButton.click();

        return true;
    }

    private static Boolean logIntoDev(WebDriver driver) throws InterruptedException {
        driver.get("https://signon.service-now.com/x_snc_sso_auth.do?pageId=username");

        WebDriverWait waitDev = new WebDriverWait(driver, Duration.ofSeconds(20));
        WebElement emailField = waitDev.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
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
        String totpCode = String.valueOf(new GoogleAuthProvider(props.getProperty("totp_key")).getCode());
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
