package plugin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.Dispenser;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Dropper;
import org.bukkit.block.EnderChest;
import org.bukkit.block.Furnace;
import org.bukkit.block.Hopper;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Repeater;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityCreatePortalEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import plugin.Const.BlockHistory;
import plugin.Const.INV_TITLE;
import plugin.Const.ITEM_TITLE;
import plugin.Const.Infraction;


public class Main extends JavaPlugin implements Listener{
	
	//Values
	public static Main plugin = null;
	public static FileConfiguration config;
	public Functions func;
	public Database db;
	public Events events;
	public List<Block> undo_blocks = new ArrayList<>();
	

			
	Map<UUID, UUID> tprequest = new HashMap<>();
	public String[] welcomes_messages = {"just joined the server - glhf!", "joined your party.", "just showed up. Hold my beer.",
										"just joined. Hide your bananas.","has arrived. Party's over.", "We were expecting you",
										"We hope you brought pizza.","Leave your weapons by the door."};	
	
	public static boolean db_connected = false;
	public void onEnable(){
		plugin = this;
		config = getConfig();
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		
//		if(Bukkit.getWorld("world3") == null)
//		{
//			Functions.print("Loading world3");
//			Main.plugin.getServer().createWorld(new WorldCreator("world3"));
//			
//		}
		
		db = new Database();
		
		try {
			func = new Functions(db);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		events = new Events(func, db);
		
		if(db_connected) {
		plugin.getServer().getConsoleSender().sendMessage(Database.Author_text + " PortalPlugin §aEnabled");
		}else {
			plugin.getServer().getConsoleSender().sendMessage(Database.Author_text + " PortalPlugin §aEnabled §c[LIMITED FUNCTIONS]");
			func.ERROR("onEnable()", null, "Database is not connected",  new Throwable().getStackTrace());
			
		}
			
	}
	
	@SuppressWarnings({ "deprecation", "static-access", "unchecked" })
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
			
		//Checking sender is a player and not console
		if(sender instanceof Player){
			
			
			
			if(label.equalsIgnoreCase("openinv")){ // Open Inventory
				Player p = (Player) sender;
				if(p.isOp()) {
					String pname = args[0];
					if(!pname.isEmpty()) {
						Player t = Bukkit.getPlayer(pname);
						if(t != null) {
							Inventory inv = t.getInventory();
							p.openInventory(inv);
							p.sendMessage("§3Showing inventory for §7" + pname);
						}else 
							p.sendMessage("§cCould not find Player: " + pname);
					}
					else 
						p.sendMessage("§c/openinv [Player]");
				}
				else 
					p.sendMessage("§cYou rehave to be a server operatior to use this command.");
				
			}else if(label.equalsIgnoreCase("vanish")){ //Vanish 
				Player p = (Player) sender;
				if(p.isOp()) {
					List<String> vanished_players = Main.config.getStringList("Vanish");
					
					if(!vanished_players.contains(p.getName())) {
						
						for(Player player : Bukkit.getServer().getOnlinePlayers()) {
							if(!player.isOp()) {
								player.hidePlayer(p);
								player.sendMessage(p.getPlayerListName() + "§7 Left the game.");
							}else 
								player.sendMessage(p.getPlayerListName() + "§4 Just went vanish for your information");
						}
						
						p.sendMessage("§3Vanish §aenabled");
						
						vanished_players.add(p.getName());
						Main.config.set("Vanish", vanished_players);
						plugin.saveConfig();
					}else {
						for(Player player : Bukkit.getServer().getOnlinePlayers()) {
							
								player.showPlayer(p);
						}
						p.sendMessage("§3Vanish §cdisabled");
						vanished_players.remove(p.getName());
						Main.config.set("Vanish", vanished_players);
						plugin.saveConfig();
						
						
					}
					
					
					}else 
						p.sendMessage("§cYou have to be a server operatior to use this command.");
				}else if(label.equalsIgnoreCase("punishstick")){
					Player p = (Player) sender;
					if(p.isOp()) {
						
						ItemStack item = new ItemStack(Material.BLAZE_ROD);
						item.getItemMeta().setDisplayName("§c§lPortal's Punish Stick");
						p.playSound(p.getLocation(),Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1f);
						p.playEffect(p.getLocation(), Effect.BOW_FIRE, 1);
						p.sendMessage("§7 Be carefull with it");
						p.getInventory().addItem(item);
					}
					
					
					
				}else if(label.equalsIgnoreCase("firework")){
					Player p = (Player) sender;
						
					spawnFireworks(p.getLocation(), 10);
					
					
				}else if(label.equalsIgnoreCase("msg")){
					Player p = (Player) sender;
						if(!(args[0].isEmpty() && args[1].isEmpty())) {
							
						
						Player t = Bukkit.getPlayer(args[0]);
						if(t != null) {
						StringBuilder sb = new StringBuilder();
						for (int i = 1; i < args.length; i++)
							 sb.append(args[i]).append(' ');
						
						String msg = sb.toString().trim();
						t.sendMessage(p.getPlayerListName() + " §6messaged §2you:§7 " + msg);
						
						p.sendMessage("§2You §6messaged " + t.getPlayerListName() + " §7" + msg);
						t.playSound(t.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 1);
						
						}else
							p.sendMessage("§cCould not find player: §f" + args[0]);
						}else
							p.sendMessage("§/msg [Player] [Messages]");
					
				}else if(label.equalsIgnoreCase("tpa")){
					Player p = (Player) sender;
					if(!(args[0].isEmpty())) {
						Player t = Bukkit.getPlayer(args[0]);
						if(t != null) {
							
							tprequest.put(p.getUniqueId(),t.getUniqueId());
							t.playSound(t.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 1);
							t.sendMessage(p.getDisplayName() + "§7 wants to teleport to you. Write to /tpaccept");
							p.sendMessage("§7Tp request sent to: " + t.getDisplayName());
							
						}else
							p.sendMessage("§cCould not find player: §f" + args[0]);
							
						
					}else 
						p.sendMessage("§c/tp [Player])");
					
					}
				
			
				
		
		if(label.equalsIgnoreCase("home")){
			
			Player p = (Player) sender;
			
			
			if(Main.config.getString("muted").contains(p.getName())) {
				
				p.sendMessage("§cYou can not use the home commands while §6muted");
				return false;
			}
			
			
			if(args.length == 0) {

				if(!db_connected) {
					p.sendMessage("§cThis function is disabled because the Database is not connected");
					p.sendMessage("§2Your houses will still be protected. ");
					p.sendMessage("§3You can still use /home tp to teleport to your homes");
					return false;
				}
			p.openInventory(func.getEditMenu());
			return false;
			}
			
			if( args == null || args.length > 3) { p.sendMessage("§c§lError");  return true; }
			if(args[0].equalsIgnoreCase("set")) {
				if(func.checkSpam(p.getName(), (short) 2,5)) {
					
					p.kickPlayer("§cSpaming home set command");
					return false;
				}
				if(!db_connected) {
					p.sendMessage("§cThis function is disabled because the Database is not connected");
					return false;
				}
				
				if(args.length > 2) {
					if(!func.checkBadWords(args[1], p, "home set")) {
						Functions.print("bad words ? -> home set");
						return false;
					}
					
				
					short rad;
					try {
						
				        rad = Short.parseShort(args[2]);
				       
				    } catch (NumberFormatException e) {
				       p.sendMessage("§cRadius must be an integer");
				       p.playSound(p.getLocation(),Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
				       return false;
				    }
					if(rad <= 300 && rad != 1) {
						
						String kname = args[1].toLowerCase();
						
						if(kname.length() > 20) {
							p.sendMessage("§cWrite a shorter name, Max length is 20 characters ");
							p.playSound(p.getLocation(),Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
							return false;
						}
						
						
						String regex = "^[A-Za-z0-9_]*$";
						Pattern pattern = Pattern.compile(regex);
						Matcher matcher = pattern.matcher(kname);
						
						
						if(!matcher.matches()) {
							
							p.sendMessage("§cThe 'home name' can only have Letters, Nummbers and Underscores.");
							p.playSound(p.getLocation(),Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
							return false;
						}
						if(func.player_wait.contains(p.getName())) {
							p.sendMessage("§3Still processing your previous commands");
							return false;
							
						}
						
						if(p.getWorld().getEnvironment().equals(World.Environment.NETHER) ||  p.getWorld().getEnvironment().equals(World.Environment.THE_END)) {
							p.sendMessage("§cYou can no longer place homes in NETHER or THE END");
							return false;
						}
						
						if(func.toManyHouses(p.getName()) == true) {
							p.sendMessage("§cYou have reached the maximum amount of homes");
							p.sendMessage("§9Solve this by leaving or removing a home via /home");
							return false;
						}
						
						int size = 0;
						
						for (Object id : func.homes.keySet()) {
						Map<String, Object> data = ((Map<String, Object>)func.homes.get(id.toString()));
						String pname = data.get("Creator").toString();
						String name = data.get("Name").toString();
						String output = func.intefears(p, id.toString(), p.getLocation(), rad, true);
			     		if(p.getName().equals(pname)) {
			     			size++;
			     			if(name.equalsIgnoreCase(kname)) {
			     				p.sendMessage("§cYou already have a home called: §7"  + kname);
			     				p.playSound(p.getLocation(),Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
			     				return false;
			     			}
			     		}
						
			     		if(output != "") {
			     			
				    		
			    			p.sendMessage("§c§lTHIS AREA IS ALREADY PROTECTED");
			        		if(p.isOp())
			    				p.sendMessage("§6§lKEY:§f" + id);
			        		p.playSound(p.getLocation(),Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
			        		return false;
			     			}
				    	}
						
				    	
					    if(size <= 27) {
					    int rmd = (int)(Math.random() * (func.materials.length));
					    String mname = func.materials[rmd].name();
					    func.player_wait.add(p.getName());
					    Thread thread1 = new Thread(new Thread1(kname, p,mname, p.getLocation(), rad , func.homes));
				        thread1.start();
						
					
					    	}else
								p.sendMessage("§cYou have execded the max numbers of homes §7(27)");
						
					}else
				p.sendMessage("§cThe max radius is 300. Minimum: 2");
					
			}else
				p.sendMessage("§c/home set [Home Name] [Radius]");
			}
			
			else if(args[0].equalsIgnoreCase("remove")){
				if(!db_connected) {
					p.sendMessage("§cThis function is disabled because the Database is not connected");
					return false;
				}
				Inventory inv = null;
				if(args.length > 1 && p.isOp()) {
					
					inv = func.inventory_Create(INV_TITLE.REMOVE_HOME, args[1], true, false, false,false,false);
					if(inv != null) {
						
						p.openInventory(inv);
						p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
						
			    	}else
			    		p.sendMessage("§7" + args[1] + "§c does do not have any homes right now..");
				}else {
				 inv = func.inventory_Create(INV_TITLE.REMOVE_HOME, p.getName(), true, false, false,false,true);
				if(inv != null) {
					
					p.openInventory(inv);
					p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
					
		    	}else
		    		p.sendMessage("§cYou do not have any homes right now");
				}
	    	
		}else if(args[0].equalsIgnoreCase("tp")){
			
			
			if(p.isOp()) {
				if(args.length > 1 && args.length < 3) {
					
					Inventory inv = func.inventory_Create(INV_TITLE.TELEPORT_HOME, args[1], true,false, false,false,false);
					
					if(inv != null) {
						
						p.openInventory(inv);
						p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
			    	
					}else {
			    		p.sendMessage("§3" + args[1] + "§c does do not have any homes right now");
			    		
				}
					return false;
				}
			}
			if(func.checkSpam(p.getName(), (short) 2,5)) {
				
				p.kickPlayer("Spaming home tp command");
				return false;
			}
			Inventory inv = func.inventory_Create(INV_TITLE.TELEPORT_HOME, p.getName(), true, true, false,false,false);
			
			if(inv != null) {
				
				p.openInventory(inv);
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
	    	
			}else
	    		p.sendMessage("§cYou do not have any homes right now..");
			
		} else if(args[0].equalsIgnoreCase("add")){
			if(!db_connected) {
				p.sendMessage("§cThis function is disabled because the Database is not connected");
				p.sendMessage("§2Your houses will still be protected. ");
				p.sendMessage("§3You can still use /home tp to teleport to your homes");
				return false;
			}
			if(func.checkSpam(p.getName(), (short) 2,5)) {
				
				p.kickPlayer("Spaming home add command");
				return false;
			}
			if(args.length > 1) {
				
				boolean played = Bukkit.getOfflinePlayer(args[1]).hasPlayedBefore();
				if(p.getName().equalsIgnoreCase(args[1])) { p.sendMessage("§cYou can not add your self.."); return false;}
				
				if(played) {
				
					if(func.toManyHouses(args[1]) == true) {
						p.sendMessage("§cThe player you want to add have reached the maximum amount of homes");
						p.sendMessage("§7The player must leave or remove a home first");
						return false;
					}
					
				Inventory inv = func.inventory_Create(INV_TITLE.ALLOW_PLAYER +"§0 " + args[1], p.getName(), true,false, false,true,true);
				
				if(inv != null) {
					
					p.openInventory(inv);
					p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
					
		    	
				}else
		    		p.sendMessage("§cYou do not have any homes right now..");
			}else
				p.sendMessage("§cCould not find player: §7" + args[1]);
			}else
				p.sendMessage("§c/add [Player_Name]");
			
			if(!db_connected) {
				p.sendMessage("§cThis function is disabled because the Database is not connected");
				p.sendMessage("§2Your houses will still be protected. ");
				p.sendMessage("§3You can still use /home tp to teleport to your homes");
				return false;
			}
		} if(args[0].equalsIgnoreCase("lock")){
			if(p.isOp()) {
				if(args.length > 1 && args.length < 3) {
					
					Inventory inv = func.inventory_Create(INV_TITLE.LOCK_HOME, args[1], true, false, true,false, false);
					if(inv != null) {
						
						p.openInventory(inv);
						p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
						p.sendMessage("§3Opened Inventory '" + INV_TITLE.LOCK_HOME + "§3' for player: §7", args[2]);
					}else
			    		p.sendMessage("§3" + args[1] + "§c does do not have any homes right now");
					}
				}
			} 
		if(args[0].equalsIgnoreCase("edit") && args[1].equalsIgnoreCase("permissons")){
			if(p.isOp()) {
				if(args.length > 2 && args.length < 4) {
					
					Inventory inv = func.inventory_Create(ITEM_TITLE.EDIT_PERMISSONS, args[2], true, false, false,true, false);
					if(inv != null) {
						
						p.openInventory(inv);
						p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
						p.sendMessage("§3Opened Inventory '" + ITEM_TITLE.EDIT_PERMISSONS + "§3' for player: §7", args[2]);
			    	
					}else
			    		p.sendMessage("§3" + args[1] + "§c does do not have any homes right now");
					}
				}
			} if(args[0].equalsIgnoreCase("show")){
				if(p.isOp()) {
					if(args.length > 1 && args.length < 3) {
						
						Inventory inv = func.inventory_Create(INV_TITLE.TELEPORT_HOME, args[1], true,true, false,false,false);
						
						if(inv != null) {
							
							p.openInventory(inv);
							p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
				    	
						}else
				    		p.sendMessage("§3" + args[1] + "§c does do not have any homes right now");
						
					}else
						p.sendMessage("§c/home show homes [Player]");
				}else
					p.sendMessage("§cThis command is only for operators");
			
						}else if(args[0].equalsIgnoreCase("edit") ){
							
							if(p.isOp()) {
								if(!db_connected) {
									p.sendMessage("§cThis function is disabled because the Database is not connected");
									return false;
								}
								if(args.length > 2) {
										
										int id;
										short rad;
										try {
									        id = Integer.parseInt(args[1]);
									       
									    } catch (NumberFormatException e) {
									    	p.sendMessage("§cTHE ID MUST BE AN §2INTEGER§c YOU STUPID?");
									    	p.sendMessage("§cNOT LIKE THIS: §7" + args[1]);
									    	p.getWorld().strikeLightning(p.getLocation());
									       return false;
									    }
										try {
									        rad = Short.parseShort(args[2]);
									       
									    } catch (NumberFormatException e) {
									    	p.sendMessage("§cTHE RADIUS MUST BE AN §2INTEGER§c YOU STUPID?");
									    	p.sendMessage("§cNOT LIKE THIS: §7" + args[2]);
									    	p.getWorld().strikeLightning(p.getLocation());
									       return false;
									    }
										Object obj = func.getJsonFile("player_homes.json");
										JSONObject jo = (JSONObject) obj;   
										if(jo.containsKey(id + "")) {
										try {
											int i = db.update_home_radius(id, rad);
											if(i == Const.ERROR) {
												p.sendMessage("§cSQL PASSED but no lines where in inserted");
												return false;
											}
										} catch (SQLException e) {
											p.sendMessage("§cYOU FUCKED UP");
											p.sendMessage("§7" + e.toString());
											return false;
										}
										Map<String, Object> data = ((Map<String, Object>)jo.get(id + ""));
										Object name = data.get("Name");
										Object radius = data.get("Radius");
										data.replace("Radius", rad);
										jo.replace(id, data);
										func.WriteJsonHomes(jo.toJSONString());
										func.updatefiles();
										p.sendMessage("§3Changed Radius for §7" + name + "§3 from §7" + radius + "§3 to §7" + rad + "§3 - ID: §7" + id + "§3");
										p.playSound(p.getLocation(), Sound.BLOCK_BELL_USE, 1, 1);
										}else {
											p.sendMessage("§cCould not find ID:§7" + id);
										}
									
									
								}else {
									
									p.sendMessage("§c/HOME EDIT [ID] [NEW_RADIUS]");
									p.sendMessage("§7How hard can it be? -_-");
									p.playSound(p.getLocation(), Sound.BLOCK_GLASS_BREAK, 1, 1);
								}
							}
							
						}else if(args[0].equalsIgnoreCase("debug") ){
							if(p.isOp()) {
								  
							    List<String> debug_players = Main.config.getStringList("Homes.debug");
							    if(debug_players.contains(p.getName())) {
							    	debug_players.remove(p.getName());
							    	Main.config.set("Homes.debug",debug_players);
							    	Main.plugin.saveConfig();
							    	p.sendMessage("§3Debuging §cdisabled");
							    }else {
							    	debug_players.add(p.getName());
							    	Main.config.set("Homes.debug",debug_players);
							    	Main.plugin.saveConfig();
							    	p.sendMessage("§3Debuging §aenabled");
							    }
							}
							
						}else if(args[0].equalsIgnoreCase("breaker") ) {
							if(!p.isOp()) {
								p.sendMessage("§cOp command only");
								return false;
							}
							ItemStack is = new ItemStack(Material.DIAMOND_PICKAXE);
							ItemMeta im = is.getItemMeta();
							im.addEnchant(Enchantment.SILK_TOUCH, 1,true);
							im.addEnchant(Enchantment.DIG_SPEED, 4,true);
							im.addEnchant(Enchantment.DURABILITY,1,true);
							im.setDisplayName("§cThe Breaker");
							is.setItemMeta(im);
							p.getInventory().addItem(is);
						}
						else if(args[0].equalsIgnoreCase("placer") ) {
							if(!p.isOp()) {
								p.sendMessage("§cOp command only");
								return false;
							}
							ItemStack is = new ItemStack(Material.DIAMOND_PICKAXE);
							ItemMeta im = is.getItemMeta();
						
							im.setDisplayName("§cThe Placer");
							is.setItemMeta(im);
							p.getInventory().addItem(is);
						}
						else if(args[0].equalsIgnoreCase("update") ) {
							
							
							if(!p.isOp()) {
								p.sendMessage("§cOp command only");
								return false;
							}
							if(!db_connected) {
								p.sendMessage("§cThis function is disabled because the Database is not connected");
								return false;
							}
							p.sendMessage("[Database]: §6Checking Connection");
								
								try {
									db.testDataSource(db.dataSource);
									
									p.sendMessage("[Database]: §aConnected");
									
									
								} catch (SQLException e1) {
									// TODO Auto-generated catch block
									p.sendMessage("§cCould not connect to to database. See console for error");
									p.sendMessage(e1.toString());
									e1.printStackTrace();
									
								}
								p.sendMessage("§7Updating..");
								Thread thread2 = new Thread(new Thread2(p));
								Thread thread3 = new Thread(new Thread3(p));
								
								thread2.start();
								thread3.start();

								
							
						
						}
						else if(args[0].equalsIgnoreCase("loop") ) {
							
							
							if(!p.isOp()) {
								p.sendMessage("§cOp command only");
								return false;
							}
							
							StringBuilder uuid_values = new StringBuilder();
							for(OfflinePlayer t : Bukkit.getOfflinePlayers()) {
							
								if(t == null) { continue; }
								
							
								String name = t.getName();
								System.out.println(name);
								Date date = new Date(t.getFirstPlayed());
								SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								String firstPlayed = sdf.format(date);
								
								if(name != null) {
								p.sendMessage(name);
								p.sendMessage(t.getUniqueId());
								if(uuid_values.length() == 0)
									uuid_values.append("('" +  name + "', '" + t.getUniqueId()   +  "', '" + firstPlayed + "')");
								else
									uuid_values.append(",('" + name +  "', '" + t.getUniqueId() +  "', '" + firstPlayed +  "')");
								}
							}
							String query_event_logs = "INSERT INTO players (Nickname, UUID, Joined) VALUES " + uuid_values.toString() +";";
							try {
								db.execute(query_event_logs);
								Functions.print("§3SQL §aSuccesfull");
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								p.sendMessage("§c" + e.toString());
							} 
							
						}
						else if(args[0].equalsIgnoreCase("menu") ) {
							p.sendMessage("");
							p.sendMessage("§§f-----§7Home Menu Functions§f-----");
							p.sendMessage("");
							p.sendMessage("§3Can_add§f: Makes the player able to remove / add other players");
							p.sendMessage("§7->§f Toggle by §7Right Clicking§f on 1 or more players");
							p.sendMessage("");
							p.sendMessage( "§3Can_lock§f: Makes the player able to lock and unlock the home");
							p.sendMessage("§7->§f Toggle by §7Left Clicking§f on 1 or more players");
							p.sendMessage("");
							p.sendMessage( "§3Lock Home§f: Makes doors and trapdoors locked");
							p.sendMessage("§7->§f Toggle lock by §7Left Clicking§f on 1 or more homes");
							p.sendMessage("");
							p.sendMessage( "§3Home Logs§f: Gives a book of registered events");
							p.sendMessage("§7->§f Receive by §7Left Clicking§f on the home");
							p.sendMessage("");
							p.sendMessage( "§3Diallow Player§f: disallows a player from the selected home");
							p.sendMessage("§7->§f Disallow by §7Left Clicking§f on 1 or more players");
							p.sendMessage("");
							p.sendMessage( "§3Remove home§f: Removes the selected home");
							p.sendMessage("§7->§f Remove by §7Left Clicking§f on the home");
							p.sendMessage("");
							p.sendMessage( "§3Teleport home§f: Teleports you to the selected home");
							p.sendMessage("§7->§f Teleport by §7Left Clicking§f on the home");
						
						}
						else if(args[0].equalsIgnoreCase("permissions") ) {
							p.sendMessage("");
							p.sendMessage("§§f-----§7Creator permisions§f-----");
							p.sendMessage("");
							p.sendMessage("§7->§f Can remove the home");
							p.sendMessage("§7->§f Can see the home logs");
							p.sendMessage("§7->§f Can edit allowed players permissions");
							p.sendMessage("§7->§f Can edit the home name");
							p.sendMessage("§7->§f Can change the radius");
							p.sendMessage("");
							p.sendMessage("§§f---§7Allowed Players Permissions§f---");
							p.sendMessage("§7->§f Can teleport to the home");
							p.sendMessage("§7->§f Can place/break blocks");
							p.sendMessage("§7->§f Can open/close doors");
							p.sendMessage("§7->§f Can open regular chests");
							p.sendMessage("§7->§f Can with perm (can_add) disallow or allow players");
							p.sendMessage("§7->§f Can with perm (can_lock) lock or unlock the home");
						}
						else if(args[0].equalsIgnoreCase("commands") ) {
							p.sendMessage("");
							p.sendMessage("§§f-----§7Home Commands§f-----");
							p.sendMessage("");
							p.sendMessage( "§3/home tp§f: Opens the teleport menu");
							p.sendMessage("§7->§f Teleport by §7Left Clicking§f on the home");
							p.sendMessage("");
							p.sendMessage( "§3/home add [player_name]§f: Opens the player allow menu");
							p.sendMessage("§7->§f Allow player by §7Left Clicking§f on the home");
							p.sendMessage("");
							p.sendMessage( "§3/home set [name] [radius]§f: Creates a home");
							p.sendMessage("§7->§f Calculate the radius (Min: 2, Max: 300)");
							p.sendMessage("§7->§f Count the blocks from the center to the furthest point");
							p.sendMessage("§7->§f Stand in the center when performing the home set command");
							p.sendMessage("§7->§f The Name can be max 20 charecters.");
							p.sendMessage("§7->§f The Name can not be your ingame name");
							p.sendMessage("§7->§f The Name can only contain Letters, nummbers & underscores");
							p.sendMessage("§7->§f You can only use the same name once");
							p.sendMessage("§7--->§f But you can Create same name as other players homes");
							
						}
						else if(args[0].equalsIgnoreCase("info") ) {
							p.sendMessage("");
							p.sendMessage("§§f-----§7Home Info§f-----");
							p.sendMessage("");
							TextComponent message = new TextComponent("§3/home commands§f or §6Click Here§f to see §7Commands");
							message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/home commands"));
							message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7Click§f to Display Commands").create()));
							p.spigot().sendMessage(message);
							p.sendMessage("");
							
							TextComponent message2 = new TextComponent("§3/home permissions§f or §6Click Here§f to see §7Permissions");
							message2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/home permissions"));
							message2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7Click§f to Display Permissions").create()));
							p.spigot().sendMessage(message2);
							p.sendMessage("");
							TextComponent message3 = new TextComponent("§3/home menu§f or §6Click Here§f to see §7Menu Functions");
							message3.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/home menu"));
							message3.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7Click§f to display Menu Functions").create()));
							p.spigot().sendMessage(message3);
							
						}else if(args[0].equalsIgnoreCase("opcommands") ) {
							p.sendMessage("");
							p.sendMessage("§§f-----§7Op Commands§f-----");
							p.sendMessage("");
							p.sendMessage( "§3/home show [player_name]§f: Opens the teleport menu");
							p.sendMessage("§7->§f Teleport by §7Left Clicking§f on the home");
							p.sendMessage("");
							p.sendMessage( "§3/home remove [player_name]§f: Opens the remove menu");
							p.sendMessage("§7->§f Remove home by §7Left Clicking§f on the home");
							p.sendMessage("");
							p.sendMessage( "§3/home lock [player_name]§f: Opens the lock menu");
							p.sendMessage("§7->§f Lock home by §7Left Clicking§f on the home");
							p.sendMessage("");
							p.sendMessage( "§3/home edit [home_id] [new_radius] §f: Edit the radius");
							p.sendMessage("§7->§f Get the ID thorugh /home debug or the Item Lore");
							p.sendMessage("");
							p.sendMessage("§3/mute [player_name] [reason]§f: Mutes the player");
							p.sendMessage("§7->§f The mute will diallow the chat & home commands");
							p.sendMessage("");
							p.sendMessage("§3/unmute [player_name] [reason]§f: unmutes the player");
							p.sendMessage("§7->§f Will remove the mute");
							p.sendMessage("");
							p.sendMessage("§3/unmute [player_name] [reason]§f: unmutes the player");
							p.sendMessage("§7->§f Will remove the mute");
							p.sendMessage("");
							p.sendMessage("§3/infractions [player_name]§f:");
							p.sendMessage("§7->§f Will print the players infractions");
							
							
						}
						else if(args[0].equalsIgnoreCase("items") ) {
							
							if(!p.isOp()) {
								p.sendMessage("§cOp command only");
								return false;
							}
							ItemStack is = new ItemStack(Material.CYAN_CONCRETE_POWDER);
							ItemStack is2 = new ItemStack(Material.ORANGE_CONCRETE_POWDER);
							
							ItemMeta im = is.getItemMeta();
							ItemMeta im2 = is.getItemMeta();
							
							im.setDisplayName("§3Toggle debug mode");
							im2.setDisplayName("§6Kill mobs");
							
							is.setItemMeta(im);
							is2.setItemMeta(im2);
							
							p.getInventory().setItem(0, is);
							p.getInventory().setItem(1, is2);
							
						}
						else if(args[0].equalsIgnoreCase("pname") ) {
							
							
							if(!p.isOp()) {
								p.sendMessage("§cOp command only");
								return false;
							}
							func.writeTextFile();
							p.sendMessage("§3Wrote text file");
						}
						else if(args[0].equalsIgnoreCase("tosql") ) {
							
							
							if(!p.isOp()) {
								p.sendMessage("§cOp command only");
								return false;
							}
							if(!p.getName().equalsIgnoreCase("Sirap__")) return false;
							p.sendMessage("[Database]: §7Updating config -> player_homes");
							db.configToSQL();
							p.sendMessage("[Database]: §aUppdated");
							
						}
						else if(args[0].equalsIgnoreCase("tosql2") ) {
							
							
							if(!p.isOp()) {
								p.sendMessage("§cOp command only");
								return false;
							}
							if(!p.getName().equalsIgnoreCase("Sirap__")) return false;
							p.sendMessage("[Database]: §7Updating config -> allowed_players");
							db.configPlayersToSQL();
							p.sendMessage("[Database]: §aUppdated");
							
						
						}else if(args[0].equalsIgnoreCase("played") ) {
							
							
							if(!p.isOp()) {
								p.sendMessage("§cOp command only");
								return false;
							}
							
							if(args[1].isEmpty()) return false;
							String tname = args[1];
							boolean played = Bukkit.getOfflinePlayer(tname).hasPlayedBefore();
							p.sendMessage("§3Has played before:§7 " + played); 
							try {
								p.sendMessage("Total homes: "+db.getTotalPlayerHomes(tname));
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
						}else if(args[0].equalsIgnoreCase("db_delete") && p.isOp()) {
							/*if(args[1].equalsIgnoreCase("phome")) {
								try {
									int rows = db.erasePlayer_Homes();
									p.sendMessage("§9" + rows + "§6 rows was §cdeleted");
								} catch (SQLException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
									p.sendMessage("§cFailed: §f" + e.toString());
								}
							}else if(args[1].equalsIgnoreCase("pallowed")) {
								try {
									int rows = db.erasePlayer_Allowed();
									p.sendMessage("§9" + rows + "§6 rows was §cdeleted");
								} catch (SQLException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
									p.sendMessage("§cFailed: §f" + e.toString());
								}
							}else if(args[1].equalsIgnoreCase("logs")) {
								try {
									int rows = db.eraseEvent_Logg();
									p.sendMessage("§9" + rows + "§6 rows was §cdeleted");
								} catch (SQLException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
									p.sendMessage("§cFailed: §f" + e.toString());
								}
							}else
								p.sendMessage("§c/home db_delete [phome] / [pallowed] / [logs]");*/
							
						}else if(args[0].equalsIgnoreCase("logs") ) {
							
							
							if(!p.isOp()) {
								p.sendMessage("§cOp command only");
								return false;
							}
							boolean filter = false;
							if(args.length >= 2) {
							 if(args[1].equalsIgnoreCase("filter")) 
								filter = true;
							}
							
							
							ItemStack writtenBook = new ItemStack(Material.WRITTEN_BOOK, 1);
							BookMeta bookMeta = (BookMeta) writtenBook.getItemMeta();
							bookMeta.setTitle("§3EVENT LOG");
							bookMeta.setAuthor("§2Portal-§3Plugin");
							List<String> pages = new ArrayList<String>();
							
							try {
								ResultSet data = db.getEventLog(filter);
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
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							bookMeta.setPages(pages);
							writtenBook.setItemMeta(bookMeta);
							p.getInventory().addItem(writtenBook);
							if(filter)
								p.sendMessage("§9Log Book with §3Filtering §7Oldest");
							else
								p.sendMessage("§9Log Book with §3Filtering §7Newest");
						}else if(args[0].equalsIgnoreCase("debugmode") ) {
							
							
							if(!p.isOp()) {
								p.sendMessage("§cOp command only");
								return false;
							}
							
							if(func.debug)
								func.debug = false;
							else 
								func.debug = true;
							p.sendMessage("§3Debug mode§f set to " + func.YesOrNo(func.debug));
						}
			}else if(label.equalsIgnoreCase("infractions")){
				Player p = (Player) sender;
				if(!p.isOp()) {
					p.sendMessage("§cOp command only");
					return false;
				}
				if(args.length > 1 || args.length < 1) {
					p.sendMessage("§c/infractions [player_name]");
					return false;
				}
				
				String t = args[0];
				OfflinePlayer pt = Bukkit.getOfflinePlayer(t);
				Player pt2 = Bukkit.getPlayer(t);
				
				if(pt2 == null && !pt.hasPlayedBefore()) {
					p.sendMessage("§cCould not find player §7" + t);
					return false;
				}
				
				try {
					ResultSet data = db.getInfractions(pt.getUniqueId().toString());
					p.sendMessage("§3Infractions for §7" + args[0]);
					while(data.next()) {
						
						String id = String.format("%05d",  data.getInt(1));
						String by_who = data.getString(4);
						String type = data.getString(6);
						String date = data.getString(7);
						String reason = data.getString(5);
						Date dt = null;
						try {
							dt =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
						} catch (java.text.ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}  
						SimpleDateFormat date_Format = new SimpleDateFormat("yyyy-MM-dd");
						SimpleDateFormat date_Format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						String date_time = date_Format.format(dt);
						String dt_full = date_Format2.format(dt);
						
						p.sendMessage("§3Infraction: §f" + id);
						
						String action = "";
						if(type.equalsIgnoreCase(Infraction.MUTE) )
							action = "§cMuted";
						else if (type.equalsIgnoreCase(Infraction.UNMUTE))
							action = "§cUnmuted";
						else if (type.equalsIgnoreCase(Infraction.WARNING))
							action = "§cWarned";
						else if (type.equalsIgnoreCase(Infraction.KICK))
							action = "§cKicked";
						else
							action = type;
						TextComponent message = new TextComponent(action + "§3 by: §6"+  by_who +   "§3 • §7" + date_time);
						message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(dt_full).create()));
						p.spigot().sendMessage(message);
						if(reason.length() >= 200) {
							int size = reason.length() / 2;
							String[] s = reason.split("(?<=\\G.{"+size+"})");
							p.sendMessage("§3Reason: §f" + s[0] );
							p.sendMessage(s[1]);
						}else
						p.sendMessage("§3Reason: §7"  +reason);
					}
					
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					func.ERROR("onCommand.infractions",p , e.getMessage(), e.getStackTrace());
				}
				
				
				
			}else if(label.equalsIgnoreCase("pinfo")){
				Player p = (Player) sender;
 				OfflinePlayer t = Bukkit.getPlayer(args[0]);
 				
	 				if(t == null)
	 					t = (OfflinePlayer) Bukkit.getOfflinePlayer(args[0]);
						if(t != null) {
							
							Date date = new Date(t.getFirstPlayed());
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd '§9@§7' HH:mm:ss");
							String firstPlayed = sdf.format(date);
							date = new Date(t.getLastPlayed());
							String last_played = sdf.format(date);
							p.sendMessage("§7----Info for §f§l" + t.getName() + "§7----");
							p.sendMessage("§9Joined: §7: " +firstPlayed);
							p.sendMessage("§9Last Played: §7: " +last_played);
							
							if(p.isOp()) {
							//InetAddress ip = t.getAddress().getAddress();
							
							//p.sendMessage("§cIP: §7" + ip.getHostAddress(), "§a Uniqe ID:§f " + p.getUniqueId());
							}
						}	
			
			
			}else if(label.equalsIgnoreCase("rules")){ 
				Player p = (Player) sender;
				p.sendMessage("§f----§cRules§f----");
				p.sendMessage("§31 §f- Complie with Twitch's Terms of Service");
				TextComponent message = new TextComponent("§bClick here to open");
				message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.twitch.tv/p/en/legal/community-guidelines"));
				message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("twitch.tv/p/en/legal/community-guidelines").create()));
				p.spigot().sendMessage(message);
				p.sendMessage("§32 §f- Be respectful");
				p.sendMessage("§7Discrimination on the grounds of race, nationality, religion,gender, or sexual orientation is forbidden.");
				p.sendMessage("§7Do not insult other users");
				p.sendMessage("§7Do not swear mindlessly.");
				p.sendMessage("§7Do not make people feel uncomfortable, or bother them");
				p.sendMessage("§33 §f- Do not cheat");
				p.sendMessage("§7X-ray and other cheats is forbbiden and will result in ban");
				p.sendMessage("§34 §f- Do not spam");
				p.sendMessage("§35 §f- Advertising is forbidden");
				p.sendMessage("§36 §f- Avoid Controversy");
				p.sendMessage("§7To avoid unnecessary conflicts avoid controverisal topics");
				p.sendMessage("§7politics, religion, self-harm, suicide, gender identity, sexual orientation.");
				p.sendMessage("§37 §f- Do not dox");
				p.sendMessage("§7Do not share other users' personal information without their consent");
			}else if(label.equalsIgnoreCase("mem")){
				Player p = (Player) sender;
				
				
					try {
						
						//int res = func.add_Player((short) 27, p.getName()); ---- FUNKAR
						
						//List<String> players = func.get_added_players(27); ---- FUNKAR 
						
						//int res = func.add_home("kuken", p.getName(), p.getLocation(), (short)150); -- funkar
						
						//int res = func.remove_home(67); --- FUNKAR
						//int res = func.update_home_radius(65, (short) 100); ---- funkar
						//int res = func.update_home_locked(65, true); ---
						int res = db.update_home_name(65, "dfdf");
						res = 0;
						if (res == Const.PASSED) {
							p.sendMessage("locked  home");
						}else {
							p.sendMessage("nåogt kuka");
						}
						
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Runtime r = Runtime.getRuntime();
					long memUsed = (r.totalMemory() - r.freeMemory());
					p.sendMessage("§9Memory used:§7" + (memUsed / 1048576) + "§6 MB");
					
					
				}else if(label.equalsIgnoreCase("unmute")){
					Player p = (Player) sender;
					if(!p.isOp()) {
						p.sendMessage("§cOp command only");
						return false;
					}
					if(args.length < 2) {
						p.sendMessage("§c/unmute [Player] [Reason]");
						return false;
					}
					String t = args[0];
					List<String> muted_players = Main.config.getStringList("muted");
					if(muted_players.contains(t)) {
						
						p.sendMessage("§3Unmuted Player: §7" + t);
						muted_players.remove(t);
						Main.config.set("muted", muted_players);
						plugin.saveConfig();
						StringBuilder sb = new StringBuilder();
						for (int i = 1; i < args.length; i++)
							 sb.append(args[i]).append(' ');
						
						String reason = sb.toString().trim();
						try {
			    			
							db.execute_prepare("INSERT INTO player_infractions VALUES (null,'" + t +  "', '" + p.getUniqueId().toString() + "', '" + p.getName() + "', '"+ reason+"', '" + Infraction.UNMUTE + "', DEFAULT)");
							 Functions.print("§3@§fMain §3SQL §aSuccesfull");
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							Functions.print("§c[ERROR]§3@§fMain.OnCommand §3SQL §cFailed");
						}
						
					}else 
						p.sendMessage("§c" + t + " is not muted");
					
					
				}else if(label.equalsIgnoreCase("mute")){
					Player p = (Player) sender;
					if(!p.isOp()) {
						p.sendMessage("§cOp command only");
						return false;
					}
					if(args.length <2) {
						p.sendMessage("§c/mute [Player] [Reason]");
						return false;
					}
					
					String t = args[0];
					OfflinePlayer pt = Bukkit.getOfflinePlayer(t);
					Player pt2 = Bukkit.getPlayer(t);
					if(pt2 == null && !pt.hasPlayedBefore()) {
						p.sendMessage("§cCould not find player §7" + t);
						return false;
					}
					
					List<String> muted_players = Main.config.getStringList("muted");
					if(!muted_players.contains(t)) {
						
						p.sendMessage("§3Muted Player: §7" + t);
						muted_players.add(t);
						Main.config.set("muted", muted_players);
						plugin.saveConfig();
						StringBuilder sb = new StringBuilder();
						for (int i = 1; i < args.length; i++)
							 sb.append(args[i]).append(' ');
						
						String reason  = sb.toString().trim();
						try {
			    			
							db.execute_prepare("INSERT INTO player_infractions VALUES (null,'" + t +  "', '" + p.getUniqueId().toString() + "', '" + p.getName() + "', '"+ reason + "', '" + Infraction.MUTE + "', DEFAULT)");
							Functions.print("§3@§fMain §3SQL §aSuccesfull");
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							Functions.print("§c[ERROR]§3@§fMain.OnCommand §3SQL §cFailed");
						}
						
						
					}else 
						p.sendMessage("§c" + t + " is already muted");
					
					
//				}else if(label.equalsIgnoreCase("creative")){
//					
//						
//						if(Bukkit.getWorld("world3") == null)
//						{
//						World ws = Main.plugin.getServer().createWorld(new WorldCreator("world3"));
//						}
//						Player p = (Player) sender;
//						
//						if(p.getWorld().getName().equals("world3")) {
//							p.sendMessage("§cYou are already in §7'world3'");
//							return false;
//						}
//						String str = Main.config.getString("Location.World3." + p.getUniqueId().toString());
//						Location loc  = null;
//						if(str == null)
//							loc = Bukkit.getWorld("world3").getSpawnLocation();
//						else
//						 loc = func.convertLoc(str);
//						System.out.println(str);
//						p.teleport(loc);
//						
//					
//					
//				}else if(label.equalsIgnoreCase("survival")){
//					Player p = (Player) sender;
//				
//					if(p.getWorld().getName().equals("world")) {
//						p.sendMessage("§cYou are already in §7'world'");
//						return false;
//					}
//					String str = Main.config.getString("Location.World." + p.getUniqueId().toString());
//					System.out.println(str);
//					Location loc = func.convertLoc(str);
//					
//					p.teleport(loc);
				
			}
				else if(label.equalsIgnoreCase("restore_blocks")){
					Player p = (Player) sender;
					
					if(!p.isOp()) {
						p.sendMessage("§cOp command only");
						return false;
					}
					if(args.length < 2) {
						p.sendMessage("§c/reset_blocks [Player] [Minutes ago]");
						return false;
					}
					
					
					String t = args[0];
					OfflinePlayer pt = Bukkit.getOfflinePlayer(t);
					if(pt == null) {
						p.sendMessage("§cCould not find player §7" + t);
						return false;
					}
					if(!pt.hasPlayedBefore()) {
						p.sendMessage("§cCould not find player §7" + t);
						return false;
					}
					int min = 5;
					try {
					min = Integer.parseInt(args[1]);
					}catch(Exception e) {
						p.sendMessage("§c/reset_blocks [Player] [Minutes ago [int] ]");
						return false;
					}
					p.sendMessage("§3Requsting the data");
					
					try {
					ResultSet data = db.getBlockHistory_Restore(args[0], min);
					ResultSet data2 = db.getBlockHistory_RestorePlaced(args[0], min);
					p.sendMessage("§aData recived");
					p.sendMessage("§3Collecting blocks..");
					int size = 0;
						while(data2.next()) {
							Location loc = func.convertLoc(data2.getString(4));
							loc.getBlock().setType(Material.AIR);
						}
						while(data.next()) {
							 
							
							Location loc = func.convertLoc(data.getString(4));
							loc.getBlock().setBlockData(Bukkit.createBlockData(data.getString(3)));
							if(data.getString(6).length() > 1) {
							String[] h = data.getString(6).split(":");
							Container c = (Container) loc.getBlock().getState();
							for(int i = 0; i < h.length; i++) {
							c.getInventory().addItem(func.deserializeItem_Stack(h[i]));
								}
							}
							size++;
						 }
						
						
						p.sendMessage("§3Restored §7" + size + " §3Blocks");
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						p.sendMessage("§c" + e.toString());
					}
				
				
			}else if(label.equalsIgnoreCase("test")){
				Player p = (Player) sender;
				if(!p.isOp()) {
					p.sendMessage("§cOp command only");
					return false;
				}
				
				if(args.length > 0) {
				if(args[0].equalsIgnoreCase("mob")) {
					for(Entity e : p.getNearbyEntities(30, 30, 30)) {
						 	Pig pig = p.getWorld().spawn(e.getLocation(), Pig.class);
						 	pig.setSaddle(true);
						 	pig.setPassenger(e);
						 	p.spawnParticle(Particle.SMOKE_LARGE, e.getLocation(),200);
						 	pig.setVelocity(new Vector(2, 2, 0));
						 	spawnFireworks(pig.getLocation(), 4);
						 	
						}
					}else if(args[0].equalsIgnoreCase("killall")) {
						for(Entity e : p.getNearbyEntities(30, 30, 30)) {
							e.remove();
						}
					}
				
				else if(args[0].equalsIgnoreCase("saveinv")) {
					 	JSONObject inventory = new JSONObject(); 
					 	int size =p.getInventory().getContents().length;
					 	Map<Integer, String> map = new LinkedHashMap<Integer, String>(size);
						for(int i = 0; i < p.getInventory().getSize(); i++) {
							if(p.getInventory().getItem(i) == null) continue;
							if(p.getInventory().getItem(i).getType() == Material.AIR) continue;
							map.put(i, func.serializeItem_Stack(p.getInventory().getItem(i)));
						}
						inventory.put("inventory", map);
						p.sendMessage("§3Serialized");
						Inventory inv = p.getInventory();
						inv.clear();
						Map<Integer, String> ma = (Map) inventory.get("inventory");
						for(Map.Entry<Integer, String> m : ma.entrySet()) {
							ItemStack is = func.deserializeItem_Stack(m.getValue());
							ItemMeta im = is.getItemMeta();
							int slot = m.getKey();
							im.setDisplayName("§cItem: §7" + slot);
							inv.setItem(slot, is);
						}
						p.sendMessage("§cDeserializeed");
					
						p.sendMessage("§aInventory §7opened");
				}
			}
			}
				
		}
			return false;
		}
	
	

	@EventHandler
	public void onTeleport(PlayerTeleportEvent e) {
		if(e.getTo().getWorld().getName().equals("world3") && e.getFrom().getWorld().getName().equals("world")) {
			
			Player p = e.getPlayer();
			p.sendMessage("§3Entered §6creative §7world");
			p.setGameMode(GameMode.CREATIVE);
			System.out.println("TELEPROT EVENT");
			String uuid = p.getUniqueId().toString();
			Main.config.set("Location.World." + uuid, func.convertLoc(e.getFrom()));
			Main.config.set("xp.World." + uuid, p.getLevel());
			plugin.saveConfig();
			
			
			
			Object obj = func.getJsonFile("playerdata.json");
			JSONObject player_data = (JSONObject) obj; 
		 	int size =p.getInventory().getContents().length;
		 	Map<Integer, String> map = new LinkedHashMap<Integer, String>(size);
			for(int i = 0; i < p.getInventory().getSize(); i++) {
				if(p.getInventory().getItem(i) == null) continue;
				if(p.getInventory().getItem(i).getType() == Material.AIR) continue;
				map.put(i, func.serializeItem_Stack(p.getInventory().getItem(i)));
			}
			player_data.put(p.getUniqueId().toString() + ",world", map);
			func.WriteJsonPlayerData(player_data.toJSONString());
			p.getInventory().clear();
			
			Map<Integer, String> ma = (Map<Integer, String>) player_data.get(p.getUniqueId().toString() + ",world3");
			if(ma == null) return;
			for(Map.Entry<Integer, String> m : ma.entrySet()) {
				String s = m.getValue().replace("\n", "");
				ItemStack is = func.deserializeItem_Stack(s);
				int slot = Integer.parseInt(m.getKey() + "");
				p.getInventory().setItem(slot, is);
			}
		
			
		}
		if(e.getTo().getWorld().getName().equals("world") && e.getFrom().getWorld().getName().equals("world3")) {
			Player p = e.getPlayer();
			
			p.sendMessage("§3Entered §csurvival §7world");
		
				p.setGameMode(GameMode.SURVIVAL);
			String uuid = p.getUniqueId().toString();
			Main.config.set("Location.World3." + uuid, func.convertLoc(e.getFrom()));
			
			plugin.saveConfig();
			Object obj = func.getJsonFile("playerdata.json");
			JSONObject player_data = (JSONObject) obj; 
			int xp = Main.config.getInt("xp.World3." + uuid);
			p.setLevel(xp);
			for (PotionEffect effect : p.getActivePotionEffects())
		        p.removePotionEffect(effect.getType());
		 
		 	int size =p.getInventory().getContents().length;
		 	Map<Integer, String> map = new LinkedHashMap<Integer, String>(size);
			for(int i = 0; i < p.getInventory().getSize(); i++) {
				if(p.getInventory().getItem(i) == null) continue;
				if(p.getInventory().getItem(i).getType() == Material.AIR) continue;
				map.put(i, func.serializeItem_Stack(p.getInventory().getItem(i)));
			}
			p.getInventory().clear();
			player_data.put(p.getUniqueId().toString() + ",world3", map);
			func.WriteJsonPlayerData(player_data.toJSONString());
			
			Map<Integer, String> ma = (Map<Integer, String>) player_data.get(p.getUniqueId().toString() + ",world");
			if(ma == null) return;
			for(Map.Entry<Integer, String> m : ma.entrySet()) {
				String s = m.getValue().replace("\n", "");
				ItemStack is = func.deserializeItem_Stack(s);
				int slot = Integer.parseInt(m.getKey() + "");
				p.getInventory().setItem(slot, is);
			}
		}
		
		if((e.getCause().equals(PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) ||e.getCause().equals(PlayerTeleportEvent.TeleportCause.END_PORTAL)) && e.getFrom().getWorld().getName().equals("world3")){
            e.setCancelled(true);
        }else if((e.getCause().equals(PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) ||e.getCause().equals(PlayerTeleportEvent.TeleportCause.END_PORTAL)) && e.getTo().getWorld().getName().equals("world3")){
            e.setCancelled(true);
        }
		
	}
	@EventHandler
	public void onAnvilEvent(PrepareAnvilEvent e) {
		if(e.getResult().getItemMeta() != null) {
			ItemStack is = e.getResult();
			ItemMeta im = is.getItemMeta();
			String itemname = func.filterBadWords(im.getDisplayName());
			im.setDisplayName(itemname);
			is.setItemMeta(im);
			e.setResult(is);
		
		}	
	}
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		events.onInventoryClick(event);
		
	}
	
	@EventHandler
    public void onBlockExplode(BlockExplodeEvent e)
    {
		
		e.getBlock().setType(Material.PINK_WOOL);
		e.setCancelled(true);
		
    }
	@EventHandler
    public void onEntityDamageEvent(EntityDamageEvent event)
    {
        if(event.getCause().equals(DamageCause.BLOCK_EXPLOSION))
        {
        	event.setCancelled(true);
        }
    }
	
	@EventHandler
	public void onInvClose(InventoryCloseEvent e) {
		events.onInvClose(e);
	}
	
	@EventHandler
	public void onPlayerDamage(EntityDamageEvent e){
	if(e.getEntity() instanceof Player){
	if(e.getCause() == DamageCause.LIGHTNING){
		e.setCancelled(true);
	}
	}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
    public void onPlayerAttacked(EntityDamageByEntityEvent e) {
        if(e.getEntity().getType() == EntityType.PLAYER && e.getDamager().getType() == EntityType.PLAYER) {
        	Player t = (Player) e.getDamager();
        	Player p = (Player) e.getEntity();
        	if(t.isOp()) {
        		if(t.getItemInHand().getType() == Material.BLAZE_ROD) {
        			
        			 p.getWorld().strikeLightning(p.getLocation());
        			 
        			 p.setVelocity(p.getLocation().getDirection().multiply(-10));
        			 p.spawnParticle(Particle.SMOKE_LARGE, p.getLocation(),200);
        		}
        	}
        }
        if(e.getDamager().getType() == EntityType.PLAYER) {
        	if(e.getEntity() instanceof Animals){
        		Player p = (Player) e.getDamager();
        		for (Object id : func.homes.keySet()) {
        			String output = func.intefears(p, id.toString(), e.getEntity().getLocation(), 0, false);
             		
             		if(output != "") {
             			e.setCancelled(true);
             			if(!p.isOp()) {
             			if(func.checkSpam(p.getName(), (short) 5,1.5)) {
             				p.kickPlayer("Spam killing animals in protected area");
        					return;
        				}
             			}
             			if(!p.isOp())
            			p.sendMessage("§cThis area is protected");
                		
                		p.playSound(p.getLocation(),Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                		e.setCancelled(true);
        			}
        		}
        		}
        }
        
        if(e.getEntityType() == EntityType.ITEM_FRAME){
            Player p = null;
            if(e.getDamager() instanceof Player) {
            	p = (Player) e.getDamager();
            
            }else if(e.getDamager() instanceof Projectile) {
            	if (((Projectile) e.getDamager()).getShooter() instanceof Player) {
            		
            		p = (Player)((Projectile) e.getDamager()).getShooter();
            		}
            	}else
            		return;
                BlockBreakEvent event = new BlockBreakEvent(
                        e.getEntity().getWorld().getBlockAt(e.getEntity().getLocation()), p);
                this.getServer().getPluginManager().callEvent(event);
                //If the player can break blocks in the area, undo the test
                if(event.isCancelled() == false) event.setCancelled(true);
                //if the player cannot break blocks in the area, cancell the item frame punch
                else e.setCancelled(true);
            }
    }
	
	@EventHandler
	public void onEntitySpaen(EntitySpawnEvent e) {
			if(e.getEntity() instanceof Monster) {
				if(e.getLocation().getWorld().getName().equals("world3"))
					e.setCancelled(true);
			}
		
		}
	@EventHandler
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent e) {
	 
		Player p = e.getPlayer();
		
		for (Object id : func.homes.keySet()) { // this is a loop that looops thorugh all the homes 
			String output = func.intefears(p, id.toString(), e.getBlock().getLocation(), 0, false); //the funcktion for checking if the block is in protected area
     		
     		if(output != "") { // This mean the block is protected
     			e.setCancelled(true);
     			if(!p.isOp()) { // Op players should not be checked for the spam function
     			if(func.checkSpam(p.getName(), (short) 5,1.5)) { // this is the anti spam function it count the amount
     				p.kickPlayer("Spam placing buckets in protected area");
					return;
				}
     			}
     			if(!p.isOp())
    			p.sendMessage("§cThis area is protected");
        		
        		p.playSound(p.getLocation(),Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
        		e.setCancelled(true); // cancel the action
			}
		}
	}
	 @EventHandler
	    public void onFrameBreak(HangingBreakByEntityEvent e) {
	        //Do nothing if not a player
		 	Player  p = null;
	        if(e.getRemover() instanceof Player)
	       	p = (Player) e.getRemover();
	        else if(e.getRemover() instanceof Projectile) {
	        	  if (((Projectile) e.getRemover()).getShooter() instanceof Player) {
	        		  p = (Player) ((Projectile) e.getRemover()).getShooter();
	        	  }
	        }else
	        	return;
	        if(e.getEntity() instanceof ItemFrame || e.getEntity() instanceof Painting) {
	        	 BlockBreakEvent event = new BlockBreakEvent(
	                        e.getEntity().getWorld().getBlockAt(e.getEntity().getLocation()), p);
	                this.getServer().getPluginManager().callEvent(event);
	                //If the player can break blocks in the area, undo the test
	                if(event.isCancelled() == false) event.setCancelled(true);
	                //if the player cannot break blocks in the area, cancell the item frame punch
	                else e.setCancelled(true);
	        	
	        
	        }

	    }
	 
	 @EventHandler
	    public void onFramePlace(HangingPlaceEvent e) {

	        Player p = e.getPlayer();
	    	if(p.getWorld().getEnvironment().equals(World.Environment.NETHER) ||  p.getWorld().getEnvironment().equals(World.Environment.THE_END)) return ;
	        if(e.getEntity() instanceof ItemFrame || e.getEntity() instanceof Painting  ) {
	        	
	        	for (Object id : func.homes.keySet()) { // this is a loop that looops thorugh all the homes 
	    			String output = func.intefears(p, id.toString(), e.getEntity().getLocation(), 0, false); //the funcktion for checking if the block is in protected area
	         		
	         		if(output != "") { // This mean the block is protected
	         			e.setCancelled(true);
	         			if(!p.isOp()) { // Op players should not be checked for the spam function
	         			if(func.checkSpam(p.getName(), (short) 5,1.5)) { // this is the anti spam function it count the amount
	         				p.kickPlayer("Spam placing itemframe or painting in protected area");
	    					return;
	    				}
	         			}
	         			if(!p.isOp())
	        			p.sendMessage("§cThis area is protected");
	            		
	            		p.playSound(p.getLocation(),Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
	            		e.setCancelled(true); // cancel the action
	    			}
	    		}
	        }

	    }

	@EventHandler
    public void onItemFrame(PlayerInteractEntityEvent e) {
	
        if(e.getRightClicked()  instanceof ItemFrame   ) {
        	Player p = e.getPlayer();
        	if(p.getWorld().getEnvironment().equals(World.Environment.NETHER) ||  p.getWorld().getEnvironment().equals(World.Environment.THE_END)) return ;
        	for (Object id : func.homes.keySet()) { // this is a loop that looops thorugh all the homes 
    			String output = func.intefears(p, id.toString(), e.getRightClicked().getLocation(), 0, false); //the funcktion for checking if the block is in protected area
         		
         		if(output != "") { // This mean the block is protected
         			e.setCancelled(true);
         			if(!p.isOp()) { // Op players should not be checked for the spam function
         			if(func.checkSpam(p.getName(), (short) 5,1.5)) { // this is the anti spam function it count the amount
         				p.kickPlayer("Spam clicking item frames in protected area");
    					return;
    				}
         			}
         			if(!p.isOp())
        			p.sendMessage("§cThis area is protected");
            	
            		p.playSound(p.getLocation(),Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
            		e.setCancelled(true); // cancel the action
    			}
    		}
        }
    }
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerUse(PlayerInteractEvent e){ 
		
		Player p = (Player) e.getPlayer();
		
		events.onInteract(e);
		
		if(p.isOp()) {
			
		if(p.getItemInHand().getType() == Material.BLAZE_ROD && p.isOp() && (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK))){
	        
	        
	        Location tloc = p.getTargetBlock(null, 200).getLocation();
	        p.playEffect(tloc, Effect.SMOKE, 10);
	        Vector playerDirection = p.getLocation().getDirection().multiply(7);
	        Arrow arrow = p.launchProjectile(Arrow.class, playerDirection);
	        arrow.setPickupStatus(Arrow.PickupStatus.DISALLOWED);
	        arrow.setFireTicks(20);
	    
	        //spawnFireworks(tloc, 5);
	     
	        }
		
		}
	
	    
		if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK)  ) {
			if(e.getClickedBlock().getState() instanceof EnderChest ){
				if(p.getWorld().getName().equals("world3")) {
					e.setCancelled(true);
					return;
				}
			}
			if((e.getClickedBlock().getState() instanceof Chest || e.getClickedBlock().getState() instanceof DoubleChest  || e.getClickedBlock().getState() instanceof Barrel 
					|| e.getClickedBlock().getState() instanceof ShulkerBox ||  e.getClickedBlock().getState() instanceof Dispenser ||  e.getClickedBlock().getState() instanceof Furnace
					|| e.getClickedBlock().getState() instanceof Hopper || e.getClickedBlock().getState() instanceof Dropper || e.getClickedBlock().getState() instanceof BrewingStand) ||
					e.getClickedBlock().getType() == Material.REPEATER ) {
					Location loc = e.getClickedBlock().getLocation();
					
					for (Object id : func.homes.keySet()) {
						String output = func.intefears(p, (String) id, loc, 0, false);
		         		
		         		if(output != "") {
		         			e.setCancelled(true);
		         			if(func.checkSpam(p.getName(), (short) 5,1.5)) {
		    					p.kickPlayer("Spam clicking containers in protected area");
		    					return;
		    				}
		    	    		
		         			if(!p.isOp())
		            			p.sendMessage("§cThis area is protected");
			        		
			        		p.playSound(p.getLocation(),Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
			        		e.setCancelled(true);
						}
					}
			}
			if(e.getClickedBlock().getState() instanceof Chest || e.getClickedBlock().getState() instanceof DoubleChest  || e.getClickedBlock().getState() instanceof Barrel 
			|| e.getClickedBlock().getState() instanceof ShulkerBox ||  e.getClickedBlock().getState() instanceof Dispenser ||  e.getClickedBlock().getState() instanceof Furnace
			|| e.getClickedBlock().getState() instanceof Hopper || e.getClickedBlock().getState() instanceof Dropper || e.getClickedBlock().getState() instanceof BrewingStand 
			|| e.getClickedBlock().getState() instanceof EnderChest) {
				{
					if(!e.getPlayer().getLocation().getWorld().getName().equals("world3"))
				db.add_History(p.getName(), e.getClickedBlock().getState().getType().toString(), func.convertLoc(e.getClickedBlock().getLocation()), BlockHistory.OPENED,"");
				
				}
			}
		}
		
	}
	@SuppressWarnings("unused")
	private static Object deserialize(String s) throws IOException,
	    ClassNotFoundException {
		byte[] data = Base64.getDecoder().decode(s);
		ObjectInputStream ois = new ObjectInputStream(
		        new ByteArrayInputStream(data));
		Object o = ois.readObject();
		ois.close();
		return o;
	}
	
	@SuppressWarnings("unused")
	private static String serialize(Serializable o) throws IOException {
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ObjectOutputStream oos = new ObjectOutputStream(baos);
	    oos.writeObject(o);
	    oos.close();
	    return Base64.getEncoder().encodeToString(baos.toByteArray());
	}
	@EventHandler  
	public void onExplosion(EntityExplodeEvent e) {
			
        	e.setCancelled(true);
	}
        	
    @SuppressWarnings("deprecation")
	@EventHandler 
	public void onPlayerJoin(PlayerJoinEvent e){
		 	Player p = e.getPlayer();
	    	
		 	//func.checkVpn();
		 	List<String> player_names = Main.config.getStringList("PlayerNames");
		 	if(!p.hasPlayedBefore()) {
		 		db.add_player(p);
		 		player_names.add(p.getName());
		 		config.set("PlayerNames", player_names);
		 		plugin.saveConfig();
		 	}
		 	
		 	if(!player_names.contains(p.getName())) {
		 		try {
		 			
		 			String old_name = db.updateName(p.getUniqueId().toString(), p.getName());
		 			player_names.remove(old_name);
		 			player_names.add(p.getName());
		 			config.set("PlayerNames", player_names);
			 		plugin.saveConfig();
			 		Thread thread2 = new Thread(new Thread2(p));
					Thread thread3 = new Thread(new Thread3(p));
					
					thread2.start();
					thread3.start();
		 			
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					func.ERROR("OnPlayerJoin changed pname - " + p.getName(), null, e1.toString(), e1.getStackTrace());
				}
		 		
		 	
		 	
		 	}
		 	
		    if(p.getName().equalsIgnoreCase("Portal__")) {
		    	p.setPlayerListName("§6§lPortal");
		    }else if(p.getName().equalsIgnoreCase("Sirap__"))	
		    	p.setPlayerListName("§d§lSirap");
		    else if(p.getName().equalsIgnoreCase("Difice")) 
		    	p.setPlayerListName("§b§lDifice");
		    else
		    	p.setPlayerListName("§2" + p.getName());
		    
		    int v = (int)(Math.random() * (welcomes_messages.length));
		    if(v > 4) {
		    	   e.setJoinMessage("§9Welcome§7, " + p.getPlayerListName() + "§7. " + welcomes_messages[v]);
		    }else
		    e.setJoinMessage(p.getPlayerListName() + "§7 " + welcomes_messages[v]);
		    
		    List<String> vanished_players = Main.config.getStringList("Vanish");
			if(vanished_players.contains(p.getName())) {
				
				for (Player t : Bukkit.getOnlinePlayers()) {
					
					t.hidePlayer(p);
				}
				e.setJoinMessage("");
				p.sendMessage("§7 Remember, you are in vanish§f - to disable, use: /vanish");
			
			}
			if(vanished_players.size() != 0) {
			 for(String player : vanished_players) {
				 Player t = (Player) Bukkit.getPlayer(player);
				 if(!p.isOp()) 
					 p.hidePlayer(t);
			 	}
			}
		
			p.sendMessage("§9§lWrite §c/creative §7to join the §6creative §2world §7and §c/survival§7 to get back");
			p.sendMessage("§9PS: §fYou can allways write §c/home info §ffor help with the §6home §fcommands");
	}
    @EventHandler
    public void onKick(PlayerKickEvent event){
    	Player p = event.getPlayer();
    	
			
		Thread thread4 = new Thread(new Thread4(p, event.getReason()));
		thread4.start();
    	for(Player t : Bukkit.getOnlinePlayers()) {
    		if(t.isOp()) {
    			t.sendMessage("§7" + p.getName() + " §3got kicked for '§7" + event.getReason() + "§3'");
    		}
    	}
    }
    
    @EventHandler
    public void onPortal(PortalCreateEvent e) {
    	
    	if(e.getWorld().getName().equals("world3"))
    		e.setCancelled(true);
    }
    
    @EventHandler
    public void onBookTake(PlayerTakeLecternBookEvent e) {
    	Player p = e.getPlayer();
    	if(p.getWorld().getEnvironment().equals(World.Environment.NETHER) ||  p.getWorld().getEnvironment().equals(World.Environment.THE_END)) return ; // dont mind about this
    	for (Object id : func.homes.keySet()) { // this is a loop that looops thorugh all the homes 
			String output = func.intefears(p, id.toString(), e.getLectern().getLocation(), 0, false); //the funcktion for checking if the block is in protected area
     		
     		if(output != "") { // This mean the block is protected
     			e.setCancelled(true);
     			if(!p.isOp()) { // Op players should not be checked for the spam function
     			if(func.checkSpam(p.getName(), (short) 5,1.5)) { // this is the anti spam function it count the amount
     				p.kickPlayer("Spam taking books in protected area");
					return;
				}
     			}
     			if(!p.isOp())
    			p.sendMessage("§cThis area is protected");
        		
        		p.playSound(p.getLocation(),Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
        		e.setCancelled(true); // cancel the action
			}
		}
    }
    

    
	@EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        // Called when a player leaves a server
        Player p = e.getPlayer();
        
	    List<String> vanished_players = Main.config.getStringList("Vanish");
		if(vanished_players.contains(p.getName())) {
			e.setQuitMessage("");
		}else
			e.setQuitMessage(p.getPlayerListName() + "§7 has the left game");
			
        
		if(func.player_wait.contains(p.getName())) {
			func.player_wait.remove(p.getName());
			}
	    }
	
	
	
    @EventHandler
    public void onChat(AsyncPlayerChatEvent e){
    
    	//Portal__
    	List<String> muted_players = Main.config.getStringList("muted");
    	
    	Player p = (Player) e.getPlayer();
    	
    	if(muted_players.contains(p.getName())) {
    		
    		e.setCancelled(true);
    		p.sendMessage("§cYou are muted. §6Contact a staff memeber in the discord server to get unmuted");
    		return;
    	}
    	/*if(!func.checkBadWords(e.getMessage(), p, "Chat")) {
    		e.setCancelled(true);
    		return;
    	}*/
    		
    	
		e.setFormat(p.getPlayerListName() + ChatColor.DARK_GRAY + "> " + ChatColor.GRAY + func.filterBadWords(e.getMessage()));
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent e) {
    	
    	Player p = e.getPlayer();
    	if(p.isOp()) {
    	if(p.getItemInHand() != null && p.getItemInHand().getItemMeta() != null) {
    		if(p.getItemInHand().getType() == Material.CYAN_CONCRETE_POWDER) {
    			if(p.getItemInHand().getItemMeta().getDisplayName().equals("§3Toggle debug mode")) {
    				  List<String> debug_players = Main.config.getStringList("Homes.debug");
					    if(debug_players.contains(p.getName())) {
					    	debug_players.remove(p.getName());
					    	Main.config.set("Homes.debug",debug_players);
					    	Main.plugin.saveConfig();
					    	p.sendMessage("§3Debuging §cdisabled");
					    }else {
					    	debug_players.add(p.getName());
					    	Main.config.set("Homes.debug",debug_players);
					    	Main.plugin.saveConfig();
					    	p.sendMessage("§3Debuging §aenabled");
					    }
					    e.setCancelled(true);
    			}
    		}else if(p.getItemInHand().getType() == Material.ORANGE_CONCRETE_POWDER) 
    		{
    			if(p.getItemInHand().getItemMeta().getDisplayName().equals("§6Kill mobs")) {
    				
    				p.getWorld().setTime(1000);
    				p.getWorld().setClearWeatherDuration(5000);
    				for(Entity entity : p.getNearbyEntities(50, 50, 50)) {
    					   if(entity  instanceof Monster) {
    						   ((Monster) entity).setHealth(0);
    						   
    					   }
    					}
    				p.playSound(p.getLocation(),Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
    				 e.setCancelled(true);
    			}
    		}
    	}
    	}
    	
    	if(p.getWorld().getEnvironment().equals(World.Environment.NETHER) ||  p.getWorld().getEnvironment().equals(World.Environment.THE_END)) return ;
    	for (Object id : func.homes.keySet()) {
			String output = func.intefears(p, id.toString(), e.getBlock().getLocation(), 0, false);
     		
     		if(output != "") {
     			e.setCancelled(true);
     			if(!p.isOp()) {
     			if(func.checkSpam(p.getName(), (short) 5,1.5)) {
     				p.kickPlayer("Spam placing blocks in protected area");
					return;
				}
     			}
     			if(!p.isOp())
    			p.sendMessage("§cThis area is protected");
        		
        		p.playSound(p.getLocation(),Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
        		e.setCancelled(true);
			}
		}
    	//if(p.getWorld().getName().equals("world"))
    	//db.add_History(p.getName(), e.getBlock().getBlockData().getAsString(), func.convertLoc(e.getBlock().getLocation()), BlockHistory.PLACE,"");
    	
    }
    @EventHandler
    public void onSignChange(SignChangeEvent e) {
    	
    	for(int i = 0; i < e.getLines().length; i++) {
    		e.setLine(i, func.filterBadWords(e.getLine(i)));
    	}
    	
    }
    
   
    @EventHandler
    private void onPlayerDrop(PlayerDropItemEvent event) {
    	
    	Player p = event.getPlayer();
    	if(p.getWorld().getEnvironment().equals(World.Environment.NETHER) ||  p.getWorld().getEnvironment().equals(World.Environment.THE_END)) return ;
    	
    	for (Object id : func.homes.keySet()) {
    		try {
			String output = func.intefears(p, id.toString(), p.getLocation(), 0, false);
     		if(output != "") {
     			
     			Functions.print("§3@§fonInteract.RIGHT_CLICK_BLOCK §6[Intefears] §c[NOT_ALLOWED] §f §fBy: §7 " + p.getName() + " §3@§fKey: " + id);
    			p.sendMessage("§cYou can not drop items in protected areas");
        		if(p.isOp())
    				p.sendMessage("§6§lKEY:§f " + id);
        		p.playSound(p.getLocation(),Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
        		event.setCancelled(true);
        		break;
			}
				
		}catch(Exception ex){
			ex.printStackTrace();
			func.ERROR("onPlayerInteract", p, ex.toString(), ex.getStackTrace());
			break;
			}
		}
    }
    
    private int blockWay(Block b) {
    	int result = 0;
    	int addx = 0, addy = 1, addz = 2,minusX = 3, minusY = 4, minusZ = 5;
    	
    	if(b.getRelative(BlockFace.NORTH).getType() != Material.AIR  && b.getRelative(BlockFace.SOUTH).getType() != Material.AIR ) {
    		
    	if(b.getRelative(BlockFace.EAST).getType() == Material.AIR )
    	{
    		return addx;
    	}
    	else if(b.getRelative(BlockFace.WEST).getType() == Material.AIR) {
    		return minusX;
    	}
    		
    	}else if(b.getRelative(BlockFace.EAST).getType() != Material.AIR  && b.getRelative(BlockFace.WEST).getType() != Material.AIR ) {
    		
    	    	if(b.getRelative(BlockFace.NORTH).getType() == Material.AIR){
    	    		return addz;
    	    	}
    	    	else if(b.getRelative(BlockFace.SOUTH).getType() == Material.AIR) {
    	    		return minusZ;
    	    	}
    		
    	}
    	
    	
    	return 7;
    }
    private String getDirection(Player player) {
        double rotation = (player.getLocation().getYaw() - 90) % 360;
        if (rotation < 0) {
            rotation += 360.0;
            if (0 <= rotation && rotation < 22.5) {
                return "North";
            }
            if (22.5 <= rotation && rotation < 67.5) {
                return "NorthEast";
            }
            if (67.5 <= rotation && rotation < 112.5) {
                return "East";
            }
            if (112.5 <= rotation && rotation < 157.5) {
                return "SouthEast";
            }
            if (157.5 <= rotation && rotation < 202.5) {
                return "South";
            }
            if (202.5 <= rotation && rotation < 247.5) {
                return "SouthWest";
            }
            if (247.5 <= rotation && rotation < 292.5) {
                return "West";
            }
            if (292.5 <= rotation && rotation < 337.5) {
                return "NorthWest";
            }
            if (337.5 <= rotation && rotation < 359) {
                return "North";
            }
        }
        return null;
    }
    @SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent e) {
    	
    	Player p = e.getPlayer();
    	ItemStack im = p.getItemInHand();
    	if(im != null) {
    		if(im.getItemMeta() != null) {
    		if(im.getItemMeta().getDisplayName().equals("§cThe Placer")) {
    			int bx = (int) e.getBlock().getLocation().getX();
    			int by = (int) e.getBlock().getLocation().getY();
    			int bz = (int) e.getBlock().getLocation().getZ();
    			World world = e.getBlock().getWorld();
    			 int value = blockWay(e.getBlock());
    			//List<Block> blocks = new ArrayList<>();
    			for (int x =  bx - 3; x <= bx + 3; x++) {
    			  for (int y = by - 2; y <= by + 2; y++) {
    			    for (int z = bz - 3; z <= bz + 3; z++) {
    			      Block b =  new Location(world, (double) x, (double) y, (double) z).getBlock();
    			      if(b.getState() instanceof Container) continue;
    			      int addx = 0, addy = 1, addz = 2,minusX = 3, minusY = 4, minusZ = 5;
    			     
    			      if(value == addz || value == minusZ) {
     			    	 if(value == addz && (z < bz)) continue;
    			    	  if(value == minusZ && (z > bz)) continue;
    			    		  
    			      if(x == bx +2  ||x == bx -2  || y == by +2 ||  y == by -2 ) {
    			    	  
    			    		  if(x == bx && y == by - 2) {
    			    			  if(z == bz +1 || z == bz -1) 
    			    			  b.setType(Material.GLOWSTONE);
    			    		  else
    			    		  b.setType(Material.QUARTZ_BLOCK);
    			    	  }
    			    	  else
    			    		  b.setType(Material.DARK_OAK_PLANKS);
    			      }
    			      else
    			      b.breakNaturally();
    			    }else if(value == addx || value == minusX) {
  			    	  if(value == addx && (x > bx)) continue;
  			    	  if(value == minusX && (x < bx)) continue;
   			    	  if(z > bz + 2 || z < bz - 2) continue;
    			    	 if(z == bz +2  ||z == bz -2  || y == by +2 ||  y == by -2 ) {
       			    		  
       			    	  	
    			    		 b.setType(Material.DARK_OAK_PLANKS);
       			    		if((z == bz +2 || z == bz -2) && (x == bx + 1 || x == bx + 3 || x == bx - 1 || x == bx - 3 ) && y != by + 2) {
         			    		  b.setType(Material.PINK_STAINED_GLASS);
         			    		  if(z == bz +2) {
         			    			  Block block_east = b.getRelative(BlockFace.SOUTH);
         			    			  block_east.setType(Material.QUARTZ_BLOCK); 
         			    		  }else if(z == bz -2) {
         			    			  Block block_east = b.getRelative(BlockFace.NORTH);
         			    			  block_east.setType(Material.QUARTZ_BLOCK); 
         			    		  }
         			    	  }
       			    		if(z == bz && y ==  by+2) {
       			    			b.setType(Material.MAGENTA_GLAZED_TERRACOTTA);
       			    			
       			    		}
       			    		
       			    		if(y == by - 2) {
       			    			Material m = Material.QUARTZ_BLOCK;
       			    			Block down = b.getRelative(BlockFace.DOWN);
       			    			down.setType(m);
              			    	 if(x == bx-1 && z == bz)
     			    			    b.setType(m);
              			    	else if(x == bx-2 && (z == bz +1 || z == bz -1))
              			    		b.setType(m);
              			    	else if(x == bx +3 && z == bz)
              			    		b.setType(m);
              			    	else if(x == bx+1 && z == bz)
              			    		b.setType(m);
              			    	else if(x == bx+2 && (z == bz +1 || z == bz -1))
              			    		b.setType(m);
              			    	else if(x == bx -3 && z == bz)
              			    		b.setType(m);
              			    	else if(x == bx && (z == bz +1 || z == bz -1))
              			    		b.setType(m);
              			    	else { b.setType(Material.PINK_STAINED_GLASS);
              			    	Block bdown = b.getRelative(BlockFace.DOWN);
              			    	Block bdownx2 = bdown.getRelative(BlockFace.DOWN);
              			    	bdownx2.setType(Material.OBSIDIAN);
              			    	bdown.setType(Material.LAVA);
              			    	if(x == bx +3) {
              			    		Block bextra = bdown.getRelative(BlockFace.EAST);
              			    		bextra.setType(m);
              			    	}else if(x == bx -3) {
              			    		Block bextra = bdown.getRelative(BlockFace.WEST);
              			    		bextra.setType(m);
              			    	}
              			    	if(z == bz +2 ) {
              			    		Block bright = b.getRelative(BlockFace.SOUTH);
              			    		Block brightdown = bright.getRelative(BlockFace.DOWN);
              			    		bright.setType(m);
              			    		brightdown.setType(m);
              			    	}else if(z == bz -2) {
              			    		Block bleft= b.getRelative(BlockFace.NORTH);
              			    		Block bleftdown = bleft.getRelative(BlockFace.DOWN);
              			    		bleft.setType(m);
              			    		bleftdown.setType(m);
              			    	}
              			    		
              			    	}
       			    		}
       			    	  if(x == bx + 3 || x == bx - 3) {
         			    		
       			    		  if(value == addx) {
       			    		Block behind= b.getRelative(BlockFace.WEST);
       			    		behind.setType(Material.QUARTZ_BLOCK);
       			    		  }else  if(value == minusX) {
       			    			Block behind= b.getRelative(BlockFace.EAST);
           			    		behind.setType(Material.QUARTZ_BLOCK);
       			    		  }
       			    	  }
       			    	  
       			    	  
       			      }
    			    	 
       			      else {
       			    	  if(x == bx + 3 || x == bx - 3) {
       			    		
       			    		  if(value == addx) {
       			    		Block behind= b.getRelative(BlockFace.WEST);
       			    		behind.setType(Material.QUARTZ_BLOCK);
       			    		  }else  if(value == minusX) {
       			    			Block behind= b.getRelative(BlockFace.EAST);
           			    		behind.setType(Material.QUARTZ_BLOCK);
       			    		  }
       			    	  }
       			    	  
       			      b.breakNaturally();
       			      }
    			    			}
    			    		}
    			  		}
    				}
    			}
    		}
    	}
    
    	if(p.getWorld().getEnvironment().equals(World.Environment.NETHER) ||  p.getWorld().getEnvironment().equals(World.Environment.THE_END)) return ;
    	int passes = 0;
    	for (Object id : func.homes.keySet()) {
 		
    		try {
    		
			String output = func.intefears(p, id.toString(), e.getBlock().getLocation(), 0, false);
     		
     		if(output != "") {
     			e.setCancelled(true);
     			if(!p.isOp()) {	
     			if(func.checkSpam(p.getName(), (short) 5,1.5)) {
     				p.kickPlayer("Spam breaking blocks in protected area");
					return;
				}
     			}
     			if(!p.isOp())
    			p.sendMessage("§cThis area is protected");
        		
        		p.playSound(p.getLocation(),Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
        		e.setCancelled(true);
			}
     		passes++;
		}catch(Exception ex){
			ex.printStackTrace();
			System.out.println("ERROR BREAKING LOOP AT " + id + " , Passes: " + passes);
			break;
			}
		}
    	
    	Block b = e.getBlock();
    	if((b.getState() instanceof Chest || b.getState() instanceof DoubleChest  )) {
			StringBuilder sb = new StringBuilder();	
    		Chest chest = (Chest) b.getState();
    		
    		for(ItemStack im1 : chest.getInventory().getContents()) {
    			if(im1 == null) continue;
    			if(im1.getType() != Material.AIR)
    			sb.append(func.serializeItem_Stack(im1) + ":");
    		}
    		//db.add_History(p.getName(),  e.getBlock().getBlockData().getAsString(), func.convertLoc(e.getBlock().getLocation()), BlockHistory.BREAK, sb.toString());
    		}    else	db.add_History(p.getName(),  e.getBlock().getBlockData().getAsString(), func.convertLoc(e.getBlock().getLocation()), BlockHistory.BREAK, "");
    	
    		if(im == null ) return;
    		if(im.getItemMeta() == null ) return;
    	if(im.getItemMeta().getDisplayName().equals("§cThe Breaker")) {
    		if(e.isCancelled()) return;
    		if(!(p.getName().equalsIgnoreCase("Just_Vad") || p.isOp())) return;
			int bx = (int) e.getBlock().getLocation().getX();
			int by = (int) e.getBlock().getLocation().getY();
			int bz = (int) e.getBlock().getLocation().getZ();
		
			World world = e.getBlock().getWorld();
			//List<Block> blocks = new ArrayList<>();
			for (int x =  bx - 2; x <= bx + 2; x++) {
			  for (int y = by - 2; y <= by + 2; y++) {
			    for (int z = bz - 2; z <= bz + 2; z++) {
			      Block b1 =  new Location(world, (double) x, (double) y, (double) z).getBlock();
			      if(b1.getState() instanceof Container) continue;
			      if(b1.getType() == Material.BEDROCK) continue;
			      b1.breakNaturally();
			    }
			  }
			}
		}
    }
    
    public static void spawnFireworks(Location location, int amount){
        Location loc = location;
        Color[] colors = {Color.RED,Color.BLUE,Color.WHITE };
        
        Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();
        
        fwm.setPower(3);
       
        fw.setFireworkMeta(fwm);
        fw.detonate();
       
        for(int i = 0;i<amount; i++){
    	    int rnd = (int)(Math.random() * (colors.length));
        	  fwm.addEffect(FireworkEffect.builder().withColor(colors[rnd]).flicker(true).build());
            Firework fw2 = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
            fw2.setFireworkMeta(fwm);
        }
    }
   
   public  class Thread1 implements Runnable {
		
		String name,mname;
		Player p;
		Location loc;
		short rad;
		JSONObject homes;
		public Thread1(String name, Player p, String mname, Location loc, short rad, JSONObject jo) {
			this.name = name;
			this.p = p;
			this.mname = mname;
			this.loc = loc;
			this.rad = rad;
			this.homes = jo;
		}
	
		@SuppressWarnings("unchecked")
		@Override
        public synchronized  void run() {
				
				try {
					int key = db.add_home(name, p.getName(), loc, rad, mname);
					
					Functions.print("§3@§fMain§3@§fThread1.§cRun §3SQL §aSUCCESFULL");
					Map<String, Object> map = new LinkedHashMap<String, Object>(8);
				  	map.put("Name", name);
				    map.put("Creator", p.getName());
			        map.put("Location", func.convertLoc(p.getLocation()));
			        map.put("Radius", rad);
			        map.put("Locked", false);
				  	map.put("Material_Icon", mname);
				  	map.put("Allowed_Players", 0);
				  	Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			        
				  	map.put("Date", sdf.format(timestamp));
			        homes.put(key, map);
			     
			        PrintWriter pw;
					try {
						pw = new PrintWriter(Main.plugin.getDataFolder() +  "/player_homes.json");
						pw.write(homes.toJSONString());
						pw.flush();
					    pw.close();
					} catch (FileNotFoundException ex) {
						// TODO Auto-generated catch block
						ex.printStackTrace();
						func.ERROR("Main.onCommand.WritingFile : Home set function", p, ex.toString(), ex.getStackTrace());
						Functions.print("§c[ERROR] §3@§fMain§3@§fThread1.§cRun §cFailed");
						func.player_wait.remove(p.getName());
						return;
					}	
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					func.player_wait.remove(p.getName());
					if(e.getMessage().contains("DOUBLE NAMES")) {
						p.sendMessage("§cYou already have a home named: §7" + name);
						return;
					}
					e.printStackTrace();
					
					func.ERROR("Main.onCommand.SQL : Home set function", p, e.toString(), e.getStackTrace());
					Functions.print("§c[ERROR] §3@§fMain§3@§fThread1.§cRun §cFailed");
					return;
				}
				
				p.sendMessage("§3Created Home §7" + name );
				func.player_wait.remove(p.getName());
				func.updatefiles();
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_GUITAR, 10, 10);
        }
   }

   public  class Thread2 implements Runnable {
	
	   public Player p;
	   public Thread2(Player p) {
		this.p = p;
		}
	   
	   @Override
       public synchronized  void run() {
				
				try {
					db.update_allowed_players();
					p.sendMessage("§aUpdated§f 'allowed_players.json'");
					func.updatefiles();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					
					p.sendMessage("§c" + e.getMessage());
					Functions.print("§c[ERROR]§f 'allowed_players.json': §cFailed to update");
					e.printStackTrace();
					func.ERROR("home update", p, e.toString(), e.getStackTrace());
				}
       }
   }
   
   public  class Thread4 implements Runnable {
		
	   public Player p;
	   public String msg;
	   public Thread4(Player p, String msg) {
		this.p = p;
		this.msg = msg;
		}
	   
	   @Override
       public void run() {
					try {
						db.execute_prepare("INSERT INTO player_infractions (player_name, UUID, by_who, Reason, Type) VALUES ('"+p.getName() +"', '"+p.getUniqueId()+"', 'Portal-Plugin', '"+msg+"', '"+Infraction.KICK+"')");
						Functions.print("§3@Main.Thread4.§cRun §3SQL §aSUCCESFULL");
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
   }
   }
   public  class Thread3 implements Runnable {
		
	   public Player p;
	   public Thread3(Player p) {
		this.p = p;
		}
	   
	@Override
    public synchronized  void run() {
				
				try {
					db.update_areaMap();
					p.sendMessage("§aUpdated§f 'player_homes.json'");
					func.updatefiles();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					p.sendMessage("§c" + e.toString());
					Functions.print("§c[ERROR]§f 'player_homes.json': §cFailed to update");
					func.ERROR("home update", p, e.getMessage(), e.getStackTrace());
				}
      }
  }
}
