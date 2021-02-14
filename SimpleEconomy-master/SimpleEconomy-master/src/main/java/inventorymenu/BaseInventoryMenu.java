package inventorymenu;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.gmail.charlesantlord.simpleeconomy.BankAccount;
import com.gmail.charlesantlord.simpleeconomy.CommandManager;

public abstract class BaseInventoryMenu 
{
	protected BankAccount selectedBankAccount;
	protected Player player;
	private int[] restrictedAreas;
	private static ItemStack restrictedAreaItem = null;
	
	
	public BaseInventoryMenu(Player player, BankAccount selectedBankAccount)
	{
		this.player = player;
		this.selectedBankAccount = selectedBankAccount;
		generateInventory();
		openInventory();
	}
	
	public BaseInventoryMenu() // Empty constructor used to deserialize children
	{
		
	}
	
	protected abstract boolean clickedItem(int id, boolean leftClick, boolean shift);
	protected abstract void openInventory();
	protected abstract void generateInventory();
	protected abstract boolean clickedBottomInventory();
	public abstract void closeInventory();
	
	public boolean onSelect(int id, boolean leftClick, boolean shift, Inventory clickedInventory)
	{
		// Check if the clicked inventory is the menu or the player's own inventory
		if(clickedInventory.equals(player.getOpenInventory().getTopInventory()))
		{
			if(isRestricted(id))
			{
				// We want to cancel the event to stop the player from moving the item
				return true;
			}
			else
			{
				// Either a button or an item
				if(clickedInventory.getItem(id) != null)
					return clickedItem(id, leftClick, shift);
				
				return false;
			}
		}
		else
			return clickedBottomInventory();
	}
	
	protected void placeRestrictedAreaIndicators(int locations[], Inventory inventory)
	{
		// Initialize array
		restrictedAreas = locations;
		
		// If it has not been done already...
		if(restrictedAreaItem == null)
		{
			// ...create the restricted area marker item
			ItemMeta im;
			restrictedAreaItem = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
			im = restrictedAreaItem.getItemMeta();
			im.setDisplayName(ChatColor.RED.toString() + "Restricted slot");
			restrictedAreaItem.setItemMeta(im);
		}
		
		// Place them at the selected locations
		for(int loc : locations)
			inventory.setItem(loc, restrictedAreaItem);
	}
	
	private boolean isRestricted(int id) // Find if a selected id is restricted
	{
		for(int i = 0; i < restrictedAreas.length; i++)
			if(restrictedAreas[i] == id)
				return true;
		
		return false;
	}
}