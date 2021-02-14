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
import com.gmail.charlesantlord.simpleeconomy.Company;

public class SeLeaveCompany extends SeCommand
{
	public SeLeaveCompany(Player player, BankAccount selectedAccount) 
	{
		super(player, selectedAccount);
	}

	@Override
	public void execute() 
	{
		List<Button> buttons = new ArrayList<Button>();
		buttons.add(new Button("Yes", "Leave the company",   "/se response yes", ChatColor.GREEN));
		buttons.add(new Button("No",  "Stay in the company", "/se response no",  ChatColor.RED));
		ChatUiManager.generateUi("Are you sure you want to leave " + selectedBankAccount.getName() + " ?", buttons.toArray(new Button[buttons.size()])).send(player);
	}

	@Override
	public boolean next(String input) 
	{
		switch(input)
		{
		case "yes":
			List<Company> playerCompanies = Bank.getInstance().getPlayerCompanies(player.getUniqueId());
			for(Company c : playerCompanies)
			{
				if(c.getName().equals(selectedBankAccount.getName()))
				{
					if(c.isCreator(player.getUniqueId()))
					{
						player.sendMessage("The creator can not leave the company.");
						return false;
					}
					else
					{
						c.removePlayer(player.getUniqueId());
						for(UUID uuid : c.getMembers())
							Bukkit.getPlayer(uuid).sendMessage(player.getName() + " left the company.");
					}
				}
			}

			player.sendMessage("You left " + selectedBankAccount.getName());
			return false;

		case "no":
			player.sendMessage("Cancelled leaving company");
			return false;

		default:
			player.sendMessage("Please enter either yes or no.");
			return true;
		}
	}

	@Override
	public void cancel() 
	{

	}
}