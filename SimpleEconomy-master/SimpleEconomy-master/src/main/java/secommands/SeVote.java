package secommands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.gmail.charlesantlord.simpleeconomy.BankAccount;
import com.gmail.charlesantlord.simpleeconomy.Button;
import com.gmail.charlesantlord.simpleeconomy.ChatUiManager;
import com.gmail.charlesantlord.simpleeconomy.CommandManager;
import com.gmail.charlesantlord.simpleeconomy.JSONMessage;

public class SeVote extends SeCommand
{
	private static boolean electionsAreActive = false;
	private static HashMap<String, Integer> electionCounters = new HashMap<String, Integer>();
	private static HashMap<UUID, Boolean> hasVoted = new HashMap<UUID, Boolean>();

	public SeVote(Player player, BankAccount selectedAccount) 
	{
		super(player, selectedAccount);
	}

	@Override
	public void execute() 
	{
		if(electionsAreActive)
		{
			List<Button> candidateButtons = new ArrayList<Button>();
			candidateButtons.add(new Button("Propose yourself", "Propose your candidature to become the next president", "/se response create", ChatColor.GREEN));
			candidateButtons.add(new Button("View results", "View the current election results", "/se response results", ChatColor.GOLD));

			for(String candidate : electionCounters.keySet())
				candidateButtons.add(new Button(candidate, "Vote for this candidate", "/se response " + candidate, ChatColor.BLUE));

			candidateButtons.add(new Button("Cancel", "Cancel voting", "/se cancel", ChatColor.RED));

			ChatUiManager.generateUi("Select the candidate you want to vote for or create your candidature.", candidateButtons.toArray(new Button[candidateButtons.size()])).send(player);
		}
		else
		{
			player.sendMessage("Elections are not active.");
			CommandManager.finishCommand(player.getUniqueId());
		}
	}

	@Override
	public boolean next(String input) 
	{
		if(input.equals("create"))
		{
			// Create candidature
			if(!electionCounters.containsKey(player.getName()))
			{
				electionCounters.put(player.getName(), 0);
				player.sendMessage("Added your name to the list of candidates.");
			}
			else
				player.sendMessage("You can only propose your candidature once.");
		}
		else if(input.equals("results"))
		{
			JSONMessage msg = JSONMessage.create().bar().then("Here are the current election results.\n").style(ChatColor.BOLD);
			for(String candidate : electionCounters.keySet())
				msg.then(candidate).color(ChatColor.GREEN).then(" : ").then(electionCounters.get(candidate) + " votes.\n");

			msg.send(player);
		}
		else
		{
			if(!hasVoted.containsKey(player.getUniqueId()))
			{
				if(electionCounters.containsKey(input))
				{
					player.sendMessage("Successfully voted for " + input);
					electionCounters.put(input, electionCounters.get(input) + 1);
					hasVoted.put(player.getUniqueId(), true);
				}
				else
					player.sendMessage("Candidate " + input + " can not be found.");
			}
			else
				player.sendMessage("You can only vote once.");
		}
		return true;
	}

	@Override
	public void cancel() 
	{
		player.sendMessage("Cancelled voting.");
		CommandManager.finishCommand(player.getUniqueId());
	}

	public static HashMap<String, Integer> getResults()
	{
		return electionCounters;
	}

	public static void resetResults()
	{
		electionCounters = new HashMap<String, Integer>();
		hasVoted = new HashMap<UUID, Boolean>();
	}
	
	public static void startElections()
	{
		resetResults();
		electionsAreActive = true;
	}
	
	public static void stopElections()
	{
		resetResults();
		electionsAreActive = false;
	}
}