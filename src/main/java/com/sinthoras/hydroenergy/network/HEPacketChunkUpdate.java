package com.sinthoras.hydroenergy.network;

import com.sinthoras.hydroenergy.HEUtil;
import com.sinthoras.hydroenergy.client.light.HELightSMPHooks;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

public class HEPacketChunkUpdate implements IMessage {

    ByteBuf transmissionBuffer;
    private short flagsChunkY;
    private int chunkX;
    private int chunkZ;
    private ExtendedBlockStorage[] receivedChunk = null;

    public HEPacketChunkUpdate() {

    }

    public HEPacketChunkUpdate(Chunk chunk, short flagsChunkY) {
        transmissionBuffer = Unpooled.buffer();
        transmissionBuffer.writeShort(flagsChunkY);
        transmissionBuffer.writeInt(chunk.xPosition);
        transmissionBuffer.writeInt(chunk.zPosition);
        ExtendedBlockStorage[] blockStorages = chunk.getBlockStorageArray();
        for(int chunkY=0;chunkY<blockStorages.length;chunkY++) {
            if((flagsChunkY & HEUtil.chunkYToFlag(chunkY)) > 0) {
                ExtendedBlockStorage subChunk = blockStorages[chunkY];

                byte[] lsb = subChunk.getBlockLSBArray();
                transmissionBuffer.writeBytes(lsb);

                NibbleArray msbArray = subChunk.getBlockMSBArray();
                if(msbArray != null) {
                    transmissionBuffer.writeInt(msbArray.data.length);
                    transmissionBuffer.writeBytes(msbArray.data);
                }
                else {
                    transmissionBuffer.writeInt(0);
                }

                byte[] metadata = subChunk.getMetadataArray().data;
                transmissionBuffer.writeBytes(metadata);
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBytes(transmissionBuffer);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        flagsChunkY = buf.readShort();
        chunkX = buf.readInt();
        chunkZ = buf.readInt();
        receivedChunk = new ExtendedBlockStorage[16];
        for(int chunkY=0;chunkY<16;chunkY++) {
            if((flagsChunkY & HEUtil.chunkYToFlag(chunkY)) > 0) {
                ExtendedBlockStorage subChunk = new ExtendedBlockStorage(chunkY << 4, false);

                byte[] lsb = buf.readBytes(4096).array();
                subChunk.setBlockLSBArray(lsb);

                int msbLength = buf.readInt();
                if(msbLength != 0) {
                    byte[] msb = buf.readBytes(msbLength).array();
                    subChunk.setBlockMSBArray(new NibbleArray(msb, 4));
                }

                byte[] metadata = buf.readBytes(2048).array();
                subChunk.setBlockMetadataArray(new NibbleArray(metadata, 4));

                receivedChunk[chunkY] = subChunk;
            }
        }
    }

    public static class Handler implements IMessageHandler<HEPacketChunkUpdate, IMessage> {

        @Override
        public IMessage onMessage(HEPacketChunkUpdate message, MessageContext ctx) {
            World world = Minecraft.getMinecraft().theWorld;
            Chunk chunk = world.getChunkFromChunkCoords(message.chunkX, message.chunkZ);
            ExtendedBlockStorage[] chunkStorage = chunk.getBlockStorageArray();
            for(int chunkY=0;chunkY<16;chunkY++) {
                if((message.flagsChunkY & HEUtil.chunkYToFlag(chunkY)) > 0) {
                    if(chunkStorage[chunkY] == null) {
                        chunkStorage[chunkY] = new ExtendedBlockStorage(chunkY << 4, !world.provider.hasNoSky);
                    }
                    chunkStorage[chunkY].setBlockLSBArray(message.receivedChunk[chunkY].getBlockLSBArray());
                    chunkStorage[chunkY].setBlockMSBArray(message.receivedChunk[chunkY].getBlockMSBArray());
                    chunkStorage[chunkY].setBlockMetadataArray(message.receivedChunk[chunkY].getMetadataArray());

                    HELightSMPHooks.onChunkDataLoad(chunk);
                }
            }
            return null;
        }
    }
}
