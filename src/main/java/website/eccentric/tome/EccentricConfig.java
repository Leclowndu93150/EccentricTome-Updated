package website.eccentric.tome;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = EccentricTome.ID, bus = EventBusSubscriber.Bus.MOD)
public class EccentricConfig {
    public static final ModConfigSpec.BooleanValue DISABLE_OVERLAY;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ITEMS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ALIASES;

    public static final HashMap<String, String> ALIAS_MAP = new HashMap<>();

    private static final List<String> allItems = new ArrayList<>();
    private static final List<String> allAliases = new ArrayList<>();

    public static final ModConfigSpec SPEC;

    private static final String GITHUB_BASE_URL = "https://raw.githubusercontent.com/Porting-Dead-Mods/EccentricTome-Updated/refs/heads/main/configs/";
    private static final String ITEMS_FILENAME = "items.txt";
    private static final String ALIASES_FILENAME = "aliases.txt";

    static {
        var BUILDER = new ModConfigSpec.Builder()
                .comment("Common configuration settings")
                .push("common");

        DISABLE_OVERLAY = BUILDER
                .comment("Disable overlay previewing tome conversion")
                .define("disable_overlay", false);

        ITEMS = BUILDER
                .comment("Whitelisted items")
                .defineListAllowEmpty(
                        List.of("items"),
                        () -> List.of(
                                "actuallyadditions:booklet", "ad_astra:astrodux", "ae2:guide", "alexsmobs:animal_dictionary",
                                "ars_nouveau:worn_notebook", "auroral:guide", "compactmachines:personal_shrinking_device",
                                "cookingforblockheads:no_filter_edition", "draconicevolution:info_tablet",
                                "enigmaticlegacy:the_acknowledgment", "eternal_starlight:book", "evilcraft:origins_of_darkness",
                                "fargostalismans:guide_book", "gaze:encyclopedia_unveiled", "iceandfire:bestiary",
                                "immersiveengineering:manual", "integrateddynamics:on_the_dynamics_of_integration",
                                "malum:encyclopedia_arcana", "malum:encyclopedia_esoterica", "merrymaking:merrymanual",
                                "modern_industrialization:guidebook", "nautec:nautec_guide", "neovitae:guide_book",
                                "occultism:dictionary_of_spirits", "paganbless:pagan_guide", "powah:book", "rftoolsbase:manual",
                                "rootsclassic:runic_tablet", "securitycraft:sc_manual", "silentgear:guide_book", "solonion:food_book",
                                "tconstruct:fantastic_foundry", "tconstruct:materials_and_you", "tconstruct:mighty_smelting",
                                "tconstruct:puny_smelting", "tconstruct:tinkers_gadgetry", "theoneprobe:probenote"
                        ),
                        () -> "",
                        Validator::isStringResource
                );

        ALIASES = BUILDER
                .comment("Mod aliases")
                .defineListAllowEmpty(
                        List.of("aliases"),
                        () -> List.of(
                                "mythicbotany=botania",
                                "integratedtunnels=integrateddynamics",
                                "integratedterminals=integrateddynamics",
                                "integratedcrafting=integrateddynamics",
                                "rftoolsbuilder=rftoolsbase",
                                "rftoolscontrol=rftoolsbase",
                                "rftoolsdim=rftoolsbase",
                                "rftoolspower=rftoolsbase",
                                "rftoolsstorage=rftoolsbase",
                                "rftoolsutility=rftoolsbase",
                                "rftoolspower=rftoolsbase",
                                "deepresonance=rftoolsbase",
                                "xnet=rftoolsbase",
                                "mysticalaggraditions=mysticalagriculture"
                        ),
                        () -> "",
                        Validator::isStringAlias);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static List<String> getWhitelistedItems() {
        return allItems;
    }

    public static List<String> getAliases() {
        return allAliases;
    }

    public static List<String> downloadListFromGithub(String filename) {
        String githubUrl = GITHUB_BASE_URL + filename;
        List<String> downloadedList = new ArrayList<>();

        try {
            URL url = new URL(githubUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() == 200) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (!line.isBlank() && !line.startsWith("#")) {
                            downloadedList.add(line.trim());
                        }
                    }
                }
                EccentricTome.LOGGER.info("Successfully downloaded {} entries from {}", downloadedList.size(), filename);
            } else {
                EccentricTome.LOGGER.error("Failed to download {}. HTTP code: {}", filename, connection.getResponseCode());
            }
        } catch (Exception e) {
            EccentricTome.LOGGER.error("Error downloading {}: {}", filename, e.getMessage());
        }

        return downloadedList;
    }

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent event) {
        allItems.clear();
        allAliases.clear();
        ALIAS_MAP.clear();

        List<String> downloadedItems = downloadListFromGithub(ITEMS_FILENAME);
        allItems.addAll(ITEMS.get());
        allItems.addAll(downloadedItems);

        List<String> downloadedAliases = downloadListFromGithub(ALIASES_FILENAME);
        allAliases.addAll(ALIASES.get());
        allAliases.addAll(downloadedAliases);

        for (String alias : allAliases) {
            String[] parts = alias.split("=");
            if (parts.length == 2) {
                ALIAS_MAP.put(parts[0], parts[1]);
            }
        }

        EccentricTome.LOGGER.info("Loaded {} items and {} aliases", allItems.size(), ALIAS_MAP.size());
    }

    public static class Validator {
        public static boolean isString(Object object) {
            return object instanceof String;
        }

        public static boolean isStringResource(Object object) {
            String pattern = "^.+:.+$";
            return isString(object) && ((String) object).matches(pattern);
        }

        public static boolean isStringAlias(Object object) {
            String pattern = "^.+=.+$";
            return isString(object) && ((String) object).matches(pattern);
        }
    }
}