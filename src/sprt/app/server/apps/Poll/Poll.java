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
import sprt.serialization.CookieList;
import sprt.serialization.Request;
import sprt.serialization.Status;
import sprt.serialization.ValidationException;

import java.util.Map;

/**
 * Polls what food mood the client has, and offers a discount.
 */
public class Poll extends ServerApp {
    /**
     * Initial poll step.
     */
    public static class InitialStep extends State {
        @Override
        public String name() {
            return "InitialStep";
        }
        @Override
        public String prompt() {
            return "";
        }

        /**
         * Handles a client's request
         * @param req client's request
         * @return next state + response pair. NameStep if name is not known; else FoodStep.
         * @throws ValidationException if response data is invalid
         */
        public StateResult doHandleRequest(Request req) throws ValidationException {
            var cookies = req.getCookieList();
            var fname = cookies.getValue("FName");
            var lname = cookies.getValue("LName");

            if (fname != null && lname != null)
                return new StateResult(new FoodStep(fname, lname), Status.OK);
            else
                return new StateResult(new NameStep(), Status.OK);
        }
    }

    /**
     * Gets first and last name from client
     */
    public static class NameStep extends State {
        public String prompt() {
            return "Name (First Last)> ";
        }
        @Override
        public String name() {
            return "NameStep";
        }

        /**
         * Handles a client's request
         * @param req client's request
         * @param fName first name
         * @param lName last name
         * @return next state + response pair
         * @throws ValidationException if response data is invalid
         */
        public StateResult doHandleRequest(Request req, String fName, String lName) throws ValidationException {
            return new StateResult(
                    new FoodStep(fName, lName),
                    Status.OK,
                    new CookieList().add("FName", fName).add("LName", lName)
            );
        }
    }

    /**
     * Offers discount to client based on food mood
     */
    public static class FoodStep extends State {
        private static record RestaurantOffer(String name, int pcOff) {

        }
        private static final Map<String, RestaurantOffer> OFFERS = Map.of(
                "MEXICAN", new RestaurantOffer("Tacopia", 20),
                "ITALIAN", new RestaurantOffer("Pastatic", 25)
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
            return fName + "'s Food Mood> ";
        }

        /**
         * Handles a client's request
         * @param req client's request
         * @param foodMood type of food (e.g. mexican)
         * @return No next state. Response contains an offer for a discount at a restaurant.
         * @throws ValidationException if response data is invalid
         */
        public StateResult doHandleRequest(Request req, String foodMood) throws ValidationException {
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
                    return StateResult.exit(Status.ERROR, "Repeat (in cookie list) must be integer", new CookieList().add("Repeat", "0"));
                }
            }
            ++repeat;

            var offer = OFFERS.getOrDefault(foodMood.toUpperCase(), new RestaurantOffer("McDonalds", 10));
            var newCookies = new CookieList().add("Repeat", String.valueOf(repeat));
            String msg = offer.pcOff + "% + " + repeat + "% off at " + offer.name;
            return StateResult.exit(Status.OK, msg, newCookies);
        }

    }

    /**
     * Creates new Poll application.
     */
    public Poll() {
        gotoState(new InitialStep());
    }
}