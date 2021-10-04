package com.jasperxu.seckill.util;

/**
 * Snowflake is an open source distributed ID generation algorithm developed by Twitter.
 * source code: https://programmerall.com/article/3819166656/
 */
public class SnowflakeIdGenerator {

    /**
     * Starting timestamp
     */
    private final static long START_STMP = 1480166465631L;

    /**
     * The number of digits occupied by each part
     */
    private final static long SEQUENCE_BIT = 12; //The number of digits occupied by the serial number
    private final static long MACHINE_BIT = 5; //The number of bits occupied by the machine ID
    private final static long DATACENTER_BIT = 5;//The number of bits occupied by the data center

    /**
     * Maximum value of each part
     */
    private final static long MAX_DATACENTER_NUM = -1L ^ (-1L << DATACENTER_BIT);
    private final static long MAX_MACHINE_NUM = -1L ^ (-1L << MACHINE_BIT);
    private final static long MAX_SEQUENCE = -1L ^ (-1L << SEQUENCE_BIT);

    /**
     * The displacement of each part to the left
     */
    private final static long MACHINE_LEFT = SEQUENCE_BIT;
    private final static long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    private final static long TIMESTMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;

    private long datacenterId; //Data center
    private long machineId; //Machine ID
    private long sequence = 0L; //Serial number
    private long lastStmp = -1L; //Last time stamp

    public SnowflakeIdGenerator(long datacenterId, long machineId) {
        if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId can't be greater than MAX_DATACENTER_NUM or less than 0");
        }
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("machineId can't be greater than MAX_MACHINE_NUM or less than 0");
        }
        this.datacenterId = datacenterId;
        this.machineId = machineId;
    }

    /**
     * Generate the next ID
     *
     * @return
     */
    public synchronized long nextId() {
        long currStmp = getNewstmp();
        if (currStmp < lastStmp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id");
        }

        if (currStmp == lastStmp) {
            //In the same millisecond, the serial number increases automatically
            sequence = (sequence + 1) & MAX_SEQUENCE;
            //The number of sequences in the same millisecond has reached the maximum
            if (sequence == 0L) {
                currStmp = getNextMill();
            }
        } else {
            //In different milliseconds, the serial number is set to 0
            sequence = 0L;
        }

        lastStmp = currStmp;

        return (currStmp-START_STMP) << TIMESTMP_LEFT //Timestamp part
                | datacenterId << DATACENTER_LEFT //Data center part
                | machineId << MACHINE_LEFT //Machine identification part
                | sequence; //Serial number part
    }

    private long getNextMill() {
        long mill = getNewstmp();
        while (mill <= lastStmp) {
            mill = getNewstmp();
        }
        return mill;
    }

    private long getNewstmp() {
        return System.currentTimeMillis();
    }

    public static void main(String[] args) {
        SnowflakeIdGenerator snowFlake = new SnowflakeIdGenerator(2, 3);
        for (int i = 0; i < 1000000; i++) {
            System.out.println(snowFlake.nextId());
        }
    }
}
