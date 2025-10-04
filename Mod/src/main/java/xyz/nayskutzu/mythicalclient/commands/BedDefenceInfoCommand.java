package xyz.nayskutzu.mythicalclient.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.client.Minecraft;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockColored;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import xyz.nayskutzu.mythicalclient.MythicalClientMod;

import java.util.*;

public class BedDefenceInfoCommand extends CommandBase {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final int SEARCH_RADIUS = 50; // blocks
    private static final int DEFENSE_SCAN_RADIUS = 5; // blocks around bed to scan
    private static final int MAX_SCAN_HEIGHT = 4; // blocks above bed to scan

    private static final Set<Block> DEFENSE_BLOCKS = new HashSet<>(Arrays.asList(
        Blocks.obsidian,
        Blocks.end_stone,
        Blocks.planks,
        Blocks.glass,
        Blocks.stained_glass,
        Blocks.wool,
        Blocks.stained_hardened_clay,
        Blocks.hardened_clay,
        Blocks.water,
        Blocks.flowing_water
    ));

    private static final Map<Block, DefenseInfo> BLOCK_INFO = new HashMap<Block, DefenseInfo>() {{
        put(Blocks.obsidian, new DefenseInfo("Obsidian", "&5", 50));
        put(Blocks.end_stone, new DefenseInfo("End Stone", "&e", 24));
        put(Blocks.planks, new DefenseInfo("Wood", "&6", 15));
        put(Blocks.glass, new DefenseInfo("Glass", "&b", 1));
        put(Blocks.stained_glass, new DefenseInfo("Glass", "&b", 1));
        put(Blocks.wool, new DefenseInfo("Wool", "&f", 4));
        put(Blocks.stained_hardened_clay, new DefenseInfo("Clay", "&c", 12));
        put(Blocks.hardened_clay, new DefenseInfo("Clay", "&c", 12));
        put(Blocks.water, new DefenseInfo("Water", "&1", -1));
        put(Blocks.flowing_water, new DefenseInfo("Water", "&1", -1));
    }};

    private static final Map<EnumDyeColor, TeamInfo> TEAM_COLORS = new HashMap<EnumDyeColor, TeamInfo>() {{
        put(EnumDyeColor.RED, new TeamInfo("Red", "&c", "❤"));
        put(EnumDyeColor.BLUE, new TeamInfo("Blue", "&9", "✦"));
        put(EnumDyeColor.GREEN, new TeamInfo("Green", "&a", "✤"));
        put(EnumDyeColor.YELLOW, new TeamInfo("Yellow", "&e", "✸"));
        put(EnumDyeColor.PINK, new TeamInfo("Pink", "&d", "✿"));
        put(EnumDyeColor.GRAY, new TeamInfo("Gray", "&7", "❉"));
        put(EnumDyeColor.WHITE, new TeamInfo("White", "&f", "✼"));
        put(EnumDyeColor.CYAN, new TeamInfo("Aqua", "&b", "❋"));
    }};

    @Override
    public String getCommandName() {
        return "beddefence";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/beddefence [radius]";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        try {
            int radius = SEARCH_RADIUS;
            if (args.length > 0) {
                try {
                    radius = Integer.parseInt(args[0]);
                    if (radius < 1 || radius > 100) {
                        MythicalClientMod.sendMessageToChat("&c&l[!] &cRadius must be between 1 and 100 blocks!", false);
                        return;
                    }
                } catch (NumberFormatException e) {
                    MythicalClientMod.sendMessageToChat("&c&l[!] &cInvalid radius! Using default: " + SEARCH_RADIUS, false);
                }
            }

            // Find nearest bed
            BlockPos nearestBed = findNearestBed(radius);
            if (nearestBed == null) {
                MythicalClientMod.sendMessageToChat("&c&l[!] &cNo beds found within " + radius + " blocks!", false);
                return;
            }

            // Analyze bed defenses and team
            Map<Block, Integer> defenseBlocks = scanDefenses(nearestBed);
            TeamInfo teamInfo = detectTeam(nearestBed);
            
            if (defenseBlocks.isEmpty()) {
                MythicalClientMod.sendMessageToChat("&c&l[!] &cNo defenses found around the bed!", false);
                return;
            }

            // Calculate distance and direction
            double distance = mc.thePlayer.getDistance(
                nearestBed.getX() + 0.5,
                nearestBed.getY() + 0.5,
                nearestBed.getZ() + 0.5
            );
            String direction = getDirection(nearestBed);

            // Display results
            displayDefenseInfo(nearestBed, distance, direction, defenseBlocks, teamInfo);

        } catch (Exception e) {
            MythicalClientMod.sendMessageToChat("&c&l[!] &cError analyzing bed defense: " + e.getMessage(), false);
        }
    }

    private TeamInfo detectTeam(BlockPos bedPos) {
        // Scan area around bed for team-colored wool
        for (int x = -DEFENSE_SCAN_RADIUS; x <= DEFENSE_SCAN_RADIUS; x++) {
            for (int y = -1; y <= MAX_SCAN_HEIGHT; y++) {
                for (int z = -DEFENSE_SCAN_RADIUS; z <= DEFENSE_SCAN_RADIUS; z++) {
                    BlockPos pos = bedPos.add(x, y, z);
                    Block block = mc.theWorld.getBlockState(pos).getBlock();
                    
                    if (block == Blocks.wool) {
                        EnumDyeColor color = mc.theWorld.getBlockState(pos).getValue(BlockColored.COLOR);
                        TeamInfo team = TEAM_COLORS.get(color);
                        if (team != null) {
                            return team;
                        }
                    }
                }
            }
        }
        return null;
    }

    private BlockPos findNearestBed(int radius) {
        BlockPos playerPos = mc.thePlayer.getPosition();
        BlockPos nearestBed = null;
        double minDistance = Double.MAX_VALUE;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    Block block = mc.theWorld.getBlockState(pos).getBlock();
                    
                    if (block instanceof BlockBed) {
                        double dist = mc.thePlayer.getDistance(
                            pos.getX() + 0.5,
                            pos.getY() + 0.5,
                            pos.getZ() + 0.5
                        );
                        if (dist < minDistance) {
                            minDistance = dist;
                            nearestBed = pos;
                        }
                    }
                }
            }
        }
        return nearestBed;
    }

    private Map<Block, Integer> scanDefenses(BlockPos bedPos) {
        Map<Block, Integer> defenses = new HashMap<>();

        for (int x = -DEFENSE_SCAN_RADIUS; x <= DEFENSE_SCAN_RADIUS; x++) {
            for (int y = 0; y <= MAX_SCAN_HEIGHT; y++) {
                for (int z = -DEFENSE_SCAN_RADIUS; z <= DEFENSE_SCAN_RADIUS; z++) {
                    BlockPos pos = bedPos.add(x, y, z);
                    Block block = mc.theWorld.getBlockState(pos).getBlock();

                    if (DEFENSE_BLOCKS.contains(block)) {
                        defenses.merge(block, 1, Integer::sum);
                    }
                }
            }
        }
        return defenses;
    }

    private String getDirection(BlockPos bedPos) {
        Vec3 playerLook = mc.thePlayer.getLookVec();
        Vec3 toBed = new Vec3(
            bedPos.getX() - mc.thePlayer.posX,
            bedPos.getY() - mc.thePlayer.posY,
            bedPos.getZ() - mc.thePlayer.posZ
        ).normalize();

        double dot = playerLook.dotProduct(toBed);
        String direction;

        if (dot > 0.7) direction = "ahead";
        else if (dot < -0.7) direction = "behind";
        else {
            // Calculate left/right
            Vec3 cross = playerLook.crossProduct(toBed);
            if (cross.yCoord > 0) direction = "left";
            else direction = "right";
        }

        return direction;
    }

    private void displayDefenseInfo(BlockPos bedPos, double distance, String direction, 
                                  Map<Block, Integer> defenses, TeamInfo teamInfo) {
        // Header
        MythicalClientMod.sendMessageToChat("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false);
        
        // Show team info if detected
        if (teamInfo != null) {
            MythicalClientMod.sendMessageToChat(
                String.format("%s%s TEAM BED &7Defense Analysis %s", 
                    teamInfo.color, teamInfo.name.toUpperCase(), teamInfo.symbol),
                false
            );
        } else {
            MythicalClientMod.sendMessageToChat("&c&l[BED DEFENSE] &7Analysis", false);
        }
        
        MythicalClientMod.sendMessageToChat("&8&m┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄", false);

        // Location info
        MythicalClientMod.sendMessageToChat(
            String.format("&7Location: &f%d, %d, %d &8(&7%s&8, &f%.1f &7blocks&8)",
                bedPos.getX(), bedPos.getY(), bedPos.getZ(),
                direction, distance),
            false
        );

        // Defense analysis
        MythicalClientMod.sendMessageToChat("&7Defense Layers:", false);

        // Sort blocks by blast resistance, but put water at the end
        List<Map.Entry<Block, Integer>> sortedDefenses = new ArrayList<>(defenses.entrySet());
        sortedDefenses.sort((a, b) -> {
            DefenseInfo infoA = BLOCK_INFO.get(a.getKey());
            DefenseInfo infoB = BLOCK_INFO.get(b.getKey());
            
            // Put water at the end
            if (infoA.blastResistance == -1) return 1;
            if (infoB.blastResistance == -1) return -1;
            
            return Integer.compare(infoB.blastResistance, infoA.blastResistance);
        });

        // Display each defense type
        int totalBlocks = 0;
        int waterSources = 0;
        for (Map.Entry<Block, Integer> entry : sortedDefenses) {
            Block block = entry.getKey();
            int count = entry.getValue();
            DefenseInfo info = BLOCK_INFO.get(block);
            
            if (info.blastResistance == -1) {
                waterSources = count;
                MythicalClientMod.sendMessageToChat(
                    String.format("  %s≈ %s&7: &f%d &8source blocks",
                        info.color, info.name, count),
                    false
                );
            } else {
                totalBlocks += count;
                MythicalClientMod.sendMessageToChat(
                    String.format("  %s✦ %s&7: &fx%d &8(&7Blast: &f%d&8)",
                        info.color, info.name, count, info.blastResistance),
                    false
                );
            }
        }

        // Footer with summary
        MythicalClientMod.sendMessageToChat("&8&m┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄", false);
        if (waterSources > 0) {
            MythicalClientMod.sendMessageToChat(
                String.format("&7Total Blocks: &f%d &8| &1Water Sources: &f%d", 
                    totalBlocks, waterSources),
                false
            );
        } else {
            MythicalClientMod.sendMessageToChat(
                String.format("&7Total Defense Blocks: &f%d", totalBlocks),
                false
            );
        }
        MythicalClientMod.sendMessageToChat("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false);
    }

    private static class DefenseInfo {
        String name;
        String color;
        int blastResistance;

        DefenseInfo(String name, String color, int blastResistance) {
            this.name = name;
            this.color = color;
            this.blastResistance = blastResistance;
        }
    }

    private static class TeamInfo {
        String name;
        String color;
        String symbol;

        TeamInfo(String name, String color, String symbol) {
            this.name = name;
            this.color = color;
            this.symbol = symbol;
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }
} 