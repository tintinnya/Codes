/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.

http://zetcode.com/db/postgresqljavatutorial/

*/

package fileawareness.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tintinmcleod
 */
public class TestConnectionDB {
    
    public static void main(String[] args) {

        Connection con = null;
        Statement st = null;
        ResultSet rs = null;

    
        String url = "jdbc:postgresql://localhost/thesisdb";
        String user = "thesisuser";
        String password = "thesis2013!";

        try {
            System.out.println("Trying to get connection to PostgreSQL server...");
            con = DriverManager.getConnection(url, user, password);
            System.out.println("... connected!");
            st = con.createStatement();
            rs = st.executeQuery("SELECT VERSION()");

            if (rs.next()) {
                System.out.println("SELECT VERSION() = "+rs.getString(1));
            }

        } catch (SQLException ex) {
            System.out.println("...failed: "+ex.getMessage());
            Logger lgr = Logger.getLogger(TestConnectionDB.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);

        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (st != null) {
                    st.close();
                }
                if (con != null) {
                    con.close();
                }

            } catch (SQLException ex) {
                Logger lgr = Logger.getLogger(TestConnectionDB.class.getName());
                lgr.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }
}