package org.dshaver.sins.domain.ingest.formation;

import org.dshaver.sins.service.FileTools;

import lombok.Data;

@Data
public class Formation implements FileTools.EntityClass{
    String id;
    String name;
    String description;
}
