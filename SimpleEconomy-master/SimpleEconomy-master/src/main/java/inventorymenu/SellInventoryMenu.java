package inventorymenu;

import java.util.ArrayList;
import java.util.List;

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

public class SellInventoryMenu extends InventoryMenu
{
	private Inventory priceInventory;
	private final int INVENTORY_SIZE = 54;
	private final int SWITCH_MENU_TYPE_BUTTON_INDEX = 46;
	private final int CLOSE_CHEST_BUTTON_INDEX = 45;
	private Integer pricePerTile[];
	private boolean justSwitched = false;

	public SellInventoryMenu(Player player, BankAccount selectedBankAccount) 
	{
		super(player, selectedBankAccount);
		generateMenu();
		generatePriceInventory();
		pricePerTile = new Integer[INVENTORY_SIZE];
		List<Sale> sales = Market.getInstance().getSalesByBankAccount(selectedBankAccount);
		// Place sales in menu and set prices
		if(sales != null)
		{
			int currentIndex = 0;
			for(Sale sale : sales)
			{
				// Loop for stock and place items
				for(int i = 0; i < sale.getStock(); i++)
				{
					menu.setItem(currentIndex, sale.getItem());
					pricePerTile[currentIndex] = sale.getPrice();
					placePriceIndicator(currentIndex);
					currentIndex++;
				}
			}
		}
	}

	@Override
	public void open() 
	{
		player.openInventory(menu);
	}

	@Override
	public boolean onSelect(int id, Inventory clickedInventory, boolean leftClick, boolean shift) 
	{
		if(clickedInventory == null)
			return true;

		if(player.getOpenInventory().getTopInventory().equals(menu))
		{
			// Item inventory is open
			if(clickedInventory.equals(menu))
			{
				// Check for buttons
				if(id == CLOSE_CHEST_BUTTON_INDEX)
				{
					closeMenu(true);
					return true;
				}
				else if(id == SWITCH_MENU_TYPE_BUTTON_INDEX)
				{
					switchMenuType();
					return true;
				}

				// Check for restricted indexes
				if(restrictedAreaIndexes.contains(id))
					return true;

				return false;
			}
			else
			{
				// Clicked on the bottom inventory
				return false;
			}
		}
		else
		{
			// Price inventory is open
			if(clickedInventory.equals(priceInventory))
			{
				// Check for buttons
				if(id == CLOSE_CHEST_BUTTON_INDEX)
				{
					closeMenu(true);
					return true;
				}
				else if(id == SWITCH_MENU_TYPE_BUTTON_INDEX)
				{
					switchMenuType();
					return true;
				}

				// Check for restricted indexes
				if(restrictedAreaIndexes.contains(id))
					return true;

				// Check if an item is present at the location on the menu
				if(pricePerTile[id] != null)
				{
					if(leftClick)
						pricePerTile[id] += shift ? 10 : 1;
					else
					{
						pricePerTile[id] -= shift ? 10 : 1;
						if(pricePerTile[id] < 1)
							pricePerTile[id] = 1;
					}

					placePriceIndicator(id);
				}
			}

			return true;
		}
	}

	@Override
	public void closeMenu(boolean usingButton)
	{
		if(justSwitched) // Don't save if the event was called from switching menu
		{
			justSwitched = false;
			return;
		}


		if(usingButton)
		{
			player.closeInventory();
			save();
		}
		else
			save();

		CommandManager.finishInventoryMenu(player);
	}

	public void switchMenuType()
	{
		justSwitched = true;

		if(player.getOpenInventory().getTopInventory().equals(menu))
		{
			for(int i = 0; i < INVENTORY_SIZE - 9; i++)
			{
				if(menu.getItem(i) != null)
				{
					if(pricePerTile[i] == null)
					{
						pricePerTile[i] = 1;
						placePriceIndicator(i);
					}
				}
				else
				{
					if(pricePerTile[i] != null)
					{
						pricePerTile[i] = null;
						priceInventory.setItem(i, null);
					}
				}
			}

			player.openInventory(priceInventory);
		}
		else
		{
			player.openInventory(menu);
		}
	}

	private void save()
	{
		// save state
		List<Sale> salesToSave = new ArrayList<Sale>();
		for(int i = 0; i < INVENTORY_SIZE - 9; i++)
		{
			ItemStack item = menu.getItem(i);
			if(item != null) // If the item exists...
			{
				// Create a sale object from the item and the price
				Sale tempSale = new Sale(menu.getItem(i), pricePerTile[i] == null ? 1 : pricePerTile[i], selectedBankAccount);

				// Check if there is a sale equal to this one
				int index = salesToSave.indexOf(tempSale);
				if(index == -1)
					salesToSave.add(tempSale); // If there is none, add it to the list of sales
				else
					salesToSave.get(index).incrementStock(); // If there is, increment the stock of the sale
			}
		}
		Market.getInstance().setSalesByBankAccount(salesToSave, selectedBankAccount);
	}

	private void generatePriceInventory()
	{
		priceInventory = Bukkit.createInventory(player, INVENTORY_SIZE, "Set item prices.");

		// Add restricted chest area
		for(int i = 53; i > 46; i--)
			placeRestrictedArea(i, priceInventory);

		// Add buttons
		placeButton(ChatColor.GREEN,  Material.GREEN_WOOL,  "Save and quit", 	   priceInventory, INVENTORY_SIZE - 9);
		placeButton(ChatColor.YELLOW, Material.YELLOW_WOOL, "Switch to item menu", priceInventory, INVENTORY_SIZE - 8);
	}

	private void generateMenu()
	{
		menu = Bukkit.createInventory(player, INVENTORY_SIZE, "Place items to sell.");

		// Add restricted chest area
		for(int i = 53; i > 46; i--)
			placeRestrictedArea(i, menu);

		// Add buttons
		placeButton(ChatColor.GREEN, Material.GREEN_WOOL, "Save and quit", 	      menu, INVENTORY_SIZE - 9);
		placeButton(ChatColor.BLUE,  Material.BLUE_WOOL,  "Switch to price menu", menu, INVENTORY_SIZE - 8);
	}

	private void placePriceIndicator(int position)
	{
		// Generate the ItemStack
		ItemStack priceIndicator;
		int price = pricePerTile[position];

		if(price < 10)
			priceIndicator = new ItemStack(Material.GOLD_NUGGET);
		else if(price < 50)
			priceIndicator = new ItemStack(Material.GOLD_INGOT);
		else
			priceIndicator = new ItemStack(Material.GOLD_BLOCK);

		ItemMeta im = priceIndicator.getItemMeta();
		im.setDisplayName(ChatColor.YELLOW.toString() + price);
		priceIndicator.setItemMeta(im);

		// Place the ItemStack
		priceInventory.setItem(position, priceIndicator);
	}
}