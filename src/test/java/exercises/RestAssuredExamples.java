package exercises;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.tngtech.java.junit.dataprovider.*;
import dataentities.Address;
import io.restassured.builder.*;
import io.restassured.http.*;
import io.restassured.specification.*;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@RunWith(DataProviderRunner.class)
public class RestAssuredExamples {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(9876));

    private static String myAuthenticationToken;

    /*
    @BeforeClass
    public static void retrieveToken() {

        myAuthenticationToken =

            given().
                auth().
                preemptive().
                basic("username", "password").
            when().
                get("https://my.secure/api").
            then().
                extract().
                path("");
    }
    */

    @Test
    public void usePreviouslyStoredAuthToken() {

        given().
            auth().
            oauth2(myAuthenticationToken).
        when().
            get("https://my.very.secure/api").
        then().
            assertThat().
            statusCode(200);
    }

    @DataProvider
    public static Object[][] zipCodeData() {
        return new Object[][] {
            { "us", "90210", "United States" },
            { "ca", "B2A", "Canada" }
        };
    }

    @Test
    public void validateCountryForZipCode() {

        given().
        when().
            get("http://api.zippopotam.us/us/90210").           // Do a GET call to the specified resource
        then().
            assertThat().                                         // Assert that the value of the element 'country'
            body("country", equalTo("United States"));  // in the response body equals 'United States'
    }

    @Test
    public void checkResponseHeaders() {

        given().
        when().
            get("http://api.zippopotam.us/us/90210").
        then().
            assertThat().
            statusCode(200).
        and().
            contentType(ContentType.JSON);
    }

    @Test
    public void useQueryParameter() {

        given().
            queryParam("text", "testcase").
        when().
            get("http://md5.jsontest.com").
        then().
            assertThat().
            body("md5", equalTo("7489a25fc99976f06fecb807991c61cf"));
    }

    @Test
    public void usePathParameter() {

        given().
            pathParam("countryCode","us").
            pathParam("zipCode", "90210").
        when().
            get("http://api.zippopotam.us/{countryCode}/{zipCode}").
        then().
            assertThat().
            body("country", equalTo("United States"));
    }

    @Test
    @UseDataProvider("zipCodeData")
    public void checkCountryForCountryCodeAndZipCode
        (String countryCode, String zipCode, String expectedCountry) {

        given().
            pathParam("countryCode", countryCode).
            pathParam("zipCode", zipCode).
        when().
            get("http://api.zippopotam.us/{countryCode}/{zipCode}").
        then().
            assertThat().
            body("country",equalTo(expectedCountry));
    }

    @Test
    public void useBasicAuthentication() {

        given().
            auth().
            preemptive().
            basic("username", "password").
        when().
            get("https://my.secure/api").
        then().
            assertThat().
            statusCode(200);
    }

    @Test
    public void useOAuthAuthentication() {

        given().
            auth().
            oauth2("myAuthenticationToken").
        when().
            get("https://my.very.secure/api").
        then().
            assertThat().
            statusCode(200);
    }

    @Test
    public void checkResponseTimeForApiCall() {

        given().
        when().
            get("http://api.zippopotam.us/us/90210").
        then().
            assertThat().
            time(lessThan(100L), TimeUnit.MILLISECONDS);
    }

    private static ResponseSpecification responseSpec;

    @BeforeClass
    public static void createResponseSpec() {

        responseSpec =
            new ResponseSpecBuilder().
                expectStatusCode(200).
                expectContentType(ContentType.JSON).
                build();
    }

    @Test
    public void useResponseSpec() {

        given().
        when().
            get("http://api.zippopotam.us/us/90210").
        then().
            spec(responseSpec).
        and().
            body("country", equalTo("United States"));
    }

    private static RequestSpecification requestSpec;

    @BeforeClass
    public static void createRequestSpec() {

        requestSpec =
            new RequestSpecBuilder().
                setBaseUri("http://api.zippopotam.us").
                build();
    }

    @Test
    public void useRequestSpec() {

        given().
            spec(requestSpec).
        when().
            get("/us/90210").
        then().
            assertThat().
            statusCode(200);
    }

    @Test
    public void serializeAddressToJson() {

        Address myAddress = new Address("My street", 1, 1234, "Amsterdam");

        given().
            body(myAddress).
        when().
            post("http://localhost:9876/address").
        then().
            assertThat().
            statusCode(200);
    }

    @Test
    public void deserializeJsonToAddress() {

        Address myAddress =

            given().
            when().
                get("http://localhost:9876/address").
                as(Address.class);

        Assert.assertEquals("Amsterdam", myAddress.getCity());
    }
}