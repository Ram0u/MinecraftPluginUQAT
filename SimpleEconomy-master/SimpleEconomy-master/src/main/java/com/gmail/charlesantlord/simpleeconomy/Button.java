package com.gmail.charlesantlord.simpleeconomy;

import org.bukkit.ChatColor;

public class Button 
{
	private String name;
	private String description;
	private String command;
	private ChatColor color;
	
	public Button(String name, String description, String command, ChatColor color)
	{
		this.name = name;
		this.description = description;
		this.command = command;
		this.color = color;
	}

	public String getName() 
	{
		return name;
	}

	public String getDescription() 
	{
		return description;
	}

	public String getCommand() 
	{
		return command;
	}

	public ChatColor getColor()
	{
		return color;
	}
}