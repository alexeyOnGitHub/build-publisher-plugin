package org.jenkinsci.plugins.jpp;

import java.util.Objects;

public final class RabbitMQConfiguration {

    private final String serverName;
    private final int serverPort;
    private final String userName;
    private final String password;
    private final String exchangeName;

    public RabbitMQConfiguration(String serverName, int serverPort, String userName, String password, String exchangeName) {
        this.serverName = serverName;
        this.serverPort = serverPort;
        this.userName = userName;
        this.password = password;
        this.exchangeName = exchangeName;
    }

    public boolean allFieldsEqual(RabbitMQConfiguration another) {
        return Objects.equals(serverName, another.serverName)
                && serverPort == another.serverPort
                && Objects.equals(exchangeName, another.exchangeName)
                && Objects.equals(userName, another.userName)
                && Objects.equals(password, another.password);
    }

    public String getServerName() {
        return serverName;
    }

    public int getServerPort() {
        return serverPort;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getExchangeName() {
        return exchangeName;
    }
}
