package com.gmail.charlesantlord.simpleeconomy;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.Inventory;

import inventorymenu.SellMenu;

public class Market implements ConfigurationSerializable
{
	private HashMap<BankAccount, List<Sale>> sales;
	private static Market instance;
	
	
	public Market(Map<String, Object> map)
	{
		sales = (HashMap<BankAccount, List<Sale>>) map.get("Sales");
		
		// Put items in SellMenu
		SellMenu sm = new SellMenu(sales);
			
		
		instance = this;
	}
	
	
	public static Market getInstance()
	{
		if(instance == null)
			instance = new Market();
		
		return instance;
	}
	
	
	private Market()
	{
		sales = new HashMap<BankAccount, List<Sale>>();
	}
	
	
	public void setSalesByBankAccount(List<Sale> sales, BankAccount bankAccount)
	{
		this.sales.put(bankAccount, sales);
	}
	
	
	public List<Sale> getSalesByBankAccount(BankAccount bankAccount)
	{
		return sales.get(bankAccount);
	}
	
	
	public Collection<List<Sale>> getSales()
	{
		return sales.values();
	}
	
	
	public void removeSale(Sale saleToRemove) // Used by Sale to remove itself when its stock hits 0
	{
		for(List<Sale> baSales : sales.values())
		{
			if(baSales.contains(saleToRemove))
			{
				baSales.remove(saleToRemove);
				return;
			}
		}
	}
	
	
	public void removeSales(BankAccount ba) // Used by SellMenu to remove its sales when someone is interacting with it
	{
		sales.remove(ba);
	}


	@Override
	public Map<String, Object> serialize() 
	{
		Map<String, Object> serializedMarket = new HashMap<String, Object>();
		
		// Serialize sales
		serializedMarket.put("Sales", sales);
		
		return serializedMarket;
	}
	
	
	public static List<Sale> generateSalesFromInventory(Inventory inventory)
	{
		return null;
	}
}