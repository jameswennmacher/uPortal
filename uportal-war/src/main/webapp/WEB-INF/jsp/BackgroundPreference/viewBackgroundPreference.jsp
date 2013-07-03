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

<%@ include file="/WEB-INF/jsp/include.jsp" %>
<c:set var="n"><portlet:namespace/></c:set>

<div class="background-preference-edit-link">
    <a href="javascript:;">Edit Background Image</a>    
</div>

<portlet:actionURL var="savePreferencesUrl">
    <portlet:param name="action" value="savePreferences"/>
</portlet:actionURL>

<div id="${n}jasigBackgroundPreferencePortlet" class="jasigBackgroundPreference">

    <h2>Background Image Preference</h2>
    <form id="${n}addLocationForm" class="select-location-form" action="${savePreferencesUrl}" method="post">
        <label for="backgrounImage">Preferred Background Image</label>
    	<select name="backgroundImage" id="backgroundImage">
    	<c:forEach var="image" items="${images}">
    		<option>${image}</option> 
    	</c:forEach> 
    	</select>
        
	    <input type="submit" value="Save">
	</form>
        
</div>

<script type="text/javascript">
	up.jQuery(function() {
    	var $ = up.jQuery;
    	$('.jasigBackgroundPreference').hide();
    	$('.background-preference-edit-link').click(function() {
    		$('.jasigBackgroundPreference').show();
    	});
    	
    	$('#backgroundImage').val('${backgroundImage}');
    	$('#portalPageBody').css({'background-image': 'url("${backgroundImage}")', 'background-size':'cover'});  	
    	$('.up-portlet-wrapper').css({'opacity': '0.75'});
	});
</script>
