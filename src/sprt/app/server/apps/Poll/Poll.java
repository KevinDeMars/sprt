/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package sprt.app.server.apps.Poll;

import sprt.app.server.ServerApp;
import sprt.app.server.State;
import sprt.app.server.StateResult;
import sprt.serialization.*;

import java.util.Map;

public class Poll extends ServerApp {
    public static class InitialStep extends State {
        @Override
        public String name() {
            return "InitialStep";
        }
        @Override
        public String prompt() {
            return "";
        }

        public StateResult doHandleRequest(ServerApp app, Request req) throws ValidationException {
            var cookies = req.getCookieList();
            var fname = cookies.getValue("FName");
            var lname = cookies.getValue("LName");

            if (fname != null && lname != null)
                return new StateResult(new FoodStep(fname, lname));
            else
                return new StateResult(new NameStep());
        }
    }

    public static class NameStep extends State {
        public String prompt() {
            return "Name (First Last)> ";
        }
        @Override
        public String name() {
            return "NameStep";
        }
        public StateResult doHandleRequest(ServerApp app, Request req, String fName, String lName) throws ValidationException {
            return new StateResult(
                    new FoodStep(fName, lName),
                    new CookieList().add("FName", fName).add("LName", lName)
            );
        }
    }

    public static class FoodStep extends State {
        private static record RestaurantOffer(String name, int pcOff) {

        }
        private static final Map<String, RestaurantOffer> OFFERS = Map.of(
                "MEXICAN", new RestaurantOffer("Tacopia", 20),
                "ITALIAN", new RestaurantOffer("Pastastic", 25)
        );

        private final String fName, lName;
        FoodStep(String fName, String lName) {
            this.fName = fName;
            this.lName = lName;
        }

        @Override
        public String name() {
            return "FoodStep";
        }
        @Override
        public String prompt() {
            return fName + "'s Food mood> ";
        }

        public StateResult doHandleRequest(ServerApp app, Request req, String foodMood) throws ValidationException {
            int repeat;
            var cookies = req.getCookieList();
            // create new cookie if needed
            if (cookies.getValue("Repeat") == null) {
                repeat = 0;
            }
            else {
                // parse cookie as integer
                try {
                    repeat = Integer.parseInt(req.getCookieList().getValue("Repeat"));
                }
                catch (NumberFormatException e) {
                    return new StateResult(null, new Response(
                            Status.ERROR, "NULL", "Repeat (in cookie list) must be integer")
                    );
                }
            }
            ++repeat;

            var offer = OFFERS.getOrDefault(foodMood.toUpperCase(), new RestaurantOffer("McDonalds", 10));
            var newCookies = new CookieList().add("Repeat", String.valueOf(repeat));
            String msg = offer.pcOff + "% + " + repeat + "% off at " + offer.name;
            return new StateResult(null, new Response(Status.OK, "NULL", msg, newCookies));
        }

    }

    public Poll() {
        super(new InitialStep());
    }
}