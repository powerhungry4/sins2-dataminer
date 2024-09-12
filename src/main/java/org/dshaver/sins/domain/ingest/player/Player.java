package org.dshaver.sins.domain.ingest.player;

import java.util.List;

import org.dshaver.sins.service.FileTools;

import lombok.Data;

@Data
public class Player implements FileTools.EntityClass{
    String id;
    String race;
    List<String> buildableUnits;
    List<String> factionBuildableUnits;
    List<String> buildableStrikecraft;
    List<String> structures;
    List<String> shipComponents;
    List<String> planetComponents;
    List<String> factionPlanetComponents;

    @Override
    public void extraActions(String playerId){
        this.setId(playerId);
    }
    
    @Override
    public PlayerType getSubtype() {
        return PlayerType.getType(this);
    }
}
