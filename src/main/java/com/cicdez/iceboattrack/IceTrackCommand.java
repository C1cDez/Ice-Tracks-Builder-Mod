package com.cicdez.iceboattrack;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;

public final class IceTrackCommand {
    public static final String NAME = "track";

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(NAME)
                .then(Commands.literal("new")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(IceTrackCommand::newTrack)
                        )
                )
                .then(Commands.literal("list")
                        .executes(IceTrackCommand::listTracks)
                )
                .then(Commands.literal("point")
                        .then(Commands.literal("add")
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .then(Commands.argument("point", BlockPosArgument.blockPos())
                                                .executes(IceTrackCommand::addPointToTrack)
                                        )
                                )
                        )
                        .then(Commands.literal("remove")
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .then(Commands.argument("pointIndex", IntegerArgumentType.integer(0))
                                                .executes(IceTrackCommand::removePointFromTrack)
                                        )
                                )
                        )
                        .then(Commands.literal("tpto")
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .then(Commands.argument("pointIndex", IntegerArgumentType.integer(0))
                                                .executes(IceTrackCommand::tpToPointOnTrack)
                                        )
                                )
                        )
                        .then(Commands.literal("list")
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .executes(IceTrackCommand::listPointsOfTrack)
                                )
                        )
                )
                .then(Commands.literal("height")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .then(Commands.argument("height", IntegerArgumentType.integer(0))
                                        .executes(IceTrackCommand::setTrackHeight)
                                )
                        )
                )
                .then(Commands.literal("radius")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .then(Commands.argument("radius", IntegerArgumentType.integer(1))
                                        .executes(IceTrackCommand::setTrackBuildingRadius)
                                )
                        )
                )
                .then(Commands.literal("setQ0")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .then(Commands.argument("Q0", BlockPosArgument.blockPos())
                                        .executes(IceTrackCommand::setQ0PointForTrack)
                                )
                        )
                )
                .then(Commands.literal("build")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(IceTrackCommand::buildTrack)
                        )
                )
                .then(Commands.literal("destroy")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(IceTrackCommand::destroy)
                        )
                )
        );
    }

    public static int newTrack(CommandContext<CommandSourceStack> context) throws CommandRuntimeException {
        String name = context.getArgument("name", String.class);
        IceTrack.create(name);
        print(context.getSource(), "Successful create of Track '" + name + "'");
        return 1;
    }
    public static int listTracks(CommandContext<CommandSourceStack> context) throws CommandRuntimeException {
        CommandSourceStack source = context.getSource();
        print(source, "Tracks:");
        IceTrack.TRACKS.forEach((name, track) -> print(source, name));
        return 1;
    }
    public static int addPointToTrack(CommandContext<CommandSourceStack> context) throws CommandRuntimeException {
        CommandSourceStack source = context.getSource();
        String name = context.getArgument("name", String.class);
        BlockPos blockPos = BlockPosArgument.getBlockPos(context, "point");
        IceTrack track = isTrackExists(name);
        PlanePoint point = new PlanePoint(blockPos.getX(), blockPos.getZ());
        track.addPoint(point);
        print(source, "Added point '" + point + "' to track '" + name + "'");
        return 1;
    }
    public static int removePointFromTrack(CommandContext<CommandSourceStack> context) throws CommandRuntimeException {
        CommandSourceStack source = context.getSource();
        String name = context.getArgument("name", String.class);
        int pointIndex = context.getArgument("pointIndex", Integer.class);
        IceTrack track = isTrackExists(name);
        PlanePoint removed = track.removePoint(pointIndex);
        print(source, "Removed point '" + removed + "' from track '" + name + "'");
        return 1;
    }
    public static int tpToPointOnTrack(CommandContext<CommandSourceStack> context) throws CommandRuntimeException {
        CommandSourceStack source = context.getSource();
        String name = context.getArgument("name", String.class);
        int pointIndex = context.getArgument("pointIndex", Integer.class);
        IceTrack track = isTrackExists(name);
        PlanePoint point = track.getPoint(pointIndex);
        int height = track.getHeight();
        if (source.isPlayer() && source.getPlayer() != null) {
            source.getPlayer().teleportTo(point.x, height == -1 ? 72 : height, point.y);
            print(source, "Teleported player to Point #" + pointIndex + " on Track '" + name + "'");
            return 1;
        }
        return 0;
    }
    public static int listPointsOfTrack(CommandContext<CommandSourceStack> context) throws CommandRuntimeException {
        CommandSourceStack source = context.getSource();
        String name = context.getArgument("name", String.class);
        IceTrack track = isTrackExists(name);
        for (int index = 0; index < track.countPoints(); index++) {
            print(source, "Point #" + index + ": " + track.getPoint(index).toString());
        }
        return 1;
    }
    public static int setQ0PointForTrack(CommandContext<CommandSourceStack> context) throws CommandRuntimeException {
        CommandSourceStack source = context.getSource();
        String name = context.getArgument("name", String.class);
        BlockPos Q0_ = BlockPosArgument.getBlockPos(context, "Q0");
        IceTrack track = isTrackExists(name);
        PlanePoint Q0 = new PlanePoint(Q0_.getX(), Q0_.getZ());
        track.setQ0(Q0);
        print(source, "Set Q0 Point on coordinates " + Q0 + " for Track '" + name + "'");
        return 1;
    }

    public static int setTrackHeight(CommandContext<CommandSourceStack> context) throws CommandRuntimeException {
        CommandSourceStack source = context.getSource();
        String name = context.getArgument("name", String.class);
        int height = context.getArgument("height", Integer.class);
        IceTrack track = isTrackExists(name);
        track.setHeight(height);
        print(source, "Set Y-coordinate (" + height + ") for Track '" + name + "'");
        return 1;
    }
    public static int setTrackBuildingRadius(CommandContext<CommandSourceStack> context) throws CommandRuntimeException {
        CommandSourceStack source = context.getSource();
        String name = context.getArgument("name", String.class);
        int radius = context.getArgument("radius", Integer.class);
        IceTrack track = isTrackExists(name);
        track.setTrackRadius(radius);
        print(source, "Set Track Radius (" + radius + ") for Track '" + name + "'");
        return 1;
    }

    public static int buildTrack(CommandContext<CommandSourceStack> context) throws CommandRuntimeException {
        CommandSourceStack source = context.getSource();
        String name = context.getArgument("name", String.class);
        IceTrack track = isTrackExists(name);
        track.isReady(name);

        track.calculateQPoints();
        print(source, "Calculated Q points: " + track.getQPoints());

        long before = System.currentTimeMillis();
        try {
            track.build(source.getLevel());
        } catch (Exception e) {
            dropException(e.getMessage());
        }
        long after = System.currentTimeMillis();
        print(source, "Track built in " + (after - before) + "ms");

        return 1;
    }

    public static int destroy(CommandContext<CommandSourceStack> context) throws CommandRuntimeException {
        CommandSourceStack source = context.getSource();
        String name = context.getArgument("name", String.class);
        IceTrack track = isTrackExists(name);
        track.destroy(source.getLevel());
        print(source, "Track '" + name + "' destroyed");
        return 1;
    }


    public static void print(CommandSourceStack source, String text) {
        source.sendSystemMessage(MutableComponent.create(new LiteralContents(text)));
    }
    public static IceTrack isTrackExists(String name) throws CommandRuntimeException {
        IceTrack track = IceTrack.get(name);
        if (track == null) dropException("Unable to Find Track '" + name + "'. Use 'new' to Create new Track");
        return track;
    }
    public static void dropException(String text) throws CommandRuntimeException {
        throw new CommandRuntimeException(MutableComponent.create(new LiteralContents(text)));
    }
}
