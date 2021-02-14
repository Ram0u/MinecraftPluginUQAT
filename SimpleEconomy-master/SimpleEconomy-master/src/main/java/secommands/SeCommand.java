package secommands;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.gmail.charlesantlord.simpleeconomy.BankAccount;
import com.gmail.charlesantlord.simpleeconomy.JSONMessage;

public abstract class SeCommand 
{
	protected Player player;
	protected BankAccount selectedBankAccount;
	private static HashMap<UUID, Boolean> playersAwaitingResponse = new HashMap<UUID, Boolean>();
	
	public SeCommand(Player player, BankAccount selectedAccount)
	{
		this.player = player;
		this.selectedBankAccount = selectedAccount;
	}
	
	public abstract void execute(); 		 	// Executed the first time to send menu to the player
	public abstract boolean next(String input); // Executed after to get answers from the player -- Returns false when done
	public abstract void cancel(); 			 	// Executed by the command /se cancel
	
	public static boolean isAwaitingResponse(UUID player)
	{
		if(!playersAwaitingResponse.containsKey(player))
			return false;
		
		return playersAwaitingResponse.get(player);
	}
	
	public static void setAwaitingResponse(UUID player, boolean awaiting)
	{
		playersAwaitingResponse.put(player, awaiting);
	}
	
	protected void showCancellableMessage(String message, String cancelTooltip)
	{
		JSONMessage.create(message + " ")
		.then("[CANCEL]")
		.style(ChatColor.BOLD)
		.color(ChatColor.RED)
		.tooltip(cancelTooltip)
		.runCommand("/se cancel")
		.send(player);
	}
}