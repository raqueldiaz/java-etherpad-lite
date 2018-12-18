package net.gjerull.etherpad.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

/**
 * A class for easily executing an HTTP POST request.<br />
 * <br />
 * Example:<br />
 * <br />
 * <code>
 * Request req = new POSTRequest(url_object);<br />
 * String resp = req.send();<br />
 * </code>
 */
public class POSTRequest implements Request {

    /** The url. */
    private final URL url;

    /** The body. */
    private final String body;

    /**
     * Instantiates a new POSTRequest.
     *
     * @param url  the URL object
     * @param body url-encoded (application/x-www-form-urlencoded) request body
     */
    public POSTRequest(final URL url, final String body) {
        this.url = url;
        this.body = body;
    }

    /**
     * Sends the request and returns the response.
     *
     * @return String
     * @throws Exception the exception
     */
    @Override
    public final String send() throws Exception {
        URLConnection con = this.url.openConnection();
        con.setDoOutput(true);

        OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream(),
                StandardCharsets.UTF_8);
        out.write(this.body);
        out.close();

        BufferedReader in = new BufferedReader(new InputStreamReader(
                con.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String buffer;
        while ((buffer = in.readLine()) != null) {
            response.append(buffer);
        }
        in.close();
        return response.toString();
    }
}
