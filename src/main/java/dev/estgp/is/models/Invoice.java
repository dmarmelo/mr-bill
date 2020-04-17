package dev.estgp.is.models;

import dev.estgp.is.utils.sqlite3.DBRow;
import dev.estgp.is.utils.sqlite3.DBRowList;
import dev.estgp.is.utils.sqlite3.SQLiteConn;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Invoice model
 */
public class Invoice implements Serializable {

    // Database fields
    public int id;
    public int customer_id;
    public double amount;
    public String date;
    public int complete;

    // The shared SQLite connection
    private static SQLiteConn conn = SQLiteConn.getSharedInstance();

    // Default constructor
    public Invoice() {

    }

    // DBRow constructor
    public Invoice(DBRow row) {
        super();
        this.id = (int) row.get("id");
        this.customer_id = (int) row.get("customer_id");
        this.amount = (double) row.get("amount");
        this.date = (String) row.get("date");
        this.complete = (int) row.get("complete");
    }

    // Returns invoice by id
    public static Invoice get(int id) {
        String sql = String.format("select * from invoice where id=%d", id);
        DBRowList rows = conn.executeQuery(sql);
        return rows.first(Invoice.class);
    }

    // Returns invoice by customer
    public static List<Invoice> getByCustomer(Customer customer) {
        ArrayList<Invoice> invoices = new ArrayList<>();
        String sql = String.format("select * from invoice where customer_id=%d", customer.id);
        DBRowList rows = conn.executeQuery(sql);
        for (DBRow row: rows) {
            Invoice invoice = new Invoice(row);
            invoices.add(invoice);
        }
        return invoices;
    }


    // Returns list of invoices
    public static ArrayList<Invoice> all() {
        ArrayList<Invoice> invoices = new ArrayList<>();
        DBRowList rows = conn.executeQuery("select * from invoice");
        for (DBRow row : rows) {
            Invoice invoice = new Invoice(row);
            invoices.add(invoice);
        }
        return invoices;
    }

    // Returns list of customer invoices
    public static ArrayList<Invoice> get(Customer customer) {
        ArrayList<Invoice> invoices = new ArrayList<>();
        DBRowList rows = conn.executeQuery("select * from invoice where customer_id=" + customer.id);
        for (DBRow row : rows) {
            Invoice invoice = new Invoice(row);
            invoices.add(invoice);
        }
        return invoices;
    }

    // Inserts this invoice
    public int insert() {
        String sql = String.format(Locale.US, "INSERT INTO invoice (customer_id, amount, date, complete) VALUES (%d, %f, '%s', %d)",
                customer_id, amount, date, complete);
        return conn.executeUpdate(sql);
    }

    // Updates this invoice
    public int update() {
        String sql = String.format(Locale.US, "UPDATE invoice SET customer_id=%d, amount=%f, date='%s', complete=%d WHERE id=%d",
                customer_id, amount, date, complete, id);
        return conn.executeUpdate(sql);
    }

    // Deletes this invoice
    public void delete() {
        String sql = String.format("DELETE FROM invoice WHERE id=%d", id);
        conn.executeUpdate(sql);
    }

    // Saves this invoice
    public int save() {
        if (id == 0) {
            return insert();
        } else {
            return update();
        }
    }

    @Override
    public String toString() {
        return "Invoice {" +
                "id=" + id +
                ", customer_id=" + customer_id +
                ", amount=" + amount +
                ", date='" + date + '\'' +
                ", complete=" + complete +
                '}';
    }

    public int getId() {
        return id;
    }

    public int getCustomer_id() {
        return customer_id;
    }

    public double getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }

    public int getComplete() {
        return complete;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Invoice invoice = (Invoice) o;
        return id == invoice.id &&
                customer_id == invoice.customer_id &&
                Double.compare(invoice.amount, amount) == 0 &&
                complete == invoice.complete &&
                Objects.equals(date, invoice.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, customer_id, amount, date, complete);
    }
}
