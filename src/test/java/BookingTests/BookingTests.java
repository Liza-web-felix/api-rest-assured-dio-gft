package BookingTests;

import Entities.Booking.Booking;
import Entities.Booking.BookingDates;
import Entities.Booking.BookingUser;
import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.ErrorLoggingFilter;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.*;

public class BookingTests {

    public static Faker faker;
    private static RequestSpecification request;
    private static Booking booking;
    private static BookingDates bookingDates;
    private static BookingUser user;

    @BeforeAll
    public static void Setup(){ //O método BeforeAll acontece antes de tudo
        //Essa requisição instancia as classes
        RestAssured.baseURI = "https://restful-booker.herokuapp.com";
        //Criando dados fakes para a massa de testes
        faker = new Faker();
        user = new BookingUser(faker.name().username(),
                faker.name().firstName(),
                faker.name().lastName(),
                faker.internet().safeEmailAddress(),
                faker.internet().password(8,10),
                faker.phoneNumber().toString());
        //Esse parametro retorna java fake dates correto
        bookingDates = new BookingDates("2022-12-30","2023-01-29");
        //Essas requisições instânciam o booking
        booking = new Booking(user.getFirstname(), user.getLastname(),
                (float) faker.number().randomDouble(2,50,100000),
                true,bookingDates,"");

        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter(), new ErrorLoggingFilter());
    }

    @BeforeEach
    void setRequest(){ //O método setRequest(), cria uma requisição
        request = given().config(RestAssuredConfig.config().logConfig(LogConfig.logConfig().enableLoggingOfRequestAndResponseIfValidationFails()))
                .contentType(ContentType.JSON)
                .auth().basic("admin","password123");
    }

    @Test
    public void getAllBookingsById_returnOk(){
        Response response = request
                .when()
                .get("/booking")
                .then()
                .extract().response();

        Assertions.assertNotNull(response);
        Assertions.assertEquals(200,response.statusCode());
    }

    @Test //Notação de teste e método getAllBookingsByUserFirstName desse parâmetro retorna o status code do "firstName" do usuário
    public void getAllBookingsByUserFirstName_BookingExists_returnOk(){
        request
                .when()
                .queryParam("firstName","Liz")
                .get("/booking")
                .then()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .and()
                .body("results", hasSize(greaterThan(0)));
    }

    @Test //Notação de teste e método CreateBooking, retorna o status code da criação do Booking
    public void CreateBooking_WithValidData_returnOk(){
        @SuppressWarnings("unused")
		Booking test = booking;
        given().config(RestAssuredConfig.config().logConfig(LogConfig.logConfig().enableLoggingOfRequestAndResponseIfValidationFails()))
                .contentType(ContentType.JSON)
                .when()
                .body(booking)
                .post("/booking")
                .then()
                .body(matchesJsonSchemaInClasspath("createBookingRequestSchema.json"))
                .and()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON).and().time(lessThan(3000L));
    }
}
