package org.jasig.portal.portlets.statistics;

import java.util.ArrayList;
import java.util.List;

import org.jasig.portal.events.aggr.AggregationInterval;
import org.joda.time.DateMidnight;

public class LoginReportForm {

    private DateMidnight start;
    private DateMidnight end;
    private AggregationInterval interval;
    private List<String> groups = new ArrayList<String>();

    public DateMidnight getStart() {
        return start;
    }

    public void setStart(DateMidnight start) {
        this.start = start;
    }

    public DateMidnight getEnd() {
        return end;
    }

    public void setEnd(DateMidnight end) {
        this.end = end;
    }

    public AggregationInterval getInterval() {
        return interval;
    }

    public void setInterval(AggregationInterval interval) {
        this.interval = interval;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

}
