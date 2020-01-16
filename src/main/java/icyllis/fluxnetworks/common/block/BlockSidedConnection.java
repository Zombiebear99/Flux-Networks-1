package icyllis.fluxnetworks.common.block;

import com.google.common.collect.Lists;
import icyllis.fluxnetworks.common.block.state.PropertyBoolFacing;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;

import java.util.ArrayList;

public abstract class BlockSidedConnection extends BlockFluxCore {

    private static final PropertyBoolFacing NORTH = PropertyBoolFacing.create("north", Direction.NORTH);
    private static final PropertyBoolFacing EAST = PropertyBoolFacing.create("east", Direction.EAST);
    private static final PropertyBoolFacing SOUTH = PropertyBoolFacing.create("south", Direction.SOUTH);
    private static final PropertyBoolFacing WEST = PropertyBoolFacing.create("west", Direction.WEST);
    private static final PropertyBoolFacing DOWN = PropertyBoolFacing.create("down", Direction.DOWN);
    private static final PropertyBoolFacing UP = PropertyBoolFacing.create("up", Direction.UP);
    public static final ArrayList<PropertyBoolFacing> faces = Lists.newArrayList(DOWN, UP, NORTH, SOUTH, WEST, EAST);

    BlockSidedConnection() {
        setDefaultState(getStateContainer().getBaseState()
                .with(NORTH, false).with(EAST, false)
                .with(SOUTH, false).with(WEST, false)
                .with(DOWN, false).with(UP, false).with(CONNECTED, false));
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {

        return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(NORTH, SOUTH, WEST, EAST, DOWN, UP);
    }
}
