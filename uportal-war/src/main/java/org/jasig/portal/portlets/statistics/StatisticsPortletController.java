package org.jasig.portal.portlets.statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jasig.portal.events.aggr.AggregationInterval;
import org.jasig.portal.events.aggr.AggregationIntervalHelper;
import org.jasig.portal.events.aggr.DateDimension;
import org.jasig.portal.events.aggr.TimeDimension;
import org.jasig.portal.events.aggr.groups.AggregatedGroupLookupDao;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.jasig.portal.events.aggr.login.LoginAggregation;
import org.jasig.portal.events.aggr.login.LoginAggregationDao;
import org.jasig.portal.events.aggr.login.MissingLoginDataCreator;
import org.joda.time.DateMidnight;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

import com.google.visualization.datasource.base.TypeMismatchException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableCell;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.DateTimeValue;
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.datatable.value.ValueType;

/**
 * StatisticsPortletController drives stats reporting of login data.
 * 
 * @author Jen Bourey, jennifer.bourey@gmail.com
 */
@Controller
@RequestMapping("VIEW")
public class StatisticsPortletController {
    
    protected final static String LOGIN_FORM_NAME = "loginReportRequest";

    @Autowired(required = true)
    private LoginAggregationDao<? extends LoginAggregation> loginDao;
    
    @Autowired(required = true)
    private AggregationIntervalHelper intervalHelper;
    
    @Autowired(required = true)
    private AggregatedGroupLookupDao aggregatedGroupDao;
    
    @RenderMapping
    public String getLoginView(Model model) throws TypeMismatchException {
        if (!model.containsAttribute(LOGIN_FORM_NAME)) {
            final LoginReportForm report = new LoginReportForm();
            report.setInterval(AggregationInterval.DAY);

            final List<? extends AggregatedGroupMapping> groupMappings = aggregatedGroupDao.getAllGroupMappings();
            for (AggregatedGroupMapping mapping : groupMappings) {
                report.getGroups().add(String.valueOf(mapping.getId()));
            }
            
            final DateMidnight today = new DateMidnight();
            report.setStart(today.minusMonths(1));
            report.setEnd(today);
            model.addAttribute(LOGIN_FORM_NAME, report);
        }
        return "jsp/Statistics/loginTotals";
    }
    
    @ModelAttribute("intervals")
    public AggregationInterval[] getIntervals() {
        return AggregationInterval.values();
    }
    
    @ModelAttribute("groups")
    public Map<Long, String> getGroups() {
        final Map<Long, String> groups = new HashMap<Long, String>();
        final List<? extends AggregatedGroupMapping> groupMappings = aggregatedGroupDao.getAllGroupMappings();
        for (AggregatedGroupMapping mapping : groupMappings) {
            groups.put(mapping.getId(), mapping.getGroupName());
        }
        return groups;
    }
    
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern("M/d/yyyy").toFormatter();
        binder.registerCustomEditor(DateMidnight.class, new CustomDateMidnightEditor(formatter, false));
    }
    
    @ResourceMapping
    public ModelAndView getLoginData(@ModelAttribute(LOGIN_FORM_NAME) LoginReportForm form) throws TypeMismatchException {

        final ModelAndView mv = new ModelAndView("json");
        
        final DataTable table = new JsonDataTable();
        table.addColumn(new ColumnDescription("date", ValueType.DATETIME, "Date"));

        final List<List<LoginAggregation>> data = new ArrayList<List<LoginAggregation>>();
        
        for (String group : form.getGroups()) {
            
            final Long groupId = Long.valueOf(group);
            final AggregatedGroupMapping groupMapping = aggregatedGroupDao.getGroupMapping(groupId);
            table.addColumn(new ColumnDescription(groupMapping.getGroupName(), ValueType.NUMBER, groupMapping.getGroupName()));
            
            final List<LoginAggregation> aggrs = loginDao.getLoginAggregations(
                    form.getStart(), form.getEnd(), form.getInterval(), groupMapping);
            final List<LoginAggregation> complete = intervalHelper.fillInBlanks(form.getInterval(), form.getStart().toDateTime(), form.getEnd().toDateTime(), aggrs, new MissingLoginDataCreator(groupMapping));
            data.add(complete);
        }
        
        int num = data.get(0).size();
        
        for (int i = 0; i < num; i++) {
            
            // create the row
            final TableRow row = new TableRow();

            // add the date to the first cell
            final DateDimension dateDimension = data.get(0).get(i).getDateDimension();
            final TimeDimension timeDimension = data.get(0).get(i).getTimeDimension();
            final DateTimeValue dateValue = new DateTimeValue(dateDimension.getYear(), dateDimension.getMonth()-1, dateDimension.getDay(), timeDimension.getHour(), timeDimension.getMinute(), 0, 0);
            row.addCell(new TableCell(dateValue));
            
            for (List<LoginAggregation> groupData : data) {
                // add the login count to the second cell
                row.addCell(new TableCell(new NumberValue(groupData.get(i).getLoginCount())));
            }
            table.addRow(row);
        }

        mv.addObject("logins", table);
        return mv;
    }
    
}
