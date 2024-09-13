package org.dshaver.sins.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.dshaver.sins.domain.Manifest;
import org.dshaver.sins.domain.ingest.unititem.UnitItem;
import org.dshaver.sins.domain.ingest.unititem.UnitItemType;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class ManifestService {

    public final GameFileService gameFileService;
    public Map<String, Manifest> manifestMap;

    public ManifestService(GameFileService gameFileService) {
        this.gameFileService = gameFileService;
        this.manifestMap = new <String, Manifest> HashMap();
    }

    public Map<String, String> getLocalizedText() {
        return gameFileService.getLocalizedText();
    }

    public <CT, T extends FileTools.EntityClass> Manifest<CT, T> getManifest(Class<T> entityClassU) {
        Manifest<CT, T> entityManifest = manifestMap.get(entityClassU.getName());
        if (entityManifest == null) {
            entityManifest = loadManifest(entityClassU);
            manifestMap.put(entityClassU.getName(), entityManifest);
        }

        return entityManifest;
    }

    public <T extends FileTools.EntityClass> Optional<T> getId(String id, Class<T> entityClassU) {
        return getManifest(entityClassU).getById(id);
    }

    // T represents a class for which there are entity files with a manifest
    // CT represents a subtype of T (often an enum, possibly a string; default to null)
    private <CT, T extends FileTools.EntityClass> Manifest<CT, T> loadManifest(Class<T> entityClassU) {
        Manifest<CT, T> manifest = FileTools.loadManifest(new Manifest<>(), gameFileService.getManifestPath(entityClassU));

        System.out.println(STR."Loaded \{manifest.getIds().size()} \{entityClassU.getSimpleName()} ids");

        // Load entity files
        Map<String, T> idMap = manifest.getIds().stream()
                .map((String id) -> (T)(gameFileService.readEntityFile(id, entityClassU)))
                .map((T item) -> (T)(item.populate(this)))
                .collect(Collectors.toMap(T::getId, Function.identity()));

        manifest.setIdMap(idMap);

        // Organize by type
        Multimap<CT, T> typeIndex = ArrayListMultimap.create();
        //TODO: make getType work
        idMap.values().forEach((T entity) -> typeIndex.put((CT)entity.getSubtype(), entity));
        manifest.setTypeIndex(typeIndex);

        return manifest;
    }

    public Manifest<UnitItemType, UnitItem> loadUnitItemManifest() {
        Manifest<UnitItemType, UnitItem> unitItemManifest = FileTools.loadManifest(new Manifest<>(), gameFileService.getManifestPath(UnitItem.class));

        System.out.println(STR."Loaded \{unitItemManifest.getIds().size()} unitItemIds");

        // Organize by id
        Map<String, UnitItem> unitItemMap = unitItemManifest.getIds().stream()
                .map((String id) -> (UnitItem)gameFileService.readEntityFile(id, UnitItem.class))
                .map((UnitItem item) -> (UnitItem)(item.populate(this)))
                .collect(Collectors.toMap(UnitItem::getId, Function.identity()));

        unitItemManifest.setIdMap(unitItemMap);

        // Organize by type
        Multimap<UnitItemType, UnitItem> typeIndex = ArrayListMultimap.create();
        unitItemMap.values().forEach(unitItem -> typeIndex.put(unitItem.getItemType(), unitItem));
        unitItemManifest.setTypeIndex(typeIndex);

        return unitItemManifest;
    }
}
