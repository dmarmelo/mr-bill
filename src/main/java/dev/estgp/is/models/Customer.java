package dev.estgp.is.models;

import dev.estgp.is.utils.sqlite3.DBRow;
import dev.estgp.is.utils.sqlite3.DBRowList;
import dev.estgp.is.utils.sqlite3.SQLiteConn;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Customer model
 */
public class Customer implements Serializable {

    // Database fields
    public int id;
    public int user_id;
    public String name;
    public String email;

    // The shared SQLite connection
    private static SQLiteConn conn = SQLiteConn.getSharedInstance();

    // Default constructor
    public Customer() {

    }

    // DBRow constructor
    public Customer(DBRow row) {
        super();
        this.id = (int) row.get("id");
        this.user_id = (int) row.get("user_id");
        this.name = (String) row.get("name");
        this.email = (String) row.get("email");
    }

    // Returns customer by id
    public static Customer get(int id) {
        String sql = String.format("select * from customer where id=%d", id);
        DBRowList rows = conn.executeQuery(sql);
        return rows.first(Customer.class);
    }

    // Returns customer by user
    public static List<Customer> getByUser(User user) {
        ArrayList<Customer> customers = new ArrayList<>();
        String sql = String.format("select * from customer where user_id=%d", user.id);
        DBRowList rows = conn.executeQuery(sql);
        for (DBRow row: rows) {
            Customer customer = new Customer(row);
            customers.add(customer);
        }
        return customers;
    }


    // Returns list of customers
    public static ArrayList<Customer> all() {
        ArrayList<Customer> customers = new ArrayList<>();
        DBRowList rows = conn.executeQuery("select * from customer");
        for (DBRow row : rows) {
            Customer customer = new Customer(row);
            customers.add(customer);
        }
        return customers;
    }

    // Returns customers by User
    public static ArrayList<Customer> get(User user) {
        ArrayList<Customer> customers = new ArrayList<>();
        DBRowList rows = conn.executeQuery("select * from customer where user_id=" + user.id);
        for (DBRow row : rows) {
            Customer customer = new Customer(row);
            customers.add(customer);
        }
        return customers;
    }

    // Inserts this customer
    public int insert() {
        String sql = String.format("INSERT INTO customer (user_id, name, email) VALUES (%d, '%s', '%s')",
                user_id, name, email);
        return conn.executeUpdate(sql);
    }

    // Updates this customer
    public int update() {
        String sql = String.format("UPDATE customer SET user_id=%d, name='%s', email='%s' WHERE id=%d",
                user_id, name, email, id);
        return conn.executeUpdate(sql);
    }

    // Deletes this customer
    public void delete() {
        String sql = String.format("DELETE FROM customer WHERE id=%d", id);
        conn.executeUpdate(sql);
    }

    // Saves this customer
    public int save() {
        if (id == 0) {
            return insert();
        } else {
            return update();
        }
    }

    @Override
    public String toString() {
        return "Customer {" +
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
        Customer customer = (Customer) o;
        return id == customer.id &&
                user_id == customer.user_id &&
                Objects.equals(name, customer.name) &&
                Objects.equals(email, customer.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user_id, name, email);
    }
}
