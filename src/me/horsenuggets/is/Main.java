package me.horsenuggets.is;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.DatatypeConverter;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	
	/**
	 * @author HorseNuggets
	 * @since 5/1/2020
	 */
	
	public static String itemsToString(ItemStack[] items) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			
			@SuppressWarnings("unchecked")
			Map<String, Object>[] result = new Map[items.length];
			for (int i = 0; i < items.length; i++) {
				ItemStack is = items[i];
				if (is == null) {
					result[i] = new HashMap<>();
				}
				else {
					result[i] = is.serialize();
					if (is.hasItemMeta()) {
						result[i].put("meta", is.getItemMeta().serialize());
					}
				}
			}
			
			oos.writeObject(result);
			oos.flush();
			
			return DatatypeConverter.printBase64Binary(bos.toByteArray());
		}
		catch (Exception e) {}
		return "";
	}

	@SuppressWarnings("unchecked")
	public static ItemStack[] stringToItems(String s) {
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(DatatypeConverter.parseBase64Binary(s));
			ObjectInputStream ois = new ObjectInputStream(bis);
			
			Map<String, Object>[] map = (Map<String, Object>[]) ois.readObject();
			
			ItemStack[] items = new ItemStack[map.length];

			for (int i = 0; i < items.length; i++) {
				Map<String, Object> s1 = map[i];
				if (s1.size() == 0) {
					items[i] = null;
				}
				else {
					try {
						if (s1.containsKey("meta")) {
							Map<String, Object> im = new HashMap<>((Map<String, Object>) s1.remove("meta"));
							im.put("==", "ItemMeta");
							ItemStack is = ItemStack.deserialize(s1);
							is.setItemMeta((ItemMeta) ConfigurationSerialization.deserializeObject(im));
							items[i] = is;
						}
						else {
							items[i] = ItemStack.deserialize(s1);
						}
					}
					catch (Exception e) {
						items[i] = null;
					}
				}
			}
			
			return items;
		}
		catch (Exception e) {}
		return new ItemStack[] {
			new ItemStack(Material.AIR)
		};
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			UUID uuid = player.getUniqueId();
			if (label.equalsIgnoreCase("save")) {
				ItemStack[] invc = player.getInventory().getContents();
				ItemStack[] armc = player.getInventory().getArmorContents();
				getConfig().set("inventores." + uuid + ".items", itemsToString(invc));
				getConfig().set("inventores." + uuid + ".armor", itemsToString(armc));
				saveConfig();
				return true;
			}
			else if (label.equalsIgnoreCase("load")) {
				ItemStack items[] = stringToItems(getConfig().getString("inventores." + uuid + ".items"));
				ItemStack armor[] = stringToItems(getConfig().getString("inventores." + uuid + ".armor"));
				player.getInventory().setContents(items);
				player.getInventory().setArmorContents(armor);
				return true;
			}
		}
		return false;
	}
}