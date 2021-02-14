package com.gmail.charlesantlord.simpleeconomy;

import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class SimpleEconomy extends JavaPlugin
{
	@Override
	public void onEnable()
	{
		ConfigurationSerialization.registerClass(BankAccount.class, "BankAccount");
		ConfigurationSerialization.registerClass(Company.class, "Company");
		ConfigurationSerialization.registerClass(Bank.class, "Bank");
		ConfigurationSerialization.registerClass(Sale.class, "Sale");
		ConfigurationSerialization.registerClass(Market.class, "Market");


		if(getConfig().contains("Bank"))
		{
			Bank bank = (Bank) getConfig().get("Bank");
		}

		if(getConfig().contains("Market"))
		{
			Market market = (Market) getConfig().get("Market");
		}


		getServer().getPluginManager().registerEvents(new EventListener(), this);
		CommandManager cm = new CommandManager();
		getCommand("se").setExecutor(cm);
		getCommand("setpresident").setExecutor(cm);
		getCommand("finishelections").setExecutor(cm);
		getCommand("startelections").setExecutor(cm);


		BukkitScheduler scheduler = getServer().getScheduler();
		scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run()
			{
				getLogger().info("Saving data");
				getConfig().set("Bank", Bank.getInstance());
				getConfig().set("Market", Market.getInstance());
				saveConfig();
			}
		}, 0L, 6000L);
	}

	@Override
	public void onDisable()
	{
		getConfig().set("Bank", Bank.getInstance());
		getConfig().set("Market", Market.getInstance());
		saveConfig();
	}
}