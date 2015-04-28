package net.aidantaylor.bukkit.nameplates;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

public final class NamePlates extends JavaPlugin implements Listener {
	private ScoreboardManager manager;
	private static Scoreboard board;
	private boolean modifiedchat, modifiedtab, cleanTab, modifyChat, modifyTab, onlyCustom, showHealth, debug, autoRefresh, useDisplayName;
	private int refreshInterval = 60000;
	private String[] colours = { "AQUA", "BLACK", "BLUE", "DARK_AQUA",
			"DARK_BLUE", "DARK_GRAY", "DARK_GREEN", "DARK_PURPLE", "DARK_RED",
			"GOLD", "GRAY", "GREEN", "LIGHT_PURPLE", "RED", "YELLOW", "WHITE" }, custom;
	private ArrayList<Team> teams = new ArrayList<Team>();
	private HealthBar health;
	private String OPColour = null;
	private Timer timer;

	@Override
	public void onEnable() {
		getCommand("nameplates").setExecutor(new CommandExe(this));
		getServer().getPluginManager().registerEvents(this, this);

		saveDefaultConfig();

		log(getName() + " has been enabled!", true);
		load();
	}

	@Override
	public void onDisable() {
		if (timer != null) {
			timer.cancel();
		}
		
		unload();
		
		log(getName() + " has been disabled!", true);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		setPlayer(event.getPlayer());
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		
		try {
			player.getScoreboard().getPlayerTeam(player).removePlayer(player);
		} catch(Exception e) {
			
		}
		
		if (modifyChat == true) {
			player.setDisplayName(player.getName());
		}
		
		if (modifyTab == true) {
			player.setPlayerListName(player.getName());
		}
	}

	public void load() {
		getConfig().options().copyDefaults(true);
		FileConfiguration configFile = getConfig();
		
		autoRefresh = configFile.getBoolean("autorefresh");
		refreshInterval = configFile.getInt("refreshinterval");
		
		useDisplayName = configFile.getBoolean("useDisplayName");
		
		modifyTab = configFile.getBoolean("modifytab");
		cleanTab = configFile.getBoolean("cleantab");
		
		modifyChat = configFile.getBoolean("modifychat");
		
		OPColour = configFile.getString("OPColour");
		
		showHealth = configFile.getBoolean("showhealth");
		
		debug = configFile.getBoolean("debug");
		
		try {
			onlyCustom = configFile.getBoolean("onlycustom");
			custom = configFile.getConfigurationSection("custom").getKeys(false).toArray(new String[0]);
		} catch(Exception e) {
			custom = new String[0];
		}
		
		manager = Bukkit.getScoreboardManager();
		setBoard(manager.getMainScoreboard());

		health = new HealthBar(board);

		if (showHealth == true) {
			health.showHealth();
			log("Player health bars enabled");
		} else {
			health.hidehealth();
			log("Player health bars disabled");
		}

		genColours();
		refreshPlates();
		
		if (autoRefresh == true) {
			autoRefresh();
		}
	}
	
	public void unload() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			for(Team team : teams) {
				try {
					team.removePlayer(player);
				} catch(Exception e) { }
			}
			
			if (modifyChat == true) {
				player.setDisplayName(player.getName());
			}
			
			if (modifyTab == true) {
				player.setPlayerListName(player.getName());
			}
		}
			
		for(Team team : teams) {
			try {
				team.unregister();
			} catch(Exception e) { }
		}
	}
	
	private void autoRefresh() {
		if (timer != null) {
			timer.cancel();
		}
		
		timer = new Timer();

		timer.schedule(new TimerTask() {
		    @Override
			public void run() {
			   	refreshPlates();
		    }
		}, 0, refreshInterval);
	}

	private void genColours() {
		for (String colour : colours) {
			Team team = null;

			if (board.getTeam(colour) == null) {
				team = board.registerNewTeam(colour);
			} else {
				team = board.getTeam(colour);
			}
			
			team.setPrefix(ChatColor.valueOf(colour).toString());
			team.setSuffix(ChatColor.RESET.toString());

			teams.add(team);
		}
	}

	public void reload() {
		reloadConfig();
		
		unload();
		load();
	}
	
	public void refreshPlates() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			setPlayer(player);
		}
	}
	
	public Team newTeam(String name, String prefix, String suffix) {
		Team team = null;
		
		if (board.getTeam(name) == null) {
			team = board.registerNewTeam(name);
		} else {
			team = board.getTeam(name);
		}
		
		team.setPrefix(prefix);
		team.setSuffix(suffix);
		
		teams.add(team);
		
		return team;
	}

	public void setPlayer(Player player) {
		FileConfiguration configFile = getConfig();
		
		try {
			player.setScoreboard(board);
		} catch(Exception e) { }

		Team team = null;

		String prefix = "";
		String suffix = "";

		for (String colour : colours) {
			Team r = teams.get(ArrayUtils.indexOf(colours, colour));
			r.removePlayer(player.getPlayer());
		}

		if (modifyChat == true) {
			player.setDisplayName(player.getName());
		}
		
		if (modifyTab == true) {
			player.setPlayerListName(player.getName());
		}
		
		if (player.isOp() && OPColour != null) {
			log(player.getName() + " is an Operator");
			team = newTeam("Operator", ChatColor.translateAlternateColorCodes(new String("&").charAt(0), OPColour), ChatColor.RESET.toString());
			team.addPlayer(player.getPlayer());
			
			prefix = team.getPrefix();
			suffix = team.getSuffix();
		} else {
			if (onlyCustom != true) {
				for (String colour : colours) {
					team = teams.get(ArrayUtils.indexOf(colours, colour));
					
					if (player.hasPermission("nameplates.colour." + colour.toLowerCase())) {
						team.addPlayer(player.getPlayer());
						
						prefix = team.getPrefix();
						suffix = team.getSuffix();

						log(player.getName() + " added to the team " + team.getName());
						break;
					}
				}
			}
			
			for (int i = 0; i < custom.length; i++) {
				if (player.hasPermission("nameplates.custom." + custom[i])) {
					prefix = ChatColor.translateAlternateColorCodes(new String("&").charAt(0), configFile.getString("custom." + custom[i] + ".prefix"));
					suffix = ChatColor.translateAlternateColorCodes(new String("&").charAt(0), configFile.getString("custom." + custom[i] + ".suffix"));
					
					team = newTeam(custom[i], prefix, suffix);
					team.addPlayer(player.getPlayer());
					
					prefix = team.getPrefix();
					suffix = team.getSuffix();
					log(player.getName() + " 2");
					
					break;
				}
			}
		}
		
		log(player.getName() + " prefix set to " + prefix);
		log(player.getName() + " suffix set to " + suffix);

		if (modifyChat == true) {
			modifiedchat = true;
			
			if (useDisplayName == true) {
				player.setDisplayName(prefix + player.getDisplayName() + suffix);
			} else {
				player.setDisplayName(prefix + player.getName() + suffix);;
			}
			
			log(player.getName() + " displayName set to " + player.getDisplayName());
		} else if (modifiedchat == true) {
			modifiedchat = false;
			player.setDisplayName(player.getName());
		}

		if (modifyTab == true) {
			modifiedtab = true;
			
			if (useDisplayName == true) {
				player.setPlayerListName(prefix + player.getDisplayName() + suffix);
			} else {
				player.setPlayerListName(prefix + player.getName() + suffix);
			}
			
			log(player.getName() + " listName set to " + player.getPlayerListName());
		} else if (modifiedtab == true) {
			modifiedtab = false;
			player.setPlayerListName(player.getName());
		}

		if (cleanTab == true) {
			player.setPlayerListName(ChatColor.RESET + player.getName());
			log(player.getName() + " listName set to " + player.getPlayerListName());
		}
	}
	
	public void log(String string) {
		log(string, false);
	}
	
	public void log(String string, boolean bypassdebug) {
		if (bypassdebug == true || debug == true) {
			getLogger().info(string);
		}
	}

	public static Scoreboard getBoard() {
		return board;
	}

	public static void setBoard(Scoreboard b) {
		board = b;
	}
}
