package com.demomq.netty;

// 模拟Pulsar的消息结构
class Message {
    private final long sequenceId;
    private final String content;

    public Message(long sequenceId, String content) {
        this.sequenceId = sequenceId;
        this.content = content;
    }

    public long getSequenceId() {
        return sequenceId;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "Message{sequenceId=" + sequenceId + ", content='" + content + "'}";
    }
}
