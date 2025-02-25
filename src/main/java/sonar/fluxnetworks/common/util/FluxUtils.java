package sonar.fluxnetworks.common.util;

import net.minecraft.ChatFormatting;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import sonar.fluxnetworks.api.FluxTranslate;
import sonar.fluxnetworks.api.device.FluxDeviceType;
import sonar.fluxnetworks.api.device.IFluxDevice;
import sonar.fluxnetworks.api.energy.EnergyType;
import sonar.fluxnetworks.common.data.FluxPlayerData;
import sonar.fluxnetworks.common.connection.FluxNetwork;
import sonar.fluxnetworks.register.DataAttachments;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;

@ParametersAreNonnullByDefault
public class FluxUtils {

    private static final double[] COMPACT_SCALE = new double[]{0.001D, 0.000_001D, 0.000_000_001D, 0.000_000_000_001D,
            0.000_000_000_000_001D, 0.000_000_000_000_000_001D};

    /**
     * A read-only array avoided new object creation.
     */
    public static final Direction[] DIRECTIONS = Direction.values();

    private FluxUtils() {
    }

    @Nonnull
    public static <E extends Enum<E>> E cycle(@Nonnull E val, @Nonnull E[] values) {
        int next = val.ordinal() + 1;
        if (next < values.length) {
            return values[next];
        }
        return values[0];
    }

    /**
     * Returns the direction in which the target is adjacent to the base.
     *
     * @param base   the base pos
     * @param target the target pos
     * @return the direction, or null if not adjacent
     */
    @Nullable
    public static Direction getBlockDirection(@Nonnull BlockPos base, @Nonnull BlockPos target) {
        if (base.equals(target)) {
            return null;
        }
        var test = new BlockPos.MutableBlockPos();
        for (var dir : DIRECTIONS) {
            test.set(base);
            if (test.move(dir).equals(target)) {
                return dir;
            }
        }
        return null;
    }

    @Nonnull
    public static String getTransferInfo(@Nonnull IFluxDevice flux, EnergyType energyType) {
        FluxDeviceType type = flux.getDeviceType();
        long change = flux.getTransferChange();
        if (type.isPlug()) {
            if (change == 0) {
                return FluxTranslate.INPUT.get() + ": " + ChatFormatting.GOLD + "0 " + energyType.getUsageSuffix();
            } else {
                return FluxTranslate.INPUT.get() + ": " + ChatFormatting.GREEN + "+" + energyType.getUsage(change);
            }
        }
        if (type.isPoint() || type.isController()) {
            if (change == 0) {
                return FluxTranslate.OUTPUT.get() + ": " + ChatFormatting.GOLD + "0 " + energyType.getUsageSuffix();
            } else {
                return FluxTranslate.OUTPUT.get() + ": " + ChatFormatting.RED + energyType.getUsage(change);
            }
        }
        if (type.isStorage()) {
            if (change == 0) {
                return FluxTranslate.CHANGE.get() + ": " + ChatFormatting.GOLD + "0 " + energyType.getUsageSuffix();
            } else if (change > 0) {
                return FluxTranslate.CHANGE.get() + ": " + ChatFormatting.GREEN + "+" + energyType.getUsage(change);
            } else {
                return FluxTranslate.CHANGE.get() + ": " + ChatFormatting.RED + energyType.getUsage(change);
            }
        }
        return "";
    }

    /*public static int getPlayerXP(EntityPlayer player) {
        return (int) (getExperienceForLevel(player.experienceLevel) + (player.experience * player.xpBarCap()));
    }

    public static void addPlayerXP(EntityPlayer player, int amount) {
        int experience = getPlayerXP(player) + amount;
        player.experienceTotal = experience;
        player.experienceLevel = getLevelForExperience(experience);
        int expForLevel = getExperienceForLevel(player.experienceLevel);
        player.experience = (float) (experience - expForLevel) / (float) player.xpBarCap();
    }

    public static boolean removePlayerXP(EntityPlayer player, int amount) {
        if(getPlayerXP(player) >= amount) {
            addPlayerXP(player, -amount);
            return true;
        }
        return false;
    }

    public static int xpBarCap(int level) {
        if (level >= 30)
            return 112 + (level - 30) * 9;

        if (level >= 15)
            return 37 + (level - 15) * 5;

        return 7 + level * 2;
    }

    private static int sum(int n, int a0, int d) {
        return n * (2 * a0 + (n - 1) * d) / 2;
    }

    public static int getExperienceForLevel(int level) {
        if (level == 0) return 0;
        if (level <= 15) return sum(level, 7, 2);
        if (level <= 30) return 315 + sum(level - 15, 37, 5);
        return 1395 + sum(level - 30, 112, 9);
    }

    public static int getLevelForExperience(int targetXp) {
        int level = 0;
        while (true) {
            final int xpToNextLevel = xpBarCap(level);
            if (targetXp < xpToNextLevel) return level;
            level++;
            targetXp -= xpToNextLevel;
        }
    }*/

    public static void writeGlobalPos(@Nonnull CompoundTag tag, @Nonnull GlobalPos pos) {
        BlockPos p = pos.pos();
        tag.putInt("x", p.getX());
        tag.putInt("y", p.getY());
        tag.putInt("z", p.getZ());
        tag.putString("dim", pos.dimension().location().toString());
    }

    @Nonnull
    public static GlobalPos readGlobalPos(@Nonnull CompoundTag tag) {
        return GlobalPos.of(ResourceKey.create(Registries.DIMENSION,
                        ResourceLocation.parse(tag.getString("dim"))),
                new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z")));
    }

    public static void writeGlobalPos(@Nonnull FriendlyByteBuf buffer, @Nonnull GlobalPos pos) {
        buffer.writeResourceLocation(pos.dimension().location());
        buffer.writeBlockPos(pos.pos());
    }

    @Nonnull
    public static GlobalPos readGlobalPos(@Nonnull FriendlyByteBuf buffer) {
        return GlobalPos.of(ResourceKey.create(Registries.DIMENSION,
                buffer.readResourceLocation()), buffer.readBlockPos());
    }

    @Nonnull
    public static String getDisplayPos(@Nonnull GlobalPos pos) {
        BlockPos p = pos.pos();
        return "X: " + p.getX() + " Y: " + p.getY() + " Z: " + p.getZ();
    }

    @Nonnull
    public static String getDisplayDim(@Nonnull GlobalPos pos) {
        return pos.dimension().location().toString();
    }

    public static <T> boolean addWithCheck(@Nonnull Collection<T> list, @Nullable T toAdd) {
        if (toAdd != null && !list.contains(toAdd)) {
            list.add(toAdd);
            return true;
        }
        return false;
    }

    /*@Nonnull
    public static ItemStack createItemStackFromBlock(@Nullable World world, BlockPos pos) {
        if (world == null) {
            return new ItemStack(null);
        }
        BlockState state = world.getBlockState(pos);
        return new ItemStack(state.getBlock().asItem());
    }*/

    /*public static boolean addConnection(@Nonnull IFluxDevice fluxDevice) {
        if (fluxDevice.getNetworkID() != -1) {
            IFluxNetwork network = FluxNetworkCache.INSTANCE.getNetwork(fluxDevice.getNetworkID());
            if (network.isValid()) {
                if (fluxDevice.getDeviceType().isController() && network.getConnections(FluxLogicType.CONTROLLER)
                .size() > 0) {
                    return false;
                }
                network.enqueueConnectionAddition(fluxDevice);
                return true;
            }
        }
        return false;
    }

    public static void removeConnection(@Nonnull IFluxDevice fluxDevice, boolean isChunkUnload) {
        if (fluxDevice.getNetworkID() != -1) {
            IFluxNetwork network = FluxNetworkCache.INSTANCE.getNetwork(fluxDevice.getNetworkID());
            if (network.isValid()) {
                network.enqueueConnectionRemoval(fluxDevice, isChunkUnload);
            }
        }
    }*/

    /*public static int getIntFromColor(int red, int green, int blue) {
        red = red << 16 & 0x00FF0000;
        green = green << 8 & 0x0000FF00;
        blue = blue & 0x000000FF;

        return 0xFF000000 | red | green | blue;
    }*/

    /**
     * Get a brighter color in HSV color model, from an RGB color.
     * With higher saturation and higher value.
     *
     * @param color an RGB color
     * @return a brighter RGB color
     */
    public static int getModifiedColor(int color, float factor) {
        int r = (color >> 16) & 0xff;
        int g = (color >> 8) & 0xff;
        int b = color & 0xff;

        int max = Math.max(r, Math.max(g, b));
        int min = Math.min(r, Math.min(g, b));

        int delta = max - min;

        if (delta == 0) {
            return Mth.hsvToRgb(0, 0, Math.min(factor * max / 255.0f, 1.0f));
        }

        float h;

        if (max == r) {
            h = (float) (g - b) / delta;
            if (h < 0.0f) {
                h += 6.0f;
            }
        } else if (max == g) {
            h = 2.0f + (float) (b - r) / delta;
        } else {
            h = 4.0f + (float) (r - g) / delta;
        }

        return Mth.hsvToRgb(h / 6.0f, Math.min(factor * delta / max, 1.0f), Math.min(factor * max / 255.0f, 1.0f));
    }

    /**
     * Compact format (like 3.5M)
     *
     * @param in value in
     * @return compact string
     */
    public static String compact(long in) {
        if (in < 1000) {
            return Long.toString(in);
        }
        int level = (int) (Math.log10(in) / 3) - 1;
        char pre = "kMGTPE".charAt(level);
        return String.format("%.1f%c", in * COMPACT_SCALE[level], pre);
    }

    public static String compact(long in, String suffix) {
        if (in < 1000) {
            return in + " " + suffix;
        }
        int level = (int) (Math.log10(in) / 3) - 1;
        char pre = "kMGTPE".charAt(level);
        return String.format("%.1f %c%s", in * COMPACT_SCALE[level], pre, suffix);
    }

    /*public static String format(long in, NumberFormatType style, EnergyType energy, boolean usage) {
        return compact(energy == EnergyType.EU ? in >> 2 : in, style, " " + (usage ? energy.getUsageSuffix() : energy
        .getStorageSuffix()));
    }*/

    public static boolean isBadNetworkName(@Nonnull String s) {
        return s.isEmpty() || s.length() > FluxNetwork.MAX_NETWORK_NAME_LENGTH;
    }

    public static boolean isBadPassword(@Nonnull String s) {
        if (s.isEmpty() || s.length() > FluxNetwork.MAX_PASSWORD_LENGTH) {
            return true;
        }
        for (int i = 0, e = s.length(); i < e; i++) {
            if (isBadPasswordChar(s.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if it's an invalid BMP character used with network passwords.
     *
     * @param c the char
     * @return invalid or not
     */
    public static boolean isBadPasswordChar(char c) {
        return c >= 0x7f || c <= 0x20;
    }

    /*@Nullable
    public static <T> T getCap(@Nonnull PlayerEntity player, Capability<T> capability) {
        return cap(player.getCapability(capability));
    }*/

//    @Nullable
//    @SuppressWarnings("ConstantConditions")
//    public static <T> T get(@Nonnull ICapabilityProvider provider, @Nonnull Capability<T> cap) {
//        return provider.getCapability(cap).orElse(null);
//    }

    @Nonnull
    public static FluxPlayerData getPlayerData(ServerPlayer player) {
        return player.getData(DataAttachments.PLAYER_DATA);
    }

//    @Nullable
//    @SuppressWarnings("ConstantConditions")
//    public static <T> T get(@Nonnull ICapabilityProvider provider, @Nonnull Capability<T> cap, Direction side) {
//        return provider.getCapability(cap, side).orElse(null);
//    }

    @Nullable
    public static <T> T get(@Nonnull BlockEntity target, @Nonnull BlockCapability<T, Direction> cap, Direction side) {
        return cap.getCapability(target.getLevel(), target.getBlockPos(), target.getBlockState(), target, side);
    }

    @Nullable
    public static <T> T get(@Nonnull ItemStack stack, ItemCapability<T, Void> cap) {
        return stack.getCapability(cap);
    }

    public static float getRed(int color) {
        return (float) (color >> 16 & 255) / 255.0F;
    }

    public static float getGreen(int color) {
        return (float) (color >> 8 & 255) / 255.0F;
    }

    public static float getBlue(int color) {
        return (float) (color & 255) / 255.0F;
    }

    /*public static CompoundNBT copyConfiguration(TileFluxDevice flux, CompoundNBT config) {
        for (FluxConfigurationType type : FluxConfigurationType.VALUES) {
            type.copy.copyFromTile(config, type.getNBTKey(), flux);
        }
        return config;
    }

    public static void pasteConfiguration(TileFluxDevice flux, CompoundNBT config) {
        for (FluxConfigurationType type : FluxConfigurationType.VALUES) {
            if (config.contains(type.getNBTKey())) {
                type.paste.pasteToTile(config, type.getNBTKey(), flux);
            }
        }
    }*/

    /*public static CompoundNBT getBatchEditingTag(FluxTextWidget a, FluxTextWidget b, FluxTextWidget c,
    SlidedSwitchButton d, SlidedSwitchButton e, SlidedSwitchButton f) {
        CompoundNBT tag = new CompoundNBT();
        tag.putString(ItemFluxDevice.CUSTOM_NAME, a.getText());
        tag.putInt(ItemFluxDevice.PRIORITY, b.getIntegerFromText(false));
        tag.putLong(ItemFluxDevice.LIMIT, c.getLongFromText(true));
        tag.putBoolean(ItemFluxDevice.SURGE_MODE, d != null && d.toggled);
        tag.putBoolean(ItemFluxDevice.DISABLE_LIMIT, e != null && e.toggled);
        tag.putBoolean("chunkLoad", f != null && f.toggled);
        return tag;
    }*/
}
