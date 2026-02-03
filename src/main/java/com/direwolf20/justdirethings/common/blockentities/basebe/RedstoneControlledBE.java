package com.direwolf20.justdirethings.common.blockentities.basebe;

import com.direwolf20.justdirethings.common.blocks.BlockBreakerT1;
import com.direwolf20.justdirethings.util.MiscHelpers;
import com.direwolf20.justdirethings.util.interfacehelpers.RedstoneControlData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface RedstoneControlledBE {
    RedstoneControlData getRedstoneControlData();

    BlockEntity getBlockEntity();

    default void setRedstoneSettings(int redstoneMode) {
        getRedstoneControlData().redstoneMode = MiscHelpers.RedstoneMode.values()[redstoneMode];
        if (getBlockEntity() instanceof BaseMachineBE baseMachineBE)
            baseMachineBE.markDirtyClient();
        BlockState blockState = getBlockEntity().getBlockState();
        if (blockState.hasProperty(BlockBreakerT1.ACTIVE)) {
            getBlockEntity().getLevel().setBlockAndUpdate(getBlockEntity().getBlockPos(), blockState.setValue(BlockBreakerT1.ACTIVE, isActiveRedstoneTestOnly()));
        }
    }

    default void evaluateRedstone() {
        RedstoneControlData data = getRedstoneControlData();
        if (!data.checkedRedstone) {
            boolean newRedstoneSignal = getBlockEntity().getLevel().hasNeighborSignal(getBlockEntity().getBlockPos());
            if (data.redstoneMode == MiscHelpers.RedstoneMode.PULSE && !data.receivingRedstone && newRedstoneSignal)
                data.pulsed = true;
            data.receivingRedstone = newRedstoneSignal;
            data.checkedRedstone = true;
            BlockState blockState = getBlockEntity().getBlockState();
            if (blockState.hasProperty(BlockBreakerT1.ACTIVE)) {
                getBlockEntity().getLevel().setBlockAndUpdate(getBlockEntity().getBlockPos(), blockState.setValue(BlockBreakerT1.ACTIVE, isActiveRedstoneTestOnly()));
            }
        }
    }

    default boolean isActiveRedstoneTestOnly() {
        RedstoneControlData data = getRedstoneControlData();
        MiscHelpers.RedstoneMode mode = data.redstoneMode;
        if (mode == MiscHelpers.RedstoneMode.IGNORED)
            return true;
        if (mode == MiscHelpers.RedstoneMode.LOW)
            return !data.receivingRedstone;
        if (mode == MiscHelpers.RedstoneMode.HIGH)
            return data.receivingRedstone;
        if (mode == MiscHelpers.RedstoneMode.PULSE && data.pulsed)
            return true;
        return false;
    }

    default boolean isActiveRedstone() {
        RedstoneControlData data = getRedstoneControlData();
        MiscHelpers.RedstoneMode mode = data.redstoneMode;
        if (mode == MiscHelpers.RedstoneMode.IGNORED)
            return true;
        if (mode == MiscHelpers.RedstoneMode.LOW)
            return !data.receivingRedstone;
        if (mode == MiscHelpers.RedstoneMode.HIGH)
            return data.receivingRedstone;
        if (mode == MiscHelpers.RedstoneMode.PULSE && data.pulsed) {
            data.pulsed = false;
            return true;
        }
        return false;
    }

    default void saveRedstoneSettings(CompoundTag tag) {
        tag.putInt("redstoneMode", getRedstoneControlData().redstoneMode.ordinal());
        tag.putBoolean("pulsed", getRedstoneControlData().pulsed);
        tag.putBoolean("receivingRedstone", getRedstoneControlData().receivingRedstone);
    }

    default void loadRedstoneSettings(CompoundTag tag) {
        if (tag.contains("redstoneMode")) { //Assume all the others are there too...
            getRedstoneControlData().redstoneMode = MiscHelpers.RedstoneMode.values()[tag.getInt("redstoneMode")];
            getRedstoneControlData().pulsed = tag.getBoolean("pulsed");
            getRedstoneControlData().receivingRedstone = tag.getBoolean("receivingRedstone");
        }
    }
}
