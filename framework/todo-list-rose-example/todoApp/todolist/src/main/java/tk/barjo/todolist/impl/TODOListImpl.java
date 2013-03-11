package tk.barjo.todolist.impl;

import org.apache.felix.ipojo.annotations.*;
import org.osgi.service.jdbc.DataSourceFactory;
import tk.barjo.todolist.TODOList;
import tk.barjo.todolist.TODONote;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 *
 */
@Component(immediate = true)
@Instantiate(name="DummyTodoList_Instance")
@Provides
public class TODOListImpl implements TODOList {
    //DataBase Connetion
    private Connection dbcon;

    @Requires
    private  DataSourceFactory dsf;


    @Validate
    private void start() throws SQLException {
        //Create the DataSource from the sqlite database service.
        //Use a in memory database (no file)
        Properties props = new Properties();
        props.put(DataSourceFactory.JDBC_URL, "jdbc:sqlite::memory:");
        props.put("org.jdbc.url", "jdbc:sqlite::memory:");


        DataSource ds = dsf.createDataSource(props);

        //Get the database connection
        dbcon = ds.getConnection();
        dbcon.setAutoCommit(true);

        //Create the todolist table
        Statement stment = dbcon.createStatement();
        stment.execute("CREATE TABLE IF NOT EXISTS todolist(name UNIQUE, content TEXT);");
        stment.close();
    }

    @Invalidate
    private void stop() throws SQLException {
        //Close the database connection
        dbcon.close();
    }


    public void addNote(TODONote note) {
        try {
            Statement statement = dbcon.createStatement();
            statement.executeUpdate("INSERT OR REPLACE " +
                    "INTO todolist ( name, content )  VALUES " +
                    "( '"+note.getName()+"', '"+note.getContent()+"' );");
            statement.close();
        } catch (SQLException se) {
            throw new IllegalArgumentException(se);
        }

        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean rmNote(String name) {
        int removed = 0;
        try {

            Statement statement = dbcon.createStatement();
            removed = statement.executeUpdate("DELETE FROM todolist WHERE" +
                    " name='" + name + "';");

            statement.close();
        } catch (SQLException se) {
            throw new IllegalArgumentException(se);
        }
        return removed == -1;
    }

    public TODONote getNote(String name) {
        TODONote note = null;
        try {
            Statement statement = dbcon.createStatement();
            ResultSet res  = statement.executeQuery("SELECT name,content from todolist where name = '" + name + "'");
            if (res.next()){
                note = new TODONote(res.getString("name"), res.getString("content"));
            }
            statement.close();
        } catch (SQLException se) {
            throw new IllegalArgumentException(se);
        }

        return note;
    }

    public List<TODONote> getAllNotes() {
        List<TODONote> notes = new ArrayList<TODONote>();
        Statement statement;
        try {
            statement = dbcon.createStatement();
            ResultSet res = statement.executeQuery("SELECT name, content from todolist;");
            while (res.next()) {
                notes.add(new TODONote(res.getString("name"), res.getString("content")));
            }
            statement.close();
        } catch (SQLException se) {
            throw new IllegalArgumentException(se);
        }

        return notes;
    }
}
