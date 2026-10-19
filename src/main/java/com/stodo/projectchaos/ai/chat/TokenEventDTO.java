package com.stodo.projectchaos.ai.chat;

// The token is wrapped in JSON, because SSE clients strip the leading space of a plain "data:" line.
public record TokenEventDTO(String text) {
}
