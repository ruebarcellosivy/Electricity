package com.electricity.billing.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** US014 - Disconnect/Reconnect a consumer connection. */
@Data
public class ConnectionStatusUpdateRequest {

    public enum Action {
        DISCONNECT,
        RECONNECT
    }

    @NotNull(message = "Action is required.")
    private Action action;
}
