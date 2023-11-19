package APITest;
import com.github.javafaker.Faker;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.http.Cookies;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
public class Campus {

    Faker faker= new Faker();
    RequestSpecification reqSpec;

    String BankAccountName;

    String BankAccountIban;

    String BankAccountID;

    String schoolID = "646cbb07acf2ee0d37c6d984";
@BeforeClass
    public  void login(){
    baseURI = "https://test.mersys.io/";
    Map<String, String> loginInfo = new HashMap<>();
    loginInfo.put("username","****");
    loginInfo.put("password", "****");
    loginInfo.put("rememberMe", "true");
    Cookies cookies =
            given()
                    .body(loginInfo)
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/auth/login")
                    .then()
                    .statusCode(200)
                    .extract().response().detailedCookies();
    reqSpec = new RequestSpecBuilder()
            .addCookies(cookies)
            .setContentType(ContentType.JSON)
            .build();
    }

    @Test
    public void Add(){
        String name=faker.name().firstName();
        String iban=faker.finance().iban();
        String currency="EUR";

        Map<String, Object> data=new HashMap<>();
        data.put("name",name);
        data.put("iban",iban);
        data.put("currency",currency);
        data.put("schoolId", schoolID);

        Response path=
                given()
                        .spec(reqSpec)
                        .body(data)

                        .when()
                        .post("/school-service/api/bank-accounts")

                        .then()
                        .log().body()
                        .statusCode(201)
                        .extract().response();
        BankAccountID=path.path("id");
        BankAccountName=path.path("name");
        BankAccountIban=path.path("iban");
        ;
    }

    @Test(dependsOnMethods = "Add")
    public void AddNegative(){
        Map<String,String> data=new HashMap<>();
        data.put("name",BankAccountName);
        data.put("iban",BankAccountIban);
        data.put("schoolId", schoolID);
        data.put("currency","EUR");


        given()
                .spec(reqSpec)
                .body(data)

                .when()
                .post("/school-service/api/bank-accounts")

                .then()
                .statusCode(400)
                .body("message",containsString("already"))
        ;
    }
    @Test(dependsOnMethods = "AddNegative")
    public void update(){
        String iban=faker.finance().iban();
        String name=faker.name().firstName();

        Map<String,String> data=new HashMap<>();
        data.put("name",name);
        data.put("id",BankAccountID);
        data.put("iban",iban);
        data.put("schoolId", schoolID);
        data.put("currency","EUR");

        given()
                .spec(reqSpec)
                .body(data)

                .when()
                .put("/school-service/api/bank-accounts")

                .then()
                .log().body()
                .statusCode(200)
                .body("iban",equalTo(iban))
        ;
    }
    @Test(dependsOnMethods = "update")
    public void delete(){
        given()
                .spec(reqSpec)
                .when()
                .delete("/school-service/api/bank-accounts/"+BankAccountID)

                .then()
                //.log().body()
                .statusCode(500) //jenkins icin hataya donusturuldu 200 yerine 500 donsun dedik

        ;
    }
    @Test(dependsOnMethods = "delete")
    public void deleteNegative(){
        given()
                .spec(reqSpec)
                .when()
                .delete("/school-service/api/bank-accounts/"+BankAccountID)

                .then()
                .log().body()
                .statusCode(200)
        ;
    }




}


