package com.gmail.charlesantlord.simpleeconomy;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import inventorymenu.SellMenu;

public class Sale implements ConfigurationSerializable, Comparable<Sale>
{
	private ItemStack item;
	private int price;
	private BankAccount seller;
	private int stock;
	private boolean forSale = true;
	
	public Sale(ItemStack item, int price, BankAccount seller)
	{
		this.item = item;
		this.price = price;
		this.seller = seller;
		this.stock = 1;
	}
	
	public Sale(Map<String, Object> map)
	{
		item = (ItemStack) map.get("Item");
		price = (int) map.get("Price");
		seller = (BankAccount) map.get("Seller");
		stock = (int) map.get("Stock");
	}

	public ItemStack getItem()
	{
		return item;
	}

	public int getPrice()
	{
		return price;
	}

	public BankAccount getSeller() 
	{
		return seller;
	}
	
	public void buy()
	{
		stock--;
		if(stock < 1)
		{
			Market.getInstance().removeSale(this);
			SellMenu.removeSale(this);
			forSale = false;
		}
	}
	
	public void incrementStock()
	{
		stock++;
	}
	
	public int getStock()
	{
		return stock;
	}
	
	public boolean isForSale()
	{
		return forSale;
	}
	
	public void removeFromSale()
	{
		forSale = false;
	}
	
	public boolean equals(Object other)
	{
		if(other == this)
			return true;
		
		if(!(other instanceof Sale))
			return false;
		
		Sale sale = (Sale)other;
		return sale.getItem().equals(item) && sale.getPrice() == price && sale.getSeller().equals(seller);
	}
	
	public String toString()
	{
		return "Item : " + item + ", price : " + price + ", seller : " + seller + ", stock : " + stock;
	}

	@Override
	public Map<String, Object> serialize() 
	{
		Map<String, Object> serializedSale = new HashMap<String, Object>();
		
		// Serialize item
		serializedSale.put("Item", item);
		
		// Serialize price
		serializedSale.put("Price", price);
		
		// Serialize seller
		serializedSale.put("Seller", seller);
		
		// Serialize stock
		serializedSale.put("Stock", stock);
		
		return serializedSale;
	}

	@Override
	public int compareTo(Sale o) 
	{
		return item.getType().name().compareTo(o.getItem().getType().name());
	}
}