package secommands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.gmail.charlesantlord.simpleeconomy.Bank;
import com.gmail.charlesantlord.simpleeconomy.BankAccount;

public class SeChangeTaxes extends SeCommand
{
	private int step = 0;

	public SeChangeTaxes(Player player, BankAccount selectedAccount) 
	{
		super(player, selectedAccount);
	}

	@Override
	public void execute() 
	{
		showCancellableMessage("Enter the new taxe rate in percentage. WARNING : This action will immediately collect taxes at the current rate. Current tax rate : " + Bank.getInstance().getTaxesRate() + "%", "Cancel this action");
		setAwaitingResponse(player.getUniqueId(), true);
	}

	@Override
	public boolean next(String input) 
	{
		if(step == 0)
		{
			int amount = 0;
			try
			{
				amount = Integer.parseInt(input);
			}
			catch(NumberFormatException e)
			{
				player.sendMessage("Please input a number between 0 and 100.");
				return true; // To loop
			}

			if(amount > 100 || amount < 0)
			{
				player.sendMessage("Please input a number between 0 and 100.");
				return true; // To loop
			}

			player.sendMessage("Successfully collected " + Bank.getInstance().collectTaxes() + " in taxes at the previous rate of " + Bank.getInstance().getTaxesRate() + "%");
			Bukkit.getOnlinePlayers().forEach((p) -> p.sendMessage("The president just collected taxes. Check your balance."));

			player.sendMessage("Taxes rate are now " + amount + "%");
			Bank.getInstance().setTaxesRate(amount);
			
			player.sendMessage("Now, please enter the percentage of tax revenues you want to print. Current percentage : " + Bank.getInstance().getPrintPercentage() + "%");
			step++;
			return true;
		}
		else
		{
			int amount = 0;
			try
			{
				amount = Integer.parseInt(input);
			}
			catch(NumberFormatException e)
			{
				player.sendMessage("Please input a number between 0 and 100.");
				return true;
			}
			
			if(amount > 100 || amount < 0)
			{
				player.sendMessage("Please input a number between 0 and 100.");
				return true;
			}
			
			Bank.getInstance().setPrintPercentage(amount);
			player.sendMessage("Successfully changed the print percentage to " + Bank.getInstance().getPrintPercentage() + "%");

			setAwaitingResponse(player.getUniqueId(), false);
			return false;
		}
	}

	@Override
	public void cancel() 
	{
		if(step == 1)
		{
			player.sendMessage("Could not cancel after collecting taxes");
			player.sendMessage("Now, please enter the percentage of tax revenues you want to print. Current percentage : " + Bank.getInstance().getPrintPercentage() + "%");
		}
		else
		{
			player.sendMessage("Cancelled changing tax rate.");
			setAwaitingResponse(player.getUniqueId(), false);
		}
	}
}