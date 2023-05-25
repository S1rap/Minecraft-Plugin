package plugin;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.type.Switch;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;
import plugin.Const.INV_TITLE;
import plugin.Const.ITEM_TITLE;
import plugin.Const.LogAction;

public class Events {
	private Functions func;
	private Database db;
	public Events(Functions func, Database db) {
		this.func = func;
		this.db = db;
	}
	
	public void onInteract(PlayerInteractEvent  e) {
	            // Checks if the clicked block is a trap door.
			
		
		if(e.getClickedBlock() == null) return;
				
				
	            if((e.getClickedBlock().getBlockData() instanceof Openable || e.getClickedBlock().getBlockData() instanceof Switch  || e.getClickedBlock().getBlockData() instanceof Powerable) && e.getClickedBlock().getType() != Material.IRON_DOOR && e.getClickedBlock().getType() != Material.IRON_TRAPDOOR ) {
	            	Player p = e.getPlayer();
	        		
	            	for (Object id : func.homes.keySet()) {
	            		try {
	            			Map<?, ?> data = ((Map<?, ?>)func.homes.get(id));
	            			boolean locked = Boolean.parseBoolean(data.get("Locked").toString());
	            			if(!locked) continue;
	        			String output = func.intefears(p, id.toString(), e.getClickedBlock().getLocation(), 0, false);
	             		if(output != "") {
	             			if(func.checkSpam(p.getName(), (short) 5,1.5)) {
		    					p.kickPlayer("Spam clicking door or swtich in protected area");
		    					return;
		    				}
	             			
	             			Functions.print("§3@§fonInteract.RIGHT_CLICK_BLOCK §6[Intefears] §c[NOT_ALLOWED] §f §fBy: §7 " + p.getName() + " §3@§fKey: " + id);
	            			p.sendMessage("§cThis area is protected");
	                	
	                		p.playSound(p.getLocation(),Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
	                	
	                		e.setCancelled(true);
	                		
	                		break;
	        			}
	        				
	        		}catch(Exception ex){
	        			ex.printStackTrace();
	        			func.ERROR("onPlayerInteract", p, ex.toString(), ex.getStackTrace());
	        			break;
	        			}
	        		}
	                
	            }
	}
	
	@SuppressWarnings("unchecked")
	public void onInvClose(InventoryCloseEvent e) {
		try {
		Long time = System.nanoTime();
		if (e.getView().getTitle().equals(INV_TITLE.DISALLOW_PLAYER) ) {
			
			List<ItemStack> edited_items = new ArrayList<>();
			
			for(ItemStack item : e.getInventory().getContents()) {
				
				if(item == null) continue;
				if(item.getType()  == Material.COMPASS)
					continue;
				
				List<String> lorelist = item.getItemMeta().getLore();
				String lore = lorelist.get(0);
				
				boolean is_edited = lore.contains("§cDisallowed");
				if(is_edited) {
					edited_items.add(item);
				
				}else
					continue;
				
			}
			
			if(edited_items.size() == 0)  return; 
			String action = LogAction.DISALLOWED_PLAYER;
			StringBuilder log_values = new StringBuilder();
			StringBuilder ids = new StringBuilder();
			
			JSONObject allowed_p = func.allowed_players;
			JSONObject phomes =  func.homes;
			
			StringBuilder pnames = new StringBuilder();
			for(ItemStack item : edited_items) {
				
				List<String> lorelist = item.getItemMeta().getLore();
				String ID = lorelist.get(1).split(":")[1];
				String name = item.getItemMeta().getDisplayName().replace("§6", "");
				if(pnames.length() == 0)
				pnames.append(name);
				else
					pnames.append(", " + name);
				if(ids.length() == 0)
					ids.append("ID = " + ID);
				else
					ids.append(" OR ID =" + ID);
				
				Map<String, Object> data = ((Map<String, Object>)allowed_p.get(ID));
				
				String id =data.get("by_home").toString();
				System.out.println("'" + id + "'");
				Map<String, Object> data2 = ((Map<String, Object>)phomes.get(id));
				
				int allowed = Integer.parseInt(data2.get("Allowed_Players").toString());
				allowed--;
				data2.replace("Allowed_Players", allowed);
				phomes.replace(id, data2);
				
				if(log_values.length() == 0)
					log_values.append("('" + e.getPlayer().getName()+ "','" + action + "','" + id + "', '"+  name + "')");
				else
					log_values.append(",('" + e.getPlayer().getName()+ "','" + action + "','" + id + "', '"+  name + "')");
				
				allowed_p.remove(ID);
				
			}
			
			String query_allowed_players = "DELETE FROM player_allowed WHERE "+ ids.toString() +";";
			String query_event_logs = "INSERT INTO home_event_log (player_name, action, by_home, target_player) VALUES " + log_values.toString() +";";
			Functions.print("§3@§fInvClose.§cDISALLOW §3QUERY: §f: " + query_allowed_players);
			Functions.print("§3@§fInvClose.§cDISALLOW §3QUERY_LOG: §f: " + query_event_logs);
			Thread thread1 = new Thread(new Thread1(query_allowed_players));
		    Thread thread2 = new Thread(new Thread2(query_event_logs));
		    
	        thread1.start();
	        thread2.start();
	        
	       
	        
			//INSERT INTO player_homes (ID, Locked) VALUES (68, 0),(69, 0) ON DUPLICATE KEY UPDATE Locked = VALUES(Locked);
	        //func.WriteJsonHomes(phomes.toJSONString());
			PrintWriter pw;
			try {
				pw = new PrintWriter(Main.plugin.getDataFolder() +  "/allowed_players.json");
				pw.write(allowed_p.toJSONString());
				pw.flush();
			    pw.close();
			    Functions.print(" 'allowed_players.json': §7[§cRemoved§7] §f "+ edited_items.size() +  " §7rows");
			  
			} catch (FileNotFoundException ex) {
				// TODO Auto-generated catch block
				Functions.print(" 'allowed_players.json': §cFailed §7 to remove");
				ex.printStackTrace();
				func.ERROR("InvClose disallow", null, ex.toString(), ex.getStackTrace());
			}	catch (Exception e2) {
				// TODO Auto-generated catch block
				Functions.print(" 'allowed_players.json': §cFailed");
				func.ERROR("InvClose disallow", null, e2.toString(), e2.getStackTrace());
				e2.printStackTrace();
			}	
			func.WriteJsonHomes(phomes.toJSONString());
			func.updatefiles();
			double delta_time =  (double) (System.nanoTime() - time) / 1_000_000_000;
			Functions.print("§3@§fEvents.onInventoryClose§7 Executed in: §3" + delta_time + "§fs");
			if(pnames.length() == 1)
			e.getPlayer().sendMessage("§cDisallowed §3Player §7" + pnames.toString());
			else
				e.getPlayer().sendMessage("§cDisallowed §3Players §7" + pnames.toString());
		}
		
		
		if (e.getView().getTitle().equals(INV_TITLE.EDIT_PERMISSONS) ) {
			
			List<ItemStack> edited_items = new ArrayList<>();
			
			for(ItemStack item : e.getInventory().getContents()) {
				
				if(item == null) continue;
				if(item.getType()  == Material.COMPASS)
					continue;
				
				List<String> lorelist = item.getItemMeta().getLore();
				
				//KOLLA BÅDE LORES
				int lore1 = StringUtils.countMatches( lorelist.get(0), " ");
				int lore2 = StringUtils.countMatches( lorelist.get(1), " ");
				
				boolean is_edited = lore1 > 2 || lore2 > 2;
				if(is_edited) {
					edited_items.add(item);
				
				}else
					continue;
				
			}
			
			if(edited_items.size() == 0) {Functions.print("§3@§fInvClose.§cEdit_Permissions §fReturning, no edited items"); return; }
			String action = null;
			String action2 = null;
			StringBuilder log_values = new StringBuilder();
			StringBuilder ids = new StringBuilder();
			
			JSONObject allowed_p = func.allowed_players;
			StringBuilder pnames = new StringBuilder();
			for(ItemStack item : edited_items) {
				
				List<String> lorelist = item.getItemMeta().getLore();
				String ID = lorelist.get(2).split(":")[1];
				
				
				String lore0 =  lorelist.get(0); //CAN LOCK LORE
				String lore1 = lorelist.get(1); // CAN ADD LORE
				int lore0_spaces = StringUtils.countMatches( lore0, " ");
				int lore1_spaces = StringUtils.countMatches( lore1, " ");
				
				boolean can_lock, can_add;
				if(lore0.contains("§cfalse"))
					can_lock = false;
				else
					can_lock = true;
				if(lore1.contains("§cfalse"))
					can_add = false;
				else
					can_add = true;
					
				Map<String, Object> data = ((Map<String, Object>)allowed_p.get(ID));
				
				if( lore0.contains("§atrue ")) {
					if(lore0_spaces >2) {
						action = LogAction.ADDED_CAN_LOCK;
						Functions.print("§3@§fInvClose.§cEdit_Permissions §eCAN_LOCK §2LORE §7SPACES IS = TO §63 §7AND CONTAINS CHAR §7'§aTRUE§7'  ");
						can_lock = true;
					}	
				} if(lore0.contains("§cfalse ")) {
					if(lore0_spaces >2 ) {
					action = LogAction.REMOVED_CAN_LOCK;
					can_lock = false;
					Functions.print("§3@§fInvClose.§cEdit_Permissions §eCAN_LOCK §2LORE §7SPACES IS = TO §63 §7AND CONTAINS CHAR §7'§cFALSE§7'  ");
					}
				} 
				
				if( lore1.contains("§atrue ")) {
					if(lore1_spaces >2) {
					action2 = LogAction.ADDED_CAN_ADD;
					can_add = true;
					
					Functions.print("§3@§fInvClose.§cEdit_Permissions §eCAN_ADD §2LORE1 §7SPACES IS = TO §63 §7AND CONTAINS CHAR §7'§aTRUE§7'  ");
					
					}
				} if(lore1.contains("§cfalse ")) {
					if(lore1_spaces >2) {
					action2 = LogAction.REMOVED_CAN_ADD;
					can_add = false;
					Functions.print("§3@§fInvClose.§cEdit_Permissions §eCAN_ADD §2LORE1 §7SPACES IS = TO §63 §7AND CONTAINS CHAR §7'§cFALSE§7'  ");
					}
				}
				Functions.print("§3@§fInvClose.§cEdit_Permissions§7 ----§cAction1: §f"+ action + "§7  ---  §cAction2: §f" +action2 + "§7---");
				if(action == null && action2 == null) {
					func.ERROR("@Events.InvenotryClick.onCloseInv -> Edit perms ()", (Player) e.getPlayer(), "Both action are null",new Throwable().getStackTrace());
					return;
				}
				
				Functions.print("§3@§fInvClose.§cEdit_Permissions§7----§aPASSED §CNULL §ACHECK§7----");
				
				String by_home = data.get("by_home").toString();
				String name = data.get("player_name").toString();
				//String pname = e.getPlayer().getName();

				if(pnames.length() == 0)
					pnames.append(name);
					else
						pnames.append(", " + name);
				
				int can_lock_value = can_lock  ? 1 : 0;
				int can_add_value = can_add  ? 1 : 0;
				
				if(ids.length() == 0)
					ids.append("(" + ID + ", " + can_lock_value + ", " + can_add_value  + ", " + by_home +  ")");
				else
					ids.append(",(" + ID + ", " + can_lock_value + ", " + can_add_value  + ", " + by_home +  ")");
				
				  
			        
				if(log_values.length() == 0) {
					
					if(action2 != null) {
						log_values.append("('" + e.getPlayer().getName()+ "' ,'" + action2 + "', '" + by_home + "', '"+  name + "')");
					}else if(action != null) 
						log_values.append("('" + e.getPlayer().getName()+ "' ,'" + action + "', '" + by_home + "', '"+  name + "')");
				}
				else {
					if(action != null)
					log_values.append(",('" + e.getPlayer().getName()+ "','" + action + "','" + by_home + "', '"+  name + "')");
					else if(action2 != null) {
						log_values.append(",('" + e.getPlayer().getName()+ "','" + action2 + "','" + by_home + "', '"+  name + "')");
					}
					
				}
					
				
				data.replace("can_add", can_add);
				data.replace("can_lock", can_lock);
				allowed_p.replace(ID, data);
				
			}
			String query_allowed_players= "INSERT IGNORE INTO player_allowed (ID, can_lock, can_add, by_home) VALUES " + ids.toString() +" ON DUPLICATE KEY UPDATE ID=VALUES(ID), can_lock=VALUES(can_lock), can_add=VALUES(can_add), by_home=VALUES(by_home);";
			Functions.print("§3@§fInvClose.§cEdit_Permissions§3QUERY: §f: " + query_allowed_players);
			String query_event_logs = "INSERT INTO home_event_log (player_name, action, by_home, target_player) VALUES " + log_values.toString() +";";
			Functions.print("§3@§fInvClose.§cEdit_Permissions§3QUERY: §f: " + query_event_logs);
			Thread thread1 = new Thread(new Thread1(query_allowed_players));
		    Thread thread2 = new Thread(new Thread2(query_event_logs));
		    
		    thread1.start();
	        thread2.start();
			//INSERT INTO player_homes (ID, Locked) VALUES (68, 0),(69, 0) ON DUPLICATE KEY UPDATE Locked = VALUES(Locked);
	        if(pnames.length() > 1)
				e.getPlayer().sendMessage("§3Edited permissions§7 for " + pnames.toString());
				else {
					e.getPlayer().sendMessage("§3Edited permissions§7 for " + pnames.toString());	
					
				}
			PrintWriter pw;
			try {
				pw = new PrintWriter(Main.plugin.getDataFolder() +  "/allowed_players.json");
				pw.write(allowed_p.toJSONString());
				pw.flush();
			    pw.close();
			   Functions.print( "'allowed_players.json': §7[§aChanged§7] §f "+ edited_items.size() +  " §7rows");
			} catch (FileNotFoundException ex) {
				// TODO Auto-generated catch block
				Functions.print( " 'allowed_players.json': §cFailed §7 to change");
				func.ERROR("InvClose edit_permissons", null, ex.toString(), ex.getStackTrace());
				ex.printStackTrace();
			}	
			
			func.updatefiles();
			double delta_time =  (double) (System.nanoTime() - time) / 1_000_000_000;
			Functions.print("§3@§fEvents.onInventoryClose§7 Executed in: §3" + delta_time + "§fs");
			
		}
		
		if (e.getView().getTitle().equals(INV_TITLE.LOCK_HOME) ) {
		
			List<ItemStack> edited_items = new ArrayList<>();
			
			for(ItemStack item : e.getInventory().getContents()) {
				
				if(item == null) continue;
				if(item.getType()  == Material.COMPASS)
					continue;
				
				List<String> lorelist = item.getItemMeta().getLore();
				String id_lore = lorelist.get(lorelist.size() - 1);
				
				boolean is_edited = id_lore.matches(".*\\s.*");
				if(is_edited) {
					edited_items.add(item);
				
				}else
					continue;
				
			}
			
			if(edited_items.size() == 0)  return; 
			
			StringBuilder log_values = new StringBuilder();
			StringBuilder ids = new StringBuilder();
			StringBuilder locked_homes = new StringBuilder();
			StringBuilder unlocked_homes = new StringBuilder();
			for(ItemStack item : edited_items) {
				
			List<String> lorelist = item.getItemMeta().getLore();
			int id_index = lorelist.size() -1;
			int locked_index = id_index - 1;
			String id_lore = lorelist.get(id_index);
			String id = id_lore.split(":")[1];
			id = id.replaceAll("\\s+","");
			String locked_lore = lorelist.get(locked_index); 
			boolean locked = false;
			String action = LogAction.UNLOCKED_HOME;
			
			String home_name = item.getItemMeta().getDisplayName().replace("§6", "");
			if(locked_lore.contains("true")) {// Getting the new Value of the item
				locked = true;
				action = LogAction.LOCKED_HOME;
			}
		
			int integer_locked = locked ? 1 : 0;
			
			String comma = ", ";
			if(locked_homes.length() == 0) comma = "";
			String comma2 = ", ";
			if(unlocked_homes.length() == 0) comma2 = "";
				
			if(action == LogAction.LOCKED_HOME)
				locked_homes.append(comma + home_name);
			else if(action == LogAction.UNLOCKED_HOME)
				unlocked_homes.append(comma2 + home_name);
				
				
			
			
			if(ids.length() == 0)
				ids.append("('" + id + "', '" + integer_locked + "')");
			else
				ids.append(",('" + id + "', '" + integer_locked + "')");
			
			if(log_values.length() == 0)
				log_values.append("('" + e.getPlayer().getName()+ "','" + action + "','" + id + "')");
			else
				log_values.append(",('" + e.getPlayer().getName() + "','" + action + "','" + id + "')");
			
			//querys.append("UPDATE `player_homes` SET `Locked` = '"+ integer_locked +"' WHERE `player_homes`.`ID` = '" + id + "'; ");
			//querys.append("INSERT INTO `home_event_log` (`ID`, `UUID`, `Action`, `by_home`, `removed`, `removed_date`, `created_date`) VALUES (NULL, '" +e.getPlayer().getUniqueId() +"', '"+ action +"', '"+ id +"', '0', 'null', CURRENT_TIMESTAMP()); ");
			
			
			Map<String, Object> data = ((Map<String, Object>)func.homes.get(id));
			data.replace("Locked",  locked);
			func.homes.replace(id, data);
			}
			String query_player_homes = "INSERT IGNORE INTO player_homes (ID, Locked) VALUES " + ids.toString() +" ON DUPLICATE KEY UPDATE Locked = VALUES(Locked);";
			String query_event_logs = "INSERT INTO home_event_log (player_name, action, by_home) VALUES " + log_values.toString() +";";
			
			Thread thread1 = new Thread(new Thread1(query_player_homes));
		    Thread thread2 = new Thread(new Thread2(query_event_logs));
		    
		    
	        thread1.start();
	        thread2.start();
			//INSERT INTO player_homes (ID, Locked) VALUES (68, 0),(69, 0) ON DUPLICATE KEY UPDATE Locked = VALUES(Locked);

			PrintWriter pw;
			try {
				pw = new PrintWriter(Main.plugin.getDataFolder() +  "/player_homes.json");
				pw.write(func.homes.toJSONString());
				pw.flush();
			    pw.close();
			} catch (FileNotFoundException ex) {
				// TODO Auto-generated catch block
				func.ERROR("InvClose locked_home", null, ex.toString(), ex.getStackTrace());
				ex.printStackTrace();
			}	
			
			if(locked_homes.length() > 0 && unlocked_homes.length() > 0)
			e.getPlayer().sendMessage("§3Locked: §7" + locked_homes.toString() + ". §3Unlocked: §7" + unlocked_homes.toString());
			else if(locked_homes.length() > 0)
				e.getPlayer().sendMessage("§3Locked: §7" + locked_homes.toString());
			else if(unlocked_homes.length() > 0)
				e.getPlayer().sendMessage("§3Unlocked: §7" + unlocked_homes.toString());
			func.updatefiles();
			double delta_time =  (double) (System.nanoTime() - time) / 1_000_000_000;
			Functions.print("§3@§fEvents.onInventoryClose§7 Executed in: §3" + delta_time + "§fs");
		}
		}catch(Exception ex) {
			func.ERROR("InvClose()", null, ex.toString(), ex.getStackTrace());
		}
	}
	
	
	public void onInventoryClick(InventoryClickEvent event) {
		try {
		Player p = (Player) event.getWhoClicked(); // The player that clicked the item
		
		 if(check_inv(event)) {
				if(event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase("§CReturn To Menu")  ) {
					event.setCancelled(true);

					if(func.checkSpam(p.getName(), (short) 4,2.5)) {
						
						p.closeInventory();
						p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
						p.sendMessage("§cDon't spam click");
						
						return;
					}
					
				
					
					p.openInventory(func.getEditMenu());
					p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1f);
					return;
				}
				}
		 
		 if (event.getView().getTitle().contains(INV_TITLE.ALLOW_PLAYER) ) {
				event.setCancelled(true);
				boolean check = event.getClick() == ClickType.LEFT && event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.COMPASS;
				
				if(check) {
					if(check_inv(event)) {
					event.setCancelled(true);
					if(func.player_wait.contains(p.getName())) {
						p.sendMessage("§3Still processing your previous commands");
						return;
					}else
						func.player_wait.add(p.getName());
					p.closeInventory();
					try {
					Thread thread3 = new Thread(new Thread3(event, p));
			        thread3.start();
					}catch(Exception e) {
						func.ERROR("Allowplayer Inventory click", p, e.toString(), e.getStackTrace());
						func.player_wait.remove(p.getName());
					}
				}
			}
		 }
		 
		 if (event.getView().getTitle().equals(INV_TITLE.SHOW_LOGS) ) {
				event.setCancelled(true);
				boolean check = event.getClick() == ClickType.LEFT && event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.COMPASS;
			
				if(check) {
					if(check_inv(event)) {
					event.setCancelled(true);
					p.closeInventory();
					List<String> lorelist = event.getCurrentItem().getItemMeta().getLore();
					String id_lore = lorelist.get(lorelist.size() -1).split(":")[1]; // Getting the home ID of the selected item
					int id = Integer.parseInt(id_lore);
					ItemStack book = null;
					try {
						book = func.getLogBook(id);
					} catch (SQLException e) {
						
						e.printStackTrace();
						func.ERROR("Show logs", p, e.toString(), e.getStackTrace());
					}
					p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
					p.openBook(book);
					
				}
			}
		 }
		 if (event.getView().getTitle().equals(ITEM_TITLE.CHANGE_ICON) ) {
				event.setCancelled(true);
				boolean check = event.getClick() == ClickType.LEFT && event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.COMPASS;
			
				if(check) {
					if(check_inv(event)) {
						List<String> lorelist = event.getCurrentItem().getItemMeta().getLore();
						String id_lore = lorelist.get(lorelist.size() -1).split(":")[1]; // Getting the home ID of the selected item
						p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
						p.openInventory(func.showMaterials(id_lore));
						if(func.checkSpam(p.getName(), (short) 1,1.5)) {
							
							p.closeInventory();
							p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
							p.sendMessage("§cDon't spam click");
							
							return;
						}
					
				}
			}			
		}
		 if (event.getView().getTitle().equals(INV_TITLE.CHANGE_ICON) ) {
				event.setCancelled(true);
				boolean check = event.getClick() == ClickType.LEFT && event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.COMPASS;
			
				if(check) {
					if(check_inv(event)) {
						if(func.checkSpam(p.getName(), (short) 1,1.5)) {
							
							p.closeInventory();
							p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
							p.sendMessage("§cDon't spam click");
							
							return;
						}
						List<String> lorelist = event.getCurrentItem().getItemMeta().getLore();
						String id_lore = lorelist.get(lorelist.size() -1).split(":")[1]; 
						p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
						func.change_icon(id_lore, event.getCurrentItem().getType().toString(), p, event.getCurrentItem().getItemMeta().getDisplayName());
						Inventory inv = func.inventory_Create(ITEM_TITLE.CHANGE_ICON, p.getName(), true, false, false,false, true);
						p.openInventory(inv);
				}
			}			
		}
		 if (event.getView().getTitle().equals(ITEM_TITLE.EDIT_PERMISSONS) ) {
			event.setCancelled(true);
			boolean check = event.getClick() == ClickType.LEFT && event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.COMPASS;
		
			if(check) {
				if(check_inv(event)) {
				event.setCancelled(true);
				List<String> lorelist = event.getCurrentItem().getItemMeta().getLore();
				String id = lorelist.get(lorelist.size() -1).split(":")[1]; // Getting the home ID of the selected item
				Functions.print("§3@§fInvClose.§cEdit_Permissions§f ID: §7" + id + " §fPlayer: §7" + p.getName());
				Inventory inv = func.showAllPlayers(id, p.getName(), true, false);
				event.setCancelled(true);
				if(inv == null) {
					if(func.checkSpam(p.getName(), (short) 3,1.5)) {
						
						p.closeInventory();
						p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
						p.sendMessage("§cDon't spam click");
						
						return;
					}
				p.sendMessage("§cThe selected homes does not have any addes players");	
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
				return;
				}
				
				p.openInventory(inv);
				return;
			}else
				return;
			
		}
		}
		if (event.getView().getTitle().equals(INV_TITLE.EDIT_PERMISSONS) ) {
			event.setCancelled(true);
			boolean check = event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.COMPASS;
			if(check) {
				if(check_inv(event))
			func.edit_player_perms(p, event.getInventory(), event.getCurrentItem(), event.getSlot(), event.getClick());
			}
			return;
			
		}
		else if (event.getView().getTitle().equals(INV_TITLE.LEAVE_HOME) ) {
			event.setCancelled(true);
			event.setCancelled(true);
			boolean check = event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.COMPASS;
			if(check) {
				if(check_inv(event))
			func.leave_home(p, event.getInventory(), event.getCurrentItem(), event.getSlot());
			}
		}
		else 	if (event.getView().getTitle().equals(ITEM_TITLE.SHOWING_PLAYERS) ) {
				event.setCancelled(true);
		}
		
		else if (event.getView().getTitle().equals(INV_TITLE.REMOVE_HOME) ) {
			 	event.setCancelled(true);
				boolean check = event.getClick() == ClickType.LEFT && event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.COMPASS;
				if(check) {
					if(check_inv(event))
					func.remove_home(p, event.getInventory(), event.getCurrentItem(), event.getSlot());
				}
			}
		if (event.getView().getTitle().equals(INV_TITLE.LOCK_HOME) ) {
			event.setCancelled(true);
			boolean check = event.getClick() == ClickType.LEFT && event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.COMPASS;
			if(check) {
				if(check_inv(event))
				func.lockhome(p, event.getInventory(), event.getCurrentItem(), event.getSlot());
			}
		}
		if (event.getView().getTitle().equals(INV_TITLE.DISALLOW_PLAYER) ) {
			event.setCancelled(true);
			boolean check = event.getClick() == ClickType.LEFT && event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.COMPASS;
			if(check) {
					if(check_inv(event))
					func.disallow_player(p, event.getInventory(), event.getCurrentItem(), event.getSlot());
				}
			}
		if (event.getView().getTitle().equals(ITEM_TITLE.DISALLOW_PLAYER) ) {
			event.setCancelled(true);
			boolean check = event.getClick() == ClickType.LEFT && event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.COMPASS;
			if(check) {
				
				if(check_inv(event)) {
				
				if(func.checkSpam(p.getName(), (short) 3,1.5)) {
					
					p.closeInventory();
					p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
					p.sendMessage("§cDon't spam click");
					
					return;
				}
				
				List<String> lorelist = event.getCurrentItem().getItemMeta().getLore();
				String id = lorelist.get(lorelist.size() -1).split(":")[1]; // Getting the home ID of the selected item
				Functions.print("§3@§fInvClose.§cDISALLOW_PLAYER§f ID: §7" + id + " §fPlayer: §7" + p.getName());
				Inventory inv = func.showAllPlayers(id, p.getName(), false, true);
				event.setCancelled(true);
				if(inv == null) {
					if(func.checkSpam(p.getName(), (short) 3,1.5)) {
						
						p.closeInventory();
						p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
						p.sendMessage("§cDon't spam click");
						
						return;
					}
				p.sendMessage("§cThe selected homes does not have any addes players");	
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1f);
				return;
				}
				
				p.openInventory(inv);
			}
		}
		}
		
		
		if (event.getView().getTitle().equals(ITEM_TITLE.SHOW_PLAYERS) ) {
			event.setCancelled(true);
		boolean check = event.getClick() == ClickType.LEFT && event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.COMPASS && check_inv(event);
		
		if(check) {
			if(check_inv(event)) {
			event.setCancelled(true);
			List<String> lorelist = event.getCurrentItem().getItemMeta().getLore();
			String id = lorelist.get(lorelist.size() -1).split(":")[1]; // Getting the home ID of the selected item
			Functions.print("§3@§fInvClose.§cSHOW_PLAYER§f ID: §7" + id + " §fPlayer: §7" + p.getName());
			Inventory inv = func.showAllPlayers(id, p.getName(), false, false);
			event.setCancelled(true);
			if(inv == null) {
				if(func.checkSpam(p.getName(), (short) 2,1.5)) {
					
					p.closeInventory();
					p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
					p.sendMessage("§cDon't spam click");
					
					return;
				}
			p.sendMessage("§cThe selected homes does not have any addes players");	
			p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
			return;
			}
			
			p.openInventory(inv);
			
		}
		}
		
	}
	if (event.getView().getTitle().equals("§6Home §9Menu") ) {
		event.setCancelled(true);
		boolean check = event.getClick() == ClickType.LEFT && event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR;
		
		if(check) {
			if(check_inv(event)) {
			event.setCancelled(true);
			String name = event.getCurrentItem().getItemMeta().getDisplayName();
			boolean lock = false;
			boolean edit = false;
			boolean show_added = false;
			boolean show_homes = true;
			String inv_title = "§6Witch §9Home?";
			if(func.checkSpam(p.getName(), (short) 4,2.5)) {
				
				p.closeInventory();
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
				p.sendMessage("§cDon't spam click");
				return;
			}
			
			switch (name) {
				case  ITEM_TITLE.DISALLOW_PLAYER:
					inv_title = ITEM_TITLE.DISALLOW_PLAYER;
					edit = true;
					break;
				case ITEM_TITLE.SHOW_PLAYERS:
					inv_title = ITEM_TITLE.SHOW_PLAYERS;
					edit = true;
					break;
				case ITEM_TITLE.EDIT_PERMISSONS:
					inv_title = ITEM_TITLE.EDIT_PERMISSONS;
					edit = true;
					break;
				case ITEM_TITLE.LOCK_HOME:
					inv_title = INV_TITLE.LOCK_HOME;
					lock = true;
					break;
				case ITEM_TITLE.REMOVE_HOME:
					inv_title = INV_TITLE.REMOVE_HOME;
					break;
				case ITEM_TITLE.TELEPORT_HOME:
					inv_title = INV_TITLE.TELEPORT_HOME;
					show_added = true;
					break;
				case ITEM_TITLE.CHANGE_ICON:
					inv_title = ITEM_TITLE.CHANGE_ICON;
					break;
				case ITEM_TITLE.SHOW_LOGS:
					inv_title = INV_TITLE.SHOW_LOGS;
					break;
				case ITEM_TITLE.LEAVE_HOME:
					inv_title = INV_TITLE.LEAVE_HOME;
					show_homes = false;
					show_added = true;
					break;
			
			}
			
			Inventory inv = null;
		
			try {
				
				inv = func.inventory_Create(inv_title, p.getName(), show_homes, show_added, lock,edit, true);
			}catch(Exception e) {
				e.printStackTrace();
			}
			
			if(inv == null) {
				p.sendMessage("§cYou don't have any homes and is not allowed to any");
				return;
			}
			p.openInventory(inv);
			p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1f);
		}
		}
		event.setCancelled(true);
	}
	
	
		
	if (event.getView().getTitle().equals(INV_TITLE.TELEPORT_HOME) ) {
		event.setCancelled(true);
		boolean check = event.getClick() == ClickType.LEFT && event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.COMPASS;;
		if(check) {
			if(check_inv(event)) {
			p.closeInventory();
			
			List<String> lorelist = event.getCurrentItem().getItemMeta().getLore();
			String id = lorelist.get(lorelist.size() -1).split(":")[1]; // Getting the home ID of the selected item
			Functions.print("§3@§fInvClose.§cTELEPORT_HOME§f ID: §7" + id + " §fPlayer: §7" + p.getName());
		
			Map<?, ?> data = ((Map<?, ?>)func.homes.get(id));
			
			Location tloc = func.convertLoc((String) data.get("Location"));
			
			p.teleport(tloc);
			p.sendMessage("§3Teleported to §7" + data.get("Name"));
			p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_GUITAR, 1f, 1f);
			}
		}
	}
		}catch(Exception ex) {
			ex.printStackTrace();
			func.ERROR("InventoryClick()", null, ex.toString(), ex.getStackTrace());
		}
	
	}
	public boolean check_inv(InventoryClickEvent event) {
		
		if (event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR) || event.getRawSlot() >= event.getInventory().getSize() ||event.getSlot() > event.getView().getTopInventory().getSize()) {
			
			return false;
		}
		
		return true;
	}

	public  class Thread3 implements Runnable {
		
		public InventoryClickEvent event;
		public Player p;
		public Thread3(InventoryClickEvent event, Player p) {
			this.event = event;
			this.p = p;
			
		}
	
		@SuppressWarnings("unchecked")
		@Override
        public synchronized  void run() {
			
			List<String> lorelist = event.getCurrentItem().getItemMeta().getLore();
			String id_lore = lorelist.get(lorelist.size() -1).split(":")[1]; // Getting the home ID of the selected item
			int id = Integer.parseInt(id_lore);
			String pname = event.getView().getTitle().split(" ")[2];
			int count = 0;
			int key = 0;
			try {
				count = db.getCount("SELECT COUNT(player_id) FROM player_allowed WHERE by_home = " + id + " and player_id = (SELECT ID FROM players p WHERE p.Nickname = '" + pname + "');");
				if(count > 0) {
					p.sendMessage("§c" + pname + " is already allowed");
					Functions.print("§@3§fonInventoryClick.§cAllowPlayer §f Returning because §f"+ pname +"§c  is already added §fIssued by:§c" + p.getName());
					func.player_wait.remove(p.getName());
					return;
				}
				count = db.getCount("SELECT COUNT(Creator_ID) FROM player_homes WHERE ID = " + id + " and Creator_ID = (SELECT ID FROM players p WHERE p.Nickname = '" + pname + "');");
				if(count > 0) {
					p.sendMessage("§c" + pname + " is already allowed");
					Functions.print("§@3§fonInventoryClick.§cAllowPlayer §f Returning because §f"+ pname +"§c  is already added §fIssued and is the owner. -by:§c" + p.getName());
					func.player_wait.remove(p.getName());
					return;
				}
				 key =  db.allow_Player(id, pname);
				
				 Functions.print("§3@§fonInventoryClick.§cAllowPlayer§f SQL §aSuccesfull");
				 Functions.print("§3@§fonInventoryClick.§cAllowPlayer§f COUNT; §7 " + count + "§fKey: + §7" + key);
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				func.ERROR("onInventoryClick Allow Player", p, e1.toString(), e1.getStackTrace());
				func.player_wait.remove(p.getName());
				return;
			}
			if(count > 1) {
				p.sendMessage("§c" + pname + " is already added");
				Functions.print("§3@§fonInventoryClick.§cAllowPlayer §f Returning because §f"+ pname +"§c  is already added §fIssued by:§c" + p.getName());
				func.player_wait.remove(p.getName());
				return;
			}
			
			try {
			// --------------- Add player | Allow player ----------------------
			Object obj = func.getJsonFile("allowed_players.json");
			JSONObject allowed_p = (JSONObject) obj;
			Object obj2 = func.getJsonFile("player_homes.json");
			JSONObject homes = (JSONObject) obj2;
		
			Map<String, Object> data2 = ((Map<String, Object>)homes.get(id_lore));
			Map<String, Object> map = new LinkedHashMap<String, Object>(4);
		 	map.put("by_home", id);
		    map.put("player_name", pname);
	        map.put("can_lock", false);
	        map.put("can_add", false);
	        String query_event_logs = "INSERT INTO home_event_log (player_name, action, by_home, target_player) VALUES  ('" + p.getName() + "', '" + LogAction.ALLOWED_PLAYER + "', " 
	        							+ id + ", '" + pname + "');";
	        
	        Thread thread2 = new Thread(new Thread2(query_event_logs));
	        int allowed = Integer.parseInt(data2.get("Allowed_Players").toString());
			allowed++;
			
			data2.replace("Allowed_Players", allowed);
			homes.replace(id, data2);
			
			allowed_p.put(key , map);
	        func.WriteJsonPAllowed(allowed_p.toJSONString());
	        func.WriteJsonHomes(homes.toJSONString());
			p.sendMessage("§3Allowed §7" + pname + " §3To §7"+ event.getCurrentItem().getItemMeta().getDisplayName().replace("§6", "") );
			func.updatefiles();
			thread2.start();
			func.player_wait.remove(p.getName());
			}catch(Exception ex) {
				func.ERROR("Thread3 Allowed Player " , p, ex.toString(), ex.getStackTrace());
				func.player_wait.remove(p.getName());
			}
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
					Functions.print("§3@§fEvents§3@§fThread1.§cRun §3SQL §aSUCCESFULL");
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					func.ERROR("Error player onCloseInv", null, e.toString(), e.getStackTrace());
					e.printStackTrace();
					Functions.print("§c[ERROR]§3@§fEvents§3@§fThread1.§cRun §3SQL §cFAILED");
					
				}
			     
			   
	        }
	    }


	public  class Thread2 implements Runnable {
		
		String query = "";
		public Thread2(String query) {
			this.query = query;
		}

		@Override
	    public synchronized void run() {
			 Connection conn;
			try {
				conn = Database.dataSource.getConnection();
				Statement stmt = conn.createStatement();
				stmt.execute(query);
				Functions.print("§3@§fEvents§3@§fThread2.§cRun §3SQL §aSUCCESFULL");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				func.ERROR("Disallowing player onCloseInv", null, e.toString(), e.getStackTrace());
				e.printStackTrace();
				Functions.print("§c[ERROR]§3@§fEvents§3@§fThread2.§cRun §3SQL §cFAILED");
			}
		     
		   
	    }
	}	
	}
