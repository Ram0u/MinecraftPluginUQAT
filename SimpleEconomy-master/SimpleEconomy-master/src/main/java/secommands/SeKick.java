package secommands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.gmail.charlesantlord.simpleeconomy.Bank;
import com.gmail.charlesantlord.simpleeconomy.BankAccount;
import com.gmail.charlesantlord.simpleeconomy.Button;
import com.gmail.charlesantlord.simpleeconomy.ChatUiManager;
import com.gmail.charlesantlord.simpleeconomy.CommandManager;
import com.gmail.charlesantlord.simpleeconomy.Company;

public class SeKick extends SeCommand
{
	private Company company;

	public SeKick(Player player, BankAccount selectedAccount) 
	{
		super(player, selectedAccount);
	}

	@Override
	public void execute() 
	{
		for(Company c : Bank.getInstance().getPlayerCompanies(player.getUniqueId()))
		{
			if(c.getName().equals(selectedBankAccount.getName()) && c.isCreator(player.getUniqueId()))
			{
				company = c;
				// Loop for all connected players. If they are not currently in the company, add a button to invite them.
				List<Button> buttons = new ArrayList<Button>();
				for(UUID u : c.getMembers())
				{
					if(!c.isCreator(u) && !u.equals(player.getUniqueId()))
					{
						Player p = Bukkit.getPlayer(u);
						buttons.add(new Button(p.getName(), "Kick " + p.getName(), "/se response " + p.getName(), ChatColor.DARK_RED));
					}
				}

				buttons.add(new Button("Cancel", "Cancel kick", "/se cancel", ChatColor.RED));
				ChatUiManager.generateUi("Select the player you want to kick from your company.", buttons.toArray(new Button[buttons.size()])).send(player);
				return;
			}
		}
	}

	@Override
	public boolean next(String input) 
	{
		UUID target = Bukkit.getPlayer(input).getUniqueId();

		if(company.isCreator(target))
		{
			player.sendMessage("The creator can not be kicked from his company.");
			return false;
		}

		if(company.removePlayer(target))
		{
			player.sendMessage("Kicked " + input + " from the company.");
			Player p = Bukkit.getPlayer(target);
			if(p != null)
				p.sendMessage("You have been kicked from " + company.getName());
		}
		else
			player.sendMessage("Could not find player " + input);

		return false;
	}

	@Override
	public void cancel() 
	{
		player.sendMessage("Cancelled kicking player.");
		CommandManager.finishCommand(player.getUniqueId());
	}
}