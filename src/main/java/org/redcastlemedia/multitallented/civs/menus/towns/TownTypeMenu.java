package org.redcastlemedia.multitallented.civs.menus.towns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleConstants;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleUtil;
import org.redcastlemedia.multitallented.civs.menus.CivsMenu;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;
import org.redcastlemedia.multitallented.civs.util.Util;

@CivsMenu(name = "town-type")
public class TownTypeMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        HashMap<String, Object> data = new HashMap<>();
        if (params.containsKey("townType")) {
            CivItem regionType = ItemManager.getInstance().getItemType(params.get("townType"));
            data.put("townType", regionType);
        }
        if (params.containsKey("showPrice") && "true".equals(params.get("showPrice"))) {
            data.put("showPrice", true);
        } else {
            data.put("showPrice", false);
        }
        return data;
    }

    @Override
    public ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        Player player = Bukkit.getPlayer(civilian.getUuid());
        if (player == null) {
            return new ItemStack(Material.AIR);
        }
        LocaleManager localeManager = LocaleManager.getInstance();
        TownType townType = (TownType) MenuManager.getData(civilian.getUuid(), "townType");
        if (townType == null) {
            return new ItemStack(Material.AIR);
        }
        if ("icon".equals(menuIcon.getKey())) {
            CVItem cvItem = townType.clone();
            List<String> lore = new ArrayList<>();
            lore.add(localeManager.getTranslationWithPlaceholders(player, "size") +
                    ": " + (townType.getBuildRadius() * 2 + 1) + "x" + (townType.getBuildRadius() * 2 + 1) + "x" + (townType.getBuildRadiusY() * 2 + 1));
            lore.addAll(Util.textWrap(Util.parseColors(townType.getDescription(civilian.getLocale()))));
            cvItem.setLore(lore);
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("price".equals(menuIcon.getKey())) {
            boolean hasShopPerms = Civs.perm != null && Civs.perm.has(player, "civs.shop");
            String maxLimit = civilian.isAtMax(townType);
            if (hasShopPerms && maxLimit == null) {
                CVItem priceItem = CVItem.createCVItemFromString(menuIcon.getIcon());
                priceItem.setDisplayName(localeManager.getTranslationWithPlaceholders(player, menuIcon.getName()));
                ArrayList<String> lore = new ArrayList<>();
                lore.add(localeManager.getTranslationWithPlaceholders(player, "price")
                        .replace("$1", Util.getNumberFormat(townType.getPrice(), civilian.getLocale())));
                priceItem.setLore(lore);
                ItemStack itemStack = priceItem.createItemStack();
                putActions(civilian, menuIcon, itemStack, count);
                return itemStack;
            } else if (hasShopPerms) {
                CVItem priceItem = CVItem.createCVItemFromString("BARRIER");
                priceItem.setDisplayName(localeManager.getTranslationWithPlaceholders(player, menuIcon.getName()));
                ArrayList<String> lore = new ArrayList<>();
                LocaleUtil.getTranslationMaxItem(maxLimit, townType, player, lore);
                priceItem.setLore(lore);
                return priceItem.createItemStack();
            }
        } else if ("rebuild".equals(menuIcon.getKey())) {
            if (townType.getChild() == null) {
                return new ItemStack(Material.AIR);
            }
            CVItem rebuildItem = ItemManager.getInstance()
                    .getItemType(townType.getChild().toLowerCase()).clone();
            List<String> lore = new ArrayList<>();
            lore.add(localeManager.getTranslationWithPlaceholders(player, menuIcon.getDesc())
                    .replace("$1", townType.getProcessedName())
                    .replace("$2", townType.getChild()));
            rebuildItem.setLore(lore);
            ItemStack itemStack = rebuildItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("build-reqs".equals(menuIcon.getKey())) {
            CVItem cvItem = menuIcon.createCVItem(civilian.getLocale(), count);
            String localizedName = LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                    townType.getProcessedName() + "-name");
            cvItem.setLore(Util.textWrap(LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                    menuIcon.getDesc()).replace("$1", localizedName)));
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("effects".equals(menuIcon.getKey())) {
            CVItem cvItem = menuIcon.createCVItem(civilian.getLocale(), count);
            cvItem.getLore().addAll(townType.getEffects().keySet());
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("population".equals(menuIcon.getKey())) {
            if (townType.getChild() == null || townType.getChildPopulation() < 1) {
                return new ItemStack(Material.AIR);
            }
            CVItem cvItem = menuIcon.createCVItem(civilian.getLocale(), count);
            String childName = LocaleManager.getInstance().getTranslation(
                    civilian.getLocale(), townType.getChild().toLowerCase() + "-name");
            cvItem.setDisplayName(LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                    menuIcon.getName()).replace("$1", childName)
                    .replace("$2", "" + townType.getChildPopulation()));
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        }
        return super.createItemStack(civilian, menuIcon, count);
    }
}