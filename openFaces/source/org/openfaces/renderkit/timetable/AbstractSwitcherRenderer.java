package org.openfaces.renderkit.timetable;

import org.openfaces.component.timetable.AbstractSwitcher;
import org.openfaces.component.timetable.TimetableView;
import org.openfaces.org.json.JSONObject;
import org.openfaces.renderkit.RendererBase;
import org.openfaces.util.CalendarUtil;
import org.openfaces.util.Rendering;
import org.openfaces.util.Resources;
import org.openfaces.util.ScriptBuilder;
import org.openfaces.util.StyleGroup;
import org.openfaces.util.Styles;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by IntelliJ IDEA.
 * User: Dallas
 * Date: 4/17/11
 * Time: 7:00 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractSwitcherRenderer extends RendererBase {
    private static final String DEFAULT_DATE_FORMAT = "long";

    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        if (!component.isRendered())
            return;

        AbstractSwitcher switcher = (AbstractSwitcher) component;

        Locale locale = switcher.getLocale();
        Rendering.registerDateTimeFormatObject(locale);

        TimetableView timetableView = switcher.getTimetableView();
        TimeZone timeZone = switcher.getTimeZone();

        SimpleDateFormat dateFormat = CalendarUtil.getSimpleDateFormat(switcher.getDateFormat(),
                DEFAULT_DATE_FORMAT, switcher.getPattern(), null, locale, timeZone);
        String pattern = dateFormat.toPattern();

        String clientId = switcher.getClientId(context);

        ResponseWriter writer = context.getResponseWriter();
        writer.startElement("table", switcher);

        writer.writeAttribute("id", clientId, "id");
        writer.writeAttribute("cellspacing", "0", null);
        writer.writeAttribute("cellpadding", "0", null);
        writer.writeAttribute("border", "0", null);
        String styleClass = Styles.getCSSClass(context,
                switcher, switcher.getStyle(), "o_timeSwitcher", switcher.getStyleClass());
        writer.writeAttribute("class", styleClass, null);
        writer.startElement("tbody", switcher);
        writer.startElement("tr", switcher);

        Boolean enabled = switcher.isEnabled();
        //previous button
        if (enabled) {
            writer.startElement("td", switcher);
            writer.writeAttribute("id", clientId + "::previous_button", null);
            writer.writeAttribute("class", Styles.getCSSClass(context, switcher,
                    switcher.getPreviousButtonStyle(), "o_timeSwitcher_previous_button",
                    switcher.getPreviousButtonClass()), null);
            String previousButtonImageUrl = Resources.getURL(context, switcher.getPreviousButtonImageUrl(), null,
                    "timetable/previousButton.gif");
            writer.startElement("img", switcher);
            writer.writeAttribute("src", previousButtonImageUrl, null);
            writer.endElement("img");
            writer.endElement("td");
        }

        writer.startElement("td", switcher);
        renderText(context, switcher, timetableView, dateFormat);
        writer.endElement("td");

        //next button
        if (enabled) {
            writer.startElement("td", switcher);
            writer.writeAttribute("id", clientId + "::next_button", null);
            writer.writeAttribute("class", Styles.getCSSClass(context,
                    switcher, switcher.getNextButtonStyle(), "o_timeSwitcher_next_button", switcher.getNextButtonClass()), null);
            String nextButtonImageUrl = Resources.getURL(context, switcher.getNextButtonImageUrl(), null,
                    "timetable/nextButton.gif");
            writer.startElement("img", switcher);
            writer.writeAttribute("src", nextButtonImageUrl, null);
            writer.endElement("img");
            writer.endElement("td");
        }

        writer.endElement("tr");

        writer.endElement("tbody");
        writer.endElement("table");

        JSONObject stylingParams = getStylingParamsObj(context, switcher);
        Styles.renderStyleClasses(context, switcher);

        Object[] params = {
                timetableView,
                formatDayInitParam(timetableView, timeZone),
                pattern,
                locale,
                stylingParams,
                enabled
        };

        Object[] additionalParams = getAdditionalParams(context);

        String switcherClassName = switcher.getClass().getName();
        int i = switcherClassName.lastIndexOf(".");
        switcherClassName = switcherClassName.substring(i + 1);

        ScriptBuilder script = new ScriptBuilder().initScript(context, switcher, "O$." + switcherClassName + "._init",
                concatenateArrays(params, additionalParams)
        );

        Rendering.renderInitScript(context, script,
                Resources.getUtilJsURL(context),
                Resources.getJsonJsURL(context),
                Resources.getInternalURL(context, "timetable/weekSwitcher.js"),
                Resources.getInternalURL(context, "timetable/daySwitcher.js"));
    }

    private JSONObject getStylingParamsObj(FacesContext context, AbstractSwitcher switcher) {
        JSONObject stylingParams = new JSONObject();
        Styles.addStyleJsonParam(context, switcher, stylingParams, "rolloverClass",
                switcher.getRolloverStyle(), switcher.getRolloverClass());
        Styles.addStyleJsonParam(context, switcher, stylingParams, "previousButtonRolloverClass",
                switcher.getPreviousButtonRolloverStyle(), switcher.getPreviousButtonRolloverClass());
        Styles.addStyleJsonParam(context, switcher, stylingParams, "previousButtonPressedClass",
                switcher.getPreviousButtonPressedStyle(), switcher.getPreviousButtonPressedClass(), StyleGroup.rolloverStyleGroup());
        Styles.addStyleJsonParam(context, switcher, stylingParams, "nextButtonRolloverClass",
                switcher.getNextButtonRolloverStyle(), switcher.getNextButtonRolloverClass(), StyleGroup.rolloverStyleGroup());
        Styles.addStyleJsonParam(context, switcher, stylingParams, "nextButtonPressedClass",
                switcher.getNextButtonPressedStyle(), switcher.getNextButtonPressedClass(), StyleGroup.rolloverStyleGroup());
        Styles.addStyleJsonParam(context, switcher, stylingParams, "labelRolloverClass",
                switcher.getTextRolloverStyle(), switcher.getTextRolloverClass(), StyleGroup.rolloverStyleGroup());

        return stylingParams;
    }

    private Object[] concatenateArrays(Object[] a1, Object[] a2) {
        List<Object> list = new ArrayList<Object>(Arrays.asList(a1));
        list.addAll(Arrays.asList(a2));
        return list.toArray();
    }

    protected abstract Object[] getAdditionalParams(FacesContext context);

    protected abstract String formatDayInitParam(TimetableView timetableView, TimeZone timeZone);

    protected abstract void renderText(FacesContext context, AbstractSwitcher switcher, TimetableView timetableView, SimpleDateFormat dateFormat) throws IOException;
}
