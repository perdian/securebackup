package de.perdian.apps.securebackup.fx.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ArchiverModel implements Externalizable {

    static final long serialVersionUID = 1L;

    private final ObjectProperty<Path> targetDirectory = new SimpleObjectProperty<>();
    private final ObservableList<SourceModel> sources = FXCollections.observableArrayList();
    private final StringProperty password = new SimpleStringProperty();
    private final ObjectProperty<EncryptionType> encryptionType = new SimpleObjectProperty<>(EncryptionType.OPENSSL);
    private final List<ChangeListener<? super ArchiverModel>>  changeListeners = new ArrayList<>();

    public ArchiverModel() {
        ChangeListener<Object> changeListener = (o, oldValue, newValue) -> this.getChangeListeners().forEach(listener -> listener.changed(null, this, this));
        ListChangeListener<SourceModel> sourcesChangeListener = (ListChangeListener.Change<? extends SourceModel> change) -> {
            while (change.next()) {
                change.getAddedSubList().forEach(added -> added.addChangeListener(changeListener));
            }
        };
        this.getTargetDirectory().addListener(changeListener);
        this.getSources().addListener(sourcesChangeListener);
        this.getPassword().addListener(changeListener);
        this.getEncryptionType().addListener(changeListener);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(this.getTargetDirectory().getValue().toString());
        out.writeUTF(this.getPassword().getValue());
        out.writeObject(this.getEncryptionType().getValue());
        out.writeInt(this.getSources().size());
        for (SourceModel sourceModel : this.getSources()) {
            out.writeObject(sourceModel);
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.getTargetDirectory().setValue(Paths.get(in.readUTF()));
        this.getPassword().setValue(in.readUTF());
        this.getEncryptionType().setValue((EncryptionType)in.readObject());
        int sourcesSize = in.readInt();
        for (int i=0; i < sourcesSize; i++) {
            this.getSources().add((SourceModel)in.readObject());
        }
    }

    public ObjectProperty<Path> getTargetDirectory() {
        return this.targetDirectory;
    }

    public ObservableList<SourceModel> getSources() {
        return this.sources;
    }

    public StringProperty getPassword() {
        return this.password;
    }

    public ObjectProperty<EncryptionType> getEncryptionType() {
        return this.encryptionType;
    }

    void addChangeListener(ChangeListener<? super ArchiverModel> changeListener) {
        this.getChangeListeners().add(changeListener);
    }
    List<ChangeListener<? super ArchiverModel>> getChangeListeners() {
        return this.changeListeners;
    }

}
