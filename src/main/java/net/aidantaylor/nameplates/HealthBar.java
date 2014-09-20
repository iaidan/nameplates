package net.aidantaylor.nameplates;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class HealthBar {
	private Objective health;
	private Scoreboard board;
	private DisplaySlot displayslot = DisplaySlot.BELOW_NAME;
	private String displayname = ChatColor.RED + "\u2764";
	
	public HealthBar(Scoreboard board) {
		this.board = board;
		
		setHealths();
	}

	public void hidehealth() {
		if (board.getObjective("showhealth") != null) {
			health = board.getObjective("showhealth");
			health.unregister();
		}
	}
	
	public void showHealth() {
		registerObjective();
		
		health.setDisplaySlot(displayslot);
		health.setDisplayName(displayname);
	}

	private void registerObjective() {
		if (board.getObjective("showhealth") == null) {
			health = board.registerNewObjective("showhealth", "health");
		} else {
			health = board.getObjective("showhealth");
		}
		
		setHealths();
	}
	
	public DisplaySlot getDisplaySlot() {
		return displayslot;
	}
	
	public void setDisplaySlot(DisplaySlot displayslot) {
		this.displayslot = displayslot;
		showHealth();
	}
	
	public String getDisplayStyle() {
		return displayname;
	}
	
	public void setDisplayStyle(String displayname) {
		this.displayname = displayname;
		showHealth();
	}
	
	private void setHealths() {
		for(Player player : Bukkit.getOnlinePlayers()) {
			player.setScoreboard(board);
			player.setHealth(player.getHealth());
		}
	}
}
