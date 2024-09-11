package org.dshaver.sins.domain.ingest.researchsubject;

import java.util.List;

import org.dshaver.sins.domain.ingest.exotic.ExoticPrice;
import org.dshaver.sins.domain.ingest.unit.Price;
import org.dshaver.sins.service.FileTools;

import lombok.Data;

@Data
public class ResearchSubject implements FileTools.EntityClass{
    String id;
    String name;
    String description;
    String domain;
    int tier;
    String field;
    Double researchTime;
    Price price;
    List<ExoticPrice> exoticPrice;

    @Override
    public void extraActions(String id){};
}
