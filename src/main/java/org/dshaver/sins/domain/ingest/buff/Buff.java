package org.dshaver.sins.domain.ingest.buff;

import org.dshaver.sins.service.FileTools;

import lombok.Data;

@Data
public class Buff implements FileTools.EntityClass{
    String id;
    String name;
    String description;
}
