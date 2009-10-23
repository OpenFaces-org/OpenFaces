/*
 * OpenFaces - JSF Component Library 2.0
 * Copyright (C) 2007-2009, TeamDev Ltd.
 * licensing@openfaces.org
 * Unless agreed in writing the contents of this file are subject to
 * the GNU Lesser General Public License Version 2.1 (the "LGPL" License).
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * Please visit http://openfaces.org/licensing/ for more details.
 */
package org.openfaces.renderkit.table;

import org.openfaces.component.table.AbstractTable;
import org.openfaces.component.table.BaseColumn;
import org.openfaces.component.table.TableCell;
import org.openfaces.component.table.TableColumn;
import org.openfaces.component.table.TableRow;
import org.openfaces.component.table.TreeColumn;
import org.openfaces.util.RenderingUtil;
import org.openfaces.util.StyleUtil;

import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Dmitry Pikhulya
 */
public class TableBody extends TableElement {
    public static final String CUSTOM_ROW_INDEX_ATTRIBUTE = "_customRowIndex";
    public static final String CUSTOM_CELL_INDEX_ATTRIBUTE = "_customCellIndex";
    
    private static final String COLUMN_ATTR_ORIGINAL_INDEX = "_originalIndex";

    private static final String DEFAULT_NO_RECORDS_MSG = "No records";
    private static final String DEFAULT_NO_FILTER_RECORDS_MSG = "No records satisfying the filtering criteria";
    private static final String CUSTOM_CELL_RENDERING_INFO_ATTRIBUTE = "_customCellRenderingInfo";

    private TableStructure tableStructure;

    public TableBody(TableStructure tableStructure) {
        super(tableStructure);
        this.tableStructure = tableStructure;
    }

    public void render(FacesContext facesContext, HeaderCell.AdditionalContentWriter additionalContentWriter) throws IOException {
        ResponseWriter writer = facesContext.getResponseWriter();
        AbstractTable table = (AbstractTable) tableStructure.getComponent();
        List<BaseColumn> columnsForRendering = table.getColumnsForRendering();

        writer.startElement("tbody", table);
        RenderingUtil.writeStyleAndClassAttributes(writer, table.getBodySectionStyle(), table.getBodySectionClass());
        RenderingUtil.writeNewLine(writer);

        int first = table.getFirst();
        int rows = table.getRows();
        if (rows == 0) {
            int rowCount = table.getRowCount();
            rows = (rowCount != -1) ? rowCount : Integer.MAX_VALUE;
        }
        encodeRows(facesContext, table, first, rows, columnsForRendering, additionalContentWriter);

        RenderingUtil.writeNewLine(writer);
        writer.endElement("tbody");
        RenderingUtil.writeNewLine(writer);
    }

    protected void encodeRows(
            FacesContext facesContext,
            AbstractTable table,
            int firstRowIndex,
            int rowCount,
            List<BaseColumn> columnsForRendering,
            HeaderCell.AdditionalContentWriter additionalContentWriter) throws IOException {
        boolean thereAreRows = rowCount > 0;
        ResponseWriter writer = facesContext.getResponseWriter();
        StringWriter stringWriter = new StringWriter();
        List<BodyRow> rows = new ArrayList<BodyRow>();
        facesContext.setResponseWriter(writer.cloneWithWriter(stringWriter));
        try {
            if (thereAreRows)
                rows = encodeDataRows(facesContext, stringWriter, table, firstRowIndex, rowCount, columnsForRendering);
            else if (table.getNoDataMessageAllowed())
                rows = Collections.singletonList(
                        encodeNoDataRow(facesContext, stringWriter, table, columnsForRendering));
            else
                rows = Collections.singletonList(
                        encodeFakeRow(columnsForRendering));
        } finally {
            facesContext.setResponseWriter(writer);
        }
        TableFooter footer = tableStructure.getFooter();
        boolean hasFooter = footer != null && !footer.isEmpty();
        for (int i = 0, count = rows.size(); i < count; i++) {
            BodyRow row = rows.get(i);
            row.render(facesContext, (!hasFooter && i == count - 1) ? additionalContentWriter : null);
        }

    }

    private List<BodyRow> encodeDataRows(FacesContext facesContext, StringWriter stringWriter, AbstractTable table,
                                         int firstRowIndex, int rowsToRender,
                                         List<BaseColumn> columnsForRendering) throws IOException {
        List<BodyRow> rows = new ArrayList<BodyRow>();
        ResponseWriter writer = facesContext.getResponseWriter();
        List<TableRow> customRows = getCustomRows(table);
        int lastRowIndex = firstRowIndex + rowsToRender - 1;
        Map<Integer, CustomRowRenderingInfo> customRowRenderingInfos = (Map<Integer, CustomRowRenderingInfo>) table.getAttributes().get(TableStructure.CUSTOM_ROW_RENDERING_INFOS_KEY);

        for (int rowIndex = firstRowIndex; rowIndex <= lastRowIndex; rowIndex++) {
            table.setRowIndex(rowIndex);
            if (!table.isRowAvailable())
                break;

            BodyRow row = new BodyRow();
            rows.add(row);
            List<TableRow> applicableCustomRows = getApplicableCustomRows(customRows);
            row.setApplicableCustomRows(applicableCustomRows);
            String[][] attributes = tableStructure.getBodyRowAttributes(facesContext, table);
            row.setAttributes(attributes);
            Object rowData = table.getRowData();
            int bodyRowIndex = rowIndex - firstRowIndex;

            List<String> rowStyles = getApplicableRowStyles(facesContext, customRows, table);
            String rowStyleClass = (rowStyles != null && rowStyles.size() > 0) ? classNamesToClass(rowStyles) : null;
            String additionalClass = tableStructure.getAdditionalRowClass(facesContext, table, rowData, bodyRowIndex);
            if (additionalClass != null)
                rowStyleClass = StyleUtil.mergeClassNames(rowStyleClass, additionalClass);
            if (!RenderingUtil.isNullOrEmpty(rowStyleClass)) {
                Map<Object, String> rowStylesMap = tableStructure.getRowStylesMap();
                rowStylesMap.put(bodyRowIndex, rowStyleClass);
            }

            int columnCount = columnsForRendering.size();

            CustomRowRenderingInfo customRowRenderingInfo = null;
            List<Integer> applicableRowDeclarationIndexes = new ArrayList<Integer>();
            for (TableRow tableRow : applicableCustomRows) {
                if (RenderingUtil.isComponentWithA4jSupport(tableRow)) {
                    if (customRowRenderingInfo == null)
                        customRowRenderingInfo = new CustomRowRenderingInfo(columnCount);
                    Integer rowDeclarationIndex = (Integer) tableRow.getAttributes().get(CUSTOM_ROW_INDEX_ATTRIBUTE);
                    applicableRowDeclarationIndexes.add(rowDeclarationIndex);
                }
            }
            if (customRowRenderingInfo != null)
                customRowRenderingInfo.setA4jEnabledRowDeclarationIndexes(applicableRowDeclarationIndexes);

            List<SpannedTableCell> alreadyProcessedSpannedCells = new ArrayList<SpannedTableCell>();
            List[] applicableCustomCells = prepareCustomCells(table, applicableCustomRows);

            List<BodyCell> cells = new ArrayList<BodyCell>();
            row.setCells(cells);
            for (int colIndex = 0; colIndex < columnCount;) {
                BaseColumn column = columnsForRendering.get(colIndex);
                if (!column.isRendered())
                    throw new IllegalStateException("Only rendered columns are expected in columns list. column id: " + column.getId() + "; column index = " + colIndex);

                List customCells = applicableCustomCells[colIndex];
                SpannedTableCell spannedTableCell =
                        customCells != null && customCells.size() == 1 && customCells.get(0) instanceof SpannedTableCell
                                ? (SpannedTableCell) customCells.get(0) : null;
                boolean remainingPortionOfBrokenSpannedCell = false;
                int span = 1;
                if (spannedTableCell != null) {
                    int testedColIndex = colIndex;
                    while (true) {
                        testedColIndex++;
                        if (testedColIndex == columnCount)
                            break;
                        List testedCells = applicableCustomCells[testedColIndex];
                        if (testedCells == null)
                            break;
                        SpannedTableCell testedSpannedCell = testedCells != null && testedCells.size() == 1 && testedCells.get(0) instanceof SpannedTableCell
                                ? (SpannedTableCell) testedCells.get(0) : null;
                        if (spannedTableCell != testedSpannedCell)
                            break;
                        span++;
                    }
                    column = spannedTableCell.getColumn();
                    customCells = spannedTableCell.getApplicableTableCells();
                    if (alreadyProcessedSpannedCells.contains(spannedTableCell))
                        remainingPortionOfBrokenSpannedCell = true;
                    else
                        alreadyProcessedSpannedCells.add(spannedTableCell);
                }

                UIComponent cellContentsContainer = column;
                String customCellStyle = null;
                String columnId = column.getId();
                if (customCells != null) {
                    int customCellCount = customCells.size();

                    for (int i = 0; i < customCellCount; i++) {
                        TableCell cell = (TableCell) customCells.get(i);
                        boolean cellWithCustomContent = cell.getChildCount() > 0;
                        if (cellWithCustomContent || RenderingUtil.isComponentWithA4jSupport(cell)) {
                            if (customRowRenderingInfo == null)
                                customRowRenderingInfo = new CustomRowRenderingInfo(columnCount);
                            if (cellWithCustomContent)
                                cellContentsContainer = cell;
                            CustomContentCellRenderingInfo customCellRenderingInfo =
                                    (CustomContentCellRenderingInfo) cell.getAttributes().get(CUSTOM_CELL_RENDERING_INFO_ATTRIBUTE);
                            customRowRenderingInfo.setCustomCellRenderingInfo(colIndex, customCellRenderingInfo);
                        }
                        customCellStyle = StyleUtil.mergeClassNames(customCellStyle, cell.getStyleClassForCell(facesContext, table, colIndex, columnId));
                    }
                    for (int i = colIndex + (remainingPortionOfBrokenSpannedCell ? 0 : 1), upperBound = colIndex + span;
                         i < upperBound; i++) {
                        if (customRowRenderingInfo == null)
                            customRowRenderingInfo = new CustomRowRenderingInfo(columnCount);
                        customRowRenderingInfo.setCustomCellRenderingInfo(i, new MergedCellRenderingInfo());
                    }
                }

                List<String> cellStyles = new ArrayList<String>();
                if (customCellStyle != null)
                    cellStyles.add(customCellStyle);

                if (!cellStyles.isEmpty()) {
                    String styleClass = classNamesToClass(cellStyles);
                    if (!RenderingUtil.isNullOrEmpty(styleClass)) {
                        Map<Object, String> cellStylesMap = tableStructure.getCellStylesMap();
                        cellStylesMap.put(bodyRowIndex + "x" + colIndex, styleClass);
                    }
                }

                BodyCell cell = new BodyCell();
                cells.add(cell);
                cell.setSpan(span);
                cell.setCustomCells(customCells);

                StringBuffer buf = stringWriter.getBuffer();
                int startIdx = buf.length();
                if (!remainingPortionOfBrokenSpannedCell) {
                    boolean renderCustomTreeCell = column instanceof TreeColumn && cellContentsContainer instanceof TableCell;
                    if (renderCustomTreeCell) {
                        column.getAttributes().put(TreeColumnRenderer.ATTR_CUSTOM_CELL, cellContentsContainer);
                        try {
                            column.encodeAll(facesContext);
                        } finally {
                            column.getAttributes().remove(TreeColumnRenderer.ATTR_CUSTOM_CELL);
                        }
                    } else {
                        cellContentsContainer.encodeAll(facesContext);
                    }
                    writeNonBreakableSpaceForEmptyCell(writer, table, cellContentsContainer);
                } else {
                    if (tableStructure.isEmptyCellsTreatmentRequired())
                        RenderingUtil.writeNonBreakableSpace(writer);
                }
                int endIdx = buf.length();
                String content = buf.substring(startIdx, endIdx);
                cell.setContent(content);

                colIndex += span;
            }

            if (customRowRenderingInfo != null)
                customRowRenderingInfos.put(rowIndex, customRowRenderingInfo);
        }
        table.setRowIndex(-1);

        return rows;
    }

    private boolean isColumnApplicable(
            FacesContext context,
            TableCell cell,
            AbstractTable table,
            BaseColumn column,
            int originalColIndex) {
        Object columnIds = cell.getColumnIds();
        if (columnIds != null) {
            Collection<Object> columnIdsCollection;
            if (columnIds.getClass().isArray()) {
                columnIdsCollection = Arrays.asList((Object[]) columnIds);
            } else if (columnIds instanceof Collection)
                columnIdsCollection = (Collection<Object>) columnIds;
            else
                throw new IllegalArgumentException("'columnIds' attribute of <o:cell> tag should contain either an array or a collection of column id strings, but a value of the following type encountered: " + columnIds.getClass().getName());
            String colId = column.getId();
            boolean result = columnIdsCollection.contains(colId);
            return result;
        }

        ValueExpression conditionExpression = cell.getConditionExpression();

        Map<String, Object> requestMap = context.getExternalContext().getRequestMap();
        String columnIndexVar = table.getColumnIndexVar();
        String columnIdVar = table.getColumnIdVar();
        Object prevColumnIndexVarValue = null;
        Object prevColumnIdVarValue = null;
        if (columnIndexVar != null)
            prevColumnIndexVarValue = requestMap.put(columnIndexVar, originalColIndex);
        if (columnIdVar != null) {
            String columnId = column.getId();
            prevColumnIdVarValue = requestMap.put(columnIdVar, columnId);
        }

        Boolean result = (Boolean) conditionExpression.getValue(context.getELContext());

        if (columnIndexVar != null)
            requestMap.put(columnIndexVar, prevColumnIndexVarValue);
        if (columnIdVar != null)
            requestMap.put(columnIdVar, prevColumnIdVarValue);

        return (result == null) || result;
    }

    private void writeNonBreakableSpaceForEmptyCell(ResponseWriter writer, AbstractTable table, UIComponent cellComponentsContainer) throws IOException {
        if (cellComponentsContainer instanceof TableColumn || cellComponentsContainer instanceof TableCell) {
            List<UIComponent> children = cellComponentsContainer.getChildren();
            boolean childrenEmpty = true;
            for (int childIndex = 0, childCount = children.size(); childIndex < childCount; childIndex++) {
                UIComponent child = children.get(childIndex);
                if (!TableStructure.isComponentEmpty(child)) {
                    childrenEmpty = false;
                    break;
                }
            }
            if (childrenEmpty && tableStructure.isEmptyCellsTreatmentRequired())
                RenderingUtil.writeNonBreakableSpace(writer);
        }
    }

    private List<TableRow> getApplicableCustomRows(List<TableRow> customRows) {
        List<TableRow> applicableRows = new ArrayList<TableRow>(customRows.size());
        for (TableRow tableRow : customRows) {
            if (tableRow.getCondition())
                applicableRows.add(tableRow);
        }
        return applicableRows;
    }



    private BodyRow encodeFakeRow(List<BaseColumn> columns) throws IOException {
        BodyRow row = new BodyRow();
        BodyCell cell = new BodyCell();
        row.setCells(Collections.singletonList(cell));
        int allColCount = columns.size();
        cell.setSpan(allColCount);
        cell.setStyle("display: none;");
        return row;
    }

    private BodyRow encodeNoDataRow(FacesContext context,
                                    StringWriter stringWriter,
                                    AbstractTable table,
                                    List<BaseColumn> columns
    ) throws IOException {
        BodyRow row = new BodyRow();
        ResponseWriter writer = context.getResponseWriter();
        BodyCell cell = new BodyCell();
        row.setCells(Collections.singletonList(cell));
        int allColCount = columns.size();
        if (allColCount != 0)
            cell.setSpan(allColCount);
        cell.setStyle(table.getNoDataRowStyle());
        cell.setStyleClass(table.getNoDataRowClass());

        StringBuffer buf = stringWriter.getBuffer();
        int startIdx = buf.length();
        boolean dataSourceEmpty = table.isDataSourceEmpty();
        UIComponent noDataMessage = dataSourceEmpty ? table.getNoDataMessage() : table.getNoFilterDataMessage();
        if (noDataMessage != null) {
            noDataMessage.encodeAll(context);
        } else {
            String message = (dataSourceEmpty)
                    ? DEFAULT_NO_RECORDS_MSG
                    : DEFAULT_NO_FILTER_RECORDS_MSG;
            writer.writeText(message, null);
        }
        int endIdx = buf.length();
        String content = buf.substring(startIdx, endIdx);
        cell.setContent(content);

        Map<Object, String> rowStylesMap = tableStructure.getRowStylesMap();
        rowStylesMap.put(0, TableStructure.getNoDataRowClassName(context, table));
        return row;
    }

    /**
     * Takes the list of custom row specifications applicable to a row of interest (this is a list of TableRow instances
     * because several TableRow instances can be applicable to the same row), and constructs a list of custom cell
     * specifications applicable to the cells of this row.
     *
     * @param table                a table whose row is being analyzed
     * @param applicableCustomRows a list of TableRow instances applicable to the current row
     * @return An array the size of number of rendered columns. Each array entry corresponds to each rendered cell. Each
     *         array entry contains one of the following: (1) null - means that no custom cells are applicable for this
     *         cell and it will be rendered as usual; (2) List of TableCell instances applicable to this cell;
     *         (3) List containing only one SpannedTableCell instance if an appropriate cell is part of a cell span - all
     *         cells referring to the same SpannedTableCell will be rendered as one cell spanning across several columns.
     */
    private List[] prepareCustomCells(AbstractTable table, List<TableRow> applicableCustomRows) {
        List<BaseColumn> allColumns = table.getAllColumns();
        List<BaseColumn> columnsForRendering = table.getColumnsForRendering();
        int allColCount = allColumns.size();
        for (int i = 0; i < allColCount; i++) {
            UIComponent col = allColumns.get(i);
            col.getAttributes().put(COLUMN_ATTR_ORIGINAL_INDEX, i);
        }

        int visibleColCount = columnsForRendering.size();
        List[] rowCellsByAbsoluteIndex = new List[allColCount];

        boolean thereAreCellSpans = false;
        List<TableCell> rowCellsByColReference = new ArrayList<TableCell>();
        for (int i = 0, icount = applicableCustomRows.size(); i < icount; i++) {
            TableRow row = applicableCustomRows.get(i);
            int customRowIndex = (Integer) row.getAttributes().get(CUSTOM_ROW_INDEX_ATTRIBUTE);
            List<UIComponent> children = row.getChildren();
            int freeCellIndex = 0;
            int customCellIndex = 0;
            for (int j = 0, jcount = children.size(); j < jcount; j++) {
                Object child = children.get(j);
                if (!(child instanceof TableCell))
                    continue;
                TableCell cell = (TableCell) child;

                cell.getAttributes().put(CUSTOM_CELL_RENDERING_INFO_ATTRIBUTE, new CustomContentCellRenderingInfo(customRowIndex, customCellIndex++));
                int span = cell.getSpan();
                Object columnIds = cell.getColumnIds();
                ValueExpression conditionExpression = cell.getConditionExpression();
                if (span < 1)
                    throw new IllegalArgumentException("The value of 'span' attribute of <o:cell> tag can't be less than 1, but encountered: " + span);

                if (span != 1) {
                    thereAreCellSpans = true;

                }

                if (columnIds == null && conditionExpression == null) {
                    int thisColIndex = freeCellIndex;
                    freeCellIndex += span;
                    if (thisColIndex >= allColCount)
                        throw new FacesException("The number of free cells (cells without 'column' attribute) inside of <o:row> tag should not be greater than the total number of columns in a table (" + allColCount + ")");
                    List<TableCell> applicableCells = rowCellsByAbsoluteIndex[thisColIndex];
                    if (applicableCells == null) {
                        applicableCells = new ArrayList<TableCell>();
                        rowCellsByAbsoluteIndex[thisColIndex] = applicableCells;
                    }
                    applicableCells.add(cell);
                } else {
                    rowCellsByColReference.add(cell);
                }
            }
        }
        if (rowCellsByColReference.size() > 0) {
            FacesContext context = FacesContext.getCurrentInstance();
            for (int i = 0; i < allColCount; i++) {
                BaseColumn col = allColumns.get(i);
                for (TableCell cell : rowCellsByColReference) {
                    if (isColumnApplicable(context, cell, table, col, i)) {
                        List<TableCell> applicableCells = rowCellsByAbsoluteIndex[i];
                        if (applicableCells == null) {
                            applicableCells = new ArrayList<TableCell>();
                            rowCellsByAbsoluteIndex[i] = applicableCells;
                        }
                        applicableCells.add(cell);
                    }
                }
            }
        }

        if (thereAreCellSpans) {
            for (int i = 0; i < allColCount; i++) {
                List customCellList = rowCellsByAbsoluteIndex[i];
                if (customCellList == null)
                    continue;
                int cellSpan = 1;
                for (int j = 0, jcount = customCellList.size(); j < jcount; j++) {
                    TableCell cell = (TableCell) customCellList.get(j);

                    int currentCellSpan =  cell.getSpan();
                    if (currentCellSpan != 1)
                        cellSpan = currentCellSpan;
                }
                if (cellSpan == 1)
                    continue;
                SpannedTableCell spannedTableCell = new SpannedTableCell(allColumns.get(i), customCellList);
                for (int cellIndex = i; cellIndex < i + cellSpan; cellIndex++) {
                    rowCellsByAbsoluteIndex[cellIndex] = Collections.singletonList(spannedTableCell);
                }
                i += cellSpan - 1;
            }
        }

        List[] applicableCells = new List[visibleColCount];
        for (int i = 0; i < visibleColCount; i++) {
            BaseColumn column = columnsForRendering.get(i);
            int originalColIndex = (Integer) column.getAttributes().get(COLUMN_ATTR_ORIGINAL_INDEX);
            applicableCells[i] = rowCellsByAbsoluteIndex[originalColIndex];
        }
        return applicableCells;
    }

    private List<TableRow> getCustomRows(AbstractTable table) {
        List<TableRow> customRows = new ArrayList<TableRow>();
        List<UIComponent> children = table.getChildren();
        int customRowIndex = 0;
        int customCellIndex = 0;
        for (UIComponent child : children) {
            if (child instanceof TableRow) {
                TableRow customRow = (TableRow) child;
                customRows.add(customRow);
                customRow.getAttributes().put(CUSTOM_ROW_INDEX_ATTRIBUTE, customRowIndex++);
                List<UIComponent> customRowChildren = customRow.getChildren();
                for (UIComponent rowChild : customRowChildren) {
                    if (rowChild instanceof TableCell) {
                        TableCell customCell = (TableCell) rowChild;
                        customCell.getAttributes().put(CUSTOM_CELL_INDEX_ATTRIBUTE, customCellIndex++);
                    }
                }
            }
        }
        return customRows;
    }

    private List<String> getApplicableRowStyles(FacesContext context, List<TableRow> customRows, AbstractTable table) {
        List<String> result = new ArrayList<String>();
        for (TableRow customRow : customRows) {
            String cls = customRow.getStyleClassForRow(context, table);
            if (cls != null)
                result.add(cls);
        }
        return result;
    }

    private String classNamesToClass(List<String> cellStyles) {
        StringBuilder combinedClassName = new StringBuilder();
        for (String className : cellStyles) {
            if (className != null) {
                int len = combinedClassName.length();
                if (len > 0)
                    combinedClassName.append(' ');
                combinedClassName.append(className.trim());
            }
        }
        return combinedClassName.toString();
    }



}
