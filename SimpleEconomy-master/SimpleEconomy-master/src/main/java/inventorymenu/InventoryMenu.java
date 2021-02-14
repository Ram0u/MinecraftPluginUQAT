package inventorymenu;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.gmail.charlesantlord.simpleeconomy.BankAccount;

public abstract class InventoryMenu 
{
	protected Player player;
	protected BankAccount selectedBankAccount;
	protected Inventory menu;
	protected List<Integer> restrictedAreaIndexes = new ArrayList<Integer>();
	
	public InventoryMenu(Player player, BankAccount selectedBankAccount)
	{
		this.player = player;
		this.selectedBankAccount = selectedBankAccount;
	}
	
	public abstract void open();
	public abstract boolean onSelect(int id, Inventory clickedInventory, boolean leftClick, boolean shift); // Returns true if event should be cancelled
	public abstract void closeMenu(boolean usingButton);
	
	protected void placeButton(ChatColor color, Material item, String name, Inventory menu, int position)
	{
		ItemMeta im;
		ItemStack button = new ItemStack(item);
		im = button.getItemMeta();
		im.setDisplayName(color.toString() + name);
		button.setItemMeta(im);
		menu.setItem(position, button);
	}
	
	protected void placeRestrictedArea(int position, Inventory inventory)
	{
		restrictedAreaIndexes.add(position);
		ItemMeta im;
		ItemStack slot = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
		im = slot.getItemMeta();
		im.setDisplayName(ChatColor.RED.toString() + "Restricted slot");
		slot.setItemMeta(im);
		inventory.setItem(position, slot);
	}
}