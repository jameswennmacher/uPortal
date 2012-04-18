package org.jasig.portal.events.aggr.login;

import org.apache.commons.lang.Validate;
import org.jasig.portal.events.aggr.AggregationInterval;
import org.jasig.portal.events.aggr.AggregationIntervalInfo;
import org.jasig.portal.events.aggr.DateDimension;
import org.jasig.portal.events.aggr.TimeDimension;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.joda.time.DateTime;
import org.joda.time.Minutes;

/**
 * EmptyLoginAggregation represents a login aggregation interval with no logins.
 * 
 * @author Jen Bourey, jennifer.bourey@gmail.com
 */
public class EmptyLoginAggregation implements LoginAggregation {

    private final TimeDimension timeDimension;
    private final DateDimension dateDimension;
    private final AggregationInterval interval;
    private final AggregatedGroupMapping aggregatedGroup;
    private final int duration;

    public EmptyLoginAggregation(AggregationIntervalInfo info, AggregatedGroupMapping aggregatedGroup) {
        this(info.getTimeDimension(), info.getDateDimension(), info
                .getAggregationInterval(), aggregatedGroup);
    }
    
    public EmptyLoginAggregation(TimeDimension timeDimension, DateDimension dateDimension, 
            AggregationInterval interval, AggregatedGroupMapping aggregatedGroup) {
        Validate.notNull(timeDimension);
        Validate.notNull(dateDimension);
        Validate.notNull(interval);
        Validate.notNull(aggregatedGroup);
        
        this.timeDimension = timeDimension;
        this.dateDimension = dateDimension;
        this.interval = interval;
        this.aggregatedGroup = aggregatedGroup;
        
        final DateTime start = timeDimension.getTime().toDateTime(dateDimension.getDate());
        final Minutes minutes = Minutes.minutesBetween(start, start.property(this.interval.getDateTimeFieldType()).addToCopy(1));
        duration = minutes.getMinutes();
    }

    @Override
    public TimeDimension getTimeDimension() {
        return this.timeDimension;
    }

    @Override
    public DateDimension getDateDimension() {
        return this.dateDimension;
    }

    @Override
    public AggregationInterval getInterval() {
        return this.interval;
    }

    @Override
    public int getDuration() {
        return duration;
    }

    @Override
    public AggregatedGroupMapping getAggregatedGroup() {
        return this.aggregatedGroup;
    }

    @Override
    public int getLoginCount() {
        return 0;
    }

    @Override
    public int getUniqueLoginCount() {
        return 0;
    }

}
