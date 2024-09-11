package org.dshaver.sins.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.commons.io.FileUtils.writeStringToFile;
import org.apache.commons.lang3.StringUtils;
import org.dshaver.sins.domain.Manifest;
import org.dshaver.sins.domain.export.WikiPlanetItem;
import org.dshaver.sins.domain.export.WikiStructure;
import org.dshaver.sins.domain.export.WikiUnit;
import org.dshaver.sins.domain.ingest.ManifestFile;
import org.dshaver.sins.domain.ingest.player.Player;
import org.dshaver.sins.domain.ingest.researchsubject.ResearchSubject;
import org.dshaver.sins.domain.ingest.unit.Unit;
import org.dshaver.sins.domain.ingest.unit.UnitType;
import org.dshaver.sins.domain.ingest.unit.WeaponFile;
import org.dshaver.sins.domain.ingest.unititem.UnitItem;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.MapType;

public class FileTools {

    private static final String ENTITY_DIR = "entities";
    private static final String UNIT_MANIFEST_FILE_PATH = "entities/unit.entity_manifest";
    private static final String LOCALIZED_TEXT_FILE_PATH = "localized_text/en.localized_text";
    private static final String UNIT_JSON_OUTPUT_NAME = "SoaSE2_units.json";
    private static final String STRUCTURE_JSON_OUTPUT_NAME = "SoaSE2_structures.json";
    private static final String PLANET_UPGRADE_OUTPUT_NAME = "SoaSE2_planet_items.json";
    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public static Path getTargetDir(String relativeOutputDir) {
        return Paths.get("").resolve(relativeOutputDir);
    }

    public static Path makeTargetDir(String outputDir) {
        Path targetDir = getTargetDir(outputDir);
        targetDir.toFile().mkdirs();

        return targetDir;
    }

    /**
     * Probably not going to use this anymore. Initial stab at generating the wiki syntax here. Pivoted to just exporting
     * json and have the wiki read the json via lua module.
     */
    @Deprecated
    public static void writeInitialWikiFiles(String outputDir, List<Unit> units) {
        Path targetDir = makeTargetDir(outputDir);
        units.forEach(unit -> {
            try {
                Path unitPath = targetDir.resolve(unit.getId() + ".txt");
                writeStringToFile(Files.createFile(unitPath).toFile(), unit.toString(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static boolean validSteamDir(String steamDir) {
        Path path = getPath(steamDir, LOCALIZED_TEXT_FILE_PATH);
        boolean exists = path.toFile().exists();

        if (!exists) {
            System.out.println(STR."Could not find file \{path}!");
        }

        return exists;
    }

    public static Map<String, String> readLocalizedTextFile(String steamDir) {
        Path path = getPath(steamDir, LOCALIZED_TEXT_FILE_PATH);

        try (InputStream localizedTextInput = Files.newInputStream(path)) {
            MapType typeReference = objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, String.class);

            return objectMapper.readValue(localizedTextInput, typeReference);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public interface EntityClass{
        public void extraActions(String id);
    }

    public static <T extends EntityClass> T readEntityFile(String steamDir, String id, Class<T> objectClass) {
        String extension = objectClass.getSimpleName().replaceAll("(\\p{Ll})(\\p{Lu})","$1_$2").toLowerCase();
        Path filePath = getEntityPath(steamDir, STR."\{id}.\{extension}");
        System.out.println(STR."Reading player file \{filePath}");

        try (InputStream is = Files.newInputStream(filePath)) {
            T instance = objectMapper.readValue(is, objectClass);
            
            instance.extraActions(id);

            return instance;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Unit readUnitFile(String steamDir, String unitId) {
        var unitPath = getEntityPath(steamDir, STR."\{unitId}.unit");
        System.out.println(STR."Reading unit file \{unitPath}");

        try (InputStream is = Files.newInputStream(unitPath)) {
            Unit unit = objectMapper.readValue(is, Unit.class);
            unit.setId(unitId);
            unit.findRace();
            unit.findFaction();

            if (StringUtils.isNotBlank(unit.getTargetFilterUnitType())) {
                unit.setUnitType(UnitType.valueOf(unit.getTargetFilterUnitType()));
            }

            return unit;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Player readPlayerFile(String steamDir, String playerId) {
        var playerPath = getEntityPath(steamDir, STR."\{playerId}.player");
        System.out.println(STR."Reading player file \{playerPath}");

        try (InputStream is = Files.newInputStream(playerPath)) {
            Player player = objectMapper.readValue(is, Player.class);

            player.setId(playerId);

            return player;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static WeaponFile readWeaponFile(String steamDir, String weaponId) {
        var weaponPath = getEntityPath(steamDir, STR."\{weaponId}.weapon");
        System.out.println(STR."Reading weapon file \{weaponId}");

        try (InputStream is = Files.newInputStream(weaponPath)) {
            WeaponFile weaponFile = FileTools.getObjectMapper().readValue(is, WeaponFile.class);

            return weaponFile;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static UnitItem readUnitItemFile(String steamDir, String unitItemId) {
        var unitItemPath = getEntityPath(steamDir, STR."\{unitItemId}.unit_item");
        System.out.println(STR."Reading unitItem file \{unitItemPath}");

        try (InputStream is = Files.newInputStream(unitItemPath)) {
            UnitItem unitItem = objectMapper.readValue(is, UnitItem.class);

            unitItem.setId(unitItemId);

            return unitItem;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ResearchSubject readResearchSubjectFile(String steamDir, String researchSubjectId) {
        var path = getEntityPath(steamDir, STR."\{researchSubjectId}.research_subject");
        System.out.println(STR."Reading research_subject file \{path}");

        try (InputStream is = Files.newInputStream(path)) {
            ResearchSubject researchSubject = objectMapper.readValue(is, ResearchSubject.class);

            researchSubject.setId(researchSubjectId);

            return researchSubject;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeUnitsJsonFile(String outputDir, Collection<Unit> units) {
        Path targetDir = makeTargetDir(outputDir);

        Map<String, WikiUnit> allUnitMap = getAllWikiUnits(units);
        Path allUnitsJsonPath = targetDir.resolve(UNIT_JSON_OUTPUT_NAME);

        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(allUnitsJsonPath.toFile(), allUnitMap);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeStructuresJsonFile(String outputDir, Collection<Unit> structures) {
        Path targetDir = makeTargetDir(outputDir);

        Map<String, WikiStructure> allStructuresMap = getAllWikiStructures(structures);
        Path allUnitsJsonPath = targetDir.resolve(STRUCTURE_JSON_OUTPUT_NAME);

        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(allUnitsJsonPath.toFile(), allStructuresMap);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, WikiUnit> getAllWikiUnits(Collection<Unit> units) {
        return units.stream()
                .filter(unit -> unit.getUnitType().isShip())
                .map(WikiUnit::new)
                .collect(Collectors.toMap(FileTools::unitKeyMapper, Function.identity()));
    }


    public static Map<String, WikiStructure> getAllWikiStructures(Collection<Unit> units) {
        return units.stream()
                .filter(unit -> unit.getUnitType().isBuilding())
                .map(WikiStructure::new)
                .collect(Collectors.toMap(FileTools::structureKeyMapper, Function.identity()));
    }

    private static String structureKeyMapper(WikiStructure wikiStructure) {
        List<String> keyComponents = new ArrayList<>();

        if (StringUtils.isNotBlank(wikiStructure.getRace())) {
            keyComponents.add(wikiStructure.getRace());
        }

        if (StringUtils.isNotBlank(wikiStructure.getFaction())) {
            keyComponents.add(wikiStructure.getFaction());
        }

        keyComponents.add(wikiStructure.getName());

        return String.join(" ", keyComponents);
    }

    private static String unitKeyMapper(WikiUnit wikiUnit) {
        List<String> keyComponents = new ArrayList<>();
        if (StringUtils.isNotBlank(wikiUnit.getRace())) {
            keyComponents.add(wikiUnit.getRace());
        }

        keyComponents.add(wikiUnit.getName());

        return String.join(" ", keyComponents);
    }

    public static void writePlanetItemsJsonFile(String outputDir, Collection<UnitItem> unitItems) {
        Path targetDir = makeTargetDir(outputDir);

        Map<String, WikiPlanetItem> allUnitItemMap = getAllWikiPlanetUpgrades(unitItems);
        Path allUnitsJsonPath = targetDir.resolve(PLANET_UPGRADE_OUTPUT_NAME);

        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(allUnitsJsonPath.toFile(), allUnitItemMap);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, WikiPlanetItem> getAllWikiPlanetUpgrades(Collection<UnitItem> unitItems) {
        return unitItems.stream()
                .map(WikiPlanetItem::new)
                .collect(Collectors.toMap(FileTools::planetUpgradeKeyMapper, Function.identity()));
    }

    private static String planetUpgradeKeyMapper(WikiPlanetItem wikiPlanetItem) {
        List<String> keyComponents = new ArrayList<>();
        if (wikiPlanetItem.getRace() != null) {
            keyComponents.add(wikiPlanetItem.getRace());
        }

        if (wikiPlanetItem.getFaction() != null) {
            keyComponents.add(wikiPlanetItem.getFaction());
        }

        keyComponents.add(wikiPlanetItem.getName());

        return String.join(" ", keyComponents);
    }

    public static Path getEntityPath(String steamDir, String filename) {
        return Path.of(steamDir).resolve(ENTITY_DIR).resolve(filename);
    }

    public static Path getPath(String steamDir, String filePart) {
        return Path.of(steamDir).resolve(filePart);
    }

    public static <T, U> Manifest<T, U> loadManifest(Manifest<T, U> manifest, Path pathToManifest) {
        System.out.println("Reading manifest " + pathToManifest.toAbsolutePath());

        try (InputStream is = Files.newInputStream(pathToManifest)) {
            ManifestFile manifestFile = objectMapper.readValue(is, ManifestFile.class);
            manifest.setIds(manifestFile.getIds());

            return manifest;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Manifest<UnitType, Unit> loadUnitManifest(String steamDir) {
        System.out.println("Reading unit manifest");
        Path path = getPath(steamDir, UNIT_MANIFEST_FILE_PATH);

        try (InputStream is = Files.newInputStream(path)) {
            ManifestFile manifestFile = objectMapper.readValue(is, ManifestFile.class);
            Manifest<UnitType, Unit> manifest = new Manifest<>();
            manifest.setIds(manifestFile.getIds());

            return manifest;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
