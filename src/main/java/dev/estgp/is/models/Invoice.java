package dev.estgp.is.models;

import dev.estgp.is.utils.sqlite3.DBRow;
import dev.estgp.is.utils.sqlite3.DBRowList;
import dev.estgp.is.utils.sqlite3.SQLiteConn;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Invoice model
 */
public class Invoice implements Serializable {

    // Database fields
    public int id;
    public int costumer_id;
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
        this.costumer_id = (int) row.get("costumer_id");
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

    // Returns invoice by costumer
    public static List<Invoice> getByCostumer(Costumer costumer) {
        ArrayList<Invoice> invoices = new ArrayList<>();
        String sql = String.format("select * from invoice where costumer_id=%d", costumer.id);
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

    // Returns list of costumer invoices
    public static ArrayList<Invoice> get(Costumer costumer) {
        ArrayList<Invoice> invoices = new ArrayList<>();
        DBRowList rows = conn.executeQuery("select * from invoice where costumer_id=" + costumer.id);
        for (DBRow row : rows) {
            Invoice invoice = new Invoice(row);
            invoices.add(invoice);
        }
        return invoices;
    }

    // Inserts this invoice
    public int insert() {
        String sql = String.format(Locale.US, "INSERT INTO invoice (costumer_id, amount, date, complete) VALUES (%d, %f, '%s', %d)",
                costumer_id, amount, date, complete);
        return conn.executeUpdate(sql);
    }

    // Updates this invoice
    public int update() {
        String sql = String.format(Locale.US, "UPDATE invoice SET costumer_id=%d, amount=%f, date='%s', complete=%d WHERE id=%d",
                costumer_id, amount, date, complete, id);
        System.out.println(sql);
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
                ", costumer_id=" + costumer_id +
                ", amount=" + amount +
                ", date='" + date + '\'' +
                ", complete=" + complete +
                '}';
    }

    public int getId() {
        return id;
    }

    public int getCostumer_id() {
        return costumer_id;
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
}
