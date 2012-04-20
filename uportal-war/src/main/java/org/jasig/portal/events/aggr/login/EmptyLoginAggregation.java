package org.jasig.portal.events.aggr.login;

import org.apache.commons.lang.Validate;
import org.jasig.portal.events.aggr.AggregationInterval;
import org.jasig.portal.events.aggr.AggregationIntervalInfo;
import org.jasig.portal.events.aggr.DateDimension;
import org.jasig.portal.events.aggr.TimeDimension;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;

/**
 * EmptyLoginAggregation represents a login aggregation interval with no logins.
 * 
 * @author Jen Bourey, jennifer.bourey@gmail.com
 */
public class EmptyLoginAggregation implements LoginAggregation {

    private final AggregationIntervalInfo info;
    private final AggregatedGroupMapping aggregatedGroup;
    private final int duration;

    public EmptyLoginAggregation(AggregationIntervalInfo info, AggregatedGroupMapping aggregatedGroup) {
        Validate.notNull(info);
        Validate.notNull(aggregatedGroup);
        
        this.info = info;
        this.aggregatedGroup = aggregatedGroup;
        this.duration = info.getDurationTo(info.getEnd());
    }

    @Override
    public TimeDimension getTimeDimension() {
        return this.info.getTimeDimension();
    }

    @Override
    public DateDimension getDateDimension() {
        return this.info.getDateDimension();
    }

    @Override
    public AggregationInterval getInterval() {
        return this.info.getAggregationInterval();
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
