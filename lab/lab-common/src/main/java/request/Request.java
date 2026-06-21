package request;

import java.io.Serializable;

public abstract class Request implements Serializable {
    private final String commandName;
    private String login;
    private String password;
    private Long authenticatedUserId;

    protected Request(String commandName) {
        this.commandName = commandName;
    }

    public String getCommandName() {
        return commandName;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public void setCredentials(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public Long getAuthenticatedUserId() {
        return authenticatedUserId;
    }

    public void setAuthenticatedUserId(Long authenticatedUserId) {
        this.authenticatedUserId = authenticatedUserId;
    }
}
