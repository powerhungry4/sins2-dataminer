package org.dshaver.sins.domain.ingest.weapon;

import org.dshaver.sins.service.FileTools;

import lombok.Data;

@Data
public class Weapon implements FileTools.EntityClass{
    String id;
    String name;
    String description;

    @Override
    public void extraActions(String id){};
}
