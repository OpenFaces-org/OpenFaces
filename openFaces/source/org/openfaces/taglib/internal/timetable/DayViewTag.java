/*
 * OpenFaces - JSF Component Library 2.0
 * Copyright (C) 2007-2012, TeamDev Ltd.
 * licensing@openfaces.org
 * Unless agreed in writing the contents of this file are subject to
 * the GNU Lesser General Public License Version 2.1 (the "LGPL" License).
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * Please visit http://openfaces.org/licensing/ for more details.
 */
package org.openfaces.taglib.internal.timetable;

import org.openfaces.component.timetable.DayView;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

public class DayViewTag extends org.openfaces.taglib.internal.AbstractComponentTag {

    public String getComponentType() {
        return DayView.COMPONENT_TYPE;
    }

    public String getRendererType() {
        return "org.openfaces.DayViewRenderer";
    }

    @Override
    public void setComponentProperties(FacesContext facesContext, UIComponent component) {
        super.setComponentProperties(facesContext, component);

        setObjectProperty(component, "moreLinkText");
        setStringProperty(component, "moreLinkStyle");
        setStringProperty(component, "moreLinkClass");
        setStringProperty(component, "buttonStyle");
        setStringProperty(component, "buttonClass");
        setStringProperty(component, "rolloverButtonStyle");
        setStringProperty(component, "rolloverButtonClass");
        setObjectProperty(component, "downButtonImg");
        setObjectProperty(component, "upButtonImg");
    }
}
