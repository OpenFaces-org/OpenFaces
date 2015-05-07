/*
 * OpenFaces - JSF Component Library 3.0
 * Copyright (C) 2007-2015, TeamDev Ltd.
 * licensing@openfaces.org
 * Unless agreed in writing the contents of this file are subject to
 * the GNU Lesser General Public License Version 2.1 (the "LGPL" License).
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * Please visit http://openfaces.org/licensing/ for more details.
 */

package org.openfaces.testapp.datatable;

import org.openfaces.component.table.CSVTableDataFormatter;
import org.openfaces.component.table.DataScope;
import org.openfaces.component.table.DataTable;
import org.openfaces.component.table.ExpansionState;
import org.openfaces.testapp.util.FacesUtils;
import org.openfaces.util.Faces;

import javax.faces.event.AjaxBehaviorEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Darya Shumilina
 */
public class BanksList implements Serializable {

    private List<Bank> allBanks = new ArrayList<Bank>();
    private List<Bank> banks = new ArrayList<Bank>();
    private int paginatorStyleSelectedIndex;
    private List<PaginatorStyleItem> paginatorStyles = new ArrayList<PaginatorStyleItem>();
    private Collection<State> bankStates;
    private State selectedState;
    private ExpansionState expansionState;

    public BanksList() {
        paginatorStyleSelectedIndex = 0;

        paginatorStyles.add(new PaginatorStyleItem(null, null, null, null, null, null, null, null, null));

        try {
            InputStream resource = BanksList.class.getResourceAsStream("Banks.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(resource));
            String currentString;
            int i = 0;
            while (true) {
                currentString = reader.readLine();
                if (currentString == null) break;
                String[] bankAttributes = currentString.split("\t");
                String institutionName = new String(bankAttributes[0].getBytes(), "utf-8");
                String certificateNumber = new String(bankAttributes[1].getBytes(), "utf-8");
                String city = new String(bankAttributes[2].getBytes(), "utf-8");
                String state = new String(bankAttributes[3].getBytes(), "utf-8");
                String zip = new String(bankAttributes[4].getBytes(), "utf-8");
                String country = new String(bankAttributes[5].getBytes(), "utf-8");
                String averageAssets = new String(bankAttributes[6].getBytes(), "utf-8");
                Bank bank = new Bank(institutionName.trim(), Integer.parseInt(certificateNumber.trim()), city.trim(), state.trim(),
                        Integer.parseInt(zip.trim()), country.trim(), Integer.parseInt(averageAssets.replaceAll(",", "").trim()));
                allBanks.add(bank);
                if (i++ % 7 == 0)
                    banks.add(bank);
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    public List<Bank> getBanks() {
        return banks;
    }

    public List<Bank> getAllBanks() {
        return allBanks;
    }

    public void setAllBanks(List<Bank> allBanks) {
        this.allBanks = allBanks;
    }

    public State getSelectedState() {
        return selectedState;
    }

    public void setSelectedState(State selectedState) {
        this.selectedState = selectedState;
    }

    public Collection<State> getBankStates() {
        if (bankStates == null) {
            Map<String, State> stateBanksMap = new HashMap<String, State>();
            for (Bank bank : allBanks) {
                String stateName = bank.getState().getDescription();
                State state = stateBanksMap.get(stateName);
                if (state == null) {
                    state = new State(stateName);
                    stateBanksMap.put(stateName, state);
                }
                state.getBanks().add(bank);
            }
            bankStates = stateBanksMap.values();
        }
        return bankStates;
    }

    public void selectState(AjaxBehaviorEvent event) {
        String state = (String) FacesUtils.getEventParameter(event, "state");
        if (state != null) {
            for (State bankState : bankStates) {
                if (bankState.getStateName().equals(state)) {
                    selectedState = bankState;
                }
            }
        }
    }


    public List<String> getAverageAssetsFilterValues() {
        List<String> averageAssetsRange = new ArrayList<String>();
        averageAssetsRange.add("< 50,000");
        averageAssetsRange.add("50,000 \u2013 100,000");
        averageAssetsRange.add("100,000 \u2013 200,000");
        averageAssetsRange.add("200,000 \u2013 400,000");
        averageAssetsRange.add("400,000 \u2013 800,000");
        averageAssetsRange.add("800,000 \u2013 2,000,000");
        averageAssetsRange.add("2,000,000 \u2013 10,000,000");
        averageAssetsRange.add("10,000,000 \u2013 30,000,000");
        return averageAssetsRange;
    }

    public String getAverageAssetsRange() {
        Bank bank = Faces.var("bank", Bank.class);
        int averageAsset = bank.getAverageAssets();
        if (averageAsset <= 50000)
            return "< 50,000";
        if (averageAsset <= 100000)
            return "50,000 \u2013 100,000";
        if (averageAsset <= 200000)
            return "100,000 \u2013 200,000";
        if (averageAsset <= 400000)
            return "200,000 \u2013 400,000";
        if (averageAsset <= 800000)
            return "400,000 \u2013 800,000";
        if (averageAsset <= 2000000)
            return "800,000 \u2013 2,000,000";
        if (averageAsset <= 10000000)
            return "2,000,000 \u2013 10,000,000";
        return "10,000,000 \u2013 30,000,000";
    }

    public int getPaginatorStyleSelectedIndex() {
        return paginatorStyleSelectedIndex;
    }

    public void setPaginatorStyleSelectedIndex(int paginatorStyleSelectedIndex) {
        this.paginatorStyleSelectedIndex = paginatorStyleSelectedIndex;
    }

    public List<PaginatorStyleItem> getPaginatorStyles() {
        return paginatorStyles;
    }

    public String getFirstDisabledImageUrl() {
        return "";
    }

    public String getFirstImageUrl() {
        return "";
    }

    public String getNextDisabledImageUrl() {
        return "";
    }

    public String getNextImageUrl() {
        return "";
    }

    public String getPreviousDisabledImageUrl() {
        return "";
    }

    public String getPreviousImageUrl() {
        return "";
    }

    public String getLastDisabledImageUrl() {
        return "";
    }

    public String getLastImageUrl() {
        return "";
    }

    public String getPageNumberFieldStyle() {
        return "";
    }

    public ExpansionState getExpansionState() {
        return expansionState;
    }

    public void setExpansionState(ExpansionState expansionState) {
        this.expansionState = expansionState;
    }

    public void export() {
        Faces.component("form:banks", DataTable.class).export(
                DataScope.DISPLAYED_ROWS,
                new CSVTableDataFormatter());
    }

}
