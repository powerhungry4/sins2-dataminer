package org.dshaver.sins.domain.ingest.ability;

import org.dshaver.sins.service.FileTools;

import lombok.Data;

@Data
public class Ability implements FileTools.EntityClass{
    String id;
    String name;
    String description;
}