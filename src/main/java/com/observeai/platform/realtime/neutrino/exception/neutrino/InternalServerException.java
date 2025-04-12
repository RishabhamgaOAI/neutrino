package com.observeai.platform.realtime.neutrino.exception.neutrino;

import com.observeai.platform.realtime.neutrino.util.Constants;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InternalServerException extends NeutrinoExceptions.BaseException {
    public InternalServerException(String errorDescription) {
        super(Constants.INTERNAL_SERVER_ERROR, errorDescription);
    }
}

