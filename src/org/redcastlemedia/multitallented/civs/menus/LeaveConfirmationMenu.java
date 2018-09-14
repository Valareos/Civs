package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;
import org.redcastlemedia.multitallented.civs.util.CVItem;

public class LeaveConfirmationMenu extends Menu {
    static String MENU_NAME = "CivLeaveConfirm";
    public LeaveConfirmationMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);

        LocaleManager localeManager = LocaleManager.getInstance();
        CivilianManager civilianManager = CivilianManager.getInstance();

        Civilian civilian = civilianManager.getCivilian(event.getWhoClicked().getUniqueId());

        if (Menu.isBackButton(event.getCurrentItem(), civilian.getLocale())) {
            clickBackButton(event.getWhoClicked());
            return;
        }
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        String townName = event.getInventory().getItem(0).getItemMeta().getDisplayName().replace("T:","");
        Town town = town = TownManager.getInstance().getTown(townName);


        Player player = (Player) event.getWhoClicked();
        if (event.getCurrentItem().getType().equals(Material.EMERALD)) {
            clearHistory(civilian.getUuid());
            if (town != null) {
                town.getPeople().remove(civilian.getUuid());
                player.sendMessage(localeManager.getTranslation(civilian.getLocale(),
                        "you-left-town").replace("$1", townName));
            }
            player.closeInventory();
            return;
        }
        if (event.getCurrentItem().getType().equals(Material.BARRIER)) {
            clearHistory(civilian.getUuid());
            event.getWhoClicked().closeInventory();
            return;
        }
    }

    public static Inventory createMenu(Civilian civilian, Town town) {
        Inventory inventory = Bukkit.createInventory(null, 9, MENU_NAME);
        LocaleManager localeManager = LocaleManager.getInstance();
        TownType regionType = (TownType) ItemManager.getInstance().getItemType(town.getType());
        CVItem regionTypeIcon = regionType.clone();
        regionTypeIcon.setDisplayName("T:" + town.getName());
        inventory.setItem(0, regionTypeIcon.createItemStack());

        CVItem cvItem = CVItem.createCVItemFromString("EMERALD");
        cvItem.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "leave-town"));

        inventory.setItem(3, cvItem.createItemStack());

        CVItem cvItem1 = CVItem.createCVItemFromString("BARRIER");
        cvItem1.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "cancel"));
        inventory.setItem(4, cvItem1.createItemStack());

        inventory.setItem(8, getBackButton(civilian));

        return inventory;
    }
}
