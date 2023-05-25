package plugin;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;
import com.mysql.cj.jdbc.MysqlDataSource;

import plugin.Const.BlockHistory;

public class Database {
	
	static MysqlDataSource dataSource = new MysqlConnectionPoolDataSource(); 
	public static String Author_text = "[" +  Main.plugin.getName() + "]";
	private StringBuilder query_history = new StringBuilder();
	private int query_history_index = 0;
	
	public Database(){
		 dataSource.setServerName("eu02-sql.pebblehost.com");
	     dataSource.setPortNumber(3306);
	     dataSource.setDatabaseName("customer_375920_portalplugin");
	     dataSource.setUser("customer_375920_portalplugin");
	     dataSource.setPassword("H#IJbtd5MMVxqOy@RhAo");
	     
	     try {
			testDataSource(dataSource);
			Main.plugin.getServer().getConsoleSender().sendMessage(Author_text + " Database: §aConnected successfully");
			
			//update_areaMap();
			//update_allowed_players();
			Main.db_connected = true;
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			Main.plugin.getServer().getConsoleSender().sendMessage(Author_text + " Database: §cCould not establish connection ");
			e.printStackTrace();
			Main.plugin.getServer().getConsoleSender().sendMessage(Author_text + " §C[WARNING] §fCould not establish database connection ");
			Main.plugin.getServer().getConsoleSender().sendMessage(Author_text + " §c[Disabling] §7home functions");
		}
	}
	
	public void add_player(Player p) {
		Date date = new Date(p.getFirstPlayed());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String firstPlayed = sdf.format(date);
		String query = "INSERT INTO players (Nickname, UUID, Joined) VALUES ('"+ p.getName() + "', '"+p.getUniqueId()+"', '"+firstPlayed+"');";
		
		Thread thread2 = new Thread(new Thread2(query));
		
	    thread2.start();
		
	}
	public void add_History(String pname, String Material, String Location, String Action, String content) {
		String value = "";
		if(query_history_index == 0)
		 value = "((SELECT ID FROM players p WHERE p.Nickname = '"+pname+"'),'"+Material+"','"+Location+"','"+Action+"', '"+content+"')";
		else
			 value = ",((SELECT ID FROM players p WHERE p.Nickname = '"+pname+"'),'"+Material+"','"+Location+"','"+Action+"', '"+content+"')";
		query_history.append(value);
		query_history_index++;
		if(query_history_index >= 10) {
		String query = "INSERT INTO player_block_history (player_id, block_data, location, action, content) VALUES " + query_history.toString() + ";";
		query_history = new StringBuilder();
		query_history_index = 0;
		
		 Thread thread2 = new Thread(new Thread2(query));
		 
	     thread2.start();
		}
		
	}
	
	public Connection connect() throws SQLException {
		 MysqlDataSource d = new MysqlDataSource();
		    d.setUser("root");
		    d.setPassword("");
		    d.setUrl("jdbc:mysql://localhost:3306/portalplugin");
		    d.setDatabaseName("portalplugin");
		    return d.getConnection();
	}
	
	public void testDataSource(DataSource dataSource) throws SQLException {
	    try (Connection conn = dataSource.getConnection()) {
	        if (!conn.isValid(1)) {
	            throw new SQLException("Could not establish database connection.");
	        }
	    }
	} 
	
	
	
	public int getCount(String query) throws SQLException {
		Connection conn = dataSource.getConnection();
		
		 PreparedStatement stmt  = conn.prepareStatement(query);
	
		 ResultSet data = stmt.executeQuery();
		 data.next();
		 return data.getInt(1);
		
	}
	public ResultSet getEventLog(int id) throws SQLException {

		 Connection conn = dataSource.getConnection();
		
		 String sql_query = "SELECT pa.*, ph.Name FROM home_event_log pa INNER JOIN player_homes ph ON ph.id = pa.by_home WHERE by_home = ? ORDER BY date DESC;";    // Query for loading all allowed players
		 PreparedStatement stmt  = conn.prepareStatement(sql_query);
		 stmt.setInt(1, id);
		 ResultSet data = stmt.executeQuery();
		 Functions.print("§3getEventLog() §3SQL §fRecived §aSuccesfully");
		 return data;
	}
	public String updateName(String uuid, String newPlayerName) throws SQLException {

		 Connection conn = dataSource.getConnection();
		
		 String sql_query = "SELECT Nickname FROM players WHERE UUID = ?";    // Query for loading all allowed players
		 PreparedStatement stmt  = conn.prepareStatement(sql_query);
		 stmt.setString(1, uuid);
		 ResultSet data = stmt.executeQuery();
		 Functions.print("§3getPlayerName §3SQL §fRecived §aSuccesfully");
		 data.next();

	    
	     Statement stmt2 = conn.createStatement();
	     stmt2.execute("UPDATE players SET Nickname = '" + newPlayerName + "' WHERE UUID = '" + uuid + "';");
	     Functions.print("§3UpdatePlayerName §3SQL  §aSuccesfull");
		 return data.getString(1);
	}
	
	
	
	public ResultSet getInfractions(String uuid) throws SQLException {

		 Connection conn = dataSource.getConnection();
		
		 String sql_query = "SELECT * FROM player_infractions WHERE UUID = ? ORDER BY date ASC";    // Query for loading all allowed players
		 PreparedStatement stmt  = conn.prepareStatement(sql_query);
		 stmt.setString(1, uuid);
		 ResultSet data = stmt.executeQuery();
		 Functions.print("§3getInfractions() §3SQL §fRecived §aSuccesfully");
		 return data;
	}
	public ResultSet getPlayerNames() throws SQLException {

		 Connection conn = dataSource.getConnection();
		
		 String sql_query = "SELECT Nickname FROM players";    // Query for loading all allowed players
		 PreparedStatement stmt  = conn.prepareStatement(sql_query);
		 ResultSet data = stmt.executeQuery();
		 Functions.print("§3getNames() §3SQL §fRecived §aSuccesfully");
		 return data;
	}
	
	public ResultSet getBlockHistory_Restore(String player_name, int minutes) throws SQLException {

		 Connection conn = dataSource.getConnection();
		
		 String sql_query = "SELECT * FROM player_block_history WHERE Date >= CURRENT_TIMESTAMP - INTERVAL "+minutes+" MINUTE AND player_id = (SELECT ID FROM players p WHERE p.Nickname = '"+player_name+"') AND Action = '"+BlockHistory.BREAK+"' ORDER BY ID DESC ";    // Query for loading all allowed players
		 Statement stmt  = conn.createStatement();
		// stmt.setInt(1, minutes);
		
		 
		 ResultSet data = stmt.executeQuery(sql_query);
		 Functions.print("§3SQL §fRecived §aSuccesfully");
		 return data;
	}
	public ResultSet getBlockHistory_RestorePlaced(String player_name, int minutes) throws SQLException {

		 Connection conn = dataSource.getConnection();
		
		 String sql_query = "SELECT * FROM player_block_history WHERE Date >= CURRENT_TIMESTAMP - INTERVAL "+minutes+" MINUTE AND player_id = (SELECT ID FROM players p WHERE p.Nickname = '"+player_name+"') AND Action = '"+BlockHistory.PLACE+"' ORDER BY Date DESC";    // Query for loading all allowed players
		 Statement stmt  = conn.createStatement();
		// stmt.setInt(1, minutes);
		
		 
		 ResultSet data = stmt.executeQuery(sql_query);
		 Functions.print("§3SQL §fRecived §aSuccesfully");
		 return data;
	}
	
	public ResultSet getEventLog(boolean oldest) throws SQLException {

		 Connection conn = dataSource.getConnection();
		 String sql_query;
		 if(oldest)
		  sql_query = "SELECT pa.*, ph.Name FROM home_event_log pa INNER JOIN player_homes ph ON ph.id = pa.by_home ORDER BY date ASC;";    // Query for loading all allowed players
		 else
			 sql_query = "SELECT pa.*, ph.Name FROM home_event_log pa INNER JOIN player_homes ph ON ph.id = pa.by_home ORDER BY date DESC;";    // Query for loading all allowed players
		 PreparedStatement stmt  = conn.prepareStatement(sql_query);
		 ResultSet data = stmt.executeQuery();
		 return data;
	}
	
	
	@SuppressWarnings("unchecked")
	public void update_allowed_players() throws SQLException {
		
		 Connection conn = dataSource.getConnection();
		
		 String sql_query = "SELECT pa.*, p.Nickname FROM player_allowed pa INNER JOIN players p ON p.ID = pa.player_id";    // Query for loading all allowed players
		 PreparedStatement stmt  = conn.prepareStatement(sql_query);
		 ResultSet data = stmt.executeQuery();
		 JSONObject allowed_players = new JSONObject(); 
		
		 
		 
		 while(data.next()) {
		 	Map<String, Object> map = new LinkedHashMap<String, Object>(5);
		    map.put("player_name", data.getString(7));
		    map.put("by_home", data.getInt(3));
	        map.put("can_lock", data.getBoolean(4));
	        map.put("can_add", data.getBoolean(5));
	        map.put("date", data.getString(6));
	        allowed_players.put(data.getInt(1), map);
		 }
		 
		 // Writing JSON allowed_players
		
		try {
			PrintWriter pw;
			pw = new PrintWriter(Main.plugin.getDataFolder() +  "/allowed_players.json");
			pw.write(allowed_players.toJSONString());
		    pw.flush();
		    pw.close();
		    Main.plugin.getServer().getConsoleSender().sendMessage(Author_text + " 'allowed_players.json': §aUpdate complete");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			Main.plugin.getServer().getConsoleSender().sendMessage(Author_text +  " 'allowed_players.json': §cFailed to update");
			
			e.printStackTrace();
		}
		
	    
	}
	
	@SuppressWarnings("unchecked")
	public void update_areaMap() throws SQLException {

		  Connection conn = dataSource.getConnection();
		  
		  //SELECT pa.*, ph.Name FROM home_event_log pa INNER JOIN player_homes ph ON ph.id = pa.by_home WHERE by_home = 128 ORDER BY created_date DESC;
		
		  String sql_query = "SELECT ph.*, p.Nickname FROM player_homes ph INNER JOIN players p ON p.ID = ph.Creator_ID";   // Query for loading all info with id and location 
		  PreparedStatement stmt  = conn.prepareStatement(sql_query);
		  ResultSet data = stmt.executeQuery();
		  JSONObject homes = new JSONObject(); 
		  
		  
		 
		  while(data.next()) {
			
			
			
		  	Map<String, Object> map = new LinkedHashMap<String, Object>(8);
		  	map.put("Name", data.getString(2));
		    map.put("Creator", data.getString(10));
	        map.put("Location", data.getString(4));
	        map.put("Radius", data.getShort(5));
	        map.put("Locked", data.getBoolean(6));
		  	map.put("Material_Icon", data.getString(7));
		  	map.put("Allowed_Players", data.getShort(8));
		  	map.put("Date", data.getString(9));
		  	
	        homes.put(data.getInt(1), map);
		      
		  }
		  // Writing JSON homes
		  
		  try {
			  PrintWriter pw = new PrintWriter(Main.plugin.getDataFolder() +  "/player_homes.json");
			  pw.write(homes.toJSONString());
		      pw.flush();
		      pw.close();
		      Main.plugin.getServer().getConsoleSender().sendMessage(Author_text + " 'player_homes.json': §aUpdate complete");
		  } catch (FileNotFoundException e) {
			  // TODO Auto-generated catch block
			  Main.plugin.getServer().getConsoleSender().sendMessage(Author_text +  " 'player_homes.json': §cFailed to update");
			  e.printStackTrace();
		  }
			 
		      
	     
		  
		  
		 
	}
	
	public int leave_home(String home_id, String player_name) throws SQLException {
		String query = "SELECT ID FROM player_allowed WHERE by_home = ? AND player_id = (SELECT p.ID FROM players p WHERE p.Nickname = ?);";
		
	    
		 Connection conn = dataSource.getConnection();
		 PreparedStatement stmt  = conn.prepareStatement(query);
		 stmt.setString(1, home_id);
		 stmt.setString(2, player_name);
		 ResultSet data = stmt.executeQuery();
		 data.next();
		 int key = data.getInt(1);
		 Functions.print("§3SQL §aSuccesfull §7 Recived key§6 " + key);
		 Statement stmt2 = conn.createStatement();
	     stmt2.execute("DELETE FROM player_allowed WHERE ID = " + key);
	     Functions.print("§3SQL §aSuccesfull §7- Leave Home");
	     return key;
	     
	}
	public void execute(String query) throws SQLException {
		
	     Connection conn = dataSource.getConnection();
	     
	     Statement stmt = conn.createStatement();
	     stmt.execute(query);
	    
	     
	}
	public void execute_prepare(String query) throws SQLException {
		
	     Connection conn = dataSource.getConnection();
	     
	     PreparedStatement stmt = conn.prepareStatement(query);
	     stmt.execute(query);
	     Functions.print("§3SQL §aSuccesfull");
	     
	     
	}
public int update_home_name (int id, String name) throws SQLException {
		
		// Query for all the player that is added to home_id
		String sql_query = "UPDATE `player_homes` SET `Name` = ? WHERE `player_homes`.`ID` = ?";
		Connection conn = dataSource.getConnection();
		
		// Creating prepared statement with our query
		PreparedStatement stmt  = conn.prepareStatement(sql_query);
		  
		//Setting values
	    stmt.setString(1, name);
	    stmt.setInt(2, id);
	    
		int res = stmt.executeUpdate();
	
		if(res == 0)
			return Const.ERROR;
		
		
		return Const.PASSED;
	}
	
	public int update_perms_add (int id, boolean can_add) throws SQLException {
		//No validation check Error-Check  before function is called
		
		// Update query for can_add
		String sql_query = "UPDATE `player_allowed` SET `can_add` = ? WHERE `player_allowed`.`ID` = ?";
		Connection conn = dataSource.getConnection();
		
		// Creating prepared statement with our query
		PreparedStatement stmt  = conn.prepareStatement(sql_query);
		  
		// Setting values
	    stmt.setBoolean(1, can_add);
	    stmt.setInt(2, id);
	    
		int res = stmt.executeUpdate();
	
		if(res == 0)
			return Const.ERROR;
		
		
		return Const.PASSED;
	}
	
	public int update_perms_lock (int id, boolean can_lock) throws SQLException {
		// No validation check - Error-Check  before function is called
		
		
		// Update query for can_lock 
		String sql_query = "UPDATE `player_allowed` SET `can_lock` = ? WHERE `player_allowed`.`ID` = ?";
		Connection conn = dataSource.getConnection();
		
		// Creating prepared statement with our query
		PreparedStatement stmt  = conn.prepareStatement(sql_query);
		  
		// Setting values
	    stmt.setBoolean(1, can_lock);
	    stmt.setInt(2, id);
	    
		int res = stmt.executeUpdate();
	
		if(res == 0)
			return Const.ERROR;
		
		
		return Const.PASSED;
	}
	
	
	public int update_home_locked (int id, boolean locked) throws SQLException {
		
		// Query for all the player that is added to home_id
		String sql_query = "UPDATE `player_homes` SET `Locked` = ? WHERE `player_homes`.`ID` = ?";
		Connection conn = dataSource.getConnection();
		
		// Creating prepared statement with our query
		PreparedStatement stmt  = conn.prepareStatement(sql_query);
		  
		// Setting values
	    stmt.setBoolean(1, locked);
	    stmt.setInt(2, id);
	    
		int res = stmt.executeUpdate();
	
		if(res == 0)
			return Const.ERROR;
		
		
		return Const.PASSED;
	}
	
	public int update_home_radius(int id, short radius) throws SQLException {
		
		// Query for all the player that is added to home_id
		String sql_query = "UPDATE `player_homes` SET `Radius` = ? WHERE `player_homes`.`ID` = ?";
		Connection conn = dataSource.getConnection();
		
		// Creating prepared statement with our query
		PreparedStatement stmt  = conn.prepareStatement(sql_query);
		  
		//Setting values
	    stmt.setShort(1, radius);
	    stmt.setInt(2, id);
	    
		int res = stmt.executeUpdate();
	
		if(res == 0)
			return Const.ERROR;
		
		
		return Const.PASSED;
	}
	
	
	public int remove_home(int id) {
		
		// Query for all the player that is added to home_id
		String sql_query = "DELETE FROM `player_homes` WHERE `player_homes`.`ID` = " + id;
		Thread thread1 = new Thread(new Thread1(sql_query));
        thread1.start();
		
		
		
		return Const.PASSED;
	}
	
	
	public int add_event_logg(String player_name, String Action, short by_home) throws SQLException {
		
		// Query for all the player that is added to home_id
		String sql_query = "insert into `home_event_log` values(?,?,?,?,DEFAULT,DEFAULT,DEFAULT,DEFAULT,)";
		Connection conn = dataSource.getConnection();
		
		// Creating prepared statement with our query
		PreparedStatement stmt  = conn.prepareStatement(sql_query);
		  
		//Setting values
	    stmt.setNull(1, Types.INTEGER);
	    stmt.setString(2, player_name);
        stmt.setString(3, Action);
        stmt.setShort(4, by_home);
		int res = stmt.executeUpdate();
	
		if(res == 0)
			return Const.ERROR;
		
		
		return Const.PASSED;
	}
	
	public int add_home(String name, String pname, Location loc, Short rad, String material) throws SQLException {
		
		// Query for all the player that is added to home_id
		String sql_query = "INSERT INTO player_homes values(?,?,(SELECT ID FROM players p WHERE p.Nickname = ?),?,?,?,?,?,DEFAULT)";
		Connection conn = dataSource.getConnection();
		
		
		
		// Creating prepared statement with our query
		PreparedStatement stmt  = conn.prepareStatement(sql_query, PreparedStatement.RETURN_GENERATED_KEYS);
		  
		//Setting values
	    stmt.setNull(1, Types.INTEGER);
	    stmt.setString(2, name);
        stmt.setString(3, pname);
        stmt.setString(4, convertLoc(loc));
        stmt.setShort(5, rad);
        stmt.setBoolean(6, false);
        stmt.setString(7, material);
        stmt.setInt(8, 0);
		stmt.execute();
		ResultSet rs = stmt.getGeneratedKeys();
		rs.next();
		return rs.getInt(1);
	}
	public String convertLoc(Location loc) {
			
			String result = loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
			
			return result;
		}
	
	
	public int getTotalPlayerHomes(String pname) throws SQLException {
		
		Connection conn = dataSource.getConnection();
		String sql_query = "SELECT COUNT(*) FROM player_homes WHERE Creator_ID = ?";
		PreparedStatement stmt  = conn.prepareStatement(sql_query);
		stmt.setString(1, pname);
		ResultSet data = stmt.executeQuery();
		
		data.next();
		
		return data.getInt(1);
	}
	
	public List<String> get_added_players(int home_id) throws SQLException {
		//NO CHECK VAILDATION IF HOME_ID EXISTS OR IF ALLOWED PLAYERS EXIST

		List<String> list = new ArrayList<>();
		
		
		Connection conn = dataSource.getConnection();
		
		// Query for all the player that is added to home_id
		String sql_query = "SELECT player_name FROM `player_allowed` WHERE `by_home` = ?";
		// Creating prepared statement with our query
		PreparedStatement stmt  = conn.prepareStatement(sql_query);
		stmt.setInt(1, home_id);
		ResultSet data = stmt.executeQuery();
		
		// Looping results and adding it to the list 
		while(data.next())
			list.add(data.getString(1));
		
		
		return list;
		
	}
	
	public int allow_Player(int home_id, String p) throws SQLException {
		  //NO CHECK VAILDATION IF HOME_ID EXISTS
		  
		  Connection conn = dataSource.getConnection();
		  
		  // Adding player to house
		  String sql_query = "INSERT INTO player_allowed values(?,(SELECT ID FROM players p WHERE p.Nickname = ?),?,?,?,DEFAULT)";  // Updating the already existing string sql
		  PreparedStatement stmt  = conn.prepareStatement(sql_query, PreparedStatement.RETURN_GENERATED_KEYS);
		  
		  //Values
		  stmt.setNull(1, Types.INTEGER);
		  stmt.setString(2, p);
	      stmt.setInt(3, home_id);
	      stmt.setBoolean(4, false);
	      stmt.setBoolean(5, false);
	    
	      int affectedRows = stmt.executeUpdate();

	        if (affectedRows == 0) {
	            throw new SQLException("Inserting into player_allowed failed, no rows affected.");
	        }

	        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
	            if (generatedKeys.next()) {
	                return generatedKeys.getInt(1);
	            }
	            else {
	                throw new SQLException("Inserting into player_allowed failed, no ID obtained.");
	            }
	        }
		
	}
	
	
	public int erasePlayer_Homes() throws SQLException {
		
		  Connection conn = dataSource.getConnection();
		  String query = "DELETE FROM player_homes;"; // For Player_homes (7 values)
		  String query2 = "ALTER TABLE player_homes AUTO_INCREMENT = 1;"; 
		  PreparedStatement stmt  = conn.prepareStatement(query);
		  int rows = stmt.executeUpdate();
		  System.out.println(rows + "rows Deleted from player_homes");
		  stmt  = conn.prepareStatement(query2);
		  stmt.executeUpdate();
		  return rows;
		
	}
	
	public int erasePlayer_Allowed() throws SQLException {
		
		  Connection conn = dataSource.getConnection();
		  String query = "DELETE FROM player_homes;"; // For Player_homes (7 values)
		  String query2 = "ALTER TABLE player_homes AUTO_INCREMENT = 1;";  // For Player_homes (7 values)
		  PreparedStatement stmt  = conn.prepareStatement(query);
		  int rows = stmt.executeUpdate();
		  System.out.println(rows + "rows Deleted from player_allowed");
		  stmt  = conn.prepareStatement(query2);
		  stmt.executeUpdate();
		  return rows;
		
	}
	public int eraseEvent_Logg() throws SQLException {
		
		  Connection conn = dataSource.getConnection();
		  String query = "TRUNCATE TABLE home_event_log"; // For Player_homes (7 values)
		  PreparedStatement stmt  = conn.prepareStatement(query);
		  int rows = stmt.executeUpdate();
		  Functions.print(rows + "rows Deleted from home_event_log");
		 
		  return rows;
	}
	
	public void configPlayersToSQL()  {
			
		System.out.println("----Adding Allowed Players from Config file to SQL----");
		List<String> names = Main.config.getStringList("Homes.Names");
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("FAILED TO CONNECT TO DB");
			return;
		}
		  
		List<String> plist;
		int pSize;
		String query1 = "INSERT INTO player_allowed SET by_home = ?, player_id = (SELECT ID FROM players pl WHERE pl.Nickname = ?)";  //For player_allowed (5 values)
		int failed_rows = 0;
		String query2 = "SELECT ID FROM player_homes WHERE Name = ?"; // query for finding the ID by name 
		
		PreparedStatement stmt;
		for(String n : names) {
			try {
			plist = Main.config.getStringList("Homes." + n + ".Players");
			pSize = plist.size();
			
			if(plist.size() > 1) {
				
				//Find the id of the by name that is uniqe
				  stmt  = conn.prepareStatement(query2);
				  stmt.setString(1, n);
				  System.out.println("Getting ID for '"+ n + "'");
				  ResultSet data = stmt.executeQuery();
				  data.next();
				  int ID = data.getInt(1);
				  
				  stmt  = conn.prepareStatement(query1);
			      stmt.setInt(1, ID);
			      for(int i = 1; i < pSize; i++) {
			    	  	System.out.println("Trying to allow" + plist.get(i) + "  to -> " + n);
	        		 	stmt.setString(2, plist.get(i));;
	        		 	stmt.executeUpdate();
	      	        	// Display the records inserted
	      	        	System.out.println(plist.get(i) + " allowed to -> " + n);
	      	        	
	        	}
			}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Functions.print("1 row §cfailed§f because: "+ e.getMessage());
				Functions.print("§6Skiping §frow - Name: " + n);
				
				failed_rows++;
				continue;
			}
		}
		if(failed_rows == 0)
			Functions.print("---All rows addes §csuccesfully§f---");
			else
				Functions.print("--- " + failed_rows + " Rows §cFailed§f "+ " ---");
				
				
	}
	public void configToSQL() {
		 
		Functions.print("----Adding Allowed Players from Config file to SQL----");
		List<String> names = Main.config.getStringList("Homes.Names");

		Connection conn = null;
		try {
			conn = dataSource.getConnection();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("FAILED TO CONNECT TO DB");
			return;
		}
		String query = "INSERT INTO player_homes SET Name = ?, Creator_ID = (SELECT ID FROM players pl WHERE pl.Nickname = ?), Location = ?, Radius = ?, Material_Icon = ?"; // For Player_homes (7 values)
		//
		
		double x;
		double y;
		double z;
		String worldname;
		Location tloc;
		String location;
		String pname;
		short rad;
		int failed_rows = 0;
		for(String n : names) {
			try {
			x =  Main.config.getDouble("Homes." + n + ".Location.x");
			y =  Main.config.getDouble("Homes." +n + ".Location.y"); // Spawn above the block
			z =  Main.config.getDouble("Homes." +n + ".Location.z");
			worldname =  Main.config.getString("Homes." + n + ".Location.world");
			tloc = new Location(Bukkit.getWorld(worldname), x,y,z);
			location = convertLoc(tloc); // Converts the location to a specif format for efficiency
			pname = Main.config.getStringList("Homes." + n + ".Players").get(0);
			rad = Short.parseShort(Main.config.getString("Homes." + n + ".Radius"));
			if(n.length() > 20) {
				int size = n.length() - 20;
				n = n.substring(0, n.length() - size);
			}
			 int rmd = (int)(Math.random() * (Functions.materials.length));
				
			 String mname = Functions.materials[rmd].name();
			// Prepare Statement
	        PreparedStatement stmt  = conn.prepareStatement(query);
	      
	       //
	        stmt.setString(1, n);;
	        stmt.setString(2, pname);
	        stmt.setString(3, location);
	        stmt.setShort(4, rad);
	        stmt.setString(5, mname);
	        
	        stmt.executeUpdate();
	        
	        System.out.println("Home: " + n  + " ----> players_homes");
			}catch(SQLException e) {
				// TODO Auto-generated catch block
				
				Functions.print("1 row §cfailed§f because: "+ e.getMessage());
				Functions.print("§6Skiping§f row - Name: " + n);
				failed_rows++;
				continue;
			}
			
		}
		if(failed_rows == 0)
			Functions.print("---All rows addes §asuccesfully§f---");
		else
			Functions.print("--- " + failed_rows + " Rows §cFailed§f "+ " ---");
			
		 
	        	
	        	
			
	}
	public  class Thread1 implements Runnable {
		
		String query = "";
		public Thread1(String query) {
			this.query = query;
		}
	
		@Override
        public synchronized  void run() {
			 Connection conn;
			try {
				conn = dataSource.getConnection();
				Statement stmt = conn.createStatement();
				stmt.execute(query);
				 Functions.print("§3SQL §aSuccesfull");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				Functions.ERROR2("Database.java.Thread1.Run", null, e.toString(), e.getStackTrace());
				e.printStackTrace();
			}
		     
		   
        }
    }
	
public  class Thread2 implements Runnable {
		
		String query = "";
		public Thread2(String query) {
			this.query = query;
		}
	
		@Override
        public synchronized  void run() {
			 Connection conn;
			try {
				conn = dataSource.getConnection();
				Statement stmt = conn.createStatement();
				
				stmt.execute(query);
				 Functions.print("§3SQL §aSuccesfull");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				Functions.ERROR2("Databse.java.Thread2.Run", null, e.toString(), e.getStackTrace());
				
				e.printStackTrace();
			}
		     
		   
        }
    }


	
}
