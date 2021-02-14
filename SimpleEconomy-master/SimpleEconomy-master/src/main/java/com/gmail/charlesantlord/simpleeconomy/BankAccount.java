package com.gmail.charlesantlord.simpleeconomy;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class BankAccount implements Comparable<BankAccount>, ConfigurationSerializable
{
	private int value;
	private String name;
	private int income = 0;

	public BankAccount(int defaultValue, String name)
	{
		this.value = defaultValue;
		this.name = name;
	}

	public BankAccount(Map<String, Object> map)
	{
		this.name = (String) map.get("Name");
		this.value = (int) map.get("Value");
		this.income = (int) map.get("Income");
	}

	public boolean withdraw(int amount)
	{
		if(value >= amount)
		{
			value -= amount;
			return true;
		}

		return false;
	}

	public int collectTaxes()
	{
		if(!name.equals("FederalReserve"))
		{
			int amount = (int) (income * (Bank.getInstance().getTaxesRate() / 100f));
			income = 0;
			value -= amount;
			return amount;
		}
		
		return 0;
	}
	
	public int getIncome()
	{
		return income;
	}

	public void deposit(int amount)
	{
		value += amount;
		income += amount;
	}

	public int getValue()
	{
		return value;
	}

	public String getName()
	{
		return name;
	}

	@Override
	public int compareTo(BankAccount o) 
	{
		return o.getName().compareTo(name);
	}

	@Override
	public Map<String, Object> serialize() 
	{
		Map<String, Object> serializedBankAccount = new HashMap<String, Object>();

		// Serialize name
		serializedBankAccount.put("Name", name);
		// Serialize value
		serializedBankAccount.put("Value", value);
		// Serialize income
		serializedBankAccount.put("Income", income);

		return serializedBankAccount;
	}
}