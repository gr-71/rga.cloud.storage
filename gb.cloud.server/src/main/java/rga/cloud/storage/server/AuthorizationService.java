package rga.cloud.storage.server;

import java.sql.*;

class AuthorizationService {

    private static Connection connection;
    private static Statement statement;

// Приконнектится к БД
    static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:users.db");
            statement = connection.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

// Проверка пароля пользователя
    static boolean checkUserPassword(String login, String password) {
        try {
            String str = String.format("SELECT password FROM main WHERE login = '%s'", login);
            ResultSet resultSet = statement.executeQuery(str);
            int passwordDB = resultSet.getInt(1);
            if (passwordDB == password.hashCode()) {
                System.out.println("Password is successfully checked.");
                return true;
            } else {
                System.out.println("Entered password is wrong.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

// Проверка логина пользователя
    static boolean checkUser(String login) {
        try {
            String str = String.format("SELECT login FROM main where login = '%s'", login);
            ResultSet resultSet = statement.executeQuery(str);
            if (resultSet.next()) {
                System.out.println("User " + login + " is successfully checked.");
                return true;
            } else {
                System.out.println("Such user does not exist. Please try again.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
// Добавить нового пользователя
    static void addNewUser(String login, String pass) {
        try {
            String insertQuery = "INSERT INTO main (login, password) VALUES (?, ?);";
            PreparedStatement ps = connection.prepareStatement(insertQuery);
            ps.setString(1, login);
            ps.setInt(2, pass.hashCode());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
