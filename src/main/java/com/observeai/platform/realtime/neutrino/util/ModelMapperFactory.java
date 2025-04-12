package com.observeai.platform.realtime.neutrino.util;

import org.modelmapper.ModelMapper;

import java.util.Objects;

public class ModelMapperFactory {
    private static final ModelMapper mapper;

    static {
        mapper = new CustomModelMapper();
        mapper.getConfiguration().setAmbiguityIgnored(true);
    }

    public static class CustomModelMapper extends ModelMapper {
        @Override
        public <D> D map(Object source, Class<D> destinationType) {
            if (Objects.isNull(source))
                return null;
            return super.map(source, destinationType);
        }
    }

    public static ModelMapper getModelMapper() {
        return mapper;
    }
}
