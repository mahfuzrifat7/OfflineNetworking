package chatbox;

public class LMessage {

    private String username;
    private String password;
    private String type;

    public LMessage(String str) {

        String[] x = str.split("##");

        this.username = x[0];
        this.password = x[1];
        this.type = x[2];

    }

    public String getUsername() {
        return username;
    }

    public String loginInfo() {

        if (!ChatBoxServer.usernames.contains(username))
            return "You DON'T Have an Account";

        int index = ChatBoxServer.usernames.indexOf(username);

        if (ChatBoxServer.loginStatus.get(index))
            return "You are Already Logged in!";

        if (!ChatBoxServer.passwords.get(index).equals(password))
            return "Password Incorrect!";

        if (!ChatBoxServer.types.get(index).equals(type))
            return "Type Incorrect!";

        ChatBoxServer.loginStatus.set(ChatBoxServer.usernames.indexOf(username), true);
        return "Login Successful!";

    }
}
