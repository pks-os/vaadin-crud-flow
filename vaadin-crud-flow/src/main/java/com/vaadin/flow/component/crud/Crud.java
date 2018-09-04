package com.vaadin.flow.component.crud;

/*
 * #%L
 * Vaadin Crud for Vaadin 10
 * %%
 * Copyright (C) 2017 - 2018 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file license.html distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.crud.event.CancelEvent;
import com.vaadin.flow.component.crud.event.DeleteEvent;
import com.vaadin.flow.component.crud.event.EditEvent;
import com.vaadin.flow.component.crud.event.NewEvent;
import com.vaadin.flow.component.crud.event.SaveEvent;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.shared.Registration;

import java.util.LinkedHashSet;
import java.util.Set;

@Tag("vaadin-crud")
@HtmlImport("frontend://bower_components/vaadin-crud/src/vaadin-crud.html")
@JavaScript("frontend://crudConnector.js")
public class Crud<E> extends Component {

    private final Class<E> beanType;
    private final Grid<E> grid;
    private final CrudEditor<E> editor;

    private final Set<ComponentEventListener<NewEvent>> newListeners = new LinkedHashSet<>();
    private final Set<ComponentEventListener<EditEvent<E>>> editListeners = new LinkedHashSet<>();
    private final Set<ComponentEventListener<SaveEvent>> saveListeners = new LinkedHashSet<>();
    private final Set<ComponentEventListener<CancelEvent>> cancelListeners = new LinkedHashSet<>();
    private final Set<ComponentEventListener<DeleteEvent>> deleteListeners = new LinkedHashSet<>();

    public Crud(Class<E> beanType, CrudEditor<E> editor) {
        this(beanType, new SimpleCrudGrid<>(beanType, true), editor);
    }

    public Crud(Class<E> beanType, Grid<E> grid, CrudEditor<E> editor) {
        this.beanType = beanType;

        this.grid = grid;
        this.grid.getElement().setAttribute("slot", "grid");

        this.editor = editor;
        this.editor.getView().setAttribute("slot", "form");

        registerHandlers();

        getElement().appendChild(grid.getElement(), editor.getView());
    }

    private void registerHandlers() {
        ComponentUtil.addListener(this, NewEvent.class, (ComponentEventListener<NewEvent>) e -> {
            try {
                editor.createWorkingCopyFrom(beanType.newInstance());
            } catch (Exception ex) {
                throw new RuntimeException("Unable to instantiate new bean", ex);
            }
            newListeners.forEach(listener -> listener.onComponentEvent(e));
        });

        ComponentUtil.addListener(this, EditEvent.class, (ComponentEventListener)
                ((ComponentEventListener<EditEvent<E>>) e -> {
                    editor.createWorkingCopyFrom(e.getItem());
                    editListeners.forEach(listener -> listener.onComponentEvent(e));
                }));

        ComponentUtil.addListener(this, CancelEvent.class, (ComponentEventListener<CancelEvent>) e -> {
            cancelListeners.forEach(listener -> listener.onComponentEvent(e));
            getEditor().createWorkingCopyFrom(null);
            closeDialog();
        });

        ComponentUtil.addListener(this, SaveEvent.class, (ComponentEventListener<SaveEvent>) e -> {
            saveListeners.forEach(listener -> listener.onComponentEvent(e));
            getEditor().createWorkingCopyFrom(null);
            getGrid().getDataProvider().refreshAll();
            closeDialog();
        });

        ComponentUtil.addListener(this, DeleteEvent.class, (ComponentEventListener<DeleteEvent>) e -> {
            deleteListeners.forEach(listener -> listener.onComponentEvent(e));
            getEditor().createWorkingCopyFrom(null);
            getGrid().getDataProvider().refreshAll();
            closeDialog();
        });
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        getUI().orElseThrow(() -> new IllegalStateException(
                "Connector can only be initialized for an attached Crud"))
                .getPage().executeJavaScript(
                "window.Vaadin.Flow.crudConnector.initLazy($0)",
                getElement());
    }

    private void closeDialog() {
        getElement().callFunction("__setDialogOpened", false);
    }

    public Grid<E> getGrid() {
        return grid;
    }

    public CrudEditor<E> getEditor() {
        return editor;
    }

    public Registration addNewListener(ComponentEventListener<NewEvent> listener) {
        newListeners.add(listener);
        return () -> newListeners.remove(listener);
    }

    public Registration addEditListener(ComponentEventListener<EditEvent<E>> listener) {
        editListeners.add(listener);
        return () -> editListeners.remove(listener);
    }

    public Registration addSaveListener(ComponentEventListener<SaveEvent> listener) {
        saveListeners.add(listener);
        return () -> saveListeners.remove(listener);
    }

    public Registration addCancelListener(ComponentEventListener<CancelEvent> listener) {
        cancelListeners.add(listener);
        return () -> cancelListeners.remove(listener);
    }

    public Registration addDeleteListener(ComponentEventListener<DeleteEvent> listener) {
        deleteListeners.add(listener);
        return () -> deleteListeners.remove(listener);
    }

    public DataProvider<E, ?> getDataProvider() {
        return grid.getDataProvider();
    }

    public void setDataProvider(DataProvider<E, ?> provider) {
        grid.setDataProvider(provider);
    }
}
