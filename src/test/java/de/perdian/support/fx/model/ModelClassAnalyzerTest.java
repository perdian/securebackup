package de.perdian.support.fx.model;

import com.google.common.jimfs.Jimfs;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsCollectionWithSize;
import org.hamcrest.collection.IsEmptyCollection;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.List;

public class ModelClassAnalyzerTest {

    @Test
    public void shouldCreateNewModelAndPersistChanges() {

        FileSystem fileSystem = Jimfs.newFileSystem();
        Path modelFile = fileSystem.getPath(this.getClass().getName());

        ExampleModel exampleModel = new ModelBuilder<>(ExampleModel.class).createModel(modelFile);
        MatcherAssert.assertThat(exampleModel.getStringProperty().getValue(), IsNull.nullValue());
        MatcherAssert.assertThat(exampleModel.getSimpleListProperty(), IsEmptyCollection.empty());
        MatcherAssert.assertThat(exampleModel.getSubModel().getIntegerProperty().getValue(), IsEqual.equalTo(0));

        ListModel listModelA = new ListModel();
        ListModel listModelB = new ListModel();
        listModelB.getStringProperty().setValue("listModelB_newString");

        exampleModel.getSimpleListProperty().setAll(List.of("A", "B", "C"));
        exampleModel.getStringProperty().setValue("foo");
        exampleModel.getSubModel().getIntegerProperty().setValue(42);
        exampleModel.getComplexListProperty().setAll(listModelA, listModelB);

        ExampleModel exampleModelReloaded = new ModelBuilder<>(ExampleModel.class).createModel(modelFile);
        MatcherAssert.assertThat(exampleModelReloaded.getSimpleListProperty(), IsIterableContainingInOrder.contains("A", "B", "C"));
        MatcherAssert.assertThat(exampleModelReloaded.getStringProperty().getValue(), IsEqual.equalTo("foo"));
        MatcherAssert.assertThat(exampleModelReloaded.getSubModel().getIntegerProperty().getValue(), IsEqual.equalTo(42));
        MatcherAssert.assertThat(exampleModelReloaded.getComplexListProperty(), IsCollectionWithSize.hasSize(2));
        MatcherAssert.assertThat(exampleModelReloaded.getComplexListProperty().get(1).getStringProperty().getValue(), IsEqual.equalTo("listModelB_newString"));

    }

    static class ExampleModel {

        private final StringProperty stringProperty = new SimpleStringProperty();
        private final ObservableList<String> simpleListProperty = FXCollections.observableArrayList();
        private final SubModel subModel = new SubModel();
        private final ObservableList<ListModel> complexListProperty = FXCollections.observableArrayList();

        StringProperty getStringProperty() {
            return this.stringProperty;
        }

        ObservableList<String> getSimpleListProperty() {
            return this.simpleListProperty;
        }

        SubModel getSubModel() {
            return subModel;
        }

        ObservableList<ListModel> getComplexListProperty() {
            return this.complexListProperty;
        }

    }

    static class SubModel {

        private final IntegerProperty integerProperty = new SimpleIntegerProperty();

        IntegerProperty getIntegerProperty() {
            return this.integerProperty;
        }

    }

    static class ListModel {

        private final StringProperty stringProperty = new SimpleStringProperty();

        StringProperty getStringProperty() {
            return this.stringProperty;
        }

    }

}
