package com.project.collaborativeauthenticationapplication.service.network.messages;

import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;

public class SliceMessage extends AbstractMessage {

    private final BigNumber slice;

    public SliceMessage(BigNumber slice) {
        this.slice = slice;
    }

    public BigNumber getSlice() {
        return slice;
    }
}
