package org.dshaver.sins.service;

import java.nio.file.Path;
import java.util.Map;

public class GameFileService {
    private static final String ENTITY_DIRECTORY = "entities/";
    private static final String ENTITY_MANIFEST_EXTENSION = ".entity_manifest";
    
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

    public <T extends FileTools.EntityClass> Path getManifestPath(Class<T> objectClass) {
        String fileName = objectClass.getSimpleName().replaceAll("(\\p{Ll})(\\p{Lu})","$1_$2").toLowerCase();
        return Path.of(steamDir).resolve(ENTITY_DIRECTORY + fileName + ENTITY_MANIFEST_EXTENSION);
    }
}
