package de.perdian.support.fx.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class ModelFactory {

    private static final Logger log = LoggerFactory.getLogger(ModelFactory.class);

    public static <T> T createModel(Class<T> modelClass, Path storageDirectory) {
        return ModelFactory.createModel(modelClass, storageDirectory, modelClass.getSimpleName() + ".object");
    }

    public static <T> T createModel(Class<T> modelClass, Path storageDirectory, String storageFileName) {
        ModelClassMetadata<T> modelClassMetadata = new ModelClassMetadata(modelClass);
        Path modelFile = storageDirectory.resolve(storageFileName);
        T modelInstance =  modelClassMetadata.createModel(modelFile);
        return modelInstance;
    }

}
