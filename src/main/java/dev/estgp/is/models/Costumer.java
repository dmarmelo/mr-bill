package dev.estgp.is.models;

import dev.estgp.is.utils.sqlite3.DBRow;
import dev.estgp.is.utils.sqlite3.DBRowList;
import dev.estgp.is.utils.sqlite3.SQLiteConn;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Costumer model
 */
public class Costumer implements Serializable {

    // Database fields
    public int id;
    public int user_id;
    public String name;
    public String email;

    // The shared SQLite connection
    private static SQLiteConn conn = SQLiteConn.getSharedInstance();

    // Default constructor
    public Costumer() {

    }

    // DBRow constructor
    public Costumer(DBRow row) {
        super();
        this.id = (int) row.get("id");
        this.user_id = (int) row.get("user_id");
        this.name = (String) row.get("name");
        this.email = (String) row.get("email");
    }

    // Returns costumer by id
    public static Costumer get(int id) {
        String sql = String.format("select * from costumer where id=%d", id);
        DBRowList rows = conn.executeQuery(sql);
        return rows.first(Costumer.class);
    }

    // Returns costumer by user
    public static List<Costumer> getByUser(User user) {
        ArrayList<Costumer> costumers = new ArrayList<>();
        String sql = String.format("select * from costumer where user_id=%d", user.id);
        DBRowList rows = conn.executeQuery(sql);
        for (DBRow row: rows) {
            Costumer costumer = new Costumer(row);
            costumers.add(costumer);
        }
        return costumers;
    }


    // Returns list of costumers
    public static ArrayList<Costumer> all() {
        ArrayList<Costumer> costumers = new ArrayList<>();
        DBRowList rows = conn.executeQuery("select * from costumer");
        for (DBRow row : rows) {
            Costumer costumer = new Costumer(row);
            costumers.add(costumer);
        }
        return costumers;
    }

    // Returns costumers by User
    public static ArrayList<Costumer> get(User user) {
        ArrayList<Costumer> costumers = new ArrayList<>();
        DBRowList rows = conn.executeQuery("select * from costumer where user_id=" + user.id);
        for (DBRow row : rows) {
            Costumer costumer = new Costumer(row);
            costumers.add(costumer);
        }
        return costumers;
    }

    // Inserts this costumer
    public int insert() {
        String sql = String.format("INSERT INTO costumer (user_id, name, email) VALUES (%d, '%s', '%s')",
                user_id, name, email);
        return conn.executeUpdate(sql);
    }

    // Updates this costumer
    public int update() {
        String sql = String.format("UPDATE costumer SET user_id=%d, name='%s', email='%s' WHERE id=%d",
                user_id, name, email, id);
        return conn.executeUpdate(sql);
    }

    // Deletes this costumer
    public void delete() {
        String sql = String.format("DELETE FROM costumer WHERE id=%d", id);
        conn.executeUpdate(sql);
    }

    // Saves this costumer
    public int save() {
        if (id == 0) {
            return insert();
        } else {
            return update();
        }
    }

    @Override
    public String toString() {
        return "Costumer {" +
                "id=" + id +
                ", user_id=" + user_id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public int getUser_id() {
        return user_id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Costumer costumer = (Costumer) o;
        return id == costumer.id &&
                user_id == costumer.user_id &&
                Objects.equals(name, costumer.name) &&
                Objects.equals(email, costumer.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user_id, name, email);
    }
}
