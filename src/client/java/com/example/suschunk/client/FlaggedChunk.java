package com.example.suschunk.client;

public class FlaggedChunk {
    public final int x;
    public final int z;
    public int score;
    public long time;
    public boolean clustered;

    public FlaggedChunk(int x, int z, int score, long time) {
        this.x = x;
        this.z = z;
        this.score = score;
        this.time = time;
        this.clustered = false;
    }
}