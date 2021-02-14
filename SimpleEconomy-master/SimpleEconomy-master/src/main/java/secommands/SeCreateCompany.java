package secommands;

import org.bukkit.entity.Player;

import com.gmail.charlesantlord.simpleeconomy.Bank;
import com.gmail.charlesantlord.simpleeconomy.BankAccount;

public final class SeCreateCompany extends SeCommand
{
	public SeCreateCompany(Player player, BankAccount selectedAccount) 
	{
		super(player, selectedAccount);
	}
	
	@Override
	public void execute() 
	{
		showCancellableMessage("Enter the name of your new company.", "Cancel the creation of the company");
		setAwaitingResponse(player.getUniqueId(), true);
	}

	@Override
	public boolean next(String input) 
	{
		if(input.contains(" "))
		{
			player.sendMessage("A company name can not contain a space. Please try again.");
			return true;
		}
		
		if(Bank.getInstance().createCompany(input, player.getUniqueId()) == null)
		{
			player.sendMessage("Two companies can not have the same name. Please try again.");
			return true;
		}
		else
		{
			setAwaitingResponse(player.getUniqueId(), false);
			player.sendMessage("Successfully created a new company.");
			return false;
		}
	}

	@Override
	public void cancel() 
	{
		player.sendMessage("Cancelled company creation.");
		setAwaitingResponse(player.getUniqueId(), false);
	}
}