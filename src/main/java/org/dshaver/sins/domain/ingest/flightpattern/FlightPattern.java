package org.dshaver.sins.domain.ingest.flightpattern;

import org.dshaver.sins.service.FileTools;

import lombok.Data;

@Data
public class FlightPattern implements FileTools.EntityClass{
    String id;
    String name;
    String description;

    @Override
    public void extraActions(String id){};
}
