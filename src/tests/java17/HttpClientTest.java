package java17;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpClientTest extends TestJPF{
    @Test
    public void testHttpClient() throws Exception {
        if (verifyNoPropertyViolation()) {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.example.com"))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertNotNull(response.body());
        }
    }
}
