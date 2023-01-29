package PetStoreTests;

import Entities.PetStore.User;
import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static io.restassured.config.LogConfig.logConfig;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class) //Setar a ordem dos testes
public class UserTests {

    private static User user;
    public static Faker faker;
    public static RequestSpecification request;

    @BeforeAll //A notação do método BeforeAll executada antes de tudo
    public static void setup(){
        RestAssured.baseURI = "https://petstore.swagger.io/v2";

        faker = new Faker();
        user = new User(faker.name().username(),
                faker.name().firstName(),
                faker.name().lastName(),
                faker.internet().safeEmailAddress(),
                faker.internet().password(8,10),
                faker.phoneNumber().toString());
    }

    @BeforeEach
    void setRequest(){
        request = given().config(RestAssured.config().logConfig(logConfig().enableLoggingOfRequestAndResponseIfValidationFails())) //Mostra um log de detalhes em caso de erros
                .header("api-key","special-key")
                .contentType(ContentType.JSON);
    }

    //Primeiro teste - POST
    @Test
    @Order(1)
    public void CreateNewUser_WithValidData_ReturnOk(){ //Sempre colocar bem especificado o que o teste irá fazer e retorna
        request.body(user)
                .when()
                .post("/user")
                .then()
                .assertThat().statusCode(200).and()
                .body("code", equalTo(200))
                .body("type", equalTo("unknown"))
                .body("message", isA(String.class))
                .body("size()", equalTo(3));
    }

    //Primeiro teste - GET
    @Test
    @Order(2)
    public void GetLogin_validUser_ReturnOk(){
        request
                .param("username", user.getUsername())
                .param("password", user.getPassword())
                .when()
                .get("/user/login")
                .then()
                .assertThat()
                .statusCode(200)
                .and()
                .time(lessThan(2000L))
                .and()
                .body(matchesJsonSchemaInClasspath("loginResponseSchema.json")); //Passando o caminho da schema, setado em um arquivo de configuração json
    }

    //teste - GET por username de usuário
    @Test
    @Order(3)
    public void GetUserByUsername_userIsValid_ReturnOk(){
        request
                .when()
                .get("/user/" + user.getUsername())
                .then()
                .assertThat()
                .statusCode(200)
                .and()
                .time(lessThan(2000L))
                .and()
                .body("firstName", equalTo(user.getFirstName()));
    }

    @Test
    @Order(4)
    public void DeleteUser_UserExists_ReturnOk(){
        request
                .when()
                .delete("/user/" + user.getUsername())
                .then()
                .assertThat()
                .statusCode(200)
                .and()
                .time(lessThan(2000L))
                .log();
    }

    @Test
    public void CreateNewUser_WithInvalidBody_ReturnBadRequest(){
        Response response = request
                .body("teste")
                .when()
                .post("/user")
                .then()
                .extract().response();
        Assertions.assertNotNull(response); //primeira validação
        Assertions.assertEquals(400, response.statusCode()); //segunda validação
        Assertions.assertTrue(response.getBody().asPrettyString().contains("unknown"));
        Assertions.assertEquals(3, response.body().jsonPath().getMap("$").size());
    }
}