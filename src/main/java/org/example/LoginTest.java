package org.example;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.qameta.allure.Allure;
import io.restassured.response.Response;
import org.example.recorder.MyScreenRecorder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import io.restassured.RestAssured;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class LoginTest {
    WebDriver driver;

    @BeforeClass
    public void setUp() throws Exception {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");

        driver = new ChromeDriver(options);
        MyScreenRecorder.startRecording("record");

    }

    @DataProvider(name = "loginData")
    public Object[][] loginData() {
        return new Object[][]{
                // Các trường hợp kiểm thử
                {"maimailinh123", "Mailinh2196!", "Your username is invalid!"},
                {"tomsmith", "Mailinh219634", "Your password is invalid!"},
                {"maimailinh13", "SuperSecretPassword", "Your username is invalid!"},
                {"", "Mailinh2196", "Your username is invalid!"}, // Tên người dùng rỗng
                {"tomsmith", "", "Your password is invalid!"}, // Mật khẩu rỗng
                {"", "", "Your username is invalid!"}, // Cả tên người dùng và mật khẩu rỗng
                {"tomsmith", "SuperSecretPassword!", "You logged into a secure area!"}, // Trường hợp đúng
        };
    }

    @Test(dataProvider = "loginData")
    public void testLogin(String username, String password, String expectedMessage) {
        driver.get("http://the-internet.herokuapp.com/login");

        WebElement usernameField = driver.findElement(By.id("username"));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));


        // Nhập username và password
        usernameField.clear(); // Xóa trường nhập liệu trước khi nhập
        passwordField.clear();
        usernameField.sendKeys(username);
        passwordField.sendKeys(password);
        loginButton.click();// click button để thực thi
        sleep(3000);

        // Xác minh thông báo lỗi hoặc thành công
        WebElement flashMessage = driver.findElement(By.cssSelector(".flash"));
        String actualMessage = flashMessage.getText();

        Assert.assertTrue(actualMessage.contains(expectedMessage), "Expected message not found.");
        Allure.step("Test passed for username: " + username + " and password: " + password);

        // Kiểm tra API nếu tên người dùng và mật khẩu là chính xác
        if (username.equals("tomsmith") && password.equals("SuperSecretPassword!")) {
            testApiLogin();
        }
    }

    public void testApiLogin() {
        // Thực hiện yêu cầu API để kiểm tra đăng nhập
        Response response = RestAssured.given()
                .baseUri("https://api.example.com")
                .basePath("/login")
                .header("Content-Type", "application/json")
                .body("{ \"username\": \"tomsmith\", \"password\": \"SuperSecretPassword!\" }")
                .post();

        // Xác minh mã trạng thái và thông báo từ API
        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200");
        String responseBody = response.getBody().asString();
        Assert.assertTrue(responseBody.contains("success"), "API response did not contain expected success message");
        Allure.step("API Test passed.");
    }

    @AfterClass
    public void tearDown() throws Exception {
        MyScreenRecorder.stopRecording();
        if (driver != null) {
            driver.quit();
        }
    }

    private void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}