<%--

    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.

--%>

<%@ taglib prefix="gvis" tagdir="/WEB-INF/tags/google-visualization" %>
<%@ include file="/WEB-INF/jsp/include.jsp"%>
<c:set var="n"><portlet:namespace/></c:set>
<script type="text/javascript" src="http://janwillemtulp.com/d3linechart/d3-v1.8.2.js"></script>
    <link type="text/css" rel="stylesheet" href="grid960.css" media="all"></link>
     
        <style type="text/css">
        path {
            stroke-width: 2;
            fill: none;
        }

        line {
            stroke: black;
        }

        text {
            font-family: Arial;
            font-size: 9pt;
        }
    </style>   
<!-- Portlet -->
<div class="fl-widget portlet" role="section">
  
  <!-- Portlet Body -->
  <div class="fl-widget-content portlet-body" role="main">
  
    <!-- Portlet Section -->
    <div id="${n}" class="portlet-section" role="region">

      <div class="portlet-section-body">
      
        <form:form commandName="loginReportRequest">
            <p>
                Generate a new login report:
            </p>
            <p>
                <form:label path="start" code="start"/>
                <form:input path="start" cssClass="datepicker"/> 
            </p>
            <p>
                <form:label path="end" code="end"/>
                <form:input path="end" cssClass="datepicker"/> 
            </p>
            <p>
                <form:label path="interval" code="Interval"/>
                <form:select path="interval">
                    <c:forEach items="${ intervals }" var="interval">
                        <form:option value="${ interval }"/>
                    </c:forEach>
                </form:select> 
            </p>
            <p>
                <form:label path="groups" code="Group"/>
                <form:select path="groups">
                    <c:forEach items="${ groups }" var="group">
                        <form:option value="${ group.key }" label="${group.value}"/>
                    </c:forEach>
                </form:select> 
            </p>
            <input type="submit" value="Submit"/>
        </form:form>
      
        <div class="chart"></div>
      </div>  

    </div>
    
  </div>

</div>

<script type="text/javascript">

    var $ = up.jQuery;
    
    var linecolors = ["steelblue", "red", "green", "yellow", "purple"];

    var drawChart = function() {
        var queryData = {
            start: $("#${n} input[name=start]").val(),
            end: $("#${n} input[name=end]").val(),
            interval: $("#${n} select[name=interval]").val()
        };
        
        var groups = $("#${n} select[name=groups]");
        if (groups.length > 0) {
            queryData.groups = groups.val();
        }
        
        $.ajax({
            url: "<portlet:resourceURL/>",
            traditional: true,
            data: queryData, 
            success: function (json) { 
                var w = $("#${n} .portlet-section-body").width();
                var h = Math.min(Math.max(w * .7, 240), 400);
                
                var lines = json.logins;
                var dates = json.dates;
                
                var ymax = 0;
                $(lines).each(function (idx, l) {
                    ymax = Math.max(ymax, d3.max(lines[0]));
                });
                
                
                var margin = 20,
                y = d3.scale.linear().domain([0, ymax]).range([0 + margin, h - margin]),
                x = d3.scale.linear().domain([0, lines[0].length]).range([0 + margin, w - margin])

                $(".chart").html("");
                var vis = d3.select(".chart")
                    .append("svg:svg")
                    .attr("width", w)
                    .attr("height", h)

                var g = vis.append("svg:g")
                    .attr("transform", "translate(0, " + h + ")");
                
                var line = d3.svg.line()
                    .x(function(d,i) { return x(i); })
                    .y(function(d) { return -1 * y(d); })
                
                $(lines).each(function (idx, l) {
                    g.append("svg:path").attr("d", line(l)).attr("stroke", linecolors[idx]);
                });
                
                g.append("svg:line")
                    .attr("x1", x(0))
                    .attr("y1", -1 * y(0))
                    .attr("x2", x(w))
                    .attr("y2", -1 * y(0))

                g.append("svg:line")
                    .attr("x1", x(0))
                    .attr("y1", -1 * y(0))
                    .attr("x2", x(0))
                    .attr("y2", -1 * y(ymax))
                
                g.selectAll(".xLabel")
                    .data(x.ticks(5))
                    .enter().append("svg:text")
                    .attr("class", "xLabel")
                    .text(function(d) { return dates[d]; })
                    .attr("x", function(d) { return x(d) })
                    .attr("y", 0)
                    .attr("text-anchor", "middle")

                g.selectAll(".yLabel")
                    .data(y.ticks(4))
                    .enter().append("svg:text")
                    .attr("class", "yLabel")
                    .text(String)
                    .attr("x", 0)
                    .attr("y", function(d) { return -1 * y(d) })
                    .attr("text-anchor", "right")
                    .attr("dy", 4)
                
                g.selectAll(".xTicks")
                    .data(x.ticks(5))
                    .enter().append("svg:line")
                    .attr("class", "xTicks")
                    .attr("x1", function(d) { return x(d); })
                    .attr("y1", -1 * y(0))
                    .attr("x2", function(d) { return x(d); })
                    .attr("y2", -1 * y(-0.3))

                g.selectAll(".yTicks")
                    .data(y.ticks(4))
                    .enter().append("svg:line")
                    .attr("class", "yTicks")
                    .attr("y1", function(d) { return -1 * y(d); })
                    .attr("x1", x(-0.3))
                    .attr("y2", function(d) { return -1 * y(d); })
                    .attr("x2", x(0))
            }, 
            dataType: "json"
        });
    };

    $(document).ready(function(){
        $(".datepicker").datepicker();
        $("#${n} form").submit(function () {
            drawChart();
            return false;
        });
    });

</script>
