package org.dshaver.sins.service;

import java.nio.file.Path;
import java.util.Map;

import org.dshaver.sins.domain.ingest.player.Player;
import org.dshaver.sins.domain.ingest.researchsubject.ResearchSubject;
import org.dshaver.sins.domain.ingest.unit.Unit;
import org.dshaver.sins.domain.ingest.unititem.UnitItem;


public class GameFileService {
    private static final String ENTITY_DIRECTORY = "entities/";
    private static final String ENTITY_MANIFEST_EXTENSION = ".entity_manifest";

    private static final String ABILITY_MANIFEST_FILE_PATH = "entities/ability.entity_manifest";
    private static final String ACTION_DATA_SOURCE_MANIFEST_FILE_PATH = "entities/action_data_source.entity_manifest";
    private static final String BUFF_MANIFEST_FILE_PATH = "entities/buff.entity_manifest";
    private static final String EXOTIC_MANIFEST_FILE_PATH = "entities/exotic.entity_manifest";
    private static final String FLIGHT_PATTERN_MANIFEST_FILE_PATH = "entities/flight_pattern.entity_manifest";
    private static final String FORMATION_MANIFEST_FILE_PATH = "entities/formation.entity_manifest";
    private static final String NPC_REWARD_MANIFEST_FILE_PATH = "entities/npc_reward.entity_manifest";
    private static final String PLAYER_MANIFEST_FILE_PATH = "entities/player.entity_manifest";
    private static final String RESEARCH_SUBJECT_MANIFEST_FILE_PATH = "entities/research_subject.entity_manifest";
    private static final String UNIT_ITEM_MANIFEST_FILE_PATH = "entities/unit_item.entity_manifest";
    private static final String UNIT_SKIN_MANIFEST_FILE_PATH = "entities/unit_skin.entity_manifest";
    private static final String UNIT_MANIFEST_FILE_PATH = "entities/unit.entity_manifest";
    private static final String WEAPON_MANIFEST_FILE_PATH = "entities/weapon.entity_manifest";
    
    private static Map<String, String> localizedText;

    private final String steamDir;
    private final String outputDir;

    public GameFileService(String steamDir, String outputDir) {
        this.steamDir = steamDir;
        this.outputDir = outputDir;
    }

    public Map<String, String> getLocalizedText() {
        if (localizedText == null) {
            localizedText = FileTools.readLocalizedTextFile(steamDir);
        }

        return localizedText;
    }

    public String getLocalizedTextForKey(String key) {
        return getLocalizedText().get(key);
    }

    public <T extends FileTools.EntityClass> T readEntityFile(String id, Class<T> objectClass) {
        return FileTools.readEntityFile(id, steamDir, objectClass);
    }

    public Player readPlayerFile(String playerId) {
        return FileTools.readPlayerFile(steamDir, playerId);
    }

    public ResearchSubject readResearchSubjectFile(String unitItemId) {
        return FileTools.readResearchSubjectFile(steamDir, unitItemId);
    }

    public UnitItem readUnitItemFile(String unitItemId) {
        return FileTools.readUnitItemFile(steamDir, unitItemId);
    }

    public Unit readUnitFile(String unitId) {
        return FileTools.readUnitFile(steamDir, unitId);
    }

    public <T extends FileTools.EntityClass> Path getManifestPath(Class<T> objectClass) {
        String fileName = objectClass.getSimpleName().replaceAll("(\\p{Ll})(\\p{Lu})","$1_$2").toLowerCase();
        return Path.of(steamDir).resolve(ENTITY_DIRECTORY + fileName + ENTITY_MANIFEST_EXTENSION);
    }

    public Path getAbilityManifestPath() {
        return getPath(ABILITY_MANIFEST_FILE_PATH);
    }

    public Path getActionDataSourceManifestPath() {
        return getPath(ACTION_DATA_SOURCE_MANIFEST_FILE_PATH);
    }

    public Path getBuffManifestPath() {
        return getPath(BUFF_MANIFEST_FILE_PATH);
    }

    public Path getExoticManifestPath() {
        return getPath(EXOTIC_MANIFEST_FILE_PATH);
    }

    public Path getFlightPatternManifestPath() {
        return getPath(FLIGHT_PATTERN_MANIFEST_FILE_PATH);
    }

    public Path getFormationManifestPath() {
        return getPath(FORMATION_MANIFEST_FILE_PATH);
    }

    public Path getNpcRewardManifestPath() {
        return getPath(NPC_REWARD_MANIFEST_FILE_PATH);
    }

    public Path getPlayerManifestPath() {
        return getPath(PLAYER_MANIFEST_FILE_PATH);
    }

    public Path getResearchSubjectManifest() {
        return getPath(RESEARCH_SUBJECT_MANIFEST_FILE_PATH);
    }

    public Path getUnitItemManifestPath() {
        return getPath(UNIT_ITEM_MANIFEST_FILE_PATH);
    }

    public Path getUnitSkinManifestPath() {
        return getPath(UNIT_SKIN_MANIFEST_FILE_PATH);
    }

    public Path getUnitManifestPath() {
        return getPath(UNIT_MANIFEST_FILE_PATH);
    }

    public Path getWeaponManifestPath() {
        return getPath(WEAPON_MANIFEST_FILE_PATH);
    }

    public Path getPath(String filePart) {
        return Path.of(steamDir).resolve(filePart);
    }
}
