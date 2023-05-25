package plugin;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import plugin.Const.INV_TITLE;
import plugin.Const.ITEM_TITLE;
import plugin.Const.Infraction;
import plugin.Const.LogAction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;

public class Functions {
	

	//INSERT INTO `player_homes` (`ID`, `Name`, `Creator`, `Location`, `Radius`, `Allowed_players_ID`, `Locked`, `Date`) VALUES (NULL, 'test', 'fsdfsffs', 'aaaaa', '5', NULL, '0', CURRENT_TIMESTAMP)
	
	private Database db;
	public static boolean debug = true;
	public List<String> player_wait = new ArrayList<>();
	public Map<String, List<Long>> spam_click = new HashMap<>();
	public Map<String, List<Long>> spam_click_block = new HashMap<>();
	public static Material[] materials = {Material.WHITE_TERRACOTTA, Material.ORANGE_TERRACOTTA, Material.MAGENTA_TERRACOTTA, Material.LIGHT_BLUE_TERRACOTTA,
								   Material.YELLOW_TERRACOTTA, Material.LIME_TERRACOTTA, Material.PINK_TERRACOTTA, Material.LIGHT_GRAY_TERRACOTTA,Material.BROWN_TERRACOTTA};
								   
	public static String[] materials_name = {"White","Orange","Magenta","Light Blue","Yellow","Lime","Pink","Light Gray","Brown"};
	public Object obj;
	public JSONObject homes;
	public Object obj2;
	public JSONObject allowed_players;
	
	public Functions(Database db) throws FileNotFoundException, IOException, ParseException {
		obj = getJsonFile("player_homes.json");
	    homes = (JSONObject) obj;
	    obj2 = getJsonFile("allowed_players.json");
	    allowed_players = (JSONObject) obj2;
		this.db = db;
	    
	}	
	
	
	public void printL(int line)
	{
		print(  "§a Passed §fline: §c" + line);
	}	
	public String convertLoc(Location loc) {
		
		String result = loc.getWorld().getName() + "," +  loc.getBlockX() + "," +  loc.getBlockY() + "," +  loc.getBlockZ();
		
		return result;
	}
	
	public Location convertLoc(String location) {
		location = location.replaceAll(" ", "");
		Location loc;
		String[] values = location.split(",");
		
		loc = new Location(Bukkit.getWorld(values[0]), Double.parseDouble(values[1]), Double.parseDouble(values[2]),Double.parseDouble(values[3]));
		return loc;
	}
	
	public void updatefiles() {
		 obj = getJsonFile("player_homes.json");
		 homes = (JSONObject) obj;

		 obj2 = getJsonFile("allowed_players.json");
		 allowed_players = (JSONObject) obj2;
		 print("Json files updated");
	}
	
	public String intefears(Player p, String id, Location loc, int radplus, boolean sethome) {
		
		
		
		Map<?, ?> data = ((Map<?, ?>)homes.get(id));
		Location tloc = this.convertLoc((String)data.get("Location"));
		int rad = ((Number)data.get("Radius")).intValue();
		String name  = data.get("Name").toString();
		String pname = data.get("Creator").toString();
		List<String> players = new ArrayList<>();
		if(!loc.getWorld().getName().equals(tloc.getWorld().getName())) return "";
		int allowed = Integer.parseInt(data.get("Allowed_Players").toString());
		if(allowed != 0) {
		for(Object key : allowed_players.keySet()) {
			Map<?, ?> ap_data = ((Map<?, ?>)allowed_players.get(key));
			boolean by_home = ap_data.get("by_home").toString().equals(id);
			if(by_home) {
			players.add(ap_data.get("player_name").toString());
			if(allowed == players.size()) break; // Avoid more loops than nessecray
				}
			}
		}
		
		players.add(pname);	
		if ( loc.distance(tloc)  < rad + radplus ){
			if(sethome)
				return pname;
			for(String s : players) {
				if(s.equalsIgnoreCase(p.getName()) || p.isOp()) {
					
					if(p.isOp()) {
						 if(Main.config.getStringList("Homes.debug").contains(p.getName())) {
							 	StringBuilder msg = new StringBuilder();
							 	
								msg.append("§3->§f"); msg.append(pname);
								msg.append(" §3Home: §f"); 
								msg.append(name); 
								msg.append(" §8[§c");
								msg.append(rad); msg.append("§8]");
								msg.append(" §3ID: §f"); 
								msg.append(id); 
								
								p.sendMessage(msg.toString());
								return pname;
						 }
					}
					
					return "";
				}
			}
		
    		return pname; // Returns that the player should not be able to do anything
		}else
			return "";
		
		}
		public Inventory showMaterials(String id) {
			int Size = materials.length;
			
			if (Size <= 9)  Size = 9;
        	else if (Size <= 18) Size= 18;
        	else if (Size <= 27) Size = 27;
        	else if (Size <= 36) Size = 36;
			Inventory inv = Bukkit.createInventory(null, Size, INV_TITLE.CHANGE_ICON);
			
			for(int i = 0; i < materials.length; i++) {
				ItemStack is = new ItemStack(materials[i]);
				ItemMeta im = is.getItemMeta();
				im.setDisplayName("§f" + materials_name[i]);
				List<String> listLore = new ArrayList<>();
				listLore.add("§8ID:" + id);
				im.setLore(listLore);
				is.setItemMeta(im);
				inv.addItem(is);
				
			}
			
			return inv;
		}
		@SuppressWarnings("deprecation")
		public Inventory showAllPlayers(String id,String player_name, boolean edit_inv, boolean disallow_inv) {

			
			int Size = 1;
			List<ItemStack> items = new ArrayList<>();
		
         	for (Object key : allowed_players.keySet()) {
         		Map<?, ?> data = ((Map<?, ?>)allowed_players.get(key));
         		if(data.get("player_name").toString().equalsIgnoreCase(player_name) && (edit_inv || disallow_inv)) continue;
         		Object by_home =data.get("by_home");
         		
         		if(by_home.toString().equals(id)) {
         			String pname =  data.get("player_name").toString();
         			ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
         			SkullMeta meta = (SkullMeta) item.getItemMeta();
         			
         			meta.setOwningPlayer(Bukkit.getOfflinePlayer(pname));
                    item.setItemMeta(meta);
                     
        			ItemMeta item_meta = item.getItemMeta();
        			List<String> listLore = new ArrayList<>();
        			if(!disallow_inv) {
         			
    		 		listLore.add("§7Can Lock: " + YesOrNo((boolean) data.get("can_lock")));
    				listLore.add("§7Can Add: " + YesOrNo((boolean) data.get("can_add")));
    				
        			}else {
        				listLore.add("§aAllowed");
        			}
        			
        			listLore.add("§8ID:" + key);
        			item_meta.setLore(listLore);
    				item_meta.setDisplayName("§6" + data.get("player_name"));
    				
         			item.setItemMeta(item_meta);
         			items.add(item);
         			Size++;
         		}
         	}
         	if(Size == 1) {print("§3@Func.§cShowAllPlayers §f Inventory is §cnull §7 Returning.. Issued by §3" + player_name) ;return null; }
        	
        	else if (Size <= 9)  Size = 9;
        	else if (Size <= 18) Size= 18;
        	else if (Size <= 27) Size = 27;
        	else if (Size <= 36) Size = 36;
         	
         	String title = "";
         	if(edit_inv) 
         		title = INV_TITLE.EDIT_PERMISSONS;
         	else if(disallow_inv)
         		title = INV_TITLE.DISALLOW_PLAYER;
         	else
         		title = INV_TITLE.SHOWING_PLAYERS;
         	
        	Inventory inv = Bukkit.createInventory(null, Size, title);
        	ItemStack item_return = new ItemStack(Material.COMPASS, 1); 
			ItemMeta im_return = item_return.getItemMeta();
			im_return.setDisplayName("§CReturn To Menu"); //Must be the same as the ClickInvEvent is listening for (Main Class)
			item_return.setItemMeta(im_return);
			inv.setItem(0, item_return); //Puting it in first slot
         	int index = 1;
    		for (ItemStack i : items) {
    			inv.setItem(index, i);
    			index++;
    		}
         	
		
		
			return inv;
		}
		
		public boolean toManyHouses(String player_name) {
			Object obj = getJsonFile("player_homes.json");
	   
	        JSONObject jo = (JSONObject) obj;      // typecasting obj to JSONObject
	    	short Size = 1; // This value we will increase when we loop through both the players owns houses. and the houses that the players is eventuelly allowed to. 
	   
	    
	        for (Object key : jo.keySet()) {
	        	Map<?, ?> data = ((Map<?, ?>)jo.get(key));
	        	String creator = (String) data.get("Creator");
	        	if(creator.equals(player_name)) {
	        		
	        		Size++;
	        	}
		    }
	        	
	        	Object obj2 = getJsonFile("allowed_players.json");
	         	JSONObject jo2 = (JSONObject) obj2;
	         	for (Object key : jo2.keySet()) {
	         		Map<?, ?> data = ((Map<?, ?>)jo2.get(key));
	         		String pname = (String) data.get("player_name");
	         		
	         		if(pname.equals(player_name)) {
	         			
	         					
	         					Size++;
         					}
	         			
         				}
	       

		    	if(Size > 35) {
		    		return true;
		    	}else
		    		return false;
		}
		
		public Inventory inventory_Create(String title, String player_name,boolean Show_Homes, boolean Show_Added, boolean plockInv, boolean pEditInv, boolean return_item) {
			
			Object obj = getJsonFile("player_homes.json");
	     	if (obj == null) return null;
	     	if (plockInv || pEditInv)
	     		Show_Added = true;
	   
	        JSONObject jo = (JSONObject) obj;      // typecasting obj to JSONObject
	    	short Size = 0; // This value we will increase when we loop through both the players owns houses. and the houses that the players is eventuelly allowed to. 
	    	if(return_item)
	    		Size = 1; //If the return item should be added we have to count it
	    	
	        List<Home> homes = new ArrayList<>();
	        if(Show_Homes) {
	        for (Object key : jo.keySet()) {
	        	Map<?, ?> data = ((Map<?, ?>)jo.get(key));
	        	String creator = (String) data.get("Creator");
	        	if(creator.equals(player_name)) {
	        		
	        		if(pEditInv && !title.contains(INV_TITLE.ALLOW_PLAYER)) {
     					short allowed_player = Short.parseShort( data.get("Allowed_Players").toString());
     					if(allowed_player == 0) {
     						
     						continue;
     					}
     				}
	        		homes.add(new Home(key, data.get("Name"), data.get("Creator"), data.get("Radius"), (boolean) data.get("Locked"), data.get("Material_Icon"),  data.get("Allowed_Players")));
	        		Size++;
	        	}
		    }
	        }
	        if(Show_Added ) {
	        	
	        	Object obj2 = getJsonFile("allowed_players.json");
	         	JSONObject jo2 = (JSONObject) obj2;
	         	for (Object key : jo2.keySet()) {
	         		Map<?, ?> data = ((Map<?, ?>)jo2.get(key));
	         		String pname = (String) data.get("player_name");
	         		
	         		if(pname.equals(player_name)) {
	         			
	         			Map<?, ?> data2 = ((Map<?, ?>)jo.get(data.get("by_home").toString()));
	         			boolean add = true;
	         			if( plockInv || pEditInv) {
	         			boolean can_add = (boolean) data.get("can_add");
	         			boolean can_lock = (boolean) data.get("can_lock");
	         			
	         			if(plockInv && !(can_lock) ||pEditInv && !(can_add) ||title.contains(ITEM_TITLE.EDIT_PERMISSONS) ) // CHecking permisons.
	         				add = false;
	         			}
	         			
	         			if(add) {
	         				
	         				if(pEditInv  && !title.contains(INV_TITLE.ALLOW_PLAYER)  ) {
	         					short allowed_player = Short.parseShort( data2.get("Allowed_Players").toString());
	         					//print(allowed_player + "§3 Allowed Players");
	         					if(allowed_player == 0) {
	         						//print(allowed_player + " = 0");
	         						continue;
	         					}
	         				}
	         				
	         					homes.add(new Home(data.get("by_home"), data2.get("Name"), data2.get("Creator"), data2.get("Radius"), (boolean) data2.get("Locked"),
	   	         					 true, (boolean) data.get("can_lock"), (boolean) data.get("can_add"), data2.get("Material_Icon"),  data2.get("Allowed_Players")));
	         					Size++;
         				}
	         			
         				}
         			}
         	}
	       
	    	if((Size == 1 && return_item) || ( Size == 0 && !(return_item))) return null;
	    	
	    	else if (Size <= 9)  Size = 9;
	    	else if (Size <= 18) Size= 18;
	    	else if (Size <= 27) Size = 27;
	    	else if (Size <= 36) Size = 36;
	    	
			Inventory inv = Bukkit.createInventory(null, Size, title);
			Material[] materials= {Material.WHITE_WOOL, Material.BLUE_WOOL,Material.RED_WOOL};
			List<ItemStack> items = new ArrayList<>();
			short ind = 0;
			
			for (Home home : homes) {
				
				Material m = Material.RED_CONCRETE;
				if(home.material != null)
					  m = Material.getMaterial(home.material.toString());;
				
				
				ItemStack i = new ItemStack(m, 1);
				ItemMeta im = i.getItemMeta();
				im.setDisplayName("§6" + home.Name);
				
				List<String> listLore = new ArrayList<>(); // Creating listlore
				
				if(home.Just_allowed) {   //ListLore for that homes that the player is allowed to
					listLore.add("§dCreator:§7 " + home.Creator);
					if(pEditInv)
					 listLore.add("§3Allowed: §7" + home.Allowed_Players);
					listLore.add("§cRadius:§7 " + home.Radius);
					listLore.add("§7Locked: " + YesOrNo(home.Locked));
		 		
				if(!(plockInv || pEditInv)) {
					listLore.add("§7---§4Perms§7---");
			 		listLore.add("§7Can Lock: " + YesOrNo(home.Can_lock));
					listLore.add("§7Can Add: " + YesOrNo(home.Can_add));
				}
				
				
				listLore.add("§8ID:" + home.ID);
				
				}else { 
					//This lore will be for the players owns homes
					if(pEditInv)
					 	listLore.add("§3Allowed: §7" + home.Allowed_Players);
				 	listLore.add("§cRadius:§7 " + home.Radius);
				 
				 	listLore.add("§7Locked: " + YesOrNo(home.Locked));
					listLore.add("§8ID:" + home.ID);
			
				}
				im.setLore(listLore);
				i.setItemMeta(im);
				items.add(i);
				ind++;
				if(ind >= materials.length) ind = 0;
						
			}
			// Creating the "back to menu" item
			ItemStack item_return = new ItemStack(Material.COMPASS, 1); 
			ItemMeta im_return = item_return.getItemMeta();
			im_return.setDisplayName("§CReturn To Menu"); //Must be the same as the ClickInvEvent is listening for (Main Class)
			item_return.setItemMeta(im_return);
			inv.setItem(0, item_return); //Puting it in first slot
			// Adding the rest of the items / homes
			
			ind = 0;
			if(return_item)
				ind = 1;
			
			for (ItemStack i : items) {
				inv.setItem(ind, i);
				ind++;
			}
			return inv;
			}

		public ItemStack getLogBook(int id) throws SQLException {
			
			ItemStack writtenBook = new ItemStack(Material.WRITTEN_BOOK, 1);
			BookMeta bookMeta = (BookMeta) writtenBook.getItemMeta();
			bookMeta.setTitle("§6EVENT LOG");
			bookMeta.setAuthor("§2Portal-§6Plugin");
			List<String> pages = new ArrayList<String>();
			ResultSet data = db.getEventLog(id);
			print("Data loggs: " + data.getFetchSize());
			 while(data.next()) {
				boolean removed = data.getBoolean(6);
				Date datetime = null;
				try {
					datetime =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(data.getString(8));
				} catch (java.text.ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}  
				SimpleDateFormat date_Format = new SimpleDateFormat("yy-MM-dd");
				SimpleDateFormat time_Format = new SimpleDateFormat("HH:mm:ss");
				
				String date = date_Format.format(datetime);
				String time = time_Format.format(datetime);
				String action = data.getString(3);
				 
				String arg = data.getString(5);
				
				String text = "§9By: §0" + data.getString(2) + "\n" + "§9->§0" +  action + "\n";
				if(arg == null)
				text = text + "§8@§6" + data.getString(9)  + "\n";
				else 
					text = text +  "§9->§0" + arg + "\n" + "§8@§6" + data.getString(9) + "\n";
				text = text + "§8@§9" + date + " §8@§9" + time; 
				if(removed)
					text = text += "§cRemoved \n " + data.getString(7);
				pages.add(text);
				
			 }
			 
			
			
			bookMeta.setPages(pages);
			writtenBook.setItemMeta(bookMeta);
			
			return writtenBook;
			
		}
		public Inventory getEditMenu() {
			
			Inventory inv = Bukkit.createInventory(null, 9, "§6Home §9Menu");
			
			ItemStack item_tp_h = new ItemStack(Material.ENDER_PEARL, 1); // The item for teleportng to home
			ItemStack item_rm_h = new ItemStack(Material.LAVA_BUCKET, 1); // The item for removing a home
			ItemStack item_lock_h = new ItemStack(Material.IRON_DOOR, 1); // The item for removing a home
			ItemStack item_rm_p = new ItemStack(Material.PLAYER_HEAD, 1); // The item for disallowing a player
			ItemStack item_show_all_p = new ItemStack(Material.BIRCH_SIGN, 1); // The Item for showing all allowed players
			ItemStack item_edit_perm = new ItemStack(Material.WRITABLE_BOOK, 1); // The Item for editing player persmssions
			ItemStack item_edit_icon = new ItemStack(Material.REDSTONE, 1); // The Item for editing player persmssions
			ItemStack item_show_logs = new ItemStack(Material.BOOK, 1); // The Item for showing the events loggs
			ItemStack item_leave_home = new ItemStack(Material.FIREWORK_ROCKET, 1); // The Item for showing the events loggs
			
			ItemMeta im_tp_h = item_tp_h.getItemMeta();
			ItemMeta im_rm_p = item_rm_p.getItemMeta();
			ItemMeta im_show_all_p = item_show_all_p.getItemMeta();
			ItemMeta im_edit_perm = item_edit_perm.getItemMeta();
			ItemMeta im_lock_h = item_lock_h.getItemMeta();
			ItemMeta im_rm_h = item_rm_h.getItemMeta();
			ItemMeta im_edit_icon = item_rm_h.getItemMeta();
			ItemMeta im_leave_home = item_leave_home.getItemMeta();
			ItemMeta im_show_logs = item_rm_h.getItemMeta();
			
			
			im_rm_p.setDisplayName(ITEM_TITLE.DISALLOW_PLAYER);
			im_show_all_p.setDisplayName(ITEM_TITLE.SHOW_PLAYERS);
			im_edit_perm.setDisplayName(ITEM_TITLE.EDIT_PERMISSONS);
			im_lock_h.setDisplayName(ITEM_TITLE.LOCK_HOME);
			im_rm_h.setDisplayName(ITEM_TITLE.REMOVE_HOME);
			im_tp_h.setDisplayName(ITEM_TITLE.TELEPORT_HOME);
			im_edit_icon.setDisplayName(ITEM_TITLE.CHANGE_ICON);
			im_leave_home.setDisplayName(ITEM_TITLE.LEAVE_HOME);
			im_show_logs.setDisplayName(ITEM_TITLE.SHOW_LOGS);
			
			
			item_rm_p.setItemMeta(im_rm_p);
			item_show_all_p.setItemMeta(im_show_all_p);
			item_edit_perm.setItemMeta(im_edit_perm);
			item_rm_h.setItemMeta(im_rm_h);
			item_lock_h.setItemMeta(im_lock_h);
			item_tp_h.setItemMeta(im_tp_h);
			item_edit_icon.setItemMeta(im_edit_icon);
			item_leave_home.setItemMeta(im_leave_home);
			item_show_logs.setItemMeta(im_show_logs);
			
			
			inv.setItem(0, item_tp_h);
			inv.setItem(1, item_lock_h);
			inv.setItem(2, item_rm_h);
			inv.setItem(3, item_show_all_p);
			inv.setItem(4, item_edit_perm);
			inv.setItem(5, item_rm_p);
			inv.setItem(6, item_edit_icon);
			inv.setItem(7, item_leave_home);
			inv.setItem(8, item_show_logs);
			return inv;
		}
	
		public static void print(String text) {
			if(debug)
				Main.plugin.getServer().getConsoleSender().sendMessage(Database.Author_text +  text);
		}
		public void ERROR(String where, Player p, String error, StackTraceElement[] e ) {
			String pname = "";
			if(p != null) {
			p.sendMessage("§cInteral Error. We are working as fast as possible to fix this issue.");
			pname = p.getName();
			}
		SimpleDateFormat formatter = new SimpleDateFormat("[yy-MM-dd HH:mm:ss]");  
		Date date = new Date();  
		String datetime = formatter.format(date);
		StringBuilder error_msg = new StringBuilder();
		error_msg.append(datetime + ":" + error + " [In: '" + where + "', By: '" + pname +"'] \n");
		for(int i = 0; i < e.length; i++) {
			
			String a = e[i].getMethodName();
			String b = e[i].getFileName();
			String c = e[i].getClassName();
			int line = e[i].getLineNumber();
			if(b != null)
			if(b.contains("Main") || b.contains("Functions") || b.contains("Events") || b.contains("Database"))
			error_msg.append(datetime +":	at " + c + "." + a + "(" + b + ":"+ line + ") \n");
		}
		WriteERROR(error_msg.toString());
		for(Player t : Bukkit.getOnlinePlayers()) {
			if(t.isOp()) {
				
				t.playSound(t.getLocation(), Sound.BLOCK_NOTE_BLOCK_BANJO, 1f, 1f);
				t.sendMessage("§c[ERROR] §f" + error);
				t.sendMessage("§7IN--> §f" + where);
				t.sendMessage("§3BY: §7" + pname);
				t.sendMessage("§c[INFORM] §DSIRAP §cON DISCORD §7Sirap#4498 §cASAP ");
			}
		}
			
		}
		
		
		public static void ERROR2(String where, Player p, String error, StackTraceElement[] e) {
			String pname = "";
			if(p != null) {
			p.sendMessage("§cInteral Error. We are working as fast as possible to fix this issue.");
			pname = p.getName();
			}
		SimpleDateFormat formatter = new SimpleDateFormat("[yy-MM-dd HH:mm:ss]");  
		Date date = new Date();  
		String datetime = formatter.format(date);
		StringBuilder error_msg = new StringBuilder();
		error_msg.append(datetime + ":" + error + " [In: '" + where + "', By: '" + pname +"'] \n");
		for(int i = 0; i < e.length; i++) {
			
			String a = e[i].getMethodName();
			String b = e[i].getFileName();
			String c = e[i].getClassName();
			int line = e[i].getLineNumber();
			
			if(b.contains("Main") || b.contains("Functions") || b.contains("Events") || b.contains("Database"))
			error_msg.append(datetime +":	at " + c + "." + a + "(" + b + ":"+ line + ") \n");
		}
		WriteERROR(error_msg.toString());
		for(Player t : Bukkit.getOnlinePlayers()) {
			if(t.isOp()) {
				
				t.playSound(t.getLocation(), Sound.BLOCK_NOTE_BLOCK_BANJO, 1f, 1f);
				t.sendMessage("§c[ERROR] §f" + error);
				t.sendMessage("§7IN--> §f" + where);
				t.sendMessage("§3BY: §7" + pname);
				t.sendMessage("§c[INFORM] §DSIRAP §cON DISCORD §7Sirap#4498 §cASAP ");
			}
		}
			
		}
		
		public boolean checkBadWords(String message, Player p, String where)
		{
			
			String[] badwords = getBadWords().split(",");
			List<String> muted_players = Main.config.getStringList("muted");
	    	
	    	for(int i = 0; i < badwords.length; i++) {
	    	if(message.contains(badwords[i])) {
	    		
	    		p.sendMessage("§cThe messages or input contains words that is against our rules");
	    		p.sendMessage("§6You are thefore muted and needs to wait for staff memeber to handle this.");
	    		
	    		muted_players.add(p.getName());
	    		Main.config.set("muted", muted_players);
	    		Main.plugin.saveConfig();
	    		for(Player ops : Bukkit.getOnlinePlayers()) {
					if(ops.isOp())
						ops.sendMessage("§6Player: §7" + p.getName() + "§6 Just got muted");
				}
	    		try {
	    			String reason = "Wrote: '"  + message + "', in: " + where;
	    			db.execute_prepare("INSERT INTO player_infractions (player_name, UUID, by_who, Reason, Type) VALUES ('"+p.getName() +"', '"+p.getUniqueId()+"', 'Portal-Plugin', '"+reason+"', '"+Infraction.KICK+"')");
					print("§3@§fFunctions.CheckBadWords §3SQL §aSuccesfull");
	    		} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					print("§3@§fFunctions.CheckBadWords §3SQL §cFailed");
				}
	    		return false;
	    		
	    		}	
	    	}
			
			return true;
		}
		
		public void writeTextFile() {
			List<String> names = new ArrayList<>();
 			try {
				ResultSet data = db.getPlayerNames();
				
				while(data.next()) {
					names.add(data.getString(1));
				}
				
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				ERROR("PlayerHomes", null, e.toString(), e.getStackTrace());
			}
			Main.config.set("PlayerNames", names);
			Main.plugin.saveConfig();
			
		}
		public String filterBadWords(String message)
		{
			
			
			String[] badwords = getBadWords().split(",");
	    	for(int i = 0; i < badwords.length; i++) {
	    		String msg_lower = message.toLowerCase();
		    	if(msg_lower.contains(badwords[i])) {
		    		String fitler = "*".repeat(badwords[i].length());
		    		char char1 = badwords[i].charAt(0);
		    		String firstChar = String.valueOf(char1);
		    		fitler = fitler.replaceFirst("[*]",firstChar);
		    		message = message.replaceAll("(?i)\\b[^\\w -]*" + badwords[i] + "[^\\w -]*\\b", fitler);
		    		}	
		    	}
			
			return message;
		}
		public String getBadWords() {
		
			
			File file = new File(Main.plugin.getDataFolder() + "/badwords.txt");
			 
	        try (FileReader fr = new FileReader(file))
	        {
	            char[] chars = new char[(int) file.length()];
	            fr.read(chars);
	 
	            String fileContent = new String(chars);
	            return fileContent;
	        }
	        catch (IOException e) {
	            e.printStackTrace();
	        }
	        System.out.println("ERROR while reading badwords");
	        return "";
		}
		
		public Object getJsonFile (String url) {
			Object obj = null;
	     	
			try {
				obj = new JSONParser().parse(new FileReader(Main.plugin.getDataFolder() +  "/" + url));
			} catch (FileNotFoundException e) {
				print(url + " File not found... Deleted?");
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return obj;
		}
		
		public boolean checkSpam(String pname, short max_clicks, double delta) {
			
			long time = System.currentTimeMillis();
			
			if(spam_click.containsKey(pname)) {
				
				List<Long> times = spam_click.get(pname);
				
				int clicks = times.size();
				if(clicks > max_clicks) {
					long first_time = times.get(0);
					double delta_time = (time - first_time) / 1000.0;
					
					print("§3@§fFunctions.CheckSpam Delta-Time: §3" + delta_time + " §f Player: §7" + pname);
					if(delta_time < delta) {
						
						spam_click.remove(pname);
						return true;
						
					}
					spam_click.remove(pname);
				}	else
				{
					times.add(time);
					spam_click.replace(pname, times);
				}
			}else {
				List<Long> times = new ArrayList<>();
				times.add(time);
				spam_click.put(pname, times);
			}
			return false;
		}
		
		public boolean checkSpamBlock(String pname, short max_clicks, double delta) {
			
			long time = System.currentTimeMillis();
			
			if(spam_click_block.containsKey(pname)) {
				
				List<Long> times = spam_click_block.get(pname);
				
				int clicks = times.size();
				if(clicks > max_clicks) {
					long first_time = times.get(0);
					double delta_time = (time - first_time) / 1000.0;
					
					print("§3@§fFunctions.CheckSpam Delta-Time: §3" + delta_time + " §f Player: §7" + pname);
					if(delta_time < delta) {
						
						spam_click_block.remove(pname);
						return true;
						
					}
					spam_click_block.remove(pname);
				}	else
				{
					times.add(time);
					spam_click_block.replace(pname, times);
				}
			}else {
				List<Long> times = new ArrayList<>();
				times.add(time);
				spam_click_block.put(pname, times);
			}
			return false;
		}
		
		
		public String YesOrNo(boolean value) {
			
			if(value)
				return "§atrue";
			else
				return "§cfalse";
			
		}
		
		@SuppressWarnings("unchecked")
		public void leave_home(Player p, Inventory inv, ItemStack IS, int slot)
		{
			p.closeInventory();
			
			
			ItemMeta im = IS.getItemMeta();
			List<String> lorelist = im.getLore();
			int id_index = lorelist.size() -1;
			String id = lorelist.get(id_index).split(":")[1]; // Getting the home ID of the selected item
			String key;
			try {
				key = db.leave_home(id, p.getName()) + "";
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				ERROR("leave_home onClick ", p, e.toString(), e.getStackTrace());
				return;
			}
			Map<String, Object> data2 = ((Map<String, Object>)homes.get(id));
			int allowed = Integer.parseInt(data2.get("Allowed_Players").toString());
			allowed--;
			data2.replace("Allowed_Players", allowed);
			homes.replace(id, data2);
			allowed_players.remove(key);
			
			WriteJsonHomes(homes.toJSONString());
			PrintWriter pw;
			try {
				pw = new PrintWriter(Main.plugin.getDataFolder() +  "/allowed_players.json");
				pw.write(allowed_players.toJSONString());
				pw.flush();
			    pw.close();
			   Functions.print( "'allowed_players.json': §7[§aChanged§7] §f 1 §7row");
			} catch (FileNotFoundException ex) {
				// TODO Auto-generated catch block
				Functions.print( " 'allowed_players.json': §cFailed §7 to change");
				ex.printStackTrace();
			}	
			updatefiles();
			p.sendMessage("§3Lefted home §7" + im.getDisplayName().replace("§6", ""));
		}
		public void lockhome(Player p, Inventory inv, ItemStack IS, int slot)
		{
			if(checkSpam(p.getName(), (short) 3,(double)2)) {
				
				inv.clear();
				
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
				p.sendMessage("§cDon't spam click");
				p.closeInventory();
				return;
			}
			
			ItemStack i = IS;
			
			ItemMeta im = IS.getItemMeta();
			List<String> lorelist = im.getLore();
			int id_index = lorelist.size() -1;
			int locked_index = id_index - 1;
			String id = lorelist.get(id_index).split(":")[1]; // Getting the home ID of the selected item
			

			String locked_lore = lorelist.get(locked_index); 
			
			boolean locked = false;
			if(locked_lore.contains("true")) {// Getting the new Value of the item
				locked = true;
			}
		
			if(locked) {
				locked = false;
				p.playSound(p.getLocation(), Sound.BLOCK_WOODEN_DOOR_OPEN, 1f, 1f);
				
			}else if(!locked) {
				locked = true;
				p.playSound(p.getLocation(), Sound.BLOCK_WOODEN_DOOR_CLOSE, 1f, 1f);
				
			}else {
				
				int currentLine = new Throwable().getStackTrace()[0].getLineNumber();
				print("§3@Functions.§clockhome [ERROR] §f  line: " + currentLine);
				return;
			}
			
			lorelist.set(locked_index, "§7Locked: " + YesOrNo(locked));
			String id_lore = lorelist.get(id_index);
			boolean is_edited = id_lore.contains(" ");
			if(is_edited) {//If its already edited, the user is setting the value back. and therfore ther is no need for us the register it as a new data input
				id = id.replaceAll("\\s+","");
				lorelist.set(id_index, "§8ID:" + id);
				print("§3@func.lockhome Was already edited id = §7" + id);
				
			}
			else 
			lorelist.set(id_index, "§8ID:" + id  + " "); // Using a space in the lore to mark the item that it has been edited. - This is to know witch ID's that should be updated in onCloseInv() 
			
			im.setLore(lorelist);
			i.setItemMeta(im);
			inv.setItem(slot, i);
			p.updateInventory();
			
		}
		
		public void remove_home(Player p, Inventory inv, ItemStack IS, int slot)
		{
			if(checkSpam(p.getName(), (short) 3,(double)2)) {
				
				inv.clear();
				
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
				p.sendMessage("§cDon't spam click");
				p.closeInventory();
				return;
			}
			
			ItemStack i = IS;
			
			ItemMeta im = IS.getItemMeta();
			List<String> lorelist = im.getLore();
			

			String lore = lorelist.get(0); 
		
			if(lore.contains("Radius")){
				
				lorelist.set(0, "§cRemove §7Home: " + im.getDisplayName() + "§7?");
				lorelist.set(1, "§9Click §7To §aConfirm");
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
			}else {
				p.closeInventory();
				
				String id = lorelist.get(lorelist.size() -1).split(":")[1];
				int ID = Integer.parseInt(id);
				Object obj = getJsonFile("player_homes.json");
				JSONObject homes = (JSONObject) obj;
				
				homes.remove(id);
				WriteJsonHomes(homes.toJSONString());
				
				Object obj2 = getJsonFile("allowed_players.json");
				JSONObject allowed_players = (JSONObject) obj2;
				List<String> keys = new ArrayList<>();
				for(Object key : allowed_players.keySet()) {
					Map<?, ?> ap_data = ((Map<?, ?>)allowed_players.get(key));
				if( ap_data.get("by_home").toString().equals(id)) {
					keys.add(key.toString());
					}
				}
				for(String key : keys) {
					allowed_players.remove(key);
				}
			
				WriteJsonPAllowed(allowed_players.toJSONString());

		        p.sendMessage("§cRemoved §3Home §7"+ im.getDisplayName().replace("§6", ""));
		    	p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
		    	String remove_query = "DELETE FROM player_homes WHERE ID = " + ID;
		    	String log_query = "INSERT INTO home_event_log (player_name, action, by_home) VALUES ('" + p.getName() + "', '" + LogAction.REMOVED_HOME + "', " + id + ");";
		    	Thread thread1 = new Thread(new Thread1(remove_query));
		    	Thread thread2 = new Thread(new Thread1(log_query));
		    	print("§cRemoving home §fID: §c" + id);
		    	updatefiles();
		        thread1.start();
		        thread2.start();
		    	return;
			}
			
			p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1f);
			/// TODO MAKE THIS GO TO SQL ON INVCLOSE
			
			im.setLore(lorelist);
			i.setItemMeta(im);
			inv.setItem(slot, i);
			p.updateInventory();
			
		}
		@SuppressWarnings("unchecked")
		public void change_icon(String home_id, String Material, Player p, String color) {
			
			try {
			@SuppressWarnings("unused")
			Material m = org.bukkit.Material.getMaterial("");
			}catch(Exception e) {
				ERROR("Functions.change_icon", p, e.toString(), e.getStackTrace());
				return;
			}
			Object obj = getJsonFile("player_homes.json");
			JSONObject homes = (JSONObject) obj;
			Map<String, Object> data = ((Map<String, Object>)homes.get(home_id));
			data.replace("Material_Icon", Material);
			homes.replace(home_id, data);
			
			String remove_query = "UPDATE player_homes SET Material_Icon = '"+Material+"' WHERE ID = " + home_id;
	    	Thread thread1 = new Thread(new Thread1(remove_query));
	        thread1.start();
	        WriteJsonHomes(homes.toJSONString());
	        updatefiles();
	        p.sendMessage("§3Changed the icon to §7" + color);
			
			
		}
		public void edit_player_perms(Player p, Inventory inv, ItemStack IS, int slot, ClickType click)
		{
			if(checkSpam(p.getName(), (short) 5,(double)4)) {
				
				inv.clear();
				
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
				p.sendMessage("§cDon't spam click");
				p.closeInventory();
				return;
			}
			
			ItemStack i = IS;
			
			ItemMeta im = IS.getItemMeta();
			List<String> lorelist = im.getLore();
			
			
			if(click == ClickType.LEFT) {
				String lore = lorelist.get(0); 
				
				int lore0_spaces = StringUtils.countMatches(lore, " ");
			if(lore.contains("§cfalse")){
				if(lore0_spaces < 3) {
				lorelist.set(0, "§7Can Lock: " + YesOrNo(true) + " ");
				print(  "§3@§fClickType:§cLEFT  §7---> §fSpaces §a== §f2 §aADDING §fSPACES §3@§fCAN_LOCK §7--> §aTRUE §fSPACES < 3 ");
				}
				else {
					
					lorelist.set(0, "§7Can Lock: " + YesOrNo(true)); // Using a space to the end to specify that this part has been edited 
					print(  "§3@§fClickType:§cLEFT  §7---> §fSpaces §c!= §f2 §cREMVOING §fSPACES §3@§fCAN_LOCK §7--> §aTRUE §fSPACES §c!< §f3");
				}
			}else {
				
				if(lore0_spaces< 3) {
					
					lorelist.set(0, "§7Can Lock: " + YesOrNo(false) + " ");
					print(  "§3@§fClickType:§cLEFT  §7---> §fSpaces §a== §f2 §aADDING §fSPACES §3@§fCAN_LOCK §7--> §cFALSE §f SPACES < 3");
					
				}
					else	{
						lorelist.set(0, "§7Can Lock: " + YesOrNo(false)); // Using a space to the end to specify that this part has been edited 
						print(  "§3@§fClickType:§cLEFT  §7---> §fSpaces §c!= §f2 §cREMVOING §fSPACES §3@§fCAN_LOCK §7--> §cFALSE §fSPACES §c!< §f3  ");
					}
			}
			
			}else if(click == ClickType.RIGHT) {
				String lore = lorelist.get(1);
				int lore1_spaces = StringUtils.countMatches(lore, " ");
				
				if(lore.contains("§cfalse")){
					if(lore1_spaces < 3) {
						
					lorelist.set(1, "§7Can Add: " + YesOrNo(true) + " ");
					print(  "§3@§fClickType:§cRIGHT §7---> §fSpaces §a== §f2 §aADDING §fSPACES §3@§fCAN_ADD §7--> §aTRUE §f SPACES < 3 ");
					}
					else {
						lorelist.set(1, "§7Can Add: " + YesOrNo(true)); // Using a space to the end to specify that this part has been edited 
						print(  "§3@§fClickType:§cRIGHT §7---> §fSpaces §c!= §f2 §cREMVOING §fSPACES §3@§fCAN_ADD §7--> §aTRUE §f SPACES §c!< §f3");
					}
				}else {
					
					if(lore1_spaces < 3) {
						
						lorelist.set(1, "§7Can Add: " + YesOrNo(false) + " ");
						print(  "§3@§fClickType:§cRIGHT §7---> §fSpaces §a== §f2 §aADDING §fSPACES §3@§fCAN_ADD §7--> §cFALSE §f SPACES < 3  ");
						
					}
						else {
							lorelist.set(1, "§7Can Add: " + YesOrNo(false)); // Using a space to the end to specify that this part has been edited 
							print(  "§3@§fClickType:§cRIGHT §7---> §fSpaces §c!= §f2 §cREMVOING §fSPACES §3@§fCAN_ADD §7--> §cFALSE §f SPACES §c!< §f3 ");
						}
				}
				
			}
		
			p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1f);
			/// TODO MAKE THIS GO TO SQL ON INVCLOSE
			
			im.setLore(lorelist);
			i.setItemMeta(im);
			inv.setItem(slot, i);
			p.updateInventory();
			
		}
		
		public void disallow_player(Player p, Inventory inv, ItemStack IS, int slot)
		{
			if(checkSpam(p.getName(), (short) 3,(double)2)) {
				
				inv.clear();
				
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
				p.sendMessage("§cDon't spam click");
				p.closeInventory();
				return;
			}
			
			ItemStack i = IS;
			
			ItemMeta im = IS.getItemMeta();
			List<String> lorelist = im.getLore();
			

			String lore = lorelist.get(0); 
		
			if(lore.contains("Allowed")){
				
				lorelist.set(0, "§cDisallowed");
				
			}else {
				
				lorelist.set(0, "§aAllowed");
			}
			
			p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1f);
			/// TODO MAKE THIS GO TO SQL ON INVCLOSE
			
			im.setLore(lorelist);
			i.setItemMeta(im);
			inv.setItem(slot, i);
			p.updateInventory();
			
		}
		public static String inventoryToString(Inventory inventory) {
		    try {
		        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		        BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
		        // Write the size of the inventory
		        dataOutput.writeInt(inventory.getSize());
		        // Save every element in the list
		        for (int i = 0; i < inventory.getSize(); i++) {
		            dataOutput.writeObject(inventory.getItem(i));
		        }
		        // Serialize that array
		        dataOutput.close();
		        return Base64Coder.encodeLines(outputStream.toByteArray());
		    } catch (IOException e) {
		        throw new IllegalStateException("Unable to save item stacks.", e);
		    }
		}
		public String serializeItem_Stack(ItemStack im) {
			String itemStackString = "";
			try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
                dataOutput.writeObject(im);
                dataOutput.close();
                itemStackString = Base64Coder.encodeLines(outputStream.toByteArray());
            } catch (IOException ex) {
                throw new IllegalStateException("Unable to save item stacks.", ex);
            }
			return itemStackString;
		}
		public ItemStack deserializeItem_Stack(String Item_String ) {
			ItemStack itemtoreturn = new ItemStack(Material.AIR);
            
            //decode the string back to an itemstack
            try {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(Item_String));
                BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
                itemtoreturn = (ItemStack) dataInput.readObject();
                dataInput.close();
            } catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (ClassNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            return itemtoreturn;
		}
		
		public void WriteJsonHomes(String s) {
			
			PrintWriter pw;
			try {
				pw = new PrintWriter(Main.plugin.getDataFolder() +  "/player_homes.json");
				pw.write(s);
				pw.flush();
			    pw.close();
			} catch (FileNotFoundException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
				ERROR("WriteJsonHomes", null, ex.toString(), ex.getStackTrace());
			}	
			
		}
		public void WriteJsonPlayerData(String s) {
			
			PrintWriter pw;
			try {
				pw = new PrintWriter(Main.plugin.getDataFolder() +  "/playerdata.json");
				pw.write(s);
				pw.flush();
			    pw.close();
			} catch (FileNotFoundException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
				ERROR("WriteJsonplayerData", null, ex.toString(), ex.getStackTrace());
			}	
			
		}
		public static void WriteERROR(String s) {
			String path = Main.plugin.getDataFolder() +  "/error_log.txt";
			String t = "";
			
			try {
				 t = Files.readString(Paths.get(path), StandardCharsets.UTF_8);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			};
			String text = t +  s;
			PrintWriter pw;
			try {
				pw = new PrintWriter(Main.plugin.getDataFolder() +  "/error_log.txt");
				pw.print(text);
				pw.flush();
			    pw.close();
			} catch (FileNotFoundException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}	
			
		}
	
		
		
		
		
		public void WriteJsonPAllowed(String s) {
			
			PrintWriter pw;
			try {
				pw = new PrintWriter(Main.plugin.getDataFolder() +  "/allowed_players.json");
				pw.write(s);
				pw.flush();
			    pw.close();
			} catch (FileNotFoundException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
				ERROR("WriteJsonHomes", null, ex.toString(), ex.getStackTrace());
			}	
			
		}
		
		public class Home{
			
			public Object Radius;
			public Object Creator;
			public Object Name;
			public Object ID;
			public boolean Locked;
			public boolean Just_allowed = false;
			public boolean Can_lock = false;
			public boolean Can_add = false;
			public Object Allowed_Players;;
			public Object material;
			public Home(Object id, Object name, Object creator ,Object radius ,boolean locked, Object material, Object Allowed_Players) {
				this.Radius = radius;
				this.Creator = creator;
				this.Name =  name;
				this.ID = id;
				this.Locked = locked;
				this.material = material;
				this.Allowed_Players = Allowed_Players;
			}
			public Home(Object id, Object name, Object creator ,Object radius ,boolean locked, boolean just_allowed, boolean can_lock, boolean can_add, Object material, Object Allowed_Players) {
				this.Radius = radius;
				this.Creator = creator;
				this.Name =  name;
				this.ID = id;
				this.Just_allowed = just_allowed;
				this.Can_lock = can_lock;
				this.Can_add = can_add;
				this.Locked = locked;
				this.material = material;
				this.Allowed_Players = Allowed_Players;
			}
			
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
					conn = Database.dataSource.getConnection();
					Statement stmt = conn.createStatement();
					stmt.execute(query);
					print("§3@§fFunctions§3@§fThread1.§cRun §3SQL §aSUCCESFULL");
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					print("§c[ERROR] §3@§fFunctions§3@§fThread1.§cRun §3SQL §CFAILED");
					e.printStackTrace();
				}
			     
			   
	        }
	    }

	}


