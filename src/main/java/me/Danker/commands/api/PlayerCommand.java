package me.Danker.commands.api;

import com.google.gson.JsonObject;
import me.Danker.config.ModConfig;
import me.Danker.handlers.APIHandler;
import me.Danker.utils.Utils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class PlayerCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "player";
    }

    @Override
    public String getCommandUsage(ICommandSender arg0) {
        return "/" + getCommandName() + " [name]";
    }

    public static String usage(ICommandSender arg0) {
        return new SkillsCommand().getCommandUsage(arg0);
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) {
            return Utils.getMatchingPlayers(args[0]);
        }
        return null;
    }

    @Override
    public void processCommand(ICommandSender arg0, String[] arg1) throws CommandException {
        // MULTI THREAD DRIFTING
        new Thread(() -> {
            EntityPlayer player = (EntityPlayer) arg0;

            // Check key
            String key = ModConfig.apiKey;
            if (key.equals("")) {
                player.addChatMessage(new ChatComponentText(ModConfig.getColour(ModConfig.errorColour) + "API key not set."));
                return;
            }

            // Get UUID for Hypixel API requests
            String username;
            String uuid;
            if (arg1.length == 0) {
                username = player.getName();
                uuid = player.getUniqueID().toString().replaceAll("[\\-]", "");
                player.addChatMessage(new ChatComponentText(ModConfig.getColour(ModConfig.mainColour) + "Checking stats of " + ModConfig.getColour(ModConfig.secondaryColour) + username));
            } else {
                username = arg1[0];
                player.addChatMessage(new ChatComponentText(ModConfig.getColour(ModConfig.mainColour) + "Checking stats of " + ModConfig.getColour(ModConfig.secondaryColour) + username));
                uuid = APIHandler.getUUID(username);
            }

            // Find stats of latest profile
            String latestProfile = APIHandler.getLatestProfileID(uuid, key);
            if (latestProfile == null) return;

            String profileURL = "https://api.hypixel.net/skyblock/profile?profile=" + latestProfile + "&key=" + key;
            System.out.println("Fetching profile...");
            JsonObject profileResponse = APIHandler.getResponse(profileURL, true);
            if (!profileResponse.get("success").getAsBoolean()) {
                String reason = profileResponse.get("cause").getAsString();
                player.addChatMessage(new ChatComponentText(ModConfig.getColour(ModConfig.errorColour) + "Failed with reason: " + reason));
                return;
            }

            // Skills
            System.out.println("Fetching skills...");
            JsonObject userObject = profileResponse.get("profile").getAsJsonObject().get("members").getAsJsonObject().get(uuid).getAsJsonObject();

            double farmingLevel = 0;
            double miningLevel = 0;
            double combatLevel = 0;
            double foragingLevel = 0;
            double fishingLevel = 0;
            double enchantingLevel = 0;
            double alchemyLevel = 0;
            double tamingLevel = 0;
            double carpentryLevel = 0;

            if (userObject.has("experience_skill_farming") || userObject.has("experience_skill_mining") || userObject.has("experience_skill_combat") || userObject.has("experience_skill_foraging") || userObject.has("experience_skill_fishing") || userObject.has("experience_skill_enchanting") || userObject.has("experience_skill_alchemy")) {
                if (userObject.has("experience_skill_farming")) {
                    farmingLevel = Utils.xpToSkillLevel(userObject.get("experience_skill_farming").getAsDouble(), 60);
                    farmingLevel = (double) Math.round(farmingLevel * 100) / 100;
                }
                if (userObject.has("experience_skill_mining")) {
                    miningLevel = Utils.xpToSkillLevel(userObject.get("experience_skill_mining").getAsDouble(), 60);
                    miningLevel = (double) Math.round(miningLevel * 100) / 100;
                }
                if (userObject.has("experience_skill_combat")) {
                    combatLevel = Utils.xpToSkillLevel(userObject.get("experience_skill_combat").getAsDouble(), 60);
                    combatLevel = (double) Math.round(combatLevel * 100) / 100;
                }
                if (userObject.has("experience_skill_foraging")) {
                    foragingLevel = Utils.xpToSkillLevel(userObject.get("experience_skill_foraging").getAsDouble(), 50);
                    foragingLevel = (double) Math.round(foragingLevel * 100) / 100;
                }
                if (userObject.has("experience_skill_fishing")) {
                    fishingLevel = Utils.xpToSkillLevel(userObject.get("experience_skill_fishing").getAsDouble(), 50);
                    fishingLevel = (double) Math.round(fishingLevel * 100) / 100;
                }
                if (userObject.has("experience_skill_enchanting")) {
                    enchantingLevel = Utils.xpToSkillLevel(userObject.get("experience_skill_enchanting").getAsDouble(), 60);
                    enchantingLevel = (double) Math.round(enchantingLevel * 100) / 100;
                }
                if (userObject.has("experience_skill_alchemy")) {
                    alchemyLevel = Utils.xpToSkillLevel(userObject.get("experience_skill_alchemy").getAsDouble(), 50);
                    alchemyLevel = (double) Math.round(alchemyLevel * 100) / 100;
                }
                if (userObject.has("experience_skill_taming")) {
                    tamingLevel = Utils.xpToSkillLevel(userObject.get("experience_skill_taming").getAsDouble(), 50);
                    tamingLevel = (double) Math.round(tamingLevel * 100) / 100;
                }
                if (userObject.has("experience_skill_carpentry")) {
                    carpentryLevel = Utils.xpToSkillLevel(userObject.get("experience_skill_carpentry").getAsDouble(), 50);
                    carpentryLevel = (double) Math.round(carpentryLevel * 100) / 100;
                }
            } else {
                // Get skills from achievement API, will be floored

                String playerURL = "https://api.hypixel.net/player?uuid=" + uuid + "&key=" + key;
                System.out.println("Fetching skills from achievement API");
                JsonObject playerObject = APIHandler.getResponse(playerURL, true);

                if (!playerObject.get("success").getAsBoolean()) {
                    String reason = profileResponse.get("cause").getAsString();
                    player.addChatMessage(new ChatComponentText(ModConfig.getColour(ModConfig.errorColour) + "Failed with reason: " + reason));
                    return;
                }

                JsonObject achievementObject = playerObject.get("player").getAsJsonObject().get("achievements").getAsJsonObject();
                if (achievementObject.has("skyblock_harvester")) {
                    farmingLevel = achievementObject.get("skyblock_harvester").getAsInt();
                }
                if (achievementObject.has("skyblock_excavator")) {
                    miningLevel = achievementObject.get("skyblock_excavator").getAsInt();
                }
                if (achievementObject.has("skyblock_combat")) {
                    combatLevel = achievementObject.get("skyblock_combat").getAsInt();
                }
                if (achievementObject.has("skyblock_gatherer")) {
                    foragingLevel = Math.min(achievementObject.get("skyblock_gatherer").getAsInt(), 50);
                }
                if (achievementObject.has("skyblock_angler")) {
                    fishingLevel = Math.min(achievementObject.get("skyblock_angler").getAsInt(), 50);
                }
                if (achievementObject.has("skyblock_augmentation")) {
                    enchantingLevel = achievementObject.get("skyblock_augmentation").getAsInt();
                }
                if (achievementObject.has("skyblock_concoctor")) {
                    alchemyLevel = Math.min(achievementObject.get("skyblock_concoctor").getAsInt(), 50);
                }
                if (achievementObject.has("skyblock_domesticator")) {
                    tamingLevel = Math.min(achievementObject.get("skyblock_domesticator").getAsInt(), 50);
                }
            }

            double skillAvg = (farmingLevel + miningLevel + combatLevel + foragingLevel + fishingLevel + enchantingLevel + alchemyLevel + tamingLevel + carpentryLevel) / 9;
            skillAvg = (double) Math.round(skillAvg * 100) / 100;
            double trueAvg = (Math.floor(farmingLevel) + Math.floor(miningLevel) + Math.floor(combatLevel) + Math.floor(foragingLevel) + Math.floor(fishingLevel) + Math.floor(enchantingLevel) + Math.floor(alchemyLevel) + Math.floor(tamingLevel) + Math.floor(carpentryLevel)) / 9;
            trueAvg = (double) Math.round(trueAvg * 100) / 100;

            // Slayers
            System.out.println("Fetching slayer stats...");
            JsonObject slayersObject = profileResponse.get("profile").getAsJsonObject().get("members").getAsJsonObject().get(uuid).getAsJsonObject().get("slayer_bosses").getAsJsonObject();
            // Zombie
            int zombieXP = 0;
            if (slayersObject.get("zombie").getAsJsonObject().has("xp")) {
                zombieXP = slayersObject.get("zombie").getAsJsonObject().get("xp").getAsInt();
            }
            // Spider
            int spiderXP = 0;
            if (slayersObject.get("spider").getAsJsonObject().has("xp")) {
                spiderXP = slayersObject.get("spider").getAsJsonObject().get("xp").getAsInt();
            }
            // Wolf
            int wolfXP = 0;
            if (slayersObject.get("wolf").getAsJsonObject().has("xp")) {
                wolfXP = slayersObject.get("wolf").getAsJsonObject().get("xp").getAsInt();
            }
            // Enderman
            int endermanXP = 0;
            if (slayersObject.get("enderman").getAsJsonObject().has("xp")) {
                endermanXP = slayersObject.get("enderman").getAsJsonObject().get("xp").getAsInt();
            }
            // Blaze
            int blazeXP = 0;
            if (slayersObject.get("blaze").getAsJsonObject().has("xp")) {
                blazeXP = slayersObject.get("blaze").getAsJsonObject().get("xp").getAsInt();
            }
            // Vampire
            int vampireXP = 0;
            if (slayersObject.get("vampire").getAsJsonObject().has("xp")) {
                vampireXP = slayersObject.get("vampire").getAsJsonObject().get("xp").getAsInt();
            }

            int totalXP = zombieXP + spiderXP + wolfXP + blazeXP + vampireXP;

            // Bank
            System.out.println("Fetching bank + purse coins...");
            double bankCoins = 0;
            double purseCoins = profileResponse.get("profile").getAsJsonObject().get("members").getAsJsonObject().get(uuid).getAsJsonObject().get("coin_purse").getAsDouble();
            purseCoins = Math.floor(purseCoins * 100.0) / 100.0;

            // Check for bank api
            if (profileResponse.get("profile").getAsJsonObject().has("banking")) {
                bankCoins = profileResponse.get("profile").getAsJsonObject().get("banking").getAsJsonObject().get("balance").getAsDouble();
                bankCoins = Math.floor(bankCoins * 100.0) / 100.0;
            }

            // Weight
            System.out.println("Fetching weight from SkyShiiyu API...");
            String weightURL = "https://sky.shiiyu.moe/api/v2/profile/" + username;
            JsonObject weightResponse = APIHandler.getResponse(weightURL, true);
            if (weightResponse.has("error")) {
                String reason = weightResponse.get("error").getAsString();
                player.addChatMessage(new ChatComponentText(ModConfig.getColour(ModConfig.errorColour) + "Failed with reason: " + reason));
                return;
            }

            double weight = weightResponse.get("profiles").getAsJsonObject().get(latestProfile).getAsJsonObject().get("data").getAsJsonObject().get("weight").getAsJsonObject().get("senither").getAsJsonObject().get("overall").getAsDouble();

            NumberFormat nf = NumberFormat.getIntegerInstance(Locale.US);
            NumberFormat nfd = NumberFormat.getNumberInstance(Locale.US);
            player.addChatMessage(new ChatComponentText(ModConfig.getDelimiter() + "\n" +
                                                        EnumChatFormatting.AQUA + " " + username + "'s Skills:\n" +
                                                        ModConfig.getColour(ModConfig.typeColour) + " Farming: " + ModConfig.getColour(ModConfig.valueColour) + EnumChatFormatting.BOLD + farmingLevel + "\n" +
                                                        ModConfig.getColour(ModConfig.typeColour) + " Mining: " + ModConfig.getColour(ModConfig.valueColour) + EnumChatFormatting.BOLD + miningLevel + "\n" +
                                                        ModConfig.getColour(ModConfig.typeColour) + " Combat: " + ModConfig.getColour(ModConfig.valueColour) + EnumChatFormatting.BOLD + combatLevel + "\n" +
                                                        ModConfig.getColour(ModConfig.typeColour) + " Foraging: " + ModConfig.getColour(ModConfig.valueColour) + EnumChatFormatting.BOLD + foragingLevel + "\n" +
                                                        ModConfig.getColour(ModConfig.typeColour) + " Fishing: " + ModConfig.getColour(ModConfig.valueColour) + EnumChatFormatting.BOLD + fishingLevel + "\n" +
                                                        ModConfig.getColour(ModConfig.typeColour) + " Enchanting: " + ModConfig.getColour(ModConfig.valueColour) + EnumChatFormatting.BOLD + enchantingLevel + "\n" +
                                                        ModConfig.getColour(ModConfig.typeColour) + " Alchemy: " + ModConfig.getColour(ModConfig.valueColour) + EnumChatFormatting.BOLD + alchemyLevel + "\n" +
                                                        ModConfig.getColour(ModConfig.typeColour) + " Taming: " + ModConfig.getColour(ModConfig.valueColour) + EnumChatFormatting.BOLD + tamingLevel + "\n" +
                                                        ModConfig.getColour(ModConfig.typeColour) + " Carpentry: " + ModConfig.getColour(ModConfig.valueColour) + EnumChatFormatting.BOLD + carpentryLevel + "\n" +
                                                        EnumChatFormatting.AQUA + " Average Skill Level: " + ModConfig.getColour(ModConfig.skillAverageColour) + EnumChatFormatting.BOLD + skillAvg + "\n" +
                                                        EnumChatFormatting.AQUA + " True Average Skill Level: " + ModConfig.getColour(ModConfig.skillAverageColour) + EnumChatFormatting.BOLD + trueAvg + "\n\n" +
                                                        EnumChatFormatting.AQUA + " " + username + "'s Total Slayer XP: " + EnumChatFormatting.GOLD + EnumChatFormatting.BOLD + nf.format(totalXP) + "\n" +
                                                        ModConfig.getColour(ModConfig.typeColour) + " Zombie XP: " + ModConfig.getColour(ModConfig.valueColour) + EnumChatFormatting.BOLD + nf.format(zombieXP) + "\n" +
                                                        ModConfig.getColour(ModConfig.typeColour) + " Spider XP: " + ModConfig.getColour(ModConfig.valueColour) + EnumChatFormatting.BOLD + nf.format(spiderXP) + "\n" +
                                                        ModConfig.getColour(ModConfig.typeColour) + " Wolf XP: " + ModConfig.getColour(ModConfig.valueColour) + EnumChatFormatting.BOLD + nf.format(wolfXP) + "\n" +
                                                        ModConfig.getColour(ModConfig.typeColour) + " Enderman XP: " + ModConfig.getColour(ModConfig.valueColour) + EnumChatFormatting.BOLD + nf.format(endermanXP) + "\n" +
                                                        ModConfig.getColour(ModConfig.typeColour) + " Blaze XP: " + ModConfig.getColour(ModConfig.valueColour) + EnumChatFormatting.BOLD + nf.format(blazeXP) + "\n" +
                                                        ModConfig.getColour(ModConfig.typeColour) + " Vampire XP: " + ModConfig.getColour(ModConfig.valueColour) + EnumChatFormatting.BOLD + nf.format(vampireXP) + "\n\n" +
                                                        EnumChatFormatting.AQUA + " " + username + "'s Coins:\n" +
                                                        ModConfig.getColour(ModConfig.typeColour) + " Bank: " + (bankCoins == 0 ? EnumChatFormatting.RED + "Bank API disabled." : EnumChatFormatting.GOLD + nf.format(bankCoins)) + "\n" +
                                                        ModConfig.getColour(ModConfig.typeColour) + " Purse: " + EnumChatFormatting.GOLD + nf.format(purseCoins) + "\n" +
                                                        ModConfig.getColour(ModConfig.typeColour) + " Total: " + EnumChatFormatting.GOLD + nf.format(bankCoins + purseCoins) + "\n\n" +
                                                        EnumChatFormatting.AQUA + " " + username + "'s Weight:\n" +
                                                        ModConfig.getColour(ModConfig.typeColour) + " Total Weight: " + ModConfig.getColour(ModConfig.valueColour) + nfd.format(weight) + "\n" +
                                                        ModConfig.getDelimiter()));
        }).start();
    }

}
