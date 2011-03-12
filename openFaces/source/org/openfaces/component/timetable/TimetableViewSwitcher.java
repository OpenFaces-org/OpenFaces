/*
 * OpenFaces - JSF Component Library 2.0
 * Copyright (C) 2007-2011, TeamDev Ltd.
 * licensing@openfaces.org
 * Unless agreed in writing the contents of this file are subject to
 * the GNU Lesser General Public License Version 2.1 (the "LGPL" License).
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * Please visit http://openfaces.org/licensing/ for more details.
 */
package org.openfaces.component.timetable;

import org.openfaces.component.select.TabSet;
import org.openfaces.util.Components;

import javax.faces.context.FacesContext;

public class TimetableViewSwitcher extends TabSet {
    public static final String COMPONENT_TYPE = "org.openfaces.TimetableViewSwitcher";
    public static final String COMPONENT_FAMILY = "org.openfaces.TimetableViewSwitcher";

    public TimetableViewSwitcher() {
        setRendererType("org.openfaces.TimetableViewSwitcherRenderer");
    }

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    @Override
    public Object saveState(FacesContext context) {
        return new Object[]{
                super.saveState(context),

        };
    }

    @Override
    public void restoreState(FacesContext context, Object stateObj) {
        Object[] state = (Object[]) stateObj;
        int i = 0;
        super.restoreState(context, state[i++]);

    }

    public Timetable getTimetable() {
        Timetable timetable = Components.getParentWithClass(this, Timetable.class);
        return timetable;
    }
}