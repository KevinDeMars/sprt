/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package sprt.app.server.loadtest;

import sprt.app.client.Client;
import sprt.serialization.*;

import java.io.IOException;
import java.util.List;

/**
 * A testing client that sends pre-determined requests and checks to see if the correct responses were obtained.
 */
public class MockClient extends Client {
    private List<Request> reqsToSend;
    private List<Response> expectedResponses;

    public MockClient(String host, int port, List<Request> reqsToSend, List<Response> expectedResponses) throws IOException {
        super(host, port, new CookieList());
        this.reqsToSend = reqsToSend;
        this.expectedResponses = expectedResponses;
    }

    @Override
    public void go() throws IOException, ValidationException {
        var responses = expectedResponses.iterator();
        for (Request r : reqsToSend) {
            r.setCookieList(r.getCookieList().addAll(this.cookies));
            r.encode(out);
            var resp = (Response) Message.decodeType(in, MessageType.Response);

            cookies.addAll(resp.getCookieList());

            if (!responses.next().equals(resp))
                throw new RuntimeException("Unexpected response");
        }
    }
}
