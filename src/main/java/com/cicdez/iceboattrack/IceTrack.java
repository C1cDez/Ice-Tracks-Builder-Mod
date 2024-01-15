package com.cicdez.iceboattrack;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IceTrack {
    private final List<PlanePoint> points;
    private PlanePoint Q0;
    private final List<PlanePoint> qPoints;

    private boolean built = false;
    private int height = -1;
    private int trackRadius = -1;

    private IceTrack() {
        this.points = new ArrayList<>();
        this.Q0 = null;
        this.qPoints = new ArrayList<>();
    }

    public static final Map<String, IceTrack> TRACKS = new HashMap<>();
    public static void create(String name) {
        IceTrack track = new IceTrack();
        TRACKS.put(name, track);
    }
    public static IceTrack get(String name) {
        return TRACKS.get(name);
    }

    public void addPoint(PlanePoint point) {
        this.points.add(point);
    }
    public PlanePoint removePoint(int index) {
        return this.points.remove(index);
    }

    public int countPoints() {
        return this.points.size();
    }

    public PlanePoint getPoint(int index) {
        return this.points.get(index);
    }

    public PlanePoint getStart() {
        return getPoint(0);
    }
    public PlanePoint getEnd() {
        return getPoint(countPoints() - 1);
    }

    public void setQ0(PlanePoint Q0) {
        this.Q0 = Q0;
    }

    public void setHeight(int height) {
        this.height = height;
    }
    public int getHeight() {
        return height;
    }
    public void setTrackRadius(int trackRadius) {
        this.trackRadius = trackRadius;
    }

    public void calculateQPoints() {
        if (Q0 == null) throw new NullPointerException("Q0 mustn't be null!");
        if (countPoints() > 1) {
            int qCount = countPoints() - 1;
            qPoints.clear();
            qPoints.add(0, Q0);
            for (int i = 1; i < qCount; i++) {
                PlanePoint qi = getPoint(i).multiply(2).subtract(qPoints.get(i - 1));
                qPoints.add(i, qi);
            }
        } else throw new ArithmeticException("Count of points must be greater than 1");
    }

    public List<PlanePoint> getPoints() {
        return points;
    }
    public List<PlanePoint> getQPoints() {
        return qPoints;
    }

    public void isReady(String name) {
        if (countPoints() < 2) IceTrackCommand.dropException("Count of Points in '" + name + "' must be greater than 1");
        if (Q0 == null) IceTrackCommand.dropException("Firstly initialize Q0 (use 'setQ0') for Track '" + name + "'");
        if (height == -1) IceTrackCommand.dropException("You didn't set Y-coordinate for Building Track '" + name + "' (use 'height')");
        if (trackRadius == -1) IceTrackCommand.dropException("You didn't set Radius for Building Track '" + name + "' (use 'radius')");
    }


    public void build(Level world) {
        for (int i = 0; i < qPoints.size(); i++) {
            buildSegment(world, getPoint(i), qPoints.get(i), getPoint(i + 1));
        }
        built = true;
    }
    private void buildSegment(Level world, PlanePoint Pk, PlanePoint Qk, PlanePoint Pk_1) {
        double lengths = distance(Pk, Qk) + distance(Qk, Pk_1);
        double dt = (double) 1 / lengths;
        for (double t = 0; t < 1; t += dt) {
            PlanePoint destPoint = interpolate(Pk, Qk, Pk_1, t);
            BlockPos pos = new BlockPos((int) destPoint.x, height, (int) destPoint.y);
            buildCircleHorizontal(this, world, pos, trackRadius, Blocks.BLUE_ICE);
        }
    }

    public static void destroyCircleHorizontal(IceTrack track, Level world, BlockPos center, int radius) {
        BlockPos startPos = center.offset(-radius, 0, -radius);
        for (int x = 0; x < radius * 2 + 1; x++) {
            for (int z = 0; z < radius * 2 + 1; z++) {
                int dx = x - radius, dz = z - radius;
                if (dx * dx + dz * dz < radius * radius) {
                    BlockPos pos = new BlockPos(startPos).offset(x, 0, z);
                    world.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
    }

    public void destroy(Level world) {
        if (!built) IceTrackCommand.dropException("Track hasn't build");
        for (int i = 0; i < qPoints.size(); i++) {
            destroySegment(world, getPoint(i), qPoints.get(i), getPoint(i + 1));
        }
        built = false;
    }

    private void destroySegment(Level world, PlanePoint Pk, PlanePoint Qk, PlanePoint Pk_1) {
        double lengths = distance(Pk, Qk) + distance(Qk, Pk_1);
        double dt = (double) 1 / lengths;
        for (double t = 0; t < 1; t += dt) {
            PlanePoint destPoint = interpolate(Pk, Qk, Pk_1, t);
            destroyCircleHorizontal(this, world, new BlockPos((int) destPoint.x, height,
                    (int) destPoint.y), trackRadius);
        }
    }



    public static double distance(PlanePoint a, PlanePoint b) {
        double dx = a.x - b.x, dy = a.y - b.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
    public static PlanePoint interpolate(PlanePoint Pk, PlanePoint Qk, PlanePoint Pk_1, double t) {
        return (Pk_1.subtract(Qk.multiply(2)).add(Pk)).multiply(t * t)
                .add((Qk.subtract(Pk)).multiply(2).multiply(t))
                .add(Pk);
    }
    public static void buildCircleHorizontal(IceTrack track, Level world, BlockPos center, int radius, Block block) {
        BlockPos startPos = center.offset(-radius, 0, -radius);
        for (int x = 0; x < radius * 2 + 1; x++) {
            for (int z = 0; z < radius * 2 + 1; z++) {
                int dx = x - radius, dz = z - radius;
                if (dx * dx + dz * dz < radius * radius) {
                    BlockPos pos = new BlockPos(startPos).offset(dx, 0, dz);
                    world.setBlock(pos, block.defaultBlockState(), 2);
                }
            }
        }
    }
}
