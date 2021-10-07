/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package sprt.serialization;

/**
 * Possible concrete types of a Message
 */
public enum MessageType {
    /** SPRT response */
    Response("R"),
    /** SPRT request */
    Request("Q");

    /** The String token representation of this type, e.g. R for response */
    public final String token;
    MessageType(String token) {
        this.token = token;
    }

    /**
     * Gets the MessageType whose token representation corresponds to the given token
     * @param token token string to look up
     * @return MessageType with matching token
     * @throws ValidationException if no MessageType has the given token
     */
    public static MessageType fromToken(String token) throws ValidationException {
        return switch (token) {
            case "R" -> MessageType.Response;
            case "Q" -> MessageType.Request;
            default -> throw new ValidationException("Invalid message type " + token, token);
        };
    }
}
