import java.io.Serializable;
import java.util.ArrayList;

public class Message implements Serializable {
    private static final long serialVersionUID = 42L;

    private String sender;      // who sent the message
    private String recipient;   // for private messages
    private String group;       // group name
    private String type;        // chat_all, chat_private
    private String text;        // actual message text


    private ArrayList<String> users;

    private boolean success;

    //  constructor
    public Message(String sender, String type, String text, String recipient, String group) {
        this.sender = sender;
        this.type = type;
        this.text = text;
        this.recipient = recipient;
        this.group = group;
    }

    // constructor
    public Message(String type) {
        this.type = type;
    }

    // getter
    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getGroup() {
        return group;
    }

    public String getType() {
        return type;
    }

    public String getText() {
        return text;
    }



    public ArrayList<String> getUsers() {
        return users;
    }

    public boolean isSuccess() {
        return success;
    }

    // setter
    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public void setGroupName(String groupName) {
        this.group = groupName;
    }



    public void setText(String text) {
        this.text = text;
    }


    public void setUsers(ArrayList<String> users) {
        this.users = users;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "Message[type=" + type +
                ", sender=" + sender +
                ", recipient=" + recipient +
                ", group=" + group +
                ", text=" + text + "]";
    }
}