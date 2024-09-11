package org.dshaver.sins.domain.ingest.npcreward;

import org.dshaver.sins.service.FileTools;

import lombok.Data;

@Data
public class NpcReward implements FileTools.EntityClass{
    String id;
    String name;
    String description;

    @Override
    public void extraActions(String id){};
}
