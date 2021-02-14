package inventorymenu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.print.attribute.standard.Finishings;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.gmail.charlesantlord.simpleeconomy.Bank;
import com.gmail.charlesantlord.simpleeconomy.BankAccount;
import com.gmail.charlesantlord.simpleeconomy.CommandManager;
import com.gmail.charlesantlord.simpleeconomy.Market;
import com.gmail.charlesantlord.simpleeconomy.Sale;

import dev.dbassett.skullcreator.SkullCreator;
import net.md_5.bungee.api.ChatColor;

public class NewMarketMenu extends BaseInventoryMenu
{
	private List<Inventory> pages;
	private int currentPage = 0;
	private List<Sale> sales;
	private static ItemStack leftArrow;
	private static ItemStack rightArrow;
	private static ItemStack refreshButton;
	private boolean falseClose = false;

	public NewMarketMenu(Player player, BankAccount selectedBankAccount) 
	{
		super(player, selectedBankAccount);
	}

	@Override
	protected boolean clickedItem(int id, boolean leftClick, boolean shift) 
	{		
		switch(id)
		{
		case 45: // Left page button
			falseClose = true;
			player.openInventory(pages.get(--currentPage));
			return true;

		case 53: // Right page button
			falseClose = true;
			player.openInventory(pages.get(++currentPage));
			return true;

		case 49: // Refresh button
			updateMenu();
			return true;

		default: // Clicked to buy an item
			if(sales.size() < 1) // To avoid an IndexOutOfBounds
				return true;
			Sale saleToBuy = sales.get(id + currentPage * 45);
			
			// Check if the money transfer worked
			if(saleToBuy != null && saleToBuy.isForSale() && Bank.getInstance().transferMoney(selectedBankAccount, saleToBuy.getSeller(), saleToBuy.getPrice()))
			{
				// If so, give the item to the player...
				//  - First, remove lore
				ItemStack itemToGive = saleToBuy.getItem();
				ItemMeta im = itemToGive.getItemMeta();
				im.setLore(null);
				itemToGive.setItemMeta(im);
				//  - Then, give it to the player
				HashMap<Integer, ItemStack> temp = player.getInventory().addItem(itemToGive);
				for(ItemStack is : temp.values())
					player.getWorld().dropItem(player.getLocation(), is); // Drop items to the player if they can't fit in the inventory
				
				// Update sale
				saleToBuy.buy();
				
				if(saleToBuy.getStock() < 1) // If there is no more stock, update the menu
					updateMenu();
				else // If there still is, update the item description
					pages.get(currentPage).setItem(id % 45, getItemStackFromSale(saleToBuy));
			}
			return true;
		}
	}

	@Override
	protected void openInventory() 
	{
		
	}

	@Override
	protected void generateInventory() 
	{
		updateMenu(); // Also has the side effect of opening the menu, which is why openInventory() is not necessary
	}

	@Override
	public void closeInventory() 
	{
		if(falseClose)
		{
			falseClose = false;
			return;
		}
		CommandManager.finishInventoryMenu(player);
	}

	private void updateMenu()
	{
		falseClose = true;
		player.closeInventory();
		generateButtons();
		pages = new ArrayList<Inventory>();

		// Get current sales list
		Collection<List<Sale>> temp = Market.getInstance().getSales();
		sales = new ArrayList<Sale>();
		// Put all sales in the list
		for(List<Sale> ls : temp)
			for(Sale s : ls)
				if(s.isForSale())
					sales.add(s);
		// Sort the list
		sales.sort(Comparator.naturalOrder());

		int saleCount = sales.size();

		// Create pages
		int pageCount = saleCount / 45 + 1;
		for(int i = 0; i < pageCount; i++)
		{
			Inventory inv = Bukkit.createInventory(null, 54, "Market - Page " + (i + 1));
			placeRestrictedAreaIndicators(new int[] { 46, 47, 48, 50, 51, 52 }, inv);
			
			// Place refresh button
			inv.setItem(49, refreshButton);
			
			// Place left page indicator
			if(i > 0) 
				inv.setItem(45, leftArrow);
			
			// Place right page indicator
			if(i < pageCount - 1) 
				inv.setItem(53, rightArrow);
			
			pages.add(inv);
		}

		// Put items in pages
		for(int i = 0; i < saleCount; i++)
			pages.get(i / 45).setItem(i % 45, getItemStackFromSale(sales.get(i)));
		
		currentPage = 0;
		player.openInventory(pages.get(0));
	}
	
	private ItemStack getItemStackFromSale(Sale sale)
	{
		ItemStack item = sale.getItem();
		ItemMeta im = item.getItemMeta();
		
		// Add item description as lore
		List<String> lore = new ArrayList<String>();
		lore.add("Cost : " +   ChatColor.YELLOW.toString() + sale.getPrice());
		lore.add("Stock : " +  ChatColor.BLUE.toString() +   sale.getStock());
		lore.add("Seller : " + ChatColor.GREEN.toString() +  sale.getSeller().getName());
		im.setLore(lore);
		item.setItemMeta(im);
		
		return item;
	}

	private void generateButtons()
	{
		if(leftArrow == null)
		{
			SkullMeta playerHeadMeta;

			// Green left arrow
			leftArrow = SkullCreator.itemFromUrl("http://textures.minecraft.net/texture/32ff8aaa4b2ec30bc5541d41c8782199baa25ae6d854cda651f1599e654cfc79");
			playerHeadMeta = (SkullMeta) leftArrow.getItemMeta();
			playerHeadMeta.setDisplayName("Go left");
			leftArrow.setItemMeta(playerHeadMeta);

			// Green right arrow
			rightArrow = SkullCreator.itemFromUrl("http://textures.minecraft.net/texture/aab95a8751aeaa3c671a8e90b83de76a0204f1be65752ac31be2f98feb64bf7f");
			playerHeadMeta = (SkullMeta) rightArrow.getItemMeta();
			playerHeadMeta.setDisplayName("Go right");
			rightArrow.setItemMeta(playerHeadMeta);

			// Refresh button
			refreshButton = SkullCreator.itemFromUrl("http://textures.minecraft.net/texture/e887cc388c8dcfcf1ba8aa5c3c102dce9cf7b1b63e786b34d4f1c3796d3e9d61");
			playerHeadMeta = (SkullMeta) refreshButton.getItemMeta();
			playerHeadMeta.setDisplayName("Refresh");
			refreshButton.setItemMeta(playerHeadMeta);
		}
	}

	@Override
	protected boolean clickedBottomInventory() 
	{
		return true;
	}
}