package com.mivideo.mifm.cpplugin;

import android.compact.impl.TaskPayload;


public class Packet {
    PacketCollector pc;
    TaskPayload content;

    public Packet() {
        super();
    }

    public Packet(PacketCollector pc, TaskPayload content) {
        this();
        this.pc = pc;
        this.content = content;
    }

    public TaskPayload getContent() {
        return this.content;
    }

    public void setContent(TaskPayload payload) {
        this.content = payload;
    }

    public void onReady() {
        pc.processPacket(this);
    }
}

