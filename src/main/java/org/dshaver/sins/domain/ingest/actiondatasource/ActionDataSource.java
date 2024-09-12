package org.dshaver.sins.domain.ingest.actiondatasource;

import org.dshaver.sins.service.FileTools;

import lombok.Data;

@Data
public class ActionDataSource implements FileTools.EntityClass{
    String id;
    String name;
    String description;
}
