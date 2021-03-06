package com.vaadin.flow.component.crud.test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.crud.testbench.CrudElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;

import com.vaadin.testbench.parallel.BrowserUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CustomGridIT extends AbstractParallelTest {

    @Before
    public void init() {
        getDriver().get(getBaseURL() + "/customgrid");
    }

    @Test
    public void editTest() {
        CrudElement crud = $(CrudElement.class).waitForFirst();
        Assert.assertFalse(crud.isEditorOpen());
        crud.openRowForEditing(0);
        Assert.assertTrue(crud.isEditorOpen());
        TextFieldElement lastNameField = crud.getEditor().$(TextFieldElement.class)
                .attribute("editor-role", "last-name").first();

        Assert.assertEquals("Sayo", lastNameField.getValue());

        // TODO(alexberazouski): Check why setValue doesn't fire the valueChange event
        lastNameField.setValue("Otto");

        // TODO(alexberazouski): Check why is it possible to click on disabled save button
        crud.getEditorSaveButton().click();

        if (BrowserUtil.isIE(getDesiredCapabilities())) {
            return;
        }

        Assert.assertFalse(crud.isEditorOpen());
        Assert.assertEquals("Otto",
                $(GridElement.class).first().getCell(0, 2).getText());
    }

    @Test
    public void customGridDoesNotReactToThemeVariantChanges() {
        CrudElement crud = $(CrudElement.class).waitForFirst();
        GridElement grid = $(GridElement.class).first();

        Assert.assertNotEquals("no-border", crud.getAttribute("theme"));
        Assert.assertNotEquals("no-border", grid.getAttribute("theme"));

        toggleBordersButton().click();
        Assert.assertEquals("no-border", crud.getAttribute("theme"));
        Assert.assertNotEquals("no-border", grid.getAttribute("theme"));
    }

    private ButtonElement toggleBordersButton() {
        return $(ButtonElement.class).onPage().id("toggleBorders");
    }
}
