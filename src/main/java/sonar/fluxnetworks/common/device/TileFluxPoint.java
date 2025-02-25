package sonar.fluxnetworks.common.device;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.energy.IEnergyStorage;
import sonar.fluxnetworks.api.device.FluxDeviceType;
import sonar.fluxnetworks.api.device.IFluxPoint;
import sonar.fluxnetworks.api.energy.IFNEnergyStorage;
import sonar.fluxnetworks.common.util.FluxGuiStack;
import sonar.fluxnetworks.register.RegistryBlockEntityTypes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TileFluxPoint extends TileFluxConnector implements IFluxPoint {

    private final FluxPointHandler mHandler = new FluxPointHandler();

    @Nullable
    private EnergyStorage mEnergyCap;

    public TileFluxPoint(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(RegistryBlockEntityTypes.FLUX_POINT.get(), pos, state);
    }

    @Nonnull
    @Override
    public FluxDeviceType getDeviceType() {
        return FluxDeviceType.POINT;
    }

    @Nonnull
    @Override
    public FluxPointHandler getTransferHandler() {
        return mHandler;
    }

    @Nonnull
    @Override
    public ItemStack getDisplayStack() {
        return FluxGuiStack.FLUX_POINT;
    }

    @Override
    @SuppressWarnings("NonExtendableApiUsage")
    public void invalidateCapabilities() {
        mEnergyCap = null;
        super.invalidateCapabilities();
    }

    @Nullable
    public <T> T getEnergyCapability(BlockCapability<T, Direction> cap, @Nullable Direction side) {
        if (!isRemoved()) {
            if (mEnergyCap == null) {
                mEnergyCap = new EnergyStorage();
            }
            return (T) mEnergyCap;
        }
        return null;
    }

    private class EnergyStorage implements IEnergyStorage, IFNEnergyStorage {

        public EnergyStorage() {
        }

        ///// FORGE \\\\\

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return (int) Math.min(getEnergyStoredL(), Integer.MAX_VALUE);
        }

        @Override
        public int getMaxEnergyStored() {
            return (int) Math.min(getMaxEnergyStoredL(), Integer.MAX_VALUE);
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return false;
        }

        ///// FLUX EXTENDED \\\\\

        @Override
        public long receiveEnergyL(long maxReceive, boolean simulate) {
            return 0;
        }

        @Override
        public long extractEnergyL(long maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public long getEnergyStoredL() {
            return mHandler.getBuffer();
        }

        @Override
        public long getMaxEnergyStoredL() {
            return Math.max(mHandler.getBuffer(), mHandler.getLimit());
        }
    }
}
