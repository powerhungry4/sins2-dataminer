package org.dshaver.sins.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.lang3.StringUtils;
import org.dshaver.sins.domain.Manifest;
import org.dshaver.sins.domain.ingest.unit.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UnitService {
    private final String steamDir;
    private final GameFileService gameFileService;

    public UnitService(GameFileService gameFileService, String steamDir) {
        this.gameFileService = gameFileService;
        this.steamDir = steamDir;
    }

    public static String getNameProperty(String unitId) {
        return STR."\{unitId}_name";
    }

    public static String getDescriptionProperty(String unitId) {
        return STR."\{unitId}_description";
    }

    public Manifest<UnitType, Unit> loadUnitManifest() {
        Manifest<UnitType, Unit> unitManifest = FileTools.loadUnitManifest(steamDir);

        System.out.println(STR."Loaded \{unitManifest.getIds().size()} unitIds");

        // Organize by id
        Map<String, Unit> unitIdMap = unitManifest.getIds().stream()
                .map(id -> FileTools.readEntityFile(id, steamDir, Unit.class)) 
                .filter(unit -> StringUtils.isNotBlank(unit.getTargetFilterUnitType()))
                .filter(unit -> unit.getUnitType().isShip() || unit.getUnitType().isBuilding())
                .map(this::populateUnit)
                .collect(Collectors.toMap(Unit::getId, Function.identity()));

        unitManifest.setIdMap(unitIdMap);

        // Organize by type
        Multimap<UnitType, Unit> typeIndex = ArrayListMultimap.create();
        unitIdMap.values().forEach(unit -> typeIndex.put(unit.getUnitType(), unit));
        unitManifest.setTypeIndex(typeIndex);

        return unitManifest;
    }

    private Unit populateUnit(Unit unit) {
        if (unit.getWeapons() != null) {
            unit = collapseWeapons(unit);
        }

        unit.setName(gameFileService.getLocalizedTextForKey(getNameProperty(unit.getId())));
        unit.setDescription(gameFileService.getLocalizedTextForKey(getDescriptionProperty(unit.getId())));

        return unit;
    }

    private Unit collapseWeapons(Unit unit) {
        Set<String> weaponIds = unit.getWeapons().getWeapons().stream().map(Weapon::getWeapon).collect(Collectors.toSet());

        Map<String, WeaponFile> weaponMap = new HashMap<>();

        for (String weaponId : weaponIds) {
            weaponMap.put(weaponId, FileTools.readWeaponFile(steamDir, weaponId));
        }

        unit.getWeapons().getWeapons().forEach(weapon -> {
            WeaponFile weaponFile = weaponMap.get(weapon.getWeapon());
            String weaponName = gameFileService.getLocalizedTextForKey(weaponFile.getName());
            weapon.fromWeaponFile(weaponMap.get(weapon.getWeapon()), weaponName);
        });

        List<Weapon> aggregatedWeapons = new ArrayList<>();

        for (int i = 0; i < unit.getWeapons().getWeapons().size(); i++) {
            Weapon weapon = unit.getWeapons().getWeapons().get(i);
            Set<String> finishedIds = aggregatedWeapons.stream().map(Weapon::getWeapon).collect(Collectors.toSet());
            if (finishedIds.contains(weapon.getWeapon())) {
                continue;
            }

            for (int j = i + 1; i < unit.getWeapons().getWeapons().size(); j++) {
                if (j >= unit.getWeapons().getWeapons().size()) {
                    break;
                }

                Weapon testWeapon = unit.getWeapons().getWeapons().get(j);
                if (weapon.getWeapon().equals(testWeapon.getWeapon())) {
                    weapon.add(testWeapon);
                }
            }

            aggregatedWeapons.add(weapon);
        }

        unit.setWeapons(new Weapons(aggregatedWeapons));

        return unit;
    }
}
