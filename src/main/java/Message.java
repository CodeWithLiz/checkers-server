import java.io.Serializable;
import java.util.ArrayList;

public class Message implements Serializable {
        private static final long serialVersionUID = 42L;

        private String type;         // "set_username", "match_found", "player_ready", "begin_game_now", "move", "chat_all"
        private String sender;
        private String text;         // Used for color ("red"/"black") or general messages
        private String opponentName; // Added to show who you are playing in the lobby
        private String groupName;
        private String recipient;

        private boolean success;
        private boolean yourTurn;    // Used in "begin_game_now" to tell client if they go first

        private ArrayList<String> users;

        // Move coordinates
        private int fromRow, fromCol, toRow, toCol;

        public Message(String type) {
                this.type = type;
        }

        // --- Getters and Setters ---

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getSender() { return sender; }
        public void setSender(String sender) { this.sender = sender; }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public String getOpponentName() { return opponentName; }
        public void setOpponentName(String opponentName) { this.opponentName = opponentName; }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public boolean isYourTurn() { return yourTurn; }
        public void setYourTurn(boolean yourTurn) { this.yourTurn = yourTurn; }

        public ArrayList<String> getUsers() { return users; }
        public void setUsers(ArrayList<String> users) { this.users = users; }

        public String getGroupName() { return groupName; }
        public void setGroupName(String groupName) { this.groupName = groupName; }

        public String getRecipient() { return recipient; }
        public void setRecipient(String recipient) { this.recipient = recipient; }

        // Move Getters/Setters
        public int getFromRow() { return fromRow; }
        public void setFromRow(int fromRow) { this.fromRow = fromRow; }

        public int getFromCol() { return fromCol; }
        public void setFromCol(int fromCol) { this.fromCol = fromCol; }

        public int getToRow() { return toRow; }
        public void setToRow(int toRow) { this.toRow = toRow; }

        public int getToCol() { return toCol; }
        public void setToCol(int toCol) { this.toCol = toCol; }
}

//    private static final long serialVersionUID = 42L;
//
//    private String sender;      // who sent the message
//    private String recipient;   // for private messages
//    private String group;       // group name
//    private String type;        // chat_all, chat_private
//    private String text;        // actual message text
//
//
//    private ArrayList<String> users;
//
//    private boolean success;
//
//    //  constructor
//    public Message(String sender, String type, String text, String recipient, String group) {
//        this.sender = sender;
//        this.type = type;
//        this.text = text;
//        this.recipient = recipient;
//        this.group = group;
//    }
//
//    // constructor
//    public Message(String type) {
//        this.type = type;
//    }
//
//    // getter
//    public String getSender() {
//        return sender;
//    }
//
//    public String getRecipient() {
//        return recipient;
//    }
//
//    public String getGroup() {
//        return group;
//    }
//
//    public String getType() {
//        return type;
//    }
//
//    public String getText() {
//        return text;
//    }
//
//
//
//    public ArrayList<String> getUsers() {
//        return users;
//    }
//
//    public boolean isSuccess() {
//        return success;
//    }
//
//    // setter
//    public void setSender(String sender) {
//        this.sender = sender;
//    }
//
//    public void setRecipient(String recipient) {
//        this.recipient = recipient;
//    }
//
//    public void setGroupName(String groupName) {
//        this.group = groupName;
//    }
//
//
//
//    public void setText(String text) {
//        this.text = text;
//    }
//
//
//    public void setUsers(ArrayList<String> users) {
//        this.users = users;
//    }
//
//    public void setSuccess(boolean success) {
//        this.success = success;
//    }
//
//    @Override
//    public String toString() {
//        return "Message[type=" + type +
//                ", sender=" + sender +
//                ", recipient=" + recipient +
//                ", group=" + group +
//                ", text=" + text + "]";
//    }
//}