package org.cougaar.tools.csmart.ui.component;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Date;
import java.util.List;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import org.cougaar.util.DBProperties;
import org.cougaar.util.Parameters;
import org.cougaar.util.DBConnectionPool;

/**
 * This class takes a structure of ComponentData objects and populates
 * the configuration database with some or all of the components
 * described by the data. The selection of applicable components is
 * still an issue.
 **/
public class PDbBase {
    public static final int RECIPE_STATUS_ABSENT = 0;
    public static final int RECIPE_STATUS_EXISTS = 1;
    public static final int RECIPE_STATUS_DIFFERS = 2;

    public static final String QUERY_FILE = "PopulateDb.q";
    protected Map substitutions = new HashMap() {
        public Object put(Object key, Object val) {
            if (val == null) throw new IllegalArgumentException("Null value for " + key);
            return super.put(key, val);
        }
    };
    protected DBProperties dbp;

    protected Connection dbConnection;
    protected Statement stmt;
    protected Statement updateStmt;
    protected boolean debug = false;
    protected PrintWriter log;

    /**
     * Constructor
     **/
    public PDbBase()
        throws SQLException, IOException
    {
        log = new PrintWriter(new FileWriter("PopulateDbQuery.log"));
        dbp = DBProperties.readQueryFile(QUERY_FILE);
        //        dbp.setDebug(true);
        String database = dbp.getProperty("database");
        String username = dbp.getProperty("username");
        String password = dbp.getProperty("password");
        String dbtype = dbp.getDBType();
        String driverParam = "driver." + dbtype;
        String driverClass = Parameters.findParameter(driverParam);
        if (driverClass == null)
            throw new SQLException("Unknown driver " + driverParam);
        try {
            Class.forName(driverClass);
        } catch (ClassNotFoundException cnfe) {
            throw new SQLException("Driver class not found: " + driverClass);
        }
        dbConnection = DBConnectionPool.getConnection(database, username, password);
        stmt = dbConnection.createStatement();
        updateStmt = dbConnection.createStatement();
    }

    /**
     * Check the status of a recipe in the database.
     * @param rc the RecipeComponent to check
     * @return RECIPE_STATUS_ABSENT -- Recipe not in database<br>
     * RECIPE_STATUS_EXISTS -- Recipe already in database with same
     * value<br> RECIPE_STATUS_DIFFERS -- Recipe already in database
     * with different value
     **/
    public int recipeExists(RecipeComponent rc) {
        try {
            if (checkRecipeExistence(rc) == null)
                return RECIPE_STATUS_ABSENT;
            else
                return RECIPE_STATUS_EXISTS;
        } catch (SQLException sqle) {
            return RECIPE_STATUS_DIFFERS;
        }
    }

    /**
     * Check the existence of a recipe.
     * @return The recipeId if the recipe is already present and matches, null if the recipe is absent
     * @exception if the recipe is present and differs.
     **/
    private String checkRecipeExistence(RecipeComponent rc) throws SQLException {
        Map newProps = new HashMap();
        for (Iterator j = rc.getPropertyNames(); j.hasNext(); ) {
            CompositeName pname = (CompositeName) j.next();
            Property prop = rc.getProperty(pname);
            Object val = prop.getValue();
            if (val == null) continue; // Don't write null values
            String sval = val.toString();
            if (sval.equals("")) continue; // Don't write empty values
            String name = pname.last().toString();
            newProps.put(name, sval);
        }
        String[] recipeIdAndClass = getRecipeIdAndClass(rc.getRecipeName());
        if (recipeIdAndClass == null) return null; // Does not exists
        // Already exists, check equality
        if (!recipeIdAndClass[1].equals(rc.getClass().getName()))
            throw new SQLException("Attempt to overwrite recipe "
                                   + rc.getRecipeName());
        Map oldProps = new HashMap();
        substitutions.put(":recipe_id:", recipeIdAndClass[0]);
        ResultSet rs =
            executeQuery(stmt, dbp.getQuery("queryLibRecipeProps", substitutions));
        while (rs.next()) {
            oldProps.put(rs.getString(1), rs.getString(2));
        }
        rs.close();
        if (!oldProps.equals(newProps))
            throw new SQLException("Attempt to overwrite recipe "
                                   + rc.getRecipeName());
        return recipeIdAndClass[0]; // Exists and matches
    }

    public String insureLibRecipe(RecipeComponent rc) throws SQLException {
        String recipeId = checkRecipeExistence(rc);
        if (recipeId != null) return recipeId;
        return insertLibRecipe(rc);
    }

    public String replaceLibRecipe(RecipeComponent rc) throws SQLException {
        removeLibRecipe(rc);
        return insertLibRecipe(rc);
    }

    public void removeLibRecipe(RecipeComponent rc) throws SQLException {
        String[] recipeIdAndClass = getRecipeIdAndClass(rc.getRecipeName());
        if (recipeIdAndClass != null) {
            substitutions.put(":recipe_id:", recipeIdAndClass[0]);
            executeUpdate(dbp.getQuery("deleteLibRecipeArgs", substitutions));
            executeUpdate(dbp.getQuery("deleteLibRecipe", substitutions));
        }
    }

    public String insertLibRecipe(RecipeComponent rc) throws SQLException {
        String recipeId = checkRecipeExistence(rc);
        if (recipeId != null) return recipeId;
        recipeId = getNextId("queryMaxRecipeId", "RECIPE-");
        substitutions.put(":recipe_id:", recipeId);
        substitutions.put(":java_class:", rc.getClass().getName());
        substitutions.put(":description:", "No description available");
        executeUpdate(dbp.getQuery("insertLibRecipe", substitutions));
        int order = 0;
        for (Iterator j = rc.getPropertyNames(); j.hasNext(); ) {
            CompositeName pname = (CompositeName) j.next();
            Property prop = rc.getProperty(pname);
            Object val = prop.getValue();
            if (val == null) continue; // Don't write null values
            String sval = val.toString();
            if (sval.equals("")) continue; // Don't write empty values
            String name = pname.last().toString();
            substitutions.put(":arg_name:", name);
            substitutions.put(":arg_value:", sval);
            substitutions.put(":arg_order:", String.valueOf(order++));
            executeUpdate(dbp.getQuery("insertLibRecipeProp", substitutions));
        }
        return recipeId;
    }

    private String[] getRecipeIdAndClass(String recipeName) throws SQLException {
        substitutions.put(":recipe_name:", recipeName);
        ResultSet rs =
            executeQuery(stmt, dbp.getQuery("queryLibRecipeByName", substitutions));
        try {
            if (rs.next()) {
                return new String[] {rs.getString(1), rs.getString(2)};
            } else {
                return null;
            }
        } finally {
            rs.close();
        }
    }

    protected String getNextId(String queryName, String prefix) {
        DecimalFormat format = new DecimalFormat(prefix + "0000");
        substitutions.put(":max_id_pattern:", prefix + "____");
        String id = format.format(1); // Default
        try {
            Statement stmt = dbConnection.createStatement();
            try {
                String query = dbp.getQuery(queryName, substitutions);
                ResultSet rs = executeQuery(stmt, query);
                try {
                    if (rs.next()) {
                        String maxId = rs.getString(1);
                        if (maxId != null) {
                            int n = format.parse(maxId).intValue();
                            id = format.format(n + 1);
                        }
                    }
                } finally {
                    rs.close();
                }
            } finally {
                stmt.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Ignore exceptions and use default
        }
        return id;
    }

    /**
     * Utility method to perform an executeUpdate statement. Also
     * additional code to be added for each executeUpdate for
     * debugging purposes.
     **/
    protected int executeUpdate(String query) throws SQLException {
        if (query == null) throw new IllegalArgumentException("executeUpdate: null query");
        try {
            long startTime = 0;
            if (log != null)
                startTime = System.currentTimeMillis();
            int result = updateStmt.executeUpdate(query);
            if (log != null) {
                long endTime = System.currentTimeMillis();
                log.println((endTime - startTime) + " " + query);
            }
            return result;
        } catch (SQLException sqle) {
            System.err.println("SQLException query: " + query);
            if (log != null) {
                log.println("SQLException query: " + query);
                log.flush();
            }
            throw sqle;
        }
    }

    /**
     * Utility method to perform an executeQuery statement. Also
     * additional code to be added for each executeQuery for
     * debugging purposes.
     **/
    protected ResultSet executeQuery(Statement stmt, String query) throws SQLException {
        if (query == null) throw new IllegalArgumentException("executeQuery: null query");
        try {
            long startTime = 0;
            if (log != null)
                startTime = System.currentTimeMillis();
            ResultSet rs = stmt.executeQuery(query);
            if (log != null) {
                long endTime = System.currentTimeMillis();
                log.println((endTime - startTime) + " " + query);
            }
            return rs;
        } catch (SQLException sqle) {
            System.err.println("SQLException query: " + query);
            if (log != null) {
                log.println("SQLException query: " + query);
                log.flush();
            }
            throw sqle;
        }
    }

    /**
     * Enables debugging
     **/
    public void setDebug(boolean newDebug) {
        debug = newDebug;
        dbp.setDebug(newDebug);
    }

    /**
     * Indicates that this is no longer needed. Closes the database
     * connection. Well-behaved users of this class will close when
     * done. Otherwise, the finalizer will close it.
     **/
    public synchronized void close() throws SQLException {
        if (dbConnection != null) {
            dbConnection.commit();
            dbConnection.close();
            dbConnection = null;
        }
    }

    protected void finalize() {
        try {
            if (dbConnection != null) close();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    /**
     * Quote a string for SQL. We don't double quotes that appear in
     * strings because we have no cases where such quotes occur.
     **/
    protected static String sqlQuote(String s) {
        if (s == null) return "null";
        int quoteIndex = s.indexOf('\'');
        while (quoteIndex >= 0) {
            s = s.substring(0, quoteIndex) + "''" + s.substring(quoteIndex + 1);
            quoteIndex = s.indexOf('\'', quoteIndex + 2);
        }
        return "'" + s + "'";
    }
}
