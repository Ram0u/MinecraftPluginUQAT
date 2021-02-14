package com.gmail.charlesantlord.simpleeconomy;
 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

public class Bank implements ConfigurationSerializable
{
	private HashMap<UUID, BankAccount> playerAccounts;
	private List<Company> companies;
	private BankAccount federalReserve;
	private static Bank instance;
	private UUID president = null;
	private boolean isInElection = false;
	private int taxesRate;
	private int printPercentage;
	
	
	public Bank(Map<String, Object> map)
	{
		// Deserialize player accounts
		playerAccounts = new HashMap<UUID, BankAccount>();
		Map<String, Object> pa = (Map<String, Object>) map.get("PlayerAccounts");
		for(String s : pa.keySet())
			playerAccounts.put(UUID.fromString(s), (BankAccount)pa.get(s));
		
		// Deserialize companies
		companies = (List<Company>) map.get("Companies");
		
		// Deserialize federal reserve
		federalReserve = (BankAccount) map.get("FederalReserve");
		
		// Deserialize president
		if(map.containsKey("President"))
			president = UUID.fromString((String)map.get("President"));
		
		// Deserialize isInElection
		isInElection = (boolean) map.get("InElection");
		
		// Deserialize tax rate
		taxesRate = (int) map.get("TaxRate");
		
		// Deserialize print percentage
		printPercentage = (int) map.get("PrintPercentage");
		
		instance = this;
	}
	
	
	public static Bank getInstance()
	{
		if(instance == null)
			instance = new Bank();
		
		return instance;
	}
	
	
	private Bank()
	{
		playerAccounts = new HashMap<UUID, BankAccount>();
		companies = new ArrayList<Company>();
		federalReserve = new BankAccount(4000, "FederalReserve");
	}
	
	
	public void setPrintPercentage(int printPercentage)
	{
		this.printPercentage = printPercentage;
	}
	
	
	public int getPrintPercentage()
	{
		return printPercentage;
	}
	
	
	public int collectTaxes()
	{
		int taxedAmount = 0;
		for(BankAccount b : getAllBankAccounts())
			taxedAmount += b.collectTaxes();
		
		federalReserve.deposit(taxedAmount);
		
		return taxedAmount;
	}
	
	
	public List<Company> getPlayerCompanies(UUID player)
	{
		List<Company> playerCompanies = new ArrayList<Company>();
		for(Company c : companies)
			if(c.hasMember(player))
				playerCompanies.add(c);
		return playerCompanies;
	}
	
	
	public List<Company> getAllCompanies()
	{
		return companies;
	}
	
	
	public List<BankAccount> getPlayerBankAccounts(UUID player)
	{
		List<BankAccount> playerBankAccounts = new ArrayList<BankAccount>();
		getPlayerCompanies(player).forEach((c) -> playerBankAccounts.add(c.getBankAccount()));
		playerBankAccounts.add(playerAccounts.get(player));
		return playerBankAccounts;
	}
	
	
	public BankAccount getPersonnalBankAccount(UUID player)
	{
		return playerAccounts.get(player);
	}
	
	
	public BankAccount getPlayerBankAccountByName(UUID player, String name)
	{
		for(BankAccount ba : getPlayerBankAccounts(player))
			if(ba.getName().equals(name))
				return ba;
		
		return null;
	}
	
	
	public List<BankAccount> getAllBankAccounts()
	{
		List<BankAccount> allBankAccounts = new ArrayList<BankAccount>();
		companies.forEach((c) -> allBankAccounts.add(c.getBankAccount()));
		allBankAccounts.addAll(playerAccounts.values());
		return allBankAccounts;
	}
	
	
	public BankAccount createPlayerAccount(Player player)
	{
		if(playerAccounts.containsKey(player.getUniqueId()))
			return null;
		
		return playerAccounts.put(player.getUniqueId(), new BankAccount(1000, player.getName()));
	}
	
	
	public Company createCompany(String name, UUID creator)
	{
		Company newCompany = new Company(name, creator);
		
		if(companies.contains(newCompany))
			return null;
		
		companies.add(newCompany);
		return newCompany;
	}
	
	
	public boolean deleteCompany(String name)
	{
		return companies.remove(new Company(name, UUID.randomUUID())); // UUID is not important in this case, we are only using .equals
	}
	
	
	public BankAccount getBankAccountByName(String name)
	{
		if(name.equals("FederalReserve"))
			return federalReserve;
		
		// Loop through companies
		for(Company c : companies)
			if(c.getName().equals(name))
				return c.getBankAccount();
		
		// Loop through player bank accounts
		UUID key = Bukkit.getPlayer(name).getUniqueId();
		if(key != null)
			return playerAccounts.get(Bukkit.getPlayer(name).getUniqueId());
		
		return null;
	}
	
	
	public Company getCompanyByName(String name)
	{
		for(Company c : companies)
			if(c.getName().equals(name))
				return c;
		
		return null;
	}
	
	
	public void setPresident(UUID newPresident)
	{
		this.president = newPresident;
	}
	
	
	public boolean isPresident(UUID player)
	{
		if(president == null)
			return false;
		
		return president.equals(player);
	}
	
	
	public boolean transferMoney(BankAccount from, BankAccount to, int amount)
	{
		if(from.withdraw(amount))
		{
			to.deposit(amount);
			return true;
		}
		
		return false;
	}
	
	public int getTaxesRate()
	{
		return taxesRate;
	}
	
	public void setTaxesRate(int newTaxesRate)
	{
		taxesRate = newTaxesRate;
	}


	@Override
	public Map<String, Object> serialize() 
	{
		Map<String, Object> serializedBank = new HashMap<String, Object>();
		
		// Serialize player accounts
		Map<String, Object> pa = new HashMap<String, Object>();
		for(UUID uuid : playerAccounts.keySet())
			pa.put(uuid.toString(), playerAccounts.get(uuid));
		serializedBank.put("PlayerAccounts", pa);
		
		// Serialize companies
		serializedBank.put("Companies", companies);
		
		// Serialize federal reserve
		serializedBank.put("FederalReserve", federalReserve);
		
		// Serialize president
		if(president != null)
			serializedBank.put("President", president.toString());
		
		// Serialize isInElections
		serializedBank.put("InElection", isInElection);
		
		// Serialize tax rate
		serializedBank.put("TaxRate", taxesRate);
		
		// Serialize print percentage
		serializedBank.put("PrintPercentage", printPercentage);
		
		return serializedBank;
	}
	
	
	public BankAccount getFederalReserve()
	{
		return this.federalReserve;
	}
}
