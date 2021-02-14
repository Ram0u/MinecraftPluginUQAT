package com.gmail.charlesantlord.simpleeconomy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class Company implements ConfigurationSerializable
{
	private String name;
	private UUID creator;
	private BankAccount bankAccount;
	private List<UUID> members;
	
	public Company(String name, UUID creator)
	{
		bankAccount = new BankAccount(0, name);
		members = new ArrayList<UUID>();
		members.add(creator);
		this.name = name;
		this.creator = creator;
	}
	
	public Company(Map<String, Object> map)
	{
		members = new ArrayList<UUID>();
		
		// Set name
		name = (String)map.get("Name");
		
		// Set creator
		creator = UUID.fromString((String)map.get("Creator"));
		
		// Set bank account
		bankAccount = (BankAccount) map.get("BankAccount");
		
		// Set members
		List<String> playerUUIDs = (List<String>) map.get("Members");
		for(String s : playerUUIDs)
			members.add(UUID.fromString(s));
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public BankAccount getBankAccount()
	{
		return bankAccount;
	}
	
	public boolean hasMember(UUID player)
	{
		return members.contains(player);
	}
	
	public List<UUID> getMembers()
	{
		return this.members;
	}
	
	public boolean isCreator(UUID player)
	{
		return creator.equals(player);
	}
	
	public boolean equals(Object other)
	{
		if(this == other)
			return true;
		
		if(other instanceof Company)
		{
			Company otherCompany = (Company)other;
			return name.equals(otherCompany.getName());
		}
		else
			return false;
	}
	
	public boolean addPlayer(UUID player)
	{
		if(members.contains(player))
			return false;
		else
		{
			members.add(player);
			return true;
		}
	}
	
	public boolean removePlayer(UUID player)
	{
		return members.remove(player);
	}
	
	public void disband()
	{
		int amount = bankAccount.getValue() / members.size();
		
		for(UUID p : members)
			Bank.getInstance().getPersonnalBankAccount(p).deposit(amount);
		Bank.getInstance().deleteCompany(name);
	}

	@Override
	public Map<String, Object> serialize() 
	{
		Map<String, Object> serializedCompany = new HashMap<String, Object>();
		
		// Serialize name
		serializedCompany.put("Name", name);
		
		// Serialize creator
		serializedCompany.put("Creator", creator.toString());
		
		// Serialize bank account
		serializedCompany.put("BankAccount", bankAccount);
		
		// Serialize members
		List<String> serializedUUIDs = new ArrayList<String>();
		for(UUID u : members)
			serializedUUIDs.add(u.toString());
		serializedCompany.put("Members", serializedUUIDs);
		
		return serializedCompany;
	}
}