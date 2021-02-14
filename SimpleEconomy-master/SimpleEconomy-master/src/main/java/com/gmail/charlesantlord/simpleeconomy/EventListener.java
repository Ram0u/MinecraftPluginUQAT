package com.gmail.charlesantlord.simpleeconomy;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import inventorymenu.BaseInventoryMenu;
import secommands.SeCommand;

public class EventListener implements Listener
{
	@EventHandler
	public void playerLogin(PlayerLoginEvent event)
	{
		Bank.getInstance().createPlayerAccount(event.getPlayer());
	}
	
	@EventHandler
	public void chatEvent(AsyncPlayerChatEvent event)
	{
		if(event.getPlayer() instanceof Player)
		{
			if(SeCommand.isAwaitingResponse(event.getPlayer().getUniqueId()))
			{
				event.setCancelled(true);
				SeCommand temp = CommandManager.getCurrentPlayerCommand(event.getPlayer().getUniqueId());
				if(temp == null)
				{
					event.getPlayer().sendMessage("The command has expired. Please try again.");
					return;
				}
				if(!temp.next(event.getMessage()))
					CommandManager.finishCommand(event.getPlayer().getUniqueId());
			}
		}
	}
	
	@EventHandler
	public void inventoryCloseEvent(InventoryCloseEvent event)
	{
		if(event != null && event.getPlayer() != null)
		{
			BaseInventoryMenu temp = CommandManager.getOpenInventoryMenu((Player) event.getPlayer());
			if(temp != null)
			{
				temp.closeInventory();
			}
		}
	}
	
	@EventHandler
	public void inventoryClickEvent(InventoryClickEvent event)
	{
		if(event == null || event.getWhoClicked() == null || event.getClickedInventory() == null)
			return;
		
		BaseInventoryMenu im = CommandManager.getOpenInventoryMenu((Player)event.getWhoClicked());
		
		if(im != null)
			event.setCancelled(
				im.onSelect(event.getSlot(), event.isLeftClick(), event.isShiftClick(), event.getClickedInventory()));
	}
}