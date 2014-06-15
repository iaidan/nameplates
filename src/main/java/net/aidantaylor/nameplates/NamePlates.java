package main.java.net.aidantaylor.nameplates;

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
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

public final class NamePlates extends JavaPlugin implements Listener {
	private ScoreboardManager manager;
	private static Scoreboard board;
	private boolean modifiedchat, modifiedtab, cleanTab, modifyChat, modifyTab, onlyCustom, showHealth, debug, autoRefresh;
	private int refreshInterval = 60000;
	private String[] colours = { "AQUA", "BLACK", "BLUE", "DARK_AQUA",
			"DARK_BLUE", "DARK_GRAY", "DARK_GREEN", "DARK_PURPLE", "DARK_RED",
			"GOLD", "GRAY", "GREEN", "LIGHT_PURPLE", "RED", "YELLOW", "WHITE" }, custom;
	private ArrayList<Team> teams = new ArrayList<Team>();
	private HealthBar health;
	private String OPColour;
	private Timer timer;

	@Override
	public void onEnable() {
		getCommand("nameplates").setExecutor(new CommandExe(this));
		getServer().getPluginManager().registerEvents(this, this);

		this.saveDefaultConfig();

		log(getName() + " has been enabled!", true);
		load();
	}

	@Override
	public void onDisable() {
		if (timer != null) {
			timer.cancel();
		}
		
		for(int i = 0; i < teams.size(); i++) {
			board.getTeam(teams.get(i).getName()).unregister();
		}
		
		log(getName() + " has been disabled!", true);
	}

	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		setPlayer(event.getPlayer());
	}

	public void load() {
		FileConfiguration configFile = getConfig();
		
		autoRefresh = configFile.getBoolean("autorefresh");
		refreshInterval = configFile.getInt("refreshinterval");
		
		modifyTab = configFile.getBoolean("modifytab");
		cleanTab = configFile.getBoolean("cleantab");
		
		modifyChat = configFile.getBoolean("modifychat");
		OPColour = ChatColor.translateAlternateColorCodes(new String("&").charAt(0), configFile.getString("OPColour"));
		
		showHealth = configFile.getBoolean("showhealth");
		
		debug = configFile.getBoolean("debug");
		
		onlyCustom = configFile.getBoolean("onlycustom");
		custom = configFile.getConfigurationSection("custom").getKeys(false).toArray(new String[0]);
		
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
	
	private void autoRefresh() {
		if (timer != null) {
			timer.cancel();
		}
		
		timer = new Timer();

		timer.schedule(new TimerTask() {
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

	@Override
	public void reloadConfig() {
		super.reloadConfig();
		getConfig().options().copyDefaults(true);
		
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
		
		if (player.isOp() && OPColour != null) {
			team = newTeam("Operator", OPColour, ChatColor.RESET.toString());
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
					
					break;
				}
			}
		}
		
		log(player.getName() + " prefix set to " + prefix);
		log(player.getName() + " suffix set to " + suffix);

		if (modifyChat == true) {
			modifiedchat = true;
			player.setDisplayName(prefix + player.getName() + suffix);
			log(player.getName() + " displayName set to " + player.getDisplayName());
		} else if (modifiedchat == true) {
			modifiedchat = false;
			player.setDisplayName(player.getName());
		}

		if (modifyTab == true) {
			modifiedtab = true;
			player.setPlayerListName(prefix + player.getName() + suffix);
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
