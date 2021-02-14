package secommands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.gmail.charlesantlord.simpleeconomy.Bank;
import com.gmail.charlesantlord.simpleeconomy.BankAccount;
import com.gmail.charlesantlord.simpleeconomy.Button;
import com.gmail.charlesantlord.simpleeconomy.ChatUiManager;
import com.gmail.charlesantlord.simpleeconomy.JSONMessage;

public class SeTransferMoney extends SeCommand
{
	private BankAccount target;
	private int step = 0;

	public SeTransferMoney(Player player, BankAccount selectedAccount) 
	{
		super(player, selectedAccount);
	}

	@Override
	public void execute() 
	{
		List<Button> buttons = new ArrayList<Button>();
		Bank.getInstance().getAllBankAccounts().forEach((ba) -> buttons.add(new Button(ba.getName(), "Transfer money to this bank account", "/se response " + ba.getName(), ChatColor.BLUE)));
		buttons.add(new Button("FederalReserve", "Transfer money to the federal reserve", "/se response FederalReserve", ChatColor.YELLOW));
		buttons.add(new Button("Cancel", "Cancel transaction", "/se cancel", ChatColor.RED));
		ChatUiManager.generateUi("Transfer money from " + selectedBankAccount.getName() + " to : ", buttons.toArray(new Button[buttons.size()])).send(player);
	}

	@Override
	public boolean next(String input) 
	{
		if(step == 0)
		{
			target = Bank.getInstance().getBankAccountByName(input);

			if(target == null)
				player.sendMessage("Invalid target.");
			
			showCancellableMessage("Enter the amount you want to send to " + target.getName() + ".", "Cancel the money transfer");
			setAwaitingResponse(player.getUniqueId(), true);
			
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
				player.sendMessage("Please input a number.");
				return true; // To loop
			}
			
			if(amount < 0)
			{
				player.sendMessage("You can not send a negative number. Please try again.");
				return true;
			}
			
			if(Bank.getInstance().transferMoney(selectedBankAccount, target, amount))
				player.sendMessage("Sent " + amount + " to " + target.getName());
			else
				player.sendMessage("You don't have enough money to do this operation.");
			
			setAwaitingResponse(player.getUniqueId(), false);
			return false;
		}
	}

	@Override
	public void cancel() 
	{
		player.sendMessage("Cancelled money transfer.");
		setAwaitingResponse(player.getUniqueId(), false);
	}
}