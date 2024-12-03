package sonar.fluxnetworks.register;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public abstract class Channel {

    /**
     * Note: Increment this if any packet is changed.
     */
    static final String PROTOCOL = "707";
    static Channel sChannel;

    @Nonnull
    static FriendlyByteBuf buffer(int index) {
        ByteBuf buf = Unpooled.buffer();
        buf.writeShort(index);
        return new FriendlyByteBuf(buf);
    }

    public static Channel get() {
        return sChannel;
    }

    @OnlyIn(Dist.CLIENT)
    public abstract void sendToServer(@Nonnull FriendlyByteBuf payload);

    public final void sendToPlayer(@Nonnull FriendlyByteBuf payload, @Nonnull Player player) {
        sendToPlayer(payload, (ServerPlayer) player);
    }

    public abstract void sendToPlayer(@Nonnull FriendlyByteBuf payload, @Nonnull ServerPlayer player);

    public abstract void sendToAll(@Nonnull FriendlyByteBuf payload);

    public abstract void sendToTrackingChunk(@Nonnull FriendlyByteBuf payload, @Nonnull LevelChunk chunk);
}
