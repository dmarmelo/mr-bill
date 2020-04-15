package dev.estgp.is.models;

import dev.estgp.is.utils.sqlite3.DBRow;
import dev.estgp.is.utils.sqlite3.DBRowList;
import dev.estgp.is.utils.sqlite3.SQLiteConn;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * User model
 */
public class User implements Serializable {

    // Database fields
    public int id;
    public String name;
    public String username;
    public transient String password;
    public transient String apikey;

    // The shared SQLite connection
    private static SQLiteConn conn = SQLiteConn.getSharedInstance();

    // Default constructor
    public User() {

    }

    // DBRow constructor
    public User(DBRow row) {
        super();
        this.id = (int) row.get("id");
        this.name = (String) row.get("name");
        this.username = (String) row.get("username");
        this.password = (String) row.get("password");
        this.apikey = (String) row.get("apikey");
    }

    // Returns user by id
    public static User get(int id) {
        String sql = String.format("select * from user where id=%d", id);
        DBRowList rows = conn.executeQuery(sql);
        return rows.first(User.class);
    }

    // Returns user by api key
    public static User get(String apikey) {
        String sql = String.format("select * from user where apikey='%s'", apikey);
        DBRowList rows = conn.executeQuery(sql);
        return rows.first(User.class);
    }

    // Returns user by username / password
    public static User authenticate(String username, String password) {
        String sql = String.format("select * from user where username='%s' and password='%s'",
                username, password);
        DBRowList rows = conn.executeQuery(sql);
        return rows.first(User.class);
    }

    // Returns list of users
    public static ArrayList<User> all() {
        ArrayList<User> users = new ArrayList<>();
        DBRowList rows = conn.executeQuery("select * from user");
        for (DBRow row : rows) {
            User user = new User(row);
            users.add(user);
        }
        return users;
    }

    // Inserts this user
    public int insert() {
        String sql = String.format("INSERT INTO user (name, username, password) VALUES ('%s', '%s', '%s')",
                name, username, password);
        return conn.executeUpdate(sql);
    }

    // Updates this user
    public int update() {
        String sql = String.format("UPDATE user SET name='%s', username='%s', password='%s' WHERE id=%d",
                name, username, password, id);
        return conn.executeUpdate(sql);
    }

    // Deletes this user
    public void delete() {
        String sql = String.format("DELETE FROM user WHERE id=%d", id);
        conn.executeUpdate(sql);
    }

    // Saves this user
    public int save() {
        if (id == 0) {
            return insert();
        } else {
            return update();
        }
    }

    @Override
    public String toString() {
        return "User {" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", apikey='" + apikey + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getApikey() {
        return apikey;
    }
}
