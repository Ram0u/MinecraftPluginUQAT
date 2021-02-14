package com.gmail.charlesantlord.simpleeconomy;

import org.bukkit.ChatColor;

public class ChatUiManager 
{
	public static JSONMessage generateUi(String title, Button buttons[])
	{
		JSONMessage message = JSONMessage.create()
			.beginCenter()
			.then("\n\n\n\n\n")
			.bar()
			.then(title)
			.color(ChatColor.WHITE)
			.newline();
		
		for(Button button : buttons)
		{
			message.then("[" + button.getName() + "]  ")
				.color(button.getColor())
				.style(ChatColor.BOLD)
				.tooltip(button.getDescription())
				.runCommand(button.getCommand());
		}
		
		message.bar();
		return message;
	}
	
	public static void addToMessage(JSONMessage message, String title, Button buttons[])
	{
		message.beginCenter().then(title).color(ChatColor.WHITE).newline();
		
		for(Button button : buttons)
		{
			message.then("[" + button.getName() + "]  ")
				.color(button.getColor())
				.style(ChatColor.BOLD)
				.tooltip(button.getDescription())
				.runCommand(button.getCommand());
		}
		
		message.bar();
	}
}