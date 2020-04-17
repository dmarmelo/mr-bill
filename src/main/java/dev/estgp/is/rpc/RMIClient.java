package dev.estgp.is.rpc;

import dev.estgp.is.models.User;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * Implements a basic RMI Client
 */
public class RMIClient {

    public static void main(String[] args) throws MalformedURLException, RemoteException, NotBoundException {

        RMIInterface look_up = (RMIInterface) Naming.lookup("//localhost:8000/RMIServer");

        String text = look_up.hello("Daniel");
        System.out.println(text);

        /*User user = new User();
        user.name = "Manual";
        user.username = "manual";
        user.password = "manual123";
        look_up.saveUser(user);*/

        /*User bart = look_up.getUser(1);
        bart.name = "Bartolo";
        look_up.updateUser(bart);*/

        /*User user = look_up.getUser(4);
        look_up.deleteUser(user);*/

        User authenticatesUser = look_up.authenticate("bart", "1234");



        System.out.println("authenticatesUser = " + authenticatesUser);


        look_up.getUsers().forEach(System.out::println);
    }
}
