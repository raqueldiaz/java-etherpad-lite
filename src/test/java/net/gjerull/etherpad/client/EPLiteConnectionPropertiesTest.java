package net.gjerull.etherpad.client;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.runner.RunWith;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;

/**
 * The Class EPLiteConnectionPropertiesTest.
 */
@RunWith(JUnitQuickcheck.class)
public class EPLiteConnectionPropertiesTest {

    /** The Constant API_VERSION. */
    private static final String API_VERSION = "1.2.12";

    /** The Constant ENCODING. */
    private static final String ENCODING = "UTF-8";

    /** The Constant RESPONSE_TEMPLATE. */
    private static final String RESPONSE_TEMPLATE = "{\n" + "  \"code\": %d,\n"
            + "  \"message\": \"%s\",\n" + "  \"data\": %s\n" + "}";

    /**
     * Domain with trailing slash when construction an api path.
     *
     * @param exampleMethod the example method
     * @throws Exception the exception
     */
    @Property
    public void domain_without_trailing_slash_when_construction_an_api_path(
            String exampleMethod) throws Exception {
        EPLiteConnection connection = new EPLiteConnection("http://example.com",
                "apikey", API_VERSION, ENCODING);
        String apiMethodPath = connection.apiPath(exampleMethod);
        assertEquals("/api/1.2.12/" + exampleMethod, apiMethodPath);
    }

    /**
     * Query string from map.
     *
     * @param padID the pad ID
     * @throws Exception the exception
     */
    @Property
    public void query_string_from_map(String padID) throws Exception {
        EPLiteConnection connection = new EPLiteConnection(
                "http://example.com/", "apikey", API_VERSION, ENCODING);
        Map<String, Object> apiArgs = new TreeMap<>(); // Ensure ordering for
                                                       // testing
        apiArgs.put("padID", padID);
        apiArgs.put("rev", 27);

        String queryString = connection.queryString(apiArgs, false);

        assertEquals("apikey=apikey&padID=" + padID + "&rev=27", queryString);
    }

    /**
     * Handle valid response from server.
     *
     * @param test1 the test 1
     * @param test2 the test 2
     * @param test3 the test 3
     * @throws Exception the exception
     */
    @Property
    public void handle_valid_response_from_server(String test1, String test2,
            String test3) throws Exception {
        EPLiteConnection connection = new EPLiteConnection(
                "http://example.com/", "apikey", API_VERSION, ENCODING);
        String listAllPads = "{\"padIDs\": [\n" + "  \"" + test1 + "\",\n"
                + "  \"" + test2 + "\",\n" + "  \"" + test3 + "\"\n" + "]}";
        String serverResponse = String.format(RESPONSE_TEMPLATE, 0,
                "no or wrong API Key", listAllPads);

        Map response = (Map) connection.handleResponse(serverResponse);

        assertEquals(test1, ((List) response.get("padIDs")).get(0));
    }
}
