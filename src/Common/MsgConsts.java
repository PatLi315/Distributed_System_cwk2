package Common;

public enum MsgConsts {

    AT("@"),
    NEW_PLAYER("new player"),
    NEW_GAME("new game"),
    TURN("turn"),
    CHAT("chat"),
    QUIT("quit"),
    DRAW("draw"),
    RESUME("resume"),
    GAME_OVER("game over"),
    DEFAULT_RESP("OK");

    private final String value;

    MsgConsts(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    // Optionally, if you need to get an enum constant from a string:
    public static MsgConsts fromString(String text) {
        for (MsgConsts msg : MsgConsts.values()) {
            if (msg.value.equalsIgnoreCase(text)) {
                return msg;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}
