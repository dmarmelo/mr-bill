package dev.estgp.is.rpc;

import dev.estgp.is.models.Customer;
import dev.estgp.is.models.Invoice;
import dev.estgp.is.models.User;
import dev.estgp.is.utils.sqlite3.SQLiteConn;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

/**
 * Implements a basic RMI Server
 */
public class RMIServer extends UnicastRemoteObject implements RMIInterface {

    public RMIServer() throws RemoteException {
        super();
    }

    @Override
    public String hello(String name) {
        return "Hello " + name + "!";
    }

    @Override
    public User getUser(int id) {
        return User.get(id);
    }

    @Override
    public List<User> getUsers() {
        return User.all();
    }

    @Override
    public int insertUser(User user) {
        return user.save();
    }

    @Override
    public int updateUser(User user) {
        return user.update();
    }

    @Override
    public void deleteUser(User user) {
        user.delete();
    }

    @Override
    public int saveUser(User user) {
        return user.save();
    }

    @Override
    public User authenticate(String username, String password) {
        return User.authenticate(username, password);
    }

    @Override
    public Customer getCustomer(int id) {
        return Customer.get(id);
    }

    @Override
    public List<Customer> getCustomers() {
        return Customer.all();
    }

    @Override
    public int insertCustomer(Customer customer) {
        return customer.insert();
    }

    @Override
    public int updateCustomer(Customer customer) {
        return customer.update();
    }

    @Override
    public void deleteCustomer(Customer customer) {
        customer.delete();
    }

    @Override
    public int saveCustomer(Customer customer) {
        return customer.save();
    }

    @Override
    public List<Customer> getCustomersFromUser(User user) throws RemoteException {
        return Customer.get(user);
    }

    @Override
    public Invoice getInvoice(int id) {
        return Invoice.get(id);
    }

    @Override
    public List<Invoice> getInvoices() {
        return Invoice.all();
    }

    @Override
    public int insertInvoice(Invoice invoice) {
        return invoice.insert();
    }

    @Override
    public int updateInvoice(Invoice invoice) {
        return invoice.update();
    }

    @Override
    public void deleteInvoice(Invoice invoice) {
        invoice.delete();
    }

    @Override
    public int saveInvoice(Invoice invoice) {
        return invoice.save();
    }

    public static void main(String[] args) throws Exception {
        //System.setProperty("java.rmi.server.hostname", "172.20.107.155");

        // Configure database connection
        SQLiteConn conn = SQLiteConn.getSharedInstance();
        conn.init("data/database.db");
        conn.recreate("data/database.sql");

        LocateRegistry.createRegistry(8000);
        Naming.rebind("//localhost:8000/RMIServer", new RMIServer());
        System.out.println("RMIServer ready");
    }
}
