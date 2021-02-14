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

public class SeDisband extends SeCommand
{
	private Company companyToDisband;
	
	public SeDisband(Player player, BankAccount selectedAccount) 
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
				companyToDisband = c;
				List<Button> buttons = new ArrayList<Button>();
				buttons.add(new Button("Yes", "Leave the company",   "/se response yes", ChatColor.GREEN));
				buttons.add(new Button("No",  "Stay in the company", "/se response no",  ChatColor.RED));
				ChatUiManager.generateUi("Are you sure you want to disband " + selectedBankAccount.getName() + " ? The money in its bank account will be split between its members.", buttons.toArray(new Button[buttons.size()])).send(player);
				return;
			}
		}
		
		player.sendMessage("You can only disband a company if you are its creator.");
		CommandManager.finishCommand(player.getUniqueId());
	}

	@Override
	public boolean next(String input) 
	{
		switch(input)
		{
		case "yes":
			for(UUID member : companyToDisband.getMembers())
			{
				Player p = Bukkit.getPlayer(member);
				if(p != null)
					p.sendMessage("The company " + companyToDisband.getName() + " has been disbanded.");
			}
			companyToDisband.disband();
			player.sendMessage("Disbanded " + selectedBankAccount.getName());
			return false;

		case "no":
			player.sendMessage("Cancelled disbanding company");
			return false;

		default:
			player.sendMessage("Please press either yes or no.");
			return true;
		}
	}

	@Override
	public void cancel() 
	{
		
	}
}