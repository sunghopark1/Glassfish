import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/**
 * Unit test for alternate docroot support and welcome pages.
 * See
 *   https://glassfish.dev.java.net/issues/show_bug.cgi?id=6731
 *   ("how to protect files residing in docroot in Glassfish")
 * for details.
 *
 * This test configures the virtual server "server" with the following
 * alternate docroot properties:
 *
 *   <property
 *     name="alternatedocroot_1"
 *     value="from=/mytest dir=/tmp"/>
 *   <property
 *     name="alternatedocroot_2"
 *     value="from=/mytest/* dir=/tmp"/>
 *
 * and ensures that a request with a URI of the form /mytest will first be
 * redirected to "/mytest/", before the contents of the welcome page located
 * in "/tmp/mytest/index.jsp" in the local filesystem will be served
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME = "virtual-server-alternate-docroot-welcomepage-redirect";

    private String host;
    private String port;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for alternate docroot support");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {     
        try { 
            invoke();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invoke() throws Exception {
        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET /mytest HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("\r\n".getBytes());

        InputStream is = sock.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = null;
        String location = null;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
            if (line.startsWith("Location:")) {
                location = line;
            }
        }

        if (location == null) {
            throw new Exception("Missing Location response header");
        }

        String redirect = location.substring("Location:".length()).trim();
        followRedirect(new URL(redirect));
    }

    private void followRedirect(URL url) throws Exception {

        System.out.println("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Wrong response code. Expected: 200"
                                + ", received: " + responseCode);
        }

        InputStream is = conn.getInputStream();
        BufferedReader input = new BufferedReader(new InputStreamReader(is));
        String line = null;
        while ((line = input.readLine()) != null) {
            System.out.println(line);
            if (line.equals("HELLO WORLD")) {
                break;
            }
        }

        if (line == null) {
            throw new Exception("Missing or unexpected content for " + url);
        }
    }

}
