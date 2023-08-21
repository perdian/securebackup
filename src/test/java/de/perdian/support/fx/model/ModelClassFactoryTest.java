package de.perdian.support.fx.model;

import com.google.common.jimfs.Jimfs;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsEmptyCollection;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.List;

public class ModelClassFactoryTest {

    @Test
    public void shouldCreateNewModelAndPersistChanges() {

        FileSystem fileSystem = Jimfs.newFileSystem();
        Path fileSystemBase = fileSystem.getPath(this.getClass().getName());

        ExampleModel exampleModel = ModelFactory.createModel(ExampleModel.class, fileSystemBase);
        MatcherAssert.assertThat(exampleModel.getStringProperty().getValue(), IsNull.nullValue());
        MatcherAssert.assertThat(exampleModel.getListProperty(), IsEmptyCollection.empty());
        MatcherAssert.assertThat(exampleModel.getSubModel().getIntegerProperty().getValue(), IsEqual.equalTo(0));

        exampleModel.getListProperty().setAll(List.of("A", "B", "C"));
        exampleModel.getStringProperty().setValue("foo");
        exampleModel.getSubModel().getIntegerProperty().setValue(42);

        ExampleModel exampleModelReloaded = ModelFactory.createModel(ExampleModel.class, fileSystemBase);
        MatcherAssert.assertThat(exampleModelReloaded.getListProperty(), IsIterableContainingInOrder.contains("A", "B", "C"));
        MatcherAssert.assertThat(exampleModelReloaded.getStringProperty().getValue(), IsEqual.equalTo("foo"));
        MatcherAssert.assertThat(exampleModelReloaded.getSubModel().getIntegerProperty().getValue(), IsEqual.equalTo(42));

    }


    static class ExampleModel {

        private final StringProperty stringProperty = new SimpleStringProperty();
        private final ObservableList<String> listProperty = FXCollections.observableArrayList();
        private final SubModel subModel = new SubModel();

        StringProperty getStringProperty() {
            return this.stringProperty;
        }

        ObservableList<String> getListProperty() {
            return this.listProperty;
        }

        SubModel getSubModel() {
            return subModel;
        }

    }

    static class SubModel {

        private final IntegerProperty integerProperty = new SimpleIntegerProperty();

        IntegerProperty getIntegerProperty() {
            return this.integerProperty;
        }

    }

}
