package com.gamblingplugin.structures;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class RouletteWheel {

    private final GamblingPlugin plugin;
    private final Location center;
    private final String id; // Unique identifier
    private final List<ArmorStand> numbers = new ArrayList<>();
    private ArmorStand arrow;
    private ArmorStand infoStand;
    private boolean spinning = false;

    // Roulette numbers in American standard order (with 00)
    // 00 is represented as -1 internally for easy handling
    private static final int[] NUMBERS = {
            0, 28, 9, 26, 30, 11, 7, 20, 32, 17, 5, 22, 34, 15, 3, 24,
            36, 13, 1, -1, 27, 10, 25, 29, 12, 8, 19, 31, 18, 6, 21, 33,
            16, 4, 23, 35, 14, 2
    };

    public RouletteWheel(GamblingPlugin plugin, Location center, String id) {
        this.plugin = plugin;
        this.center = center.clone().add(0.5, 0, 0.5); // Center of block
        this.id = id;
    }

    public void spawn() {
        // Spawn the arrow (pointer)
        arrow = (ArmorStand) center.getWorld().spawnEntity(center.clone().add(0, 1.5, 0), EntityType.ARMOR_STAND);
        arrow.setGravity(false);
        arrow.setVisible(false);
        arrow.setSmall(true);
        arrow.getEquipment().setHelmet(new ItemStack(Material.GOLD_NUGGET)); // Represents the pointer
        arrow.setCustomName("§6▼");
        arrow.setCustomNameVisible(true);

        // Spawn info stand above arrow
        infoStand = (ArmorStand) center.getWorld().spawnEntity(center.clone().add(0, 1.8, 0), EntityType.ARMOR_STAND);
        infoStand.setGravity(false);
        infoStand.setVisible(false);
        infoStand.setSmall(true);
        infoStand.setCustomName("§6§l" + id); // Show roulette ID
        infoStand.setCustomNameVisible(true);

        // Spawn numbers in a circle with perfect spacing
        double radius = 3.5; // Increased for better spacing and visibility
        double angleStep = 2 * Math.PI / NUMBERS.length;

        for (int i = 0; i < NUMBERS.length; i++) {
            double angle = i * angleStep;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);
            Location numLoc = center.clone().add(x, 0.8, z); // Lowered to be closer to table

            // Face center for proper orientation
            numLoc.setDirection(center.toVector().subtract(numLoc.toVector()));

            // Create number ArmorStand
            ArmorStand as = (ArmorStand) center.getWorld().spawnEntity(numLoc, EntityType.ARMOR_STAND);
            as.setGravity(false);
            as.setVisible(false);
            as.setSmall(true);
            as.setCustomName(getColor(NUMBERS[i]) + getNumberDisplay(NUMBERS[i]));
            as.setCustomNameVisible(true);
            numbers.add(as);

            // Spawn colored block below number for better visibility
            Location blockLoc = center.clone().add(x, 0.3, z); // Lower block position
            ArmorStand block = (ArmorStand) center.getWorld().spawnEntity(blockLoc, EntityType.ARMOR_STAND);
            block.setGravity(false);
            block.setVisible(false);
            block.setSmall(true);
            block.getEquipment().setHelmet(getConcrete(NUMBERS[i]));
            numbers.add(block);
        }

        // Table generation removed - roulette is now floating only
        // generateTable();
    }

    private void generateTable() {
        double innerRadius = 3.4; // Just under ArmorStand radius
        double borderRadius = 3.7; // Tighter border

        int startY = center.getBlockY(); // Floor level

        for (double x = -borderRadius; x <= borderRadius; x++) {
            for (double z = -borderRadius; z <= borderRadius; z++) {
                double distSq = x * x + z * z;
                Location loc = center.clone().add(x, 0, z);
                loc.setY(startY); // Ensure we are at the floor level

                if (distSq <= innerRadius * innerRadius) {
                    // Green Concrete Center
                    loc.getBlock().setType(Material.GREEN_CONCRETE);
                } else if (distSq <= borderRadius * borderRadius) {
                    // Wood Border
                    loc.getBlock().setType(Material.DARK_OAK_PLANKS);
                }
            }
        }
    }

    public void remove() {
        if (arrow != null)
            arrow.remove();
        if (infoStand != null)
            infoStand.remove();

        for (ArmorStand as : numbers) {
            as.remove();
        }
        numbers.clear();
    }

    public void spinTo(int targetNumber, Runnable onFinish) {
        if (spinning)
            return;
        spinning = true;

        if (infoStand != null)
            infoStand.setCustomName("§e§lGirando...");

        // Reset colors before animation
        for (int i = 0; i < numbers.size(); i += 2) { // Every other is a number (interleaved with blocks)
            ArmorStand as = numbers.get(i);
            int arrayIndex = i / 2;
            if (arrayIndex < NUMBERS.length) {
                int num = NUMBERS[arrayIndex];
                as.setCustomName(getColor(num) + getNumberDisplay(num));
                // Reset block
                ArmorStand block = numbers.get(i + 1);
                block.getEquipment().setHelmet(getConcrete(num));
            }
        }

        // Calculate target angle
        int targetIndex = -1;
        for (int i = 0; i < NUMBERS.length; i++) {
            if (NUMBERS[i] == targetNumber) {
                targetIndex = i;
                break;
            }
        }

        double angleStep = 2 * Math.PI / NUMBERS.length;
        double targetAngleOffset = -(targetIndex * angleStep);

        // Wheel spins Clockwise
        int wheelRotations = 5;
        double wheelTotalRotation = (wheelRotations * 2 * Math.PI) + targetAngleOffset;

        // Virtual Ball spins Counter-Clockwise
        // We want the ball to do roughly 7 rotations relative to the world
        int ballRotations = 7;
        double ballTotalRotation = ballRotations * 2 * Math.PI;

        // Calculate ratio to ensure they finish at the exact same time
        double ballSpeedRatio = ballTotalRotation / wheelTotalRotation;

        new BukkitRunnable() {
            double currentSpeed = 0.0;
            double maxSpeed = 0.6;
            double currentWheelOffset = 0;
            double currentBallOffset = 0;
            double remainingWheel = wheelTotalRotation;
            int phase = 0;
            int lastHighlightedIndex = -1;
            int ticks = 0; // Track animation ticks

            @Override
            public void run() {
                if (remainingWheel > 0) {

                    if (phase == 0) {
                        currentSpeed += 0.03;
                        if (currentSpeed >= maxSpeed) {
                            currentSpeed = maxSpeed;
                            phase = 1;
                        }
                    } else if (phase == 1) {
                        // Start decelerating earlier (last 2.5 rotations)
                        if (remainingWheel < 5 * Math.PI) {
                            phase = 2;
                        }
                    } else if (phase == 2) {
                        currentSpeed *= 0.96;
                        if (currentSpeed < 0.005)
                            currentSpeed = 0.005; // Very slow crawl
                    }

                    double moveWheel = Math.min(currentSpeed, remainingWheel);
                    currentWheelOffset += moveWheel;
                    remainingWheel -= moveWheel;

                    // Move ball proportionally
                    double moveBall = moveWheel * ballSpeedRatio;
                    currentBallOffset -= moveBall; // Ball moves opposite (counter-clockwise)

                    RouletteWheel.this.updatePositions(currentWheelOffset);

                    // Highlight logic
                    // We need to find the number closest to the ball's position
                    // Ball position (angle) is currentBallOffset (negative)
                    // Number position (angle) is (index * angleStep) + currentWheelOffset

                    int closestIndex = -1;
                    double minDiff = Double.MAX_VALUE;

                    for (int i = 0; i < NUMBERS.length; i++) {
                        double numberAngle = (i * angleStep) + currentWheelOffset;
                        double diff = Math.abs(angleDifference(numberAngle, currentBallOffset));
                        if (diff < minDiff) {
                            minDiff = diff;
                            closestIndex = i;
                        }
                    }

                    if (closestIndex != -1 && closestIndex != lastHighlightedIndex) {
                        // Unhighlight previous
                        if (lastHighlightedIndex != -1) {
                            RouletteWheel.this.resetNumberColor(lastHighlightedIndex);
                        }
                        // Highlight new
                        RouletteWheel.this.highlightNumberByIndex(closestIndex);
                        lastHighlightedIndex = closestIndex;

                        // Add sound effect on each number pass
                        float pitch = 0.5f + (float) (currentWheelOffset / (wheelTotalRotation)) * 1.5f;
                        center.getWorld().playSound(center, Sound.BLOCK_NOTE_BLOCK_HAT, 0.3f, pitch);
                    }

                    // Add particle effects during spin
                    if (ticks % 3 == 0) {
                        center.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, center.clone().add(0, 1, 0), 2, 0.5,
                                0.5, 0.5, 0);
                    }

                } else {
                    this.cancel();
                    spinning = false;

                    // Final highlight on winner
                    if (lastHighlightedIndex != -1) {
                        RouletteWheel.this.resetNumberColor(lastHighlightedIndex);
                    }
                    RouletteWheel.this.highlightNumber(targetNumber);

                    // Winner celebration effects
                    Location winnerLoc = center.clone().add(0, 1.5, 0);
                    center.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, winnerLoc, 50, 0.5, 0.5, 0.5, 0.1);
                    center.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, winnerLoc, 30, 0.5, 0.5, 0.5, 0);
                    center.getWorld().playSound(center, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 1.2f);
                    center.getWorld().playSound(center, Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.5f);

                    if (infoStand != null) {
                        String color = RouletteWheel.this.getColor(targetNumber);
                        String colorCode = color.equals("§c") ? "§c" : (color.equals("§a") ? "§a" : "§8");
                        String colorName = color.equals("§c") ? "ROJO" : (color.equals("§a") ? "VERDE" : "NEGRO");
                        infoStand.setCustomName(colorCode + "§l" + colorName + " " + targetNumber);
                    }
                    if (onFinish != null)
                        onFinish.run();
                    ;
                }
            }

            private double angleDifference(double a1, double a2) {
                double diff = (a1 - a2 + Math.PI) % (2 * Math.PI) - Math.PI;
                return diff < -Math.PI ? diff + 2 * Math.PI : diff;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void updatePositions(double wheelOffset) {
        double radius = 3.5; // Match spawn radius
        double angleStep = 2 * Math.PI / NUMBERS.length;

        // Update Wheel positions during animation
        for (int i = 0; i < NUMBERS.length; i++) {
            double angle = (i * angleStep) + wheelOffset;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);

            Location numLoc = center.clone().add(x, 0.8, z); // Match spawn height (lowered)
            numLoc.setDirection(center.toVector().subtract(numLoc.toVector()));

            ArmorStand numAS = numbers.get(i * 2);
            numAS.teleport(numLoc);

            Location blockLoc = center.clone().add(x, 0.3, z); // Match spawn height (lowered)
            ArmorStand blockAS = numbers.get(i * 2 + 1);
            blockAS.teleport(blockLoc);
        }
    }

    private void highlightNumberByIndex(int index) {
        if (index < 0 || index >= NUMBERS.length)
            return;
        int listIndex = index * 2;
        if (listIndex >= numbers.size())
            return;

        ArmorStand as = numbers.get(listIndex);
        as.setCustomName("§e§l" + getNumberDisplay(NUMBERS[index]));
        ArmorStand block = numbers.get(listIndex + 1);
        block.getEquipment().setHelmet(new ItemStack(Material.YELLOW_CONCRETE));
    }

    private void resetNumberColor(int index) {
        if (index < 0 || index >= NUMBERS.length)
            return;
        int listIndex = index * 2;
        if (listIndex >= numbers.size())
            return;

        int number = NUMBERS[index];
        ArmorStand as = numbers.get(listIndex);
        as.setCustomName(getColor(number) + getNumberDisplay(number));
        ArmorStand block = numbers.get(listIndex + 1);
        block.getEquipment().setHelmet(getConcrete(number));
    }

    private void highlightNumber(int number) {
        for (int i = 0; i < NUMBERS.length; i++) {
            if (NUMBERS[i] == number) {
                int listIndex = i * 2;
                if (listIndex < numbers.size()) {
                    ArmorStand as = numbers.get(listIndex);
                    as.setCustomName("§e§l" + getNumberDisplay(number));
                    // Change block below to Yellow Concrete
                    ArmorStand block = numbers.get(listIndex + 1);
                    block.getEquipment().setHelmet(new ItemStack(Material.YELLOW_CONCRETE));
                }
                break;
            }
        }
    }

    private String getColor(int number) {
        if (number == 0 || number == -1) // 0 and 00 are both green
            return "§a"; // Green
        // Red numbers: 1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34,
        // 36
        // Black numbers: 2, 4, 6, 8, 10, 11, 13, 15, 17, 20, 22, 24, 26, 28, 29, 31,
        // 33, 35
        int[] red = { 1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36 };
        for (int r : red) {
            if (number == r)
                return "§c";
        }
        return "§8"; // Black (Dark Gray)
    }

    private ItemStack getConcrete(int number) {
        if (number == 0 || number == -1) // 0 and 00 are both green
            return new ItemStack(Material.LIME_CONCRETE);
        int[] red = { 1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36 };
        for (int r : red) {
            if (number == r)
                return new ItemStack(Material.RED_CONCRETE);
        }
        return new ItemStack(Material.BLACK_CONCRETE);
    }

    /**
     * Get display string for a number (handles 00 display)
     */
    private String getNumberDisplay(int number) {
        if (number == -1) {
            return "00"; // Display double zero
        }
        return String.valueOf(number);
    }

    public boolean isSpinning() {
        return spinning;
    }

    public boolean isPartOfWheel(ArmorStand stand) {
        // Check if the stand is one of our number stands, the arrow, or info stand
        return numbers.contains(stand) || stand.equals(arrow) || stand.equals(infoStand);
    }

    public String getId() {
        return id;
    }

    public Location getCenter() {
        return center.clone();
    }
}
