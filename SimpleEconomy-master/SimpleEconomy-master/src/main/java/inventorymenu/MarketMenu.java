package inventorymenu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.gmail.charlesantlord.simpleeconomy.Bank;
import com.gmail.charlesantlord.simpleeconomy.BankAccount;
import com.gmail.charlesantlord.simpleeconomy.Market;
import com.gmail.charlesantlord.simpleeconomy.Sale;

import dev.dbassett.skullcreator.SkullCreator;
import net.md_5.bungee.api.ChatColor;

public class MarketMenu extends InventoryMenu
{
	private ItemStack redLeftArrow;
	private ItemStack redRightArrow;
	private ItemStack greenLeftArrow;
	private ItemStack greenRightArrow;

	private final int LEFT_ARROW_INDEX = 45;
	private final int RIGHT_ARROW_INDEX = 53;

	private List<Inventory> pages;
	private int currentPage = 0;

	private Collection<List<Sale>> sales;
	
	private HashMap<Integer, Sale> salePositions;
	

	public MarketMenu(Player player, BankAccount selectedBankAccount) 
	{
		super(player, selectedBankAccount);
		pages = new ArrayList<Inventory>();
		sales = Market.getInstance().getSales();
		generateButtons();
		salePositions = new HashMap<Integer, Sale>();
		generateMenu();
	}

	@Override
	public void open() 
	{
		player.openInventory(pages.get(0));
	}

	@Override
	public boolean onSelect(int id, Inventory clickedInventory, boolean leftClick, boolean shift) 
	{
		if(clickedInventory.equals(player.getOpenInventory().getTopInventory())) // Player clicked on the top inventory
		{
			// RestrictedAreas
			if(restrictedAreaIndexes.contains(id))
				return true;
			
			// Change page buttons
			if(id == LEFT_ARROW_INDEX)
			{
				// Change page to the left
				if(currentPage > 0)
				{
					currentPage--;
					player.openInventory(pages.get(currentPage));
				}
				
				return true;
			}
			else if(id == RIGHT_ARROW_INDEX)
			{
				// Change page to the right
				if(currentPage < pages.size() - 1)
				{
					currentPage++;
					player.openInventory(pages.get(currentPage));
				}
				
				return true;
			}
			
			// Find the index of the open inventory and get the sale associated with it to validate if the offer is still valid
			boolean exists = false;
			Sale temp = salePositions.get(currentPage * 45 + id);
			exists = Market.getInstance().getSalesByBankAccount(temp.getSeller()).contains(temp);
			if(exists)
			{
				if(Bank.getInstance().transferMoney(selectedBankAccount, temp.getSeller(), temp.getPrice()))
				{
					ItemStack itemToAdd = temp.getItem();
					ItemMeta im = itemToAdd.getItemMeta();
					im.setLore(null);
					itemToAdd.setItemMeta(im);
					player.getInventory().addItem(itemToAdd);
					salePositions.get(currentPage * 45 + id).buy();
					updateSale(id, currentPage);
				}
			}
			return true;
		}
		
		return true;
	}
	
	private void updateSale(int id, int page)
	{
		Sale singleSale = salePositions.get(page * 45 + id);
		if(singleSale.getStock() == 0)
		{
			pages.get(page).setItem(id, null);
			salePositions.remove(singleSale);
		}
		else
		{
			ItemStack temp = singleSale.getItem().clone();
			ItemMeta meta = temp.getItemMeta();
			List<String> lore = new ArrayList<String>();
			lore.add("Cost : " +   ChatColor.YELLOW.toString() + singleSale.getPrice());
			lore.add("Stock : " +  ChatColor.BLUE.toString() +   singleSale.getStock());
			lore.add("Seller : " + ChatColor.GREEN.toString() +  singleSale.getSeller().getName());
			meta.setLore(lore);
			temp.setItemMeta(meta);
			pages.get(page).setItem(id, temp);
		}
	}

	@Override
	public void closeMenu(boolean usingButton) 
	{
		
	}

	private void generateMenu()
	{
		pages.add(createInventory(0));
		
		// Place items in the menus
		int currentIndex = 0;
		for(List<Sale> baSale : sales)
		{
			for(Sale singleSale : baSale)
			{
				int pageNumber = currentIndex / 45;
				if(pageNumber > pages.size() - 1)
					pages.add(createInventory(pageNumber));
				
				ItemStack temp = singleSale.getItem().clone();
				ItemMeta meta = temp.getItemMeta();
				List<String> lore = new ArrayList<String>();
				lore.add("Cost : " +   ChatColor.YELLOW.toString() + singleSale.getPrice());
				lore.add("Stock : " +  ChatColor.BLUE.toString() +   singleSale.getStock());
				lore.add("Seller : " + ChatColor.GREEN.toString() +  singleSale.getSeller().getName());
				meta.setLore(lore);
				temp.setItemMeta(meta);
				pages.get(pageNumber).setItem(currentIndex % 45, temp);
				salePositions.put(currentIndex, singleSale);
				currentIndex++;
			}
		}
	}
	
	private Inventory createInventory(int pageIndex)
	{
		Inventory temp = Bukkit.createInventory(player, 54, "Market - page " + (pageIndex + 1));
		
		// Place restricted area
		for(int i = 46; i < 53; i++)
			placeRestrictedArea(i, temp);
		
		// Place buttons
		temp.setItem(LEFT_ARROW_INDEX, redLeftArrow);
		temp.setItem(RIGHT_ARROW_INDEX, redRightArrow);
		
		return temp;
	}

	private void generateButtons()
	{
		SkullMeta playerHeadMeta;

		// Red left arrow
		redLeftArrow = SkullCreator.itemFromUrl("http://textures.minecraft.net/texture/1c5a8aa8a4c03600a2b5a4eb6beb51d590260b095ee1cdaa976b09bdfe5661c6");
		playerHeadMeta = (SkullMeta) redLeftArrow.getItemMeta();
		playerHeadMeta.setDisplayName("Go left");
		redLeftArrow.setItemMeta(playerHeadMeta);

		// Red right arrow
		redRightArrow = SkullCreator.itemFromUrl("http://textures.minecraft.net/texture/a6af217aeddf0f40064969ebb2042f7aeafbc7d0f175a27624133a3befd10281");
		playerHeadMeta = (SkullMeta) redRightArrow.getItemMeta();
		playerHeadMeta.setDisplayName("Go right");
		redRightArrow.setItemMeta(playerHeadMeta);

		// Green left arrow
		greenLeftArrow = SkullCreator.itemFromUrl("http://textures.minecraft.net/texture/32ff8aaa4b2ec30bc5541d41c8782199baa25ae6d854cda651f1599e654cfc79");
		playerHeadMeta = (SkullMeta) greenLeftArrow.getItemMeta();
		playerHeadMeta.setDisplayName("Go left");
		greenLeftArrow.setItemMeta(playerHeadMeta);

		// Green right arrow
		greenRightArrow = SkullCreator.itemFromUrl("http://textures.minecraft.net/texture/aab95a8751aeaa3c671a8e90b83de76a0204f1be65752ac31be2f98feb64bf7f");
		playerHeadMeta = (SkullMeta) greenRightArrow.getItemMeta();
		playerHeadMeta.setDisplayName("Go right");
		greenRightArrow.setItemMeta(playerHeadMeta);
	}
}