package dev.estgp.is.rpc;

import dev.estgp.is.models.Customer;
import dev.estgp.is.models.Invoice;
import dev.estgp.is.models.User;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RMIInterface extends Remote {

    public String hello(String name) throws RemoteException;

    // User
    public User getUser(int id) throws RemoteException;
    public List<User> getUsers() throws RemoteException;
    public int insertUser(User user) throws RemoteException;
    public int updateUser(User user) throws RemoteException;
    public void deleteUser(User user) throws RemoteException;
    public int saveUser(User user) throws RemoteException;
    public User authenticate(String username, String password) throws RemoteException;

    // Customer
    public Customer getCustomer(int id) throws RemoteException;
    public List<Customer> getCustomers() throws RemoteException;
    public int insertCustomer(Customer customer) throws RemoteException;
    public int updateCustomer(Customer customer) throws RemoteException;
    public void deleteCustomer(Customer customer) throws RemoteException;
    public int saveCustomer(Customer customer) throws RemoteException;
    public List<Customer> getCustomersFromUser(User user) throws RemoteException;

    // Invoice
    public Invoice getInvoice (int id) throws RemoteException;
    public List<Invoice> getInvoices() throws RemoteException;
    public int insertInvoice(Invoice invoice) throws RemoteException;
    public int updateInvoice(Invoice invoice) throws RemoteException;
    public void deleteInvoice(Invoice invoice) throws RemoteException;
    public int saveInvoice(Invoice invoice) throws RemoteException;

}
