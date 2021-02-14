package secommands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.gmail.charlesantlord.simpleeconomy.Bank;
import com.gmail.charlesantlord.simpleeconomy.BankAccount;
import com.gmail.charlesantlord.simpleeconomy.Button;
import com.gmail.charlesantlord.simpleeconomy.ChatUiManager;
import com.gmail.charlesantlord.simpleeconomy.CommandManager;
import com.gmail.charlesantlord.simpleeconomy.Company;

public class SeInvitePlayer extends SeCommand
{
	Company selectedCompany;
	
	public SeInvitePlayer(Player player, BankAccount selectedAccount) 
	{
		super(player, selectedAccount);
	}

	@Override
	public void execute() 
	{
		selectedCompany = Bank.getInstance().getCompanyByName(selectedBankAccount.getName());
		
		if(selectedCompany == null)
		{
			player.sendMessage("Invalid company name.");
			cancel();
			return;
		}
		
		// Loop for all connected players. If they are not currently in the company, add a button to invite them.
		List<Button> buttons = new ArrayList<Button>();
		for(Player p : Bukkit.getOnlinePlayers())
			if(!selectedCompany.hasMember(p.getUniqueId()))
				buttons.add(new Button(p.getName(), "Invite " + p.getName(), "/se response " + p.getName(), ChatColor.GREEN));
		
		// Add cancel button
		buttons.add(new Button("Cancel", "Cancel the invitation", "/se cancel", ChatColor.RED));
		// Generate invite interface
		ChatUiManager.generateUi("Select the player you want to invite to " + selectedBankAccount.getName(), buttons.toArray(new Button[buttons.size()])).send(player);
		setAwaitingResponse(player.getUniqueId(), true);
	}

	@Override
	public boolean next(String input) 
	{
		for(Player p : Bukkit.getOnlinePlayers())
		{
			if(input.equals(p.getName()))
			{
				player.sendMessage("The invite was sent to " + p.getName());
				CommandManager.sendInvite(p.getUniqueId(), selectedCompany);
				List<Button> buttons = new ArrayList<Button>();
				buttons.add(new Button("Accept", "Accept the invitation", "/se accept " + selectedCompany.getName(), ChatColor.GREEN));
				buttons.add(new Button("Refuse", "Refuse the invitation", "/se refuse " + selectedCompany.getName(), ChatColor.RED));
				ChatUiManager.generateUi("You recieved an invitation from " + player.getName() + " to join " + selectedCompany.getName(), buttons.toArray(new Button[buttons.size()])).send(p);
				setAwaitingResponse(p.getUniqueId(), false);
				return false;
			}
		}
		
		return false;
	}

	@Override
	public void cancel() 
	{
		player.sendMessage("Cancelled player invite.");
		setAwaitingResponse(player.getUniqueId(), false);
	}
}