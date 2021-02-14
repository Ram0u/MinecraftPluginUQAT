package com.gmail.charlesantlord.simpleeconomy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import secommands.SeCommand;
import secommands.SeCreateCompany;
import secommands.SeDisband;
import secommands.SeInvitePlayer;
import secommands.SeKick;
import secommands.SeLeaveCompany;
import secommands.SeTransferMoney;
import secommands.SeVote;
import secommands.SeChangeTaxes;
import inventorymenu.BaseInventoryMenu;
import inventorymenu.InventoryMenu;
import inventorymenu.MarketMenu;
import inventorymenu.NewMarketMenu;
import inventorymenu.SellInventoryMenu;
import inventorymenu.SellMenu;


public class CommandManager implements CommandExecutor
{
	// TODO: On peut pas reutiliser le meme menu pour transferer sinon ca met une erreur
	// TODO: Quand on quitte ca cancel l'operation
	// TODO: Quand t'es deco qqun peut pas tenvoyer de larjan pis ca met une erreur
	private static HashMap<UUID, SeCommand> currentPlayerCommands = new HashMap<UUID, SeCommand>();
	private static HashMap<UUID, BankAccount> selectedAccounts = new HashMap<UUID, BankAccount>();
	private static HashMap<UUID, BaseInventoryMenu> openInventoryMenu = new HashMap<UUID, BaseInventoryMenu>();
	private static HashMap<UUID, List<Company>> pendingInvites = new HashMap<UUID, List<Company>>();

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) 
	{
		if(!(sender instanceof Player))
		{
			sender.sendMessage("This command can ony be executed by a player.");
			return true;
		}

		Player player = (Player)sender;

		switch(command.getName().toLowerCase())
		{
		case "setpresident":
			if(player.isOp())
			{
				if(Bukkit.getPlayer(args[0]) == null)
				{
					player.sendMessage("The player " + args[0] + " could not be found.");
					return true;
				}
				
				Bank.getInstance().setPresident(Bukkit.getPlayer(args[0]).getUniqueId());
			}
			return true;
			
		case "finishelections":
			if(player.isOp())
			{
				HashMap<String, Integer> votes = SeVote.getResults();
				SeVote.stopElections();
				
				JSONMessage msg = JSONMessage.create().bar().then("Elections are over. Here are the final election results.\n").style(ChatColor.BOLD);
				for(String candidate : votes.keySet())
					msg.then(candidate).color(ChatColor.GREEN).then(" : ").then(votes.get(candidate) + " votes.\n");
				
				for(Player p : Bukkit.getOnlinePlayers())
					msg.send(p);
			}
			return true;
			
		case "startelections":
			if(player.isOp())
			{
				SeVote.startElections();
				
				for(Player p : Bukkit.getOnlinePlayers())
					p.sendMessage("Elections are now active.");
			}
			return true;
		
		case "se":
			JSONMessage message; // Message to be sent

			if(args.length == 0)
			{
				List<Company> companies = Bank.getInstance().getPlayerCompanies(player.getUniqueId());

				List<Button> buttons = new ArrayList<Button>();

				buttons.add(new Button("Create company", "Create a company", "/se create", ChatColor.YELLOW));

				if(Bank.getInstance().isPresident(player.getUniqueId()))
				{
					buttons.add(new Button("Federal Reserve", "Select this account", "/se select " + "FederalReserve", ChatColor.RED));
				}

				buttons.add(new Button("My account", "Select this account", "/se select " + player.getName(), ChatColor.GREEN));

				if(companies != null)
					for(Company c : companies)
						buttons.add(new Button(c.getName(), "Select this account", "/se select " + c.getName(), ChatColor.BLUE));

				message = ChatUiManager.generateUi("Select the bank account you want to use.", buttons.toArray(new Button[buttons.size()]));

				message.send(player);

				return true;

			}

			BankAccount selectedAccount = null;

			switch(args[0])
			{
			default:
				player.sendMessage("This command is not recognized by the SimpleEconomy plugin.");
				return true;


			case "select":
				// Try to load bank account
				BankAccount ba = Bank.getInstance().getPlayerBankAccountByName(player.getUniqueId(), args[1]);
				
				// Needed as the player's UUID is not associated with the federal reserve
				if(Bank.getInstance().isPresident(player.getUniqueId()) && args[1].equals("FederalReserve"))
					ba = Bank.getInstance().getFederalReserve();
				
				// If the bank couldn't load the desired bank account in relation to the player, exit
				if(ba == null)
				{
					player.sendMessage("You don't have access to this bank account. ");
					return true;
				}
				
				// Set the selected account in the HashMap
				selectedAccounts.put(player.getUniqueId(), ba);
				
				// Create the buttons everyone has access to
				List<Button> generalActionButtons = new ArrayList<Button>();
				generalActionButtons.add(new Button("Back",      "Return to the account selection screen",         "/se",          ChatColor.LIGHT_PURPLE));
				generalActionButtons.add(new Button("Balance",   "See the available balance in your bank account", "/se balance",  ChatColor.BLUE));
				generalActionButtons.add(new Button("Sell",      "Sell items on the market",                       "/se sell",     ChatColor.GREEN)); 
				generalActionButtons.add(new Button("Transfer",  "Transfer money to the desired bank account",     "/se transfer", ChatColor.RED));
				generalActionButtons.add(new Button("Market",    "Open the market section to look at offers",      "/se market",   ChatColor.YELLOW));
				generalActionButtons.add(new Button("Elections", "Vote for your favourite candidate",              "/se vote", 	   ChatColor.DARK_PURPLE));
				
				// Generate the first part of the UI, which is the same for everyone
				if(ba.getName().equals("FederalReserve"))
					message = ChatUiManager.generateUi("Select the action you want to perform.", generalActionButtons.toArray(new Button[generalActionButtons.size()]));
				else
				{
					int income =  ba.getIncome();
					int taxRate = Bank.getInstance().getTaxesRate();
					message = JSONMessage.create
							("\n\n\n\n\nDue taxes : ").style(ChatColor.BOLD).color(ChatColor.AQUA).then("Income (" + income + ") x Tax rate (" +taxRate + "%) = " + (int)(income * (taxRate / 100f))).then("\n").bar();
					ChatUiManager.addToMessage(message, "Select the action you want to perform.", generalActionButtons.toArray(new Button[generalActionButtons.size()]));
				}

				// President options
				if(ba.getName().equals("FederalReserve"))
				{
					// Create president buttons
					List<Button> presidentActionButtons = new ArrayList<Button>();
					presidentActionButtons.add(new Button("Set tax rate",  "Change taxes",  "/se changeTaxes",  ChatColor.GREEN));
					presidentActionButtons.add(new Button("Collect taxes", "Collect taxes", "/se collectTaxes", ChatColor.GREEN));
					// Add president actions section
					ChatUiManager.addToMessage(message, "President actions", presidentActionButtons.toArray(new Button[presidentActionButtons.size()]));
				}
				
				// Company options
				List<Company> companies = Bank.getInstance().getPlayerCompanies(player.getUniqueId());
				if(!companies.isEmpty()) // Only show company options to the player if he/she is in one
				{
					List<Button> companyActionButtons = new ArrayList<Button>();
					// Loop in the player's companies to add more options 
					for(Company c : companies)
					{
						// If their names are the same, the bank account is part of the company.
						if(c.getName().equals(ba.getName())) 
						{
							// Everyone in the company has access to this option.
							companyActionButtons.add(new Button("Invite", "Invite a player", "/se invite", ChatColor.BLUE));
							if(c.isCreator(player.getUniqueId()))
							{
								// Add actions which are exclusive to the creator
								companyActionButtons.add(new Button("Kick",    "Kick a player",       "/se kick",    ChatColor.GREEN));
								companyActionButtons.add(new Button("Disband", "Select this account", "/se disband", ChatColor.RED));
							}
							else
							{
								// Add the option to leave the company if you are not the creator
								companyActionButtons.add(new Button("Leave", "Leave the company", "/se leave", ChatColor.RED));
							}
							
							// Add company management section
							ChatUiManager.addToMessage(message, "Company management", companyActionButtons.toArray(new Button[companyActionButtons.size()]));
							break;
						}
					}
				}

				message.send(player);
				return true;
				
				
			case "vote":
				SeVote voteCmd = new SeVote(player, selectedAccounts.get(player.getUniqueId()));
				voteCmd.execute();
				currentPlayerCommands.put(player.getUniqueId(), voteCmd);
				return true;
				
				
			case "disband":
				SeDisband disbandCmd = new SeDisband(player, selectedAccounts.get(player.getUniqueId()));
				disbandCmd.execute();
				currentPlayerCommands.put(player.getUniqueId(), disbandCmd);
				return true;
				
				
			case "leave":
				SeLeaveCompany leaveCmd = new SeLeaveCompany(player, selectedAccounts.get(player.getUniqueId()));
				leaveCmd.execute();
				currentPlayerCommands.put(player.getUniqueId(), leaveCmd);
				return true;


			case "changeTaxes":
				SeChangeTaxes changeTaxesCmd = new SeChangeTaxes(player, selectedAccounts.get(player.getUniqueId()));
				changeTaxesCmd.execute();
				currentPlayerCommands.put(player.getUniqueId(), changeTaxesCmd);
				return true;

			case "invite":
				/*
				selectedAccount = selectedAccounts.get(player.getUniqueId());

				List<Player> players = Bukkit.getWorld("world").getPlayers();

				List<Button> possiblePlayers = new ArrayList<Button>();

				for(Player p : players)
					if(!p.getName().equals(player.getName()))
						if(Bank.getInstance().getCompanyByName(args[1]).hasMember(p.getUniqueId()))
						{

						}
						else
						{
							possiblePlayers.add(new Button(p.getName(), "Invite this player ", "/se invitePlayer " + p.getName(), ChatColor.GREEN));
						}

				message = ChatUiManager.generateUi("Select player to invite", possiblePlayers.toArray(new Button[possiblePlayers.size()]));

				message.send(player);*/
				
				SeInvitePlayer inviteCmd = new SeInvitePlayer(player, selectedAccounts.get(player.getUniqueId()));
				inviteCmd.execute();
				currentPlayerCommands.put(player.getUniqueId(), inviteCmd);
				return true;


			case "invitePlayer":
				/*
				BankAccount selectedPlayer = null;
				if(args.length > 1)
					selectedPlayer = Bank.getInstance().getBankAccountByName(args[1]);

				selectedAccount = selectedAccounts.get(player.getUniqueId());

				List<Player> tempPlayers = Bukkit.getWorld("world").getPlayers();

				for(Player p : tempPlayers)
					if(selectedPlayer.equals(Bank.getInstance().getBankAccountByName(p.getName())))
						Bank.getInstance().getCompanyByName(selectedAccount.getName()).addPlayer(p.getUniqueId());*/

				return true;

			case "kick":
				/*

				selectedAccount = selectedAccounts.get(player.getUniqueId());

				List<Player> playersInTheWorld = Bukkit.getWorld("world").getPlayers();

				List<Button> possiblePlayersToKick = new ArrayList<Button>();

				for(Player p : playersInTheWorld)
					if(Bank.getInstance().getCompanyByName(args[1]).hasMember(p.getUniqueId()))
						possiblePlayersToKick.add(new Button(p.getName(), "kick this player ", "/se kickPlayer " + p.getName(), ChatColor.GREEN));


				message = ChatUiManager.generateUi("Select player to kick", possiblePlayersToKick.toArray(new Button[possiblePlayersToKick.size()]));

				message.send(player);*/
				
				SeKick kickCmd = new SeKick(player, selectedAccounts.get(player.getUniqueId()));
				kickCmd.execute();
				currentPlayerCommands.put(player.getUniqueId(), kickCmd);
				return true;


			case "kickPlayer":
				/*
				BankAccount selectedPlayerToKick = null;
				if(args.length > 1)
					selectedPlayerToKick = Bank.getInstance().getBankAccountByName(args[1]);

				selectedAccount = selectedAccounts.get(player.getUniqueId());

				List<Player> tempPlayersToKick = Bukkit.getWorld("world").getPlayers();

				for(Player p : tempPlayersToKick)
					if(selectedPlayerToKick.equals(Bank.getInstance().getBankAccountByName(p.getName())))
						Bank.getInstance().getCompanyByName(selectedAccount.getName()).removePlayer(p.getUniqueId());*/

				return true;


			case "create":
				SeCreateCompany createCmd = new SeCreateCompany(player, selectedAccounts.get(player.getUniqueId()));
				createCmd.execute();
				currentPlayerCommands.put(player.getUniqueId(), createCmd);
				return true;


			case "transfer":
				SeTransferMoney transferCmd = new SeTransferMoney(player, selectedAccounts.get(player.getUniqueId()));
				transferCmd.execute();
				currentPlayerCommands.put(player.getUniqueId(), transferCmd);
				return true;


			case "cancel":
				SeCommand cmd = currentPlayerCommands.get(player.getUniqueId());
				if(cmd == null)
					player.sendMessage("No action to cancel.");
				else
				{
					cmd.cancel();
					currentPlayerCommands.put(player.getUniqueId(), null);
				}
				return true;


			case "response":
				SeCommand currentCmd = currentPlayerCommands.get(player.getUniqueId());
				if(currentCmd == null)
				{
					player.sendMessage("Operation has expired.");
					return true;
				}
				if(!currentCmd.next(args[1])) // When next returns false, it means the SeCommand should stop.
					currentPlayerCommands.put(player.getUniqueId(), null);
				return true;


			case "balance":
				player.sendMessage("Balance : " + selectedAccounts.get(player.getUniqueId()).getValue());
				return true;


			case "sell":
				BaseInventoryMenu current = new SellMenu(player, selectedAccounts.get(player.getUniqueId()));
				openInventoryMenu.put(player.getUniqueId(), current);
				return true;


			case "market":
				current = new NewMarketMenu(player, selectedAccounts.get(player.getUniqueId()));
				openInventoryMenu.put(player.getUniqueId(), current);
				return true;
				
				
			case "collectTaxes":
				// Prevent players from running this command if they are not president
				if(!Bank.getInstance().isPresident(player.getUniqueId()))
				{
					player.sendMessage("Only the president has access to this command.");
					return true;
				}
				player.sendMessage("Successfully collected " + Bank.getInstance().collectTaxes() + " in taxes.");
				Bukkit.getOnlinePlayers().forEach((p) -> p.sendMessage("The president just collected taxes. Check your balance."));
				return true;
				
			case "accept": // Accept an invite
				List<Company> invites = pendingInvites.get(player.getUniqueId());
				for(Company c : invites)
				{
					if(c.getName().equals(args[1]))
					{
						c.addPlayer(player.getUniqueId());
						invites.remove(c);
						player.sendMessage("You are now a member of " + args[1]);
						for(Player p : Bukkit.getOnlinePlayers())
							if(c.hasMember(p.getUniqueId()))
								p.sendMessage(player.getName() + " has joined " + c.getName());
						return true;
					}
				}
				player.sendMessage("Company " + args[1] + " not found in your invitations.");
				return true;
				
			case "refuse": // Refuse an invite
				invites = pendingInvites.get(player.getUniqueId());
				for(Company c : invites)
				{
					if(c.getName().equals(args[1]))
					{
						invites.remove(c);
						player.sendMessage("Refused invitation from " + c.getName());
						return true;
					}
				}
				player.sendMessage("Company " + args[1] + " not found in your invitations.");
				return true;
			}

		default:
			return true;
		}
	}

	public static SeCommand getCurrentPlayerCommand(UUID player) 
	{
		return currentPlayerCommands.get(player);
	}

	public static void finishCommand(UUID player)
	{
		currentPlayerCommands.put(player, null);
	}

	public static BaseInventoryMenu getOpenInventoryMenu(Player player)
	{
		return openInventoryMenu.get(player.getUniqueId());
	}

	public static void finishInventoryMenu(Player player)
	{
		openInventoryMenu.put(player.getUniqueId(), null);
	}
	
	public static void sendInvite(UUID target, Company from)
	{
		List<Company> invites = pendingInvites.get(target);
		// If the list of companies is null, create it
		if(invites == null)
		{
			pendingInvites.put(target, new ArrayList<Company>());
			invites = pendingInvites.get(target);
		}
		invites.add(from);
	}
}