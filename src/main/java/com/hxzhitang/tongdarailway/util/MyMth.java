package com.hxzhitang.tongdarailway.util;

import com.hxzhitang.tongdarailway.railway.RegionPos;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static com.hxzhitang.tongdarailway.Tongdarailway.CHUNK_GROUP_SIZE;

public class MyMth {
    /**
     * 从区域中的区块坐标计算区块的世界坐标
     * @param regionPos 大区域坐标
     * @param chunkIndexX 区块在区域内的坐标
     * @return 区块世界坐标
     */
    public static int chunkPosXFromRegionPos(RegionPos regionPos, int chunkIndexX) {
        return regionPos.x() * CHUNK_GROUP_SIZE + chunkIndexX;
    }

    /**
     * 从区域中的区块坐标计算区块的世界坐标
     * @param regionPos 大区域坐标
     * @param chunkIndexZ 区块在区域内的坐标
     * @return 区块世界坐标
     */
    public static int chunkPosZFromRegionPos(RegionPos regionPos, int chunkIndexZ) {
        return regionPos.z() * CHUNK_GROUP_SIZE + chunkIndexZ;
    }

    /**
     * 从区块世界坐标计算其所在的区域坐标
     * @param chunkPos 区块世界坐标
     * @return 所在区域坐标
     */
    public static RegionPos regionPosFromChunkPos(ChunkPos chunkPos) {
        return new RegionPos(Math.floorDiv(chunkPos.x, CHUNK_GROUP_SIZE), Math.floorDiv(chunkPos.z, CHUNK_GROUP_SIZE));
    }

    public static ChunkPos getChunkPos(int x, int z) {
        return new ChunkPos(Math.floorDiv(x, 16), Math.floorDiv(z, 16));
    }

    public static Vec3 inRegionPos2WorldPos(RegionPos regionPos, Vec3 vec3) {
        return vec3.add(new Vec3(regionPos.x()*CHUNK_GROUP_SIZE*16, 0, regionPos.z()*CHUNK_GROUP_SIZE*16));
    }

    public static int getSign(double number) {
        return number > 0 ? 1 : (number < 0 ? -1 : 0);
    }

    /**
     * 标准化角度，标准化到0，45，90，135度
     * @param a 输入向量
     * @return 单位八向向量
     */
    // 主函数：找到最接近的8方向单位向量
    public static Vec3 get8Dir(Vec3 a) {
        // 第一步：将输入向量投影到xz平面（忽略y分量）
        Vec3 projection = new Vec3(a.x, 0, a.z);

        // 如果投影长度接近0，返回默认方向（比如朝前）
        if (Math.abs(projection.x) < 1e-9 && Math.abs(projection.z) < 1e-9) {
            return new Vec3(0, 0, 1); // 默认朝z轴正方向
        }

        // 第二步：计算投影向量在xz平面上的角度（弧度）
        double angleRad = Math.atan2(projection.x, projection.z);

        // 将弧度转换为角度（0-360度）
        double angleDeg = Math.toDegrees(angleRad);
        if (angleDeg < 0) {
            angleDeg += 360;
        }

        // 第三步：定义8个方向的角度和对应的单位向量
        double[] directions = {0.0, 45.0, 90.0, 135.0, 180.0, 225.0, 270.0, 315.0};
        List<Vec3> directionVectors = new ArrayList<>();

        // 创建8个方向的单位向量
        for (double dirAngle : directions) {
            double rad = Math.toRadians(dirAngle);
            directionVectors.add(new Vec3(Math.sin(rad), 0, Math.cos(rad)));
        }

        // 第四步：找到最接近的方向
        double minDiff = Double.MAX_VALUE;
        Vec3 closestDirection = directionVectors.get(0);

        for (int i = 0; i < directions.length; i++) {
            // 计算角度差异（考虑360度的循环特性）
            double diff = Math.abs(angleDeg - directions[i]);
            diff = Math.min(diff, 360 - diff); // 处理循环，如355度和0度只差5度

            if (diff < minDiff) {
                minDiff = diff;
                closestDirection = directionVectors.get(i);
            }
        }

        return closestDirection;
    }


    /**
     * 将向量 (vec.x, 0, vec.z) 绕y轴旋转 仅8向
     * 根据cross的值决定向左或向右旋转
     * @param vec 输入向量（y分量不会被旋转影响）
     * @param cross 叉积值，决定旋转方向
     * @param angleDeg 旋转角度（度）只为0、45、90、135度
     * @return 旋转后的Vec3
     */
    public static Vec3 rotateAroundY(Vec3 vec, double cross, double angleDeg) {
        // 标准化角度
        int angle = ((int)Math.round(angleDeg / 45.0) * 45) % 360;
        if (angle < 0) angle += 360;

        // 确定旋转角度（考虑方向）
        int rotateAngle;
        if (cross > 0) {
            rotateAngle = angle;
        } else if (cross < 0) {
            rotateAngle = -angle;
        } else {
            rotateAngle = 0;
        }

        // 旋转角度只能是0, 45, 90, 135度（根据题目）
        // 我们处理所有可能的输入和旋转组合
        double x = vec.x;
        double z = vec.z;

        // 由于是八向单位向量，我们可以直接列举所有可能性
        // 对每个可能的旋转角度进行处理
        int steps = (rotateAngle / 45) % 8;
        if (steps < 0) steps += 8;

        // 旋转steps步（每步45度）
        for (int i = 0; i < steps; i++) {
            // 旋转90度的变换：(x, z) -> (-z, x)
            // 但我们需要45度旋转，所以需要特殊处理
            // 实际上，八向单位向量的45度旋转可以直接查表
            double newX, newZ;

            // 根据当前方向确定下一步
            if (Mth.equal(x, 1) && Mth.equal(z, 0)) { newX = 0.7071067811865476; newZ = 0.7071067811865476; } // 45°: √2/2
            else if (Math.abs(x - 0.7071067811865476) < 1e-10 && Math.abs(z - 0.7071067811865476) < 1e-10) { newX = 0; newZ = 1; }
            else if (Mth.equal(x, 0) && Mth.equal(z, 1)) { newX = -0.7071067811865476; newZ = 0.7071067811865476; }
            else if (Math.abs(x + 0.7071067811865476) < 1e-10 && Math.abs(z - 0.7071067811865476) < 1e-10) { newX = -1; newZ = 0; }
            else if (Mth.equal(x, -1) && Mth.equal(z, 0)) { newX = -0.7071067811865476; newZ = -0.7071067811865476; }
            else if (Math.abs(x + 0.7071067811865476) < 1e-10 && Math.abs(z + 0.7071067811865476) < 1e-10) { newX = 0; newZ = -1; }
            else if (Mth.equal(x, 0) && Mth.equal(z, -1)) { newX = 0.7071067811865476; newZ = -0.7071067811865476; }
            else if (Math.abs(x - 0.7071067811865476) < 1e-10 && Math.abs(z + 0.7071067811865476) < 1e-10) { newX = 1; newZ = 0; }
            else {
                // 如果不是标准方向，返回原向量
                return new Vec3(x, 0, z);
            }

            x = newX;
            z = newZ;
        }

        return new Vec3(x, 0, z);
    }

    public static Vec3 getCurveStart(BlockPos pos, Vec3 axis) {
        boolean vertical = axis.y != 0;
        return VecHelper.getCenterOf(pos)
                .add(0, (vertical ? 0 : -.5f), 0)
                .add(axis.scale(.5));
    }

    public static int splitFunc(double x) {
        if (x < 0.0001 && x > -0.0001)
            return 0;
        if (x > 0)
            return 1;
        else if (x < 0)
            return -1;
        return 0;
    }

    public static Vec3i myCeil(Vec3 v) {
        int x = (int) (Mth.equal(v.x, 0) ? 0 : v.x > 0 ? Math.ceil(v.x) : Math.floor(v.x));
        int y = (int) (Mth.equal(v.y, 0) ? 0 : v.y > 0 ? Math.ceil(v.y) : Math.floor(v.y));
        int z = (int) (Mth.equal(v.z, 0) ? 0 : v.z > 0 ? Math.ceil(v.z) : Math.floor(v.z));
        return new Vec3i(x, y, z);
    }

}
