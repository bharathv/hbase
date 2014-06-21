<%@ page contentType="text/html;charset=UTF-8"
  import="java.util.*"
  import="org.apache.commons.lang.StringEscapeUtils"
  import="org.apache.hadoop.conf.Configuration"
  import="org.apache.hadoop.hbase.client.HBaseAdmin"
  import="org.apache.hadoop.hbase.master.HMaster"
  import="org.apache.hadoop.hbase.HConstants"
  import="org.apache.hadoop.hbase.master.MetaRegion"
  import="org.apache.hadoop.hbase.HColumnDescriptor"
  import="org.apache.hadoop.hbase.HRegionInfo"
  import="org.apache.hadoop.hbase.HServerAddress"
  import="org.apache.hadoop.hbase.HServerInfo"
  import="org.apache.hadoop.hbase.HTableDescriptor"
  import="org.apache.hadoop.hbase.master.AssignmentPlan" 
  import="org.apache.hadoop.hbase.master.RegionPlacement"
  import="org.apache.hadoop.hbase.master.RegionAssignmentSnapshot"
  import="org.apache.hadoop.hbase.util.Base64"
  import="org.apache.hadoop.hbase.util.Bytes"
  import="org.apache.hadoop.hbase.util.FSUtils"
  import="org.apache.hadoop.hbase.util.JvmVersion"
%><%
  HMaster master = (HMaster)getServletContext().getAttribute(HMaster.MASTER);
  Configuration conf = master.getConfiguration();
  RegionPlacement rp = new RegionPlacement(conf);
  RegionAssignmentSnapshot snapShot = rp.getRegionAssignmentSnapshot();
  Map<String, HRegionInfo> regionNameToRegionInfoMap = snapShot.getRegionNameToRegionInfoMap();

  String regionNameStr = null;
  String regionName64 = request.getParameter("regionName64");
  if (regionName64 != null && regionName64.length() > 0) {
    byte[] regionName = Base64.decode(regionName64, Base64.URL_SAFE);
    if (regionName != null) {
      regionNameStr = Bytes.toStringBinary(regionName);
    }
  }
  String primaryRS = null;
  String secondaryRS = null;
  String tertiaryRS = null;
  String favoredNodes = "";
  String error = "";
  if (regionNameStr != null) {
		HRegionInfo region = regionNameToRegionInfoMap.get(regionNameStr);
		List<HServerAddress> favoredNodesList = null;
		if (region == null) {
			error = " because cannot find this region in META !";
		}	else {
			primaryRS= request.getParameter("primaryRS");
			favoredNodes += primaryRS + ",";
		
			secondaryRS= request.getParameter("secondaryRS");
			favoredNodes += secondaryRS + ",";
		
			tertiaryRS= request.getParameter("tertiaryRS");
			favoredNodes += tertiaryRS;
			if (primaryRS == null || primaryRS.length() == 0 ||
				secondaryRS == null || secondaryRS.length() == 0 || 
				tertiaryRS == null || tertiaryRS.length() == 0 )  {
				error = " because the favored nodes are incomplete : " + favoredNodes;
			} else {
				try {
					favoredNodesList = RegionPlacement.getFavoredNodeList(favoredNodes);
				} catch (IllegalArgumentException e) {
					error = " because received the invalid favored nodes: " + e.toString();
				}
				if (favoredNodesList != null) {
					AssignmentPlan newPlan = new AssignmentPlan();
					newPlan.updateAssignmentPlan(region, favoredNodesList);
					try {
						rp.updateAssignmentPlan(newPlan);
					} catch (Exception e) {
						error = " because caught some exceptions during the update " + e.toString(); 
					} 
				}
			}
		}
		snapShot = rp.getRegionAssignmentSnapshot();
  }
  AssignmentPlan plan = snapShot.getExistingAssignmentPlan();
  Map<HRegionInfo, List<HServerAddress>> assignmentMap = plan.getAssignmentMap();
  Map<HRegionInfo, HServerAddress> currentAssignment = snapShot.getRegionToRegionServerMap();

%><!DOCTYPE html> 
<html lang="en">
<head>
	<meta charset="utf-8"/>
	<meta http-equiv="Content-Type" content="text/html;charset=UTF-8"/>
	<title>HBase Assignment Plan</title>
	<link rel="stylesheet" type="text/css" href="/static/hbase.css" />
</head>
<body>
	<a id="logo" href="http://wiki.apache.org/lucene-hadoop/Hbase"><img src="/static/hbase_logo_med.gif" alt="HBase Logo" title="HBase Logo" /></a>
	<h1 id="page_title">Region Assignment Plan</h1>
	<p id="links_menu"><a href="/master.jsp">Master</a></p>
	<hr id="head_rule" />

	<h2>Region Assignment Plan </h2>
<%
	if (error.length() != 0) {
%>
	<h3>Failed to update the favored nodes: <font color="#FF0000"><%=favoredNodes%> </font>, </h3>
	<h3>for the region: <font color="#FF0000"> <%= StringEscapeUtils.escapeHtml(regionNameStr) %> </font></h3>
	<h3><%=error%></h3>
<%
	} else if (regionNameStr != null) {
%>
	<h3> Succeeded to update the favored nodes: <font color="#32CD32"><%=favoredNodes%> </font>,</h3>
	<h3> for the region: <font color="#32CD32"><%= StringEscapeUtils.escapeHtml(regionNameStr) %> </font></h3>
<%			
	}
%>
	<table>
		<tr>
      <th>Region Name</th>
      <th>Position</th>
      <th>Primary RS</th>
      <th>Secondary RS</th>
      <th>Tertiary RS</th>
      <th>Actions</th>
		</tr>
<%				
	for (Map.Entry<String, HRegionInfo> entry : regionNameToRegionInfoMap.entrySet()) {
		HRegionInfo regionInfo = entry.getValue();
		List<HServerAddress> favoredNodeList = assignmentMap.get(regionInfo);
		byte[] regionName = regionInfo.getRegionName();
		regionNameStr = regionInfo.getRegionNameAsString();
		
		String position = "Not Assigned";
		String tdClass = "bad";
		
		HServerAddress currentRS = currentAssignment.get(regionInfo);
		if (currentRS != null) {
		  AssignmentPlan.POSITION favoredNodePosition =
		      AssignmentPlan.getFavoredServerPosition(favoredNodeList, currentRS);
		  if (favoredNodePosition == null) {
		    position = "" + currentRS;
		  } else {
		    position = favoredNodePosition.toString();
		    if (favoredNodePosition == AssignmentPlan.POSITION.PRIMARY) {
		      tdClass = "good";
		    } else {
		      tdClass = "warn";
		    }
		  }
		}
%>
		<tr><form method="post">
			<input type="hidden" name="regionName64" value="<%= Base64.encodeBytes(regionName, Base64.URL_SAFE) %>">
			<td><%= StringEscapeUtils.escapeHtml(regionNameStr) %></td>
			<td class="<%= tdClass %>"><%= position %></td>
			<td><input type="text" size="40" name="primaryRS" value="<%= favoredNodeList == null ? "" : favoredNodeList.get(AssignmentPlan.POSITION.PRIMARY.ordinal()).getHostNameWithPort() %>"</td>
			<td><input type="text" size="40" name="secondaryRS" value="<%= favoredNodeList == null ? "" : favoredNodeList.get(AssignmentPlan.POSITION.SECONDARY.ordinal()).getHostNameWithPort() %>"</td>
			<td><input type="text" size="40" name="tertiaryRS" value="<%= favoredNodeList == null ? "" : favoredNodeList.get(AssignmentPlan.POSITION.TERTIARY.ordinal()).getHostNameWithPort() %>"</td>
			<td><input type="submit" size="5" value="Update"></td>
		</form></tr>
<%   
	}  
%>
		</table>
	</body>
</html>
