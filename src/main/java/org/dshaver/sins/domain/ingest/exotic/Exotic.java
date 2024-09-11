package org.dshaver.sins.domain.ingest.exotic;

import org.dshaver.sins.service.FileTools;

import lombok.Data;

@Data
public class Exotic implements FileTools.EntityClass{
    String id;
    String name;
    String description;
    Integer ai_trade_value;

    @Override
    public void extraActions(String id){};
}
