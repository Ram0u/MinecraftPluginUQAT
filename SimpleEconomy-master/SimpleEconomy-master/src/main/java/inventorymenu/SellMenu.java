package inventorymenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.gmail.charlesantlord.simpleeconomy.BankAccount;
import com.gmail.charlesantlord.simpleeconomy.CommandManager;
import com.gmail.charlesantlord.simpleeconomy.Market;
import com.gmail.charlesantlord.simpleeconomy.Sale;

public class SellMenu extends BaseInventoryMenu
{
	private static HashMap<BankAccount, Inventory> itemMenus = new HashMap<BankAccount, Inventory>();
	private static HashMap<BankAccount, Inventory> priceMenus = new HashMap<BankAccount, Inventory>();
	private static HashMap<BankAccount, List<UUID>> currentlyEditingPlayers = new HashMap<BankAccount, List<UUID>>();
	private Inventory currentItemMenu;
	private Inventory currentPriceMenu;
	private static ItemStack priceMenuButton = null;
	private static ItemStack itemMenuButton = null;
	private static ItemStack saveAndQuitButton = null;
	private boolean switchedMode = false;


	public SellMenu(Player player, BankAccount selectedBankAccount) 
	{
		super(player, selectedBankAccount);

		// Put the current player in the list of players editing the sales
		if(!currentlyEditingPlayers.containsKey(selectedBankAccount))
			currentlyEditingPlayers.put(selectedBankAccount, new ArrayList<UUID>());
		currentlyEditingPlayers.get(selectedBankAccount).add(player.getUniqueId());

		// Remove listings from the market
		List<Sale> temp = Market.getInstance().getSalesByBankAccount(selectedBankAccount);
		if(temp != null)
			temp.forEach((s) -> s.removeFromSale());
		Market.getInstance().removeSales(selectedBankAccount);
	}

	@Override
	protected boolean clickedItem(int id, boolean leftClick, boolean shift)
	{
		switch(id)
		{
		case 45: // Save and quit button
			if(shift) // Used to prevent a bug where if the player is holding shift, the item will go in his inventory
				return true;
			closeInventory();
			player.closeInventory();
			return true;

		case 46: // Switch view button
			switchMode();
			return true;

		default:
			return clickedItemNotButton(id, leftClick, shift);
		}
	}

	@Override
	protected void generateInventory() 
	{
		// Load desired inventories from the static HashMaps
		Inventory temp = itemMenus.get(selectedBankAccount);
		if(temp == null)
		{
			// If the inventory did not exist before, create it here
			currentItemMenu = Bukkit.createInventory(null, 54, "Item menu - " + selectedBankAccount.getName());
			itemMenus.put(selectedBankAccount, currentItemMenu);
			currentPriceMenu = Bukkit.createInventory(null, 54, "Price menu - " + selectedBankAccount.getName());
			priceMenus.put(selectedBankAccount, currentPriceMenu);

			// Add restricted areas
			placeRestrictedAreaIndicators(new int[] { 47, 48, 49, 50, 51, 52, 53 }, currentItemMenu);
			placeRestrictedAreaIndicators(new int[] { 47, 48, 49, 50, 51, 52, 53 }, currentPriceMenu);

			// Place buttons
			generateButtons();
			currentItemMenu.setItem(45, saveAndQuitButton);
			currentPriceMenu.setItem(45, saveAndQuitButton);
			currentItemMenu.setItem(46, priceMenuButton);
			currentPriceMenu.setItem(46, itemMenuButton);
		}
		else
		{
			// Load inventories from the HashMaps
			currentItemMenu = itemMenus.get(selectedBankAccount);
			currentPriceMenu = priceMenus.get(selectedBankAccount);
			
			for(ItemStack is : currentItemMenu)
			{
				if(is != null)
				{
					ItemMeta im = is.getItemMeta();
					im.setLore(null);
					is.setItemMeta(im);
				}
			}

			// Add restricted areas
			placeRestrictedAreaIndicators(new int[] { 47, 48, 49, 50, 51, 52, 53 }, currentItemMenu);
			placeRestrictedAreaIndicators(new int[] { 47, 48, 49, 50, 51, 52, 53 }, currentPriceMenu);
		}
	}

	@Override
	protected void openInventory() 
	{
		player.openInventory(currentItemMenu);
	}

	@Override
	public void closeInventory() 
	{
		if(switchedMode)
		{
			switchedMode = false;
			return;
		}
		// Remove the current player from the list of currently editing players
		List<UUID> temp = currentlyEditingPlayers.get(selectedBankAccount);
		temp.remove(player.getUniqueId());

		// If no one is editing the sales, add them to the market
		if(temp.isEmpty())
			Market.getInstance().setSalesByBankAccount(save(), selectedBankAccount);
		
		CommandManager.finishInventoryMenu(player);
	}

	private List<Sale> save()
	{
		List<Sale> sales = new ArrayList<Sale>();
		for(int i = 0; i < 45; i++)
		{
			ItemStack item = currentItemMenu.getItem(i);
			if(item != null)
			{
				Sale sale;
				ItemStack priceItem = currentPriceMenu.getItem(i);

				// Create a sale from the item and the price at the i location
				if(priceItem == null)
					sale = new Sale(item, 1, selectedBankAccount);
				else
					sale = new Sale(item, getPriceBySlot(i), selectedBankAccount);

				// Check if there is a sale with the same characteristics as this one. If so, increment the stock
				int index = sales.indexOf(sale);
				if(index != -1)
					sales.get(index).incrementStock();
				else
					sales.add(sale);
			}
		}
		Market.getInstance().setSalesByBankAccount(sales, selectedBankAccount);

		return sales;
	}

	private void switchMode()
	{
		switchedMode = true;
		
		// Depending on the current open inventory, switch to the other one.
		if(player.getOpenInventory().getTopInventory().equals(currentItemMenu))
		{
			// If in item menu, place the currency indicators in the price menu.
			for(int i = 0; i < 54; i++)
			{
				ItemStack itemMenuItem = currentItemMenu.getItem(i);
				ItemStack priceMenuItem = currentPriceMenu.getItem(i);

				// If there is an item in a slot, but not in the price menu...
				if(itemMenuItem != null && priceMenuItem == null)
				{
					// ...place one
					ItemStack temp = new ItemStack(Material.GOLD_NUGGET);
					ItemMeta im = temp.getItemMeta();
					im.setDisplayName(ChatColor.YELLOW.toString() + "1");
					temp.setItemMeta(im);
					currentPriceMenu.setItem(i, temp);
				}
				// If there is a price to an item which does not exist
				else if(itemMenuItem == null && priceMenuItem != null)
					currentPriceMenu.setItem(i, null);
			}
			player.openInventory(currentPriceMenu);
		}
		else
			player.openInventory(currentItemMenu);
		
	}

	private boolean clickedItemNotButton(int id, boolean leftClick, boolean shift)
	{
		if(player.getOpenInventory().getTopInventory().equals(currentItemMenu))
		{
			// Clicked on the item menu
			return false;
		}
		else
		{
			// Clicked on the price menu
			int newPrice = getPriceBySlot(id);

			// If shift is pressed, change value by increments of 10
			if(leftClick)
				newPrice += shift ? 10 : 1;
			else
				newPrice -= shift ? 10 : 1;

			// Avoid getting negative or 0 price
			if(newPrice < 1)
				newPrice = 1;

			setItemPrice(id, newPrice);

			return true;
		}
	}

	private void generateButtons()
	{
		// Generate button ItemStacks
		if(priceMenuButton == null)
		{
			priceMenuButton = new ItemStack(Material.GOLD_INGOT);
			ItemMeta im = priceMenuButton.getItemMeta();
			im.setDisplayName(ChatColor.YELLOW.toString() + "Switch to price view");
			priceMenuButton.setItemMeta(im);
		}
		if(itemMenuButton == null)
		{
			itemMenuButton = new ItemStack(Material.IRON_PICKAXE);
			ItemMeta im = itemMenuButton.getItemMeta();
			im.setDisplayName(ChatColor.BLUE.toString() + "Switch to item view");
			itemMenuButton.setItemMeta(im);
		}
		if(saveAndQuitButton == null)
		{
			saveAndQuitButton = new ItemStack(Material.GREEN_WOOL);
			ItemMeta im = saveAndQuitButton.getItemMeta();
			im.setDisplayName(ChatColor.GREEN.toString() + "Save and quit");
			saveAndQuitButton.setItemMeta(im);
		}
	}

	private int getPriceBySlot(int id)
	{
		return Integer.parseInt(currentPriceMenu.getItem(id).getItemMeta().getDisplayName().substring(2));
	}

	private void setItemPrice(int id, int newPrice)
	{
		ItemStack temp = currentPriceMenu.getItem(id);

		if(newPrice < 10)
			temp.setType(Material.GOLD_NUGGET);
		else if(newPrice < 50)
			temp.setType(Material.GOLD_INGOT);
		else
			temp.setType(Material.GOLD_BLOCK);

		ItemMeta im = temp.getItemMeta();
		im.setDisplayName(ChatColor.YELLOW.toString() + newPrice);
		temp.setItemMeta(im);
		currentPriceMenu.setItem(id, temp);
	}

	public static void removeSale(Sale sale)
	{
		// Get reference to the items
		Inventory itemMenu = itemMenus.get(sale.getSeller());
		Inventory priceMenu = priceMenus.get(sale.getSeller());

		// Remove the items of a sale
		for(int i = 0; i < 45; i++)
		{
			if(itemMenu.getItem(i) != null && itemMenu.getItem(i).equals(sale.getItem()))
			{
				itemMenu.setItem(i, null);
				priceMenu.setItem(i, null);
				return;
			}
		}
	}
	
	public SellMenu(HashMap<BankAccount, List<Sale>> sales)
	{
		for(BankAccount ba : sales.keySet())
		{
			// Create inventories
			currentItemMenu = Bukkit.createInventory(null, 54, "Item menu - " + ba.getName());
			currentPriceMenu = Bukkit.createInventory(null, 54, "Price menu - " + ba.getName());
			
			// Add sales from the map
			int currentId = 0;
			for(Sale sale : sales.get(ba))
			{
				for(int i = 0; i < sale.getStock(); i++)
				{
					currentItemMenu.setItem(currentId, sale.getItem());
					currentPriceMenu.setItem(currentId, new ItemStack(Material.GOLD_NUGGET));
					setItemPrice(currentId, sale.getPrice());
					currentId++;
				}
			}
			
			// Place buttons
			generateButtons();
			currentItemMenu.setItem(45, saveAndQuitButton);
			currentPriceMenu.setItem(45, saveAndQuitButton);
			currentItemMenu.setItem(46, priceMenuButton);
			currentPriceMenu.setItem(46, itemMenuButton);
			
			// Add to static HashMaps
			itemMenus.put(ba, currentItemMenu);
			priceMenus.put(ba, currentPriceMenu);
		}
	}

	@Override
	protected boolean clickedBottomInventory() 
	{
		return false;
	}
}