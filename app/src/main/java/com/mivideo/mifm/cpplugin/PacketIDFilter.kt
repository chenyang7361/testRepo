package com.mivideo.mifm.cpplugin

import android.text.TextUtils
import com.mivideo.mifm.cpplugin.Packet
import com.mivideo.mifm.cpplugin.PacketFilter

class PacketIDFilter(private var packetId: String) : PacketFilter {

    override fun accept(packet: Packet?): Boolean {
        if (packet == null) {
            return false
        }
        if (packet.getContent() == null) {
            return false
        }
        val payloadContent = packet.getContent()
        return if (TextUtils.isEmpty(payloadContent.ch)) {
            false
        } else payloadContent.ch == this.packetId
    }

}