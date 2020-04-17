package dev.estgp.is;

import dev.estgp.is.models.User;
import dev.estgp.is.rpc.RMIInterface;
import dev.estgp.is.utils.sqlite3.SQLiteConn;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import static org.junit.jupiter.api.Assertions.*;

public class RMITest {

    static RMIInterface look_up;

    // The application SQLite connection
    public static SQLiteConn conn = SQLiteConn.getSharedInstance();

    @BeforeAll
    public static void connectRMI() throws RemoteException, NotBoundException, MalformedURLException {
        look_up = (RMIInterface) Naming.lookup("//localhost:8000/RMIServer");
    }

    @BeforeEach
    public void rebuildBD() {
        conn.recreate("data/database.sql");
    }

    @Test
    // Existing users must not return null
    public void testAuthenticationSuccess() throws RemoteException {
        User bart = look_up.authenticate("bart", "1234");
        assertNotNull(bart);
    }

    @Test
    // Non-existing users must return null
    public void testAuthenticationFail() throws RemoteException {
        User bart = look_up.authenticate("bart", "fail");
        assertNull(bart);
    }

    @Test
    // Test correct user
    public void testAuthenticationCorrectUser() throws RemoteException {
        User bart = look_up.authenticate("bart", "1234");
        assertEquals(bart.name, "Bart Simpson");
    }

    @Test
    // Deletes user
    public void testDeleteUser() throws RemoteException {
        int userId = 2;
        User user = look_up.getUser(userId);
        look_up.deleteUser(user);
        User user1 = look_up.getUser(userId);
        assertNull(user1);
    }

}
