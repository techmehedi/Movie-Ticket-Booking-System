package MovieTicketBookingSystem;

import java.sql.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.InputStream;


public class DatabaseOperation {
    static final String DB_URL = "jdbc:mysql://localhost:3306";
    static String USER;
    static String PASS;
    
    static {
        try (InputStream input = DatabaseOperation.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new FileNotFoundException("config.properties file not found in the classpath");
            }
            Properties props = new Properties();
            props.load(input);
            USER = props.getProperty("DB_USER");
            PASS = props.getProperty("DB_PASS");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public Connection connectToDatabase(){
        Connection conn = null;
        try{
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
			Statement statement = conn.createStatement();
			statement.executeUpdate("USE MovieTicket");
        }catch (Exception e){
            System.out.println(e);
        }
        return conn;
    }
    public void CreateTable(String tableName, String... tableColumns) {
    	try {
	    	Connection conn = connectToDatabase();
	    	Statement statement = conn.createStatement();
	    	boolean TableDoesNotExist = statement.execute("SELECT * FROM information_schema.tables WHERE Table_schema = 'movieticket' AND Table_name = '" + tableName + "' ");
	    	if(TableDoesNotExist) {
		    	StringBuilder sBuilder = new StringBuilder();
		    	sBuilder.append("CREATE TABLE " + tableName + " (");
		    	for (String columns : tableColumns) {
		    		sBuilder.append(columns + ", ");
		    	}
		    	sBuilder.setLength(sBuilder.length() - 2);
		    	sBuilder.append(" );");
		    	String sqlStatement = sBuilder.toString();
		    	statement.executeUpdate(sqlStatement);
		    	System.out.println("Creating Table " + tableName);
	    	}
    	} catch (SQLException e){
    		e.printStackTrace();
    	}
    }
    
    public void initializeDatabase() {
    	try{
    		Connection conn = connectToDatabase();
 			Statement statement = conn.createStatement();
 			statement.executeUpdate("CREATE DATABASE IF NOT EXISTS MovieTicket;");
 			statement.executeUpdate("USE MovieTicket");
 			CreateTable("Users", "UserID INT AUTO_INCREMENT", 
 					"Username VARCHAR(255)" , 
 					"Password VARCHAR(255)", 
 					"Name VARCHAR(255)", 
 					"Phone VARCHAR(15)", 
 					"Address VARCHAR(255)", 
 					"PRIMARY KEY (UserID)");
 	 			CreateTable("Admins", "AdminID INT AUTO_INCREMENT", 
 	 					"Username VARCHAR(255)", 
 	 					"Password VARCHAR(255)", 
 	 					"PRIMARY KEY (AdminID)");
 	 			CreateTable("Theaters", "TheaterID INT AUTO_INCREMENT", 
 	 					"Location VARCHAR(255)", 
 	 					"SeatingCapacity INT", 
 	 					"PRIMARY KEY (TheaterID)");
 	 			CreateTable("Movies", "MovieID INT AUTO_INCREMENT",
 	 					"Title VARCHAR(255)",
 	 					"Genre VARCHAR(255)",
 	 					"Rating DECIMAL(3,2)",
 	 					"Duration INT",
 	 					"Synopsis TEXT",
 	 					"PRIMARY KEY (MovieID)");
 	 			CreateTable("Showtimes", "ShowtimeID INT AUTO_INCREMENT",
 	 					"MovieID INT",
 	 					"TheaterID INT",
 	 					"Showtime DATETIME",
 	 					"PRIMARY KEY (ShowtimeID)",
 	 					"FOREIGN KEY (MovieID) REFERENCES Movies(MovieID)",
 	 					"FOREIGN KEY (TheaterID) REFERENCES Theaters(TheaterID)");
 	 			CreateTable("Bookings", "BookingID INT AUTO_INCREMENT",
 	 					"UserID INT",
 	 					"ShowtimeID INT",
 	 					"SelectedSeats INT",
 	 					"PaymentStatus BOOLEAN",
 	 					"PRIMARY KEY (BookingID)",
 	 					"FOREIGN KEY (UserID) REFERENCES Users(UserID)",
 	 					"FOREIGN KEY (ShowtimeID) REFERENCES Showtimes(ShowtimeID)");
    	} catch (SQLException e) {
    		e.printStackTrace();
    	}
    }
    
    public int executeUpdate(String sql,Object[] values){
        int rowsAffected = 0;
        try(Connection conn = connectToDatabase();
            PreparedStatement ps = conn.prepareStatement(sql)){
                for(int i = 0; i < values.length; i++){
                    ps.setObject(i+1, values[i]);
                }
                rowsAffected = ps.executeUpdate();
            }catch (SQLException e){
                e.printStackTrace();
            }
            return rowsAffected;
    }

    public List<Map<String, Object>> getRecords(String sql) {
        List<Map<String, Object>> records = new ArrayList<>();
        try (Connection conn = connectToDatabase();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery()) {
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(rsmd.getColumnName(i), rs.getObject(i));
                }
                records.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    public int getSeatingCapacity(String sql,int parameter){
        int record = 0;
        try(Connection conn = connectToDatabase();
            PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, parameter);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                record = rs.getInt("SeatingCapacity");
            }

            }catch (SQLException e){
                e.printStackTrace();
            }
        return record;
    }


    public ArrayList<Integer> getBookedSeats(int showtimeID){
        String sql = "SELECT SelectedSeats from bookings where ShowtimeID = ?";
        ArrayList<Integer> bookedSeats = new ArrayList<>();

        try(Connection conn = connectToDatabase();
            PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, showtimeID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                bookedSeats.add(rs.getInt("SelectedSeats"));
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return bookedSeats;
    }

    public ArrayList<Integer> getShowtimeDetailsForUser(int userID,int showtimeID){
        String sql = "SELECT SelectedSeats from bookings where ShowtimeID = ?";
        ArrayList<Integer> bookedSeats = new ArrayList<>();

        try(Connection conn = connectToDatabase();
            PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, showtimeID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                bookedSeats.add(rs.getInt("SelectedSeats"));
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return bookedSeats;
    }

    public void getAllBookingsForUser(int userID){
        String sql = "SELECT * from bookings where userid =?";
        try(Connection conn = connectToDatabase();
            PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, userID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                System.out.println("----- Booking Details -----");
                System.out.println("Booking ID: "+rs.getInt("BookingID"));
                System.out.println("Showtime ID: "+rs.getInt("ShowtimeID"));
                System.out.println("Seat number: "+rs.getInt("SelectedSeats"));
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void getShowtimeDetails(String sql,int showtimeID){
        try(Connection conn = connectToDatabase();
        PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, showtimeID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int showtimeID_sql = rs.getInt("ShowtimeID");
                String title = rs.getString("Title");
                int duration = rs.getInt("Duration");
                Time showtime = rs.getTime("Showtime");
                System.out.println("ShowtimeID: " + showtimeID_sql + ", Title: " + title + ", Duration: " + duration + ", Showtime: " + showtime);
            }
        }catch (SQLException e){
            e.printStackTrace();
        }

    }

    public int removeBooking(int bookingID){
        //enter any booking id and works
        //future scope : to verify if booking is users or not
        String sql = "DELETE from bookings where BookingID =?";
        int rowsAffected = 0;
        try(Connection conn = connectToDatabase();
            PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, bookingID);
            rowsAffected = ps.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }
        return rowsAffected;
    }

    public String validatePass(String sql,String username){
        String pass = "";
        try(Connection conn = connectToDatabase();
            PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                pass = rs.getString("password");
            }

        }catch (SQLException e){
                e.printStackTrace();
        }
        return pass;
    }

    public int fetchUserID(String sql , String username){
        int userID = 0;
        try(Connection conn = connectToDatabase();
            PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                userID = rs.getInt("UserID");
            }

        }catch (SQLException e){
                e.printStackTrace();
        }

        return userID;
    }
    
    public void deleteTableEntry(String sql) {
    	try(Connection conn = connectToDatabase();
                PreparedStatement ps = conn.prepareStatement(sql)){
    			ps.executeUpdate();
    	} catch (SQLException e) {
			e.printStackTrace();
		}
    }
}